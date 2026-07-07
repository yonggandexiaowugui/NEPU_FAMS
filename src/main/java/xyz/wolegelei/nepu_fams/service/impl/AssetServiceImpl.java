package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.AssetStatus;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.common.ResultCode;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.dto.asset.AssetAddDTO;
import xyz.wolegelei.nepu_fams.dto.asset.AssetQueryDTO;
import xyz.wolegelei.nepu_fams.dto.asset.AssetUpdateDTO;
import xyz.wolegelei.nepu_fams.entity.Asset;
import xyz.wolegelei.nepu_fams.entity.AssetCategory;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.AssetCategoryMapper;
import xyz.wolegelei.nepu_fams.mapper.AssetMapper;
import xyz.wolegelei.nepu_fams.mapper.CollegeMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.AssetService;
import xyz.wolegelei.nepu_fams.vo.asset.AssetVO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetCategoryMapper assetCategoryMapper;
    private final CollegeMapper collegeMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public IPage<AssetVO> page(AssetQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Page<Asset> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Asset> wrapper = buildQueryWrapper(dto, currentUser);

        if (StringUtils.hasText(dto.getOrderBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(dto.getOrderDirection());
            if ("createTime".equals(dto.getOrderBy())) {
                wrapper.orderBy(true, isAsc, Asset::getCreateTime);
            } else if ("purchaseDate".equals(dto.getOrderBy())) {
                wrapper.orderBy(true, isAsc, Asset::getPurchaseDate);
            } else if ("purchasePrice".equals(dto.getOrderBy())) {
                wrapper.orderBy(true, isAsc, Asset::getPurchasePrice);
            } else {
                wrapper.orderByDesc(Asset::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Asset::getCreateTime);
        }

        IPage<Asset> assetPage = assetMapper.selectPage(page, wrapper);
        return assetPage.convert(this::convertToVO);
    }

    private LambdaQueryWrapper<Asset> buildQueryWrapper(AssetQueryDTO dto, SysUser currentUser) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Asset::getName, dto.getKeyword())
                    .or()
                    .like(Asset::getAssetNo, dto.getKeyword()));
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(Asset::getCategoryId, dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Asset::getStatus, dto.getStatus());
        }
        if (dto.getCollegeId() != null) {
            wrapper.eq(Asset::getCollegeId, dto.getCollegeId());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(Asset::getPurchasePrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(Asset::getPurchasePrice, dto.getMaxPrice());
        }

        String role = currentUser.getRole();
        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            wrapper.eq(Asset::getCollegeId, currentUser.getCollegeId());
        } else if (RoleConstants.USER.equals(role)) {
            if (currentUser.getCollegeId() == null) {
                wrapper.eq(Asset::getId, -1L);
            } else {
                wrapper.eq(Asset::getCollegeId, currentUser.getCollegeId());
            }
        }

        return wrapper;
    }

    @Override
    public AssetVO getById(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }
        String role = currentUser.getRole();
        if (RoleConstants.COLLEGE_ADMIN.equals(role) && !asset.getCollegeId().equals(currentUser.getCollegeId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (RoleConstants.USER.equals(role)
                && !asset.getCollegeId().equals(currentUser.getCollegeId())
                && !currentUserId.equals(asset.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return convertToVO(asset);
    }

    @Override
    public void add(AssetAddDTO dto) {
        checkAdminPermission();
        checkDataPermissionForCollege(dto.getCollegeId());

        Asset asset = new Asset();
        BeanUtils.copyProperties(dto, asset);

        asset.setAssetNo(getAssetNo());
        asset.setStatus(AssetStatus.IDLE.getCode());
        asset.setIsDeleted(0);
        if (asset.getCurrentValue() == null) {
            asset.setCurrentValue(asset.getPurchasePrice());
        }

        assetMapper.insert(asset);
    }

    @Override
    public void update(Long id, AssetUpdateDTO dto) {
        checkAdminPermission();
        Asset exist = assetMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }
        checkDataPermissionForCollege(exist.getCollegeId());
        if (dto.getCollegeId() != null) {
            checkDataPermissionForCollege(dto.getCollegeId());
        }
        if (StringUtils.hasText(dto.getStatus()) && !isValidStatus(dto.getStatus())) {
            throw new BusinessException("资产状态不正确");
        }

        Asset asset = new Asset();
        BeanUtils.copyProperties(dto, asset);
        asset.setId(id);

        assetMapper.updateById(asset);
    }

    @Override
    public void delete(Long id) {
        checkAdminPermission();
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }
        checkDataPermissionForCollege(asset.getCollegeId());

        if (!AssetStatus.IDLE.getCode().equals(asset.getStatus())) {
            throw new BusinessException("只有闲置状态的资产才能删除");
        }

        assetMapper.deleteById(id);
    }

    @Override
    public List<AssetVO> export(AssetQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        checkAdminPermission();

        LambdaQueryWrapper<Asset> wrapper = buildQueryWrapper(dto, currentUser);
        wrapper.orderByDesc(Asset::getCreateTime);

        List<Asset> assets = assetMapper.selectList(wrapper);
        return assets.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long id, AssetStatus status) {
        checkAdminPermission();
        Asset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }
        checkDataPermissionForCollege(asset.getCollegeId());

        asset.setStatus(status.getCode());
        assetMapper.updateById(asset);
    }

    @Override
    public String getAssetNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "ZC" + dateStr;

        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Asset::getAssetNo, prefix);
        wrapper.orderByDesc(Asset::getAssetNo);
        wrapper.last("limit 1");

        Asset lastAsset = assetMapper.selectOne(wrapper);
        int sequence = 1;
        if (lastAsset != null && lastAsset.getAssetNo() != null) {
            String lastNo = lastAsset.getAssetNo();
            String seqStr = lastNo.substring(prefix.length());
            try {
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }

        return prefix + String.format("%04d", sequence);
    }

    private boolean isValidStatus(String status) {
        for (AssetStatus assetStatus : AssetStatus.values()) {
            if (assetStatus.getCode().equals(status)) {
                return true;
            }
        }
        return false;
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

    private void checkDataPermissionForCollege(Long targetCollegeId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole())) {
            if (targetCollegeId == null || !targetCollegeId.equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }

    private AssetVO convertToVO(Asset asset) {
        AssetVO vo = new AssetVO();
        BeanUtils.copyProperties(asset, vo);

        vo.setStatusName(AssetStatus.getLabelByCode(asset.getStatus()));

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

        if (asset.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(asset.getUserId());
            if (user != null) {
                vo.setUserName(user.getRealName());
            }
        }

        return vo;
    }
}
