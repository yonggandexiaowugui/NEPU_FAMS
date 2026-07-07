package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.*;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordBatchDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordDTO;
import xyz.wolegelei.nepu_fams.entity.*;
import xyz.wolegelei.nepu_fams.mapper.*;
import xyz.wolegelei.nepu_fams.service.InventoryRecordService;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryAnalysisVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryDiffVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryRecordVO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryRecordServiceImpl implements InventoryRecordService {

    private final InventoryRecordMapper inventoryRecordMapper;
    private final InventoryTaskMapper inventoryTaskMapper;
    private final AssetMapper assetMapper;
    private final AssetCategoryMapper assetCategoryMapper;
    private final CollegeMapper collegeMapper;
    private final SysUserMapper sysUserMapper;

    @Value("${llm.api-url:}")
    private String llmApiUrl;

    @Value("${llm.api-key:}")
    private String llmApiKey;

    @Value("${llm.model:gpt-4o-mini}")
    private String llmModel;

    @Override
    public List<InventoryRecordVO> listByTaskId(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        LambdaQueryWrapper<InventoryRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InventoryRecord::getTaskId, taskId);
        List<InventoryRecord> records = inventoryRecordMapper.selectList(recordWrapper);
        Map<Long, InventoryRecord> recordMap = records.stream()
                .collect(Collectors.toMap(InventoryRecord::getAssetId, r -> r, (a, b) -> a));

        LambdaQueryWrapper<Asset> assetWrapper = new LambdaQueryWrapper<>();
        if (task.getCollegeId() != null) {
            assetWrapper.eq(Asset::getCollegeId, task.getCollegeId());
        }
        if (task.getCategoryId() != null) {
            assetWrapper.eq(Asset::getCategoryId, task.getCategoryId());
        }
        List<Asset> assets = assetMapper.selectList(assetWrapper);

        List<InventoryRecordVO> result = new ArrayList<>();
        for (Asset asset : assets) {
            InventoryRecord record = recordMap.get(asset.getId());
            if (record != null) {
                result.add(convertToVO(record));
            } else {
                result.add(convertAssetToVO(asset, taskId));
            }
        }

        result.sort((a, b) -> {
            if (Boolean.TRUE.equals(a.getIsChecked()) && !Boolean.TRUE.equals(b.getIsChecked())) {
                return 1;
            }
            if (!Boolean.TRUE.equals(a.getIsChecked()) && Boolean.TRUE.equals(b.getIsChecked())) {
                return -1;
            }
            return 0;
        });

        return result;
    }

    @Override
    public void addRecord(InventoryRecordDTO dto) {
        InventoryTask task = inventoryTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        if (!InventoryStatus.IN_PROGRESS.getCode().equals(task.getStatus())) {
            throw new BusinessException("盘点任务已完成，不能再录入数据");
        }

        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }

        saveRecord(dto, asset, StpUtil.getLoginIdAsLong());
    }

    @Override
    public void batchAddRecords(InventoryRecordBatchDTO dto) {
        InventoryTask task = inventoryTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        if (!InventoryStatus.IN_PROGRESS.getCode().equals(task.getStatus())) {
            throw new BusinessException("盘点任务已完成，不能再录入数据");
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();

        if (dto.getRecords() != null) {
            for (InventoryRecordDTO recordDTO : dto.getRecords()) {
                recordDTO.setTaskId(dto.getTaskId());
                Asset asset = assetMapper.selectById(recordDTO.getAssetId());
                if (asset != null) {
                    saveRecord(recordDTO, asset, currentUserId);
                }
            }
        }
    }

    private void saveRecord(InventoryRecordDTO dto, Asset asset, Long recorderId) {
        Integer actualQuantity = resolveActualQuantity(dto);
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getTaskId, dto.getTaskId());
        wrapper.eq(InventoryRecord::getAssetId, dto.getAssetId());
        InventoryRecord existRecord = inventoryRecordMapper.selectOne(wrapper);

        if (existRecord != null) {
            existRecord.setActualQuantity(actualQuantity);
            existRecord.setRemark(dto.getRemark());
            existRecord.setRecorderId(recorderId);
            inventoryRecordMapper.updateById(existRecord);
        } else {
            InventoryRecord record = new InventoryRecord();
            BeanUtils.copyProperties(dto, record);
            record.setActualQuantity(actualQuantity);
            record.setAssetNo(asset.getAssetNo());
            record.setAssetName(asset.getName());
            record.setRecorderId(recorderId);
            record.setIsDeleted(0);
            inventoryRecordMapper.insert(record);
        }
    }

    private Integer resolveActualQuantity(InventoryRecordDTO dto) {
        if (Boolean.FALSE.equals(dto.getIsChecked())) {
            return 0;
        }
        if (dto.getActualQuantity() != null) {
            return dto.getActualQuantity();
        }
        if (dto.getActualCount() != null) {
            return dto.getActualCount();
        }
        return 1;
    }

    @Override
    public List<InventoryDiffVO> getDiffList(Long taskId) {
        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        LambdaQueryWrapper<Asset> assetWrapper = new LambdaQueryWrapper<>();
        if (task.getCollegeId() != null) {
            assetWrapper.eq(Asset::getCollegeId, task.getCollegeId());
        }
        if (task.getCategoryId() != null) {
            assetWrapper.eq(Asset::getCategoryId, task.getCategoryId());
        }
        List<Asset> bookAssets = assetMapper.selectList(assetWrapper);
        Map<Long, Asset> bookAssetMap = bookAssets.stream()
                .collect(Collectors.toMap(Asset::getId, a -> a));

        LambdaQueryWrapper<InventoryRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(InventoryRecord::getTaskId, taskId);
        List<InventoryRecord> records = inventoryRecordMapper.selectList(recordWrapper);
        Map<Long, InventoryRecord> recordMap = records.stream()
                .collect(Collectors.toMap(InventoryRecord::getAssetId, r -> r, (a, b) -> a));

        List<InventoryDiffVO> diffList = new ArrayList<>();

        for (Asset asset : bookAssets) {
            InventoryDiffVO diffVO = new InventoryDiffVO();
            diffVO.setAssetId(asset.getId());
            diffVO.setAssetNo(asset.getAssetNo());
            diffVO.setAssetName(asset.getName());
            diffVO.setLocation(asset.getLocation());
            diffVO.setBookQuantity(1);
            diffVO.setBookCount(1);
            fillAssetMeta(diffVO, asset);

            InventoryRecord record = recordMap.get(asset.getId());
            int actual = record == null || record.getActualQuantity() == null ? 0 : record.getActualQuantity();
            diffVO.setActualQuantity(actual);
            diffVO.setActualCount(actual);
            if (record != null) {
                diffVO.setRemark(record.getRemark());
            }
            int diff = actual - 1;
            diffVO.setDiffQuantity(diff);
            if (diff == 0) {
                diffVO.setDiffType("MATCH");
                diffVO.setRiskLevel("LOW");
                diffVO.setAnalysis("账实一致");
                diffVO.setSuggestion("可进入复核或归档流程");
            } else if (diff > 0) {
                diffVO.setDiffType("PROFIT");
                diffVO.setRiskLevel("MEDIUM");
                diffVO.setAnalysis("实盘数量大于账面数量，可能存在重复录入、未入账资产或盘点范围配置遗漏");
                diffVO.setSuggestion("核对资产编号、购置记录和盘点范围，确认后补录台账或修正盘点记录");
            } else {
                diffVO.setDiffType("LOSS");
                diffVO.setRiskLevel("HIGH");
                diffVO.setAnalysis("实盘数量小于账面数量，可能存在资产遗失、借出未归还、维修流转或未盘点");
                diffVO.setSuggestion("优先核对领用归还、维修工单和存放地点，必要时发起丢失处理");
            }

            diffList.add(diffVO);
        }

        for (InventoryRecord record : records) {
            if (!bookAssetMap.containsKey(record.getAssetId())) {
                InventoryDiffVO diffVO = new InventoryDiffVO();
                diffVO.setAssetId(record.getAssetId());
                diffVO.setAssetNo(record.getAssetNo());
                diffVO.setAssetName(record.getAssetName());
                diffVO.setBookQuantity(0);
                diffVO.setBookCount(0);
                int actual = record.getActualQuantity() == null ? 0 : record.getActualQuantity();
                diffVO.setActualQuantity(actual);
                diffVO.setActualCount(actual);
                diffVO.setDiffQuantity(actual);
                diffVO.setDiffType("PROFIT");
                diffVO.setRiskLevel("MEDIUM");
                diffVO.setAnalysis("资产不在本次盘点账面范围内，可能为录入错误、跨学院资产或范围配置遗漏");
                diffVO.setSuggestion("核对资产归属部门和盘点任务范围，确认后补建台账或修正录入记录");
                diffVO.setRemark(record.getRemark() != null ? record.getRemark() + "（资产不在盘点范围内，可能录入错误）" : "资产不在盘点范围内，可能录入错误");
                diffList.add(diffVO);
            }
        }

        return diffList;
    }

    @Override
    public InventoryAnalysisVO analyzeDiff(Long taskId) {
        List<InventoryDiffVO> diffList = getDiffList(taskId);
        long loss = diffList.stream().filter(item -> "LOSS".equals(item.getDiffType())).count();
        long profit = diffList.stream().filter(item -> "PROFIT".equals(item.getDiffType())).count();
        long match = diffList.stream().filter(item -> "MATCH".equals(item.getDiffType())).count();
        InventoryAnalysisVO vo = new InventoryAnalysisVO();
        vo.setTaskId(taskId);
        vo.setTotalCount(diffList.size());
        vo.setMatchCount((int) match);
        vo.setProfitCount((int) profit);
        vo.setLossCount((int) loss);
        vo.setSummary(generateAiDiffAnalysis(taskId));
        vo.setSuggestion(buildActionSuggestion(loss, profit));
        vo.setDifferences(diffList);
        return vo;
    }

    @Override
    public String generateAiDiffAnalysis(Long taskId) {
        List<InventoryDiffVO> diffList = getDiffList(taskId);
        String ruleAnalysis = buildRuleAnalysis(taskId, diffList);
        if (!StringUtils.hasText(llmApiUrl) || !StringUtils.hasText(llmApiKey)) {
            return ruleAnalysis;
        }
        try {
            String prompt = escapeJson("请基于以下固定资产盘点差异生成简洁的风险分析和处理建议：\n" + ruleAnalysis);
            String body = "{\"model\":\"" + escapeJson(llmModel) + "\",\"messages\":[{\"role\":\"user\",\"content\":\"" + prompt + "\"}],\"temperature\":0.2}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmApiUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + llmApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300 && StringUtils.hasText(response.body())) {
                String content = extractOpenAiContent(response.body());
                if (StringUtils.hasText(content)) {
                    return content;
                }
            }
        } catch (Exception ignored) {
            return ruleAnalysis;
        }
        return ruleAnalysis;
    }

    private String buildActionSuggestion(long loss, long profit) {
        if (loss > 0 && profit > 0) {
            return "建议先复核高风险盘亏资产，再核查盘盈资产是否未建账或重复录入，确认后同步更新资产台账。";
        }
        if (loss > 0) {
            return "建议优先核对领用、维修、调拨和存放地点记录，必要时发起资产丢失或报废流程。";
        }
        if (profit > 0) {
            return "建议核查是否存在未入账资产、重复盘点或盘点范围配置遗漏，确认后补录或修正台账。";
        }
        return "账实一致，可进入复核确认和归档流程。";
    }

    private String buildRuleAnalysis(Long taskId, List<InventoryDiffVO> diffList) {
        long loss = diffList.stream().filter(item -> "LOSS".equals(item.getDiffType())).count();
        long profit = diffList.stream().filter(item -> "PROFIT".equals(item.getDiffType())).count();
        long match = diffList.stream().filter(item -> "MATCH".equals(item.getDiffType())).count();
        int netDiff = diffList.stream().mapToInt(item -> item.getDiffQuantity() == null ? 0 : item.getDiffQuantity()).sum();
        StringBuilder builder = new StringBuilder();
        builder.append("盘点任务 ").append(taskId).append(" 规则分析：共 ").append(diffList.size()).append(" 条记录，")
                .append("账实一致 ").append(match).append(" 条，盘亏 ").append(loss).append(" 条，盘盈 ").append(profit).append(" 条，净差异 ").append(netDiff).append("。\n");
        if (loss > 0) {
            builder.append("盘亏资产需优先核对借用、维修、调拨和存放位置变更记录；必要时发起资产丢失处理。\n");
        }
        if (profit > 0) {
            builder.append("盘盈资产需核查是否重复录入、资产未建账或盘点范围配置遗漏。\n");
        }
        if (loss == 0 && profit == 0) {
            builder.append("当前未发现账实差异，可进入归档或复核流程。\n");
        }
        return builder.toString();
    }

    private String extractOpenAiContent(String body) {
        String key = "\"content\"";
        int keyIndex = body.indexOf(key);
        if (keyIndex < 0) {
            return null;
        }
        int colon = body.indexOf(':', keyIndex + key.length());
        int start = body.indexOf('"', colon + 1);
        if (colon < 0 || start < 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean escape = false;
        for (int i = start + 1; i < body.length(); i++) {
            char c = body.charAt(i);
            if (escape) {
                if (c == 'n') {
                    result.append('\n');
                } else if (c == 't') {
                    result.append('\t');
                } else {
                    result.append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
    }

    @Override
    public void confirmDiff(Long taskId, List<Long> assetIds) {
        checkAdminPermission();

        InventoryTask task = inventoryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
        }

        checkDataPermission(task);

        if (!InventoryStatus.COMPLETED.getCode().equals(task.getStatus())) {
            throw new BusinessException("只有已完成的盘点任务才能确认差异");
        }

        List<InventoryDiffVO> diffList = getDiffList(taskId);
        Map<Long, InventoryDiffVO> diffMap = diffList.stream()
                .collect(Collectors.toMap(InventoryDiffVO::getAssetId, d -> d, (a, b) -> a));

        if (assetIds != null) {
            for (Long assetId : assetIds) {
                InventoryDiffVO diff = diffMap.get(assetId);
                if (diff == null) {
                    continue;
                }

                if ("LOSS".equals(diff.getDiffType())) {
                    Asset asset = assetMapper.selectById(assetId);
                    if (asset != null) {
                        asset.setStatus(AssetStatus.LOSS.getCode());
                        assetMapper.updateById(asset);
                    }
                }
            }
        }
    }

    private InventoryRecordVO convertToVO(InventoryRecord record) {
        InventoryRecordVO vo = new InventoryRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setActualCount(record.getActualQuantity());
        vo.setBookCount(1);
        vo.setIsChecked(true);

        Asset asset = assetMapper.selectById(record.getAssetId());
        if (asset != null) {
            vo.setLocation(asset.getLocation());
            if (asset.getCategoryId() != null) {
                AssetCategory category = assetCategoryMapper.selectById(asset.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
            College college = collegeMapper.selectById(asset.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }

        if (record.getRecorderId() != null) {
            SysUser recorder = sysUserMapper.selectById(record.getRecorderId());
            if (recorder != null) {
                vo.setRecorderName(recorder.getRealName());
            }
        }

        return vo;
    }

    private InventoryRecordVO convertAssetToVO(Asset asset, Long taskId) {
        InventoryRecordVO vo = new InventoryRecordVO();
        vo.setTaskId(taskId);
        vo.setAssetId(asset.getId());
        vo.setAssetNo(asset.getAssetNo());
        vo.setAssetName(asset.getName());
        vo.setLocation(asset.getLocation());
        vo.setActualQuantity(1);
        vo.setActualCount(1);
        vo.setBookCount(1);
        vo.setIsChecked(false);

        if (asset.getCategoryId() != null) {
            AssetCategory category = assetCategoryMapper.selectById(asset.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        if (asset.getCollegeId() != null) {
            College college = collegeMapper.selectById(asset.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }

        return vo;
    }

    private void fillAssetMeta(InventoryDiffVO diffVO, Asset asset) {
        if (asset.getCategoryId() != null) {
            AssetCategory category = assetCategoryMapper.selectById(asset.getCategoryId());
            if (category != null) {
                diffVO.setCategoryName(category.getName());
            }
        }
        if (asset.getCollegeId() != null) {
            College college = collegeMapper.selectById(asset.getCollegeId());
            if (college != null) {
                diffVO.setCollegeName(college.getName());
            }
        }
    }

    private void checkAdminPermission() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String role = user.getRole();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.COLLEGE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void checkDataPermission(InventoryTask task) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole()) || RoleConstants.USER.equals(currentUser.getRole())) {
            if (task.getCollegeId() == null || !task.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }
}
