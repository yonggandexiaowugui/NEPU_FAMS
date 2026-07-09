package xyz.wolegelei.nepu_fams.asset.three.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import xyz.wolegelei.nepu_fams.asset.three.dto.AssetModelCardVO;
import xyz.wolegelei.nepu_fams.asset.three.dto.ThreeSceneDTO;
import xyz.wolegelei.nepu_fams.asset.three.entity.ThreeScene;
import xyz.wolegelei.nepu_fams.asset.three.mapper.ThreeSceneMapper;
import xyz.wolegelei.nepu_fams.asset.three.service.ThreeAssetService;
import xyz.wolegelei.nepu_fams.common.AssetStatus;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.common.ResultCode;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.entity.Asset;
import xyz.wolegelei.nepu_fams.entity.AssetCategory;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.AssetCategoryMapper;
import xyz.wolegelei.nepu_fams.mapper.AssetMapper;
import xyz.wolegelei.nepu_fams.mapper.CollegeMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.vo.asset.AssetVO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThreeAssetServiceImpl implements ThreeAssetService {

    private static final long MAX_MODEL_SIZE = 20 * 1024 * 1024;

    private static final Pattern ASSET_ID_PATTERN = Pattern.compile("\\\"assetId\\\"\\s*:\\s*(\\d+)");

    private final AssetMapper assetMapper;
    private final AssetCategoryMapper assetCategoryMapper;
    private final CollegeMapper collegeMapper;
    private final SysUserMapper sysUserMapper;
    private final ThreeSceneMapper threeSceneMapper;

    @Override
    public Map<String, String> uploadModel(MultipartFile file) throws IOException {
        checkAdminPermission();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("3D模型文件不能为空");
        }
        if (file.getSize() > MAX_MODEL_SIZE) {
            throw new BusinessException("3D模型文件不能超过20MB");
        }

        String originalName = file.getOriginalFilename() == null ? "model.glb" : file.getOriginalFilename();
        if (!originalName.toLowerCase().endsWith(".glb")) {
            throw new BusinessException("仅支持GLB格式3D模型");
        }

        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "3dmodel");
        Files.createDirectories(uploadDir);
        String fileName = UUID.randomUUID() + ".glb";
        Path target = uploadDir.resolve(fileName);
        file.transferTo(target.toFile());

        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/3dmodel/" + fileName);
        data.put("name", originalName);
        return data;
    }

    @Override
    public void bindAssetModel(Long assetId, String modelUrl) {
        checkAdminPermission();
        if (assetId == null || !StringUtils.hasText(modelUrl)) {
            throw new BusinessException("资产ID和模型路径不能为空");
        }
        Asset asset = assetMapper.selectById(assetId);
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }
        checkCollegePermission(asset.getCollegeId());

        assetMapper.update(null, new UpdateWrapper<Asset>()
                .set("model_url", modelUrl)
                .eq("id", assetId));
    }

    @Override
    public Map<String, String> getAssetModel(Long assetId) {
        Asset asset = assetMapper.selectById(assetId);
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }
        checkAssetViewPermission(asset);

        List<Map<String, Object>> rows = assetMapper.selectMaps(new QueryWrapper<Asset>()
                .select("model_url")
                .eq("id", assetId)
                .last("LIMIT 1"));
        String modelUrl = rows.isEmpty() || rows.get(0).get("model_url") == null ? "" : rows.get(0).get("model_url").toString();
        Map<String, String> data = new HashMap<>();
        data.put("modelUrl", modelUrl);
        return data;
    }

    @Override
    public List<AssetModelCardVO> listModelAssets() {
        SysUser user = currentUser();
        LambdaQueryWrapper<Asset> wrapper = Wrappers.<Asset>lambdaQuery()
                .isNotNull(Asset::getModelUrl)
                .ne(Asset::getModelUrl, "");
        if (RoleConstants.COLLEGE_ADMIN.equals(user.getRole()) || RoleConstants.USER.equals(user.getRole())) {
            if (user.getCollegeId() == null) {
                return List.of();
            }
            wrapper.eq(Asset::getCollegeId, user.getCollegeId());
        }
        wrapper.orderByDesc(Asset::getCreateTime).last("LIMIT 200");
        List<Asset> assets = assetMapper.selectList(wrapper);
        if (assets.isEmpty()) {
            return List.of();
        }

        // 批量加载分类/学院信息，减少 N+1 查询
        java.util.Set<Long> categoryIds = new java.util.HashSet<>();
        java.util.Set<Long> collegeIds = new java.util.HashSet<>();
        for (Asset asset : assets) {
            if (asset.getCategoryId() != null) categoryIds.add(asset.getCategoryId());
            if (asset.getCollegeId() != null) collegeIds.add(asset.getCollegeId());
        }
        java.util.Map<Long, String> categoryNameMap = new java.util.HashMap<>();
        if (!categoryIds.isEmpty()) {
            assetCategoryMapper.selectBatchIds(categoryIds)
                    .forEach(c -> categoryNameMap.put(c.getId(), c.getName()));
        }
        java.util.Map<Long, String> collegeNameMap = new java.util.HashMap<>();
        if (!collegeIds.isEmpty()) {
            collegeMapper.selectBatchIds(collegeIds)
                    .forEach(c -> collegeNameMap.put(c.getId(), c.getName()));
        }

        return assets.stream().map(asset -> {
            AssetModelCardVO vo = new AssetModelCardVO();
            BeanUtils.copyProperties(asset, vo);
            vo.setStatusName(AssetStatus.getLabelByCode(asset.getStatus()));
            if (asset.getCategoryId() != null) {
                vo.setCategoryName(categoryNameMap.get(asset.getCategoryId()));
            }
            if (asset.getCollegeId() != null) {
                vo.setCollegeName(collegeNameMap.get(asset.getCollegeId()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Long addScene(ThreeSceneDTO dto) {
        checkAdminPermission();
        ThreeScene scene = new ThreeScene();
        BeanUtils.copyProperties(dto, scene);
        scene.setCreateBy(StpUtil.getLoginIdAsLong());
        scene.setCreateTime(LocalDateTime.now());
        threeSceneMapper.insert(scene);
        return scene.getId();
    }

    @Override
    public void updateScene(Long id, ThreeSceneDTO dto) {
        checkAdminPermission();
        ThreeScene scene = threeSceneMapper.selectById(id);
        if (scene == null) {
            throw new BusinessException("3D场景不存在");
        }
        BeanUtils.copyProperties(dto, scene);
        scene.setId(id);
        threeSceneMapper.updateById(scene);
    }

    @Override
    public List<ThreeScene> listScenes(Long collegeId) {
        LambdaQueryWrapper<ThreeScene> wrapper = new LambdaQueryWrapper<>();
        SysUser user = currentUser();
        if (RoleConstants.COLLEGE_ADMIN.equals(user.getRole()) || RoleConstants.USER.equals(user.getRole())) {
            wrapper.eq(ThreeScene::getCollegeId, user.getCollegeId());
        } else if (collegeId != null) {
            wrapper.eq(ThreeScene::getCollegeId, collegeId);
        }
        wrapper.orderByDesc(ThreeScene::getCreateTime);
        return threeSceneMapper.selectList(wrapper);
    }

    @Override
    public void deleteScene(Long id) {
        checkAdminPermission();
        threeSceneMapper.deleteById(id);
    }

    @Override
    public List<AssetVO> listSceneAssets(Long sceneId) {
        ThreeScene scene = threeSceneMapper.selectById(sceneId);
        if (scene == null) {
            throw new BusinessException("3D场景不存在");
        }
        List<Long> assetIds = parseAssetIds(scene.getSceneLayoutJson());
        if (assetIds.isEmpty()) {
            return List.of();
        }
        return assetMapper.selectBatchIds(assetIds).stream()
                .map(this::toAssetVO)
                .collect(Collectors.toList());
    }

    private List<Long> parseAssetIds(String sceneLayoutJson) {
        List<Long> assetIds = new ArrayList<>();
        if (!StringUtils.hasText(sceneLayoutJson)) {
            return assetIds;
        }
        Matcher matcher = ASSET_ID_PATTERN.matcher(sceneLayoutJson);
        while (matcher.find()) {
            assetIds.add(Long.parseLong(matcher.group(1)));
        }
        return assetIds;
    }

    private AssetVO toAssetVO(Asset asset) {
        AssetVO vo = new AssetVO();
        BeanUtils.copyProperties(asset, vo);
        return vo;
    }

    private void checkAdminPermission() {
        String role = currentUser().getRole();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.COLLEGE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void checkCollegePermission(Long collegeId) {
        SysUser user = currentUser();
        if (RoleConstants.COLLEGE_ADMIN.equals(user.getRole()) && !collegeId.equals(user.getCollegeId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void checkAssetViewPermission(Asset asset) {
        SysUser user = currentUser();
        if (RoleConstants.COLLEGE_ADMIN.equals(user.getRole()) && !asset.getCollegeId().equals(user.getCollegeId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (RoleConstants.USER.equals(user.getRole()) && !asset.getCollegeId().equals(user.getCollegeId()) && !user.getId().equals(asset.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private SysUser currentUser() {
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }
}
