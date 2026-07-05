package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.BusinessException;
import xyz.wolegelei.nepu_fams.common.ResultCode;
import xyz.wolegelei.nepu_fams.common.RoleConstants;
import xyz.wolegelei.nepu_fams.dto.asset.AssetCategoryDTO;
import xyz.wolegelei.nepu_fams.entity.Asset;
import xyz.wolegelei.nepu_fams.entity.AssetCategory;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.AssetCategoryMapper;
import xyz.wolegelei.nepu_fams.mapper.AssetMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.AssetCategoryService;
import xyz.wolegelei.nepu_fams.vo.asset.AssetCategoryVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetCategoryServiceImpl implements AssetCategoryService {

    private final AssetCategoryMapper assetCategoryMapper;
    private final AssetMapper assetMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<AssetCategoryVO> listTree() {
        List<AssetCategory> allCategories = assetCategoryMapper.selectList(
                new LambdaQueryWrapper<AssetCategory>().orderByAsc(AssetCategory::getSort)
        );
        List<AssetCategoryVO> allVOs = allCategories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Map<Long, List<AssetCategoryVO>> childrenMap = allVOs.stream()
                .filter(vo -> vo.getParentId() != null)
                .collect(Collectors.groupingBy(AssetCategoryVO::getParentId));

        List<AssetCategoryVO> rootCategories = allVOs.stream()
                .filter(vo -> vo.getParentId() == null || vo.getParentId() == 0)
                .collect(Collectors.toList());

        buildTree(rootCategories, childrenMap);

        return rootCategories;
    }

    private void buildTree(List<AssetCategoryVO> categories, Map<Long, List<AssetCategoryVO>> childrenMap) {
        for (AssetCategoryVO category : categories) {
            List<AssetCategoryVO> children = childrenMap.get(category.getId());
            if (children != null && !children.isEmpty()) {
                category.setChildren(children);
                buildTree(children, childrenMap);
            } else {
                category.setChildren(new ArrayList<>());
            }
        }
    }

    @Override
    public List<AssetCategoryVO> listAll() {
        List<AssetCategory> categories = assetCategoryMapper.selectList(
                new LambdaQueryWrapper<AssetCategory>().orderByAsc(AssetCategory::getSort)
        );
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public AssetCategoryVO getById(Long id) {
        AssetCategory category = assetCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        return convertToVO(category);
    }

    @Override
    public void add(AssetCategoryDTO dto) {
        checkAdminPermission();
        AssetCategory category = new AssetCategory();
        BeanUtils.copyProperties(dto, category);

        Integer level = 1;
        if (dto.getParentId() != null && dto.getParentId() != 0) {
            AssetCategory parent = assetCategoryMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
            }
            level = parent.getLevel() + 1;
        } else {
            category.setParentId(0L);
        }
        category.setLevel(level);

        if (category.getSort() == null) {
            category.setSort(0);
        }

        assetCategoryMapper.insert(category);
    }

    @Override
    public void update(Long id, AssetCategoryDTO dto) {
        checkAdminPermission();
        AssetCategory exist = assetCategoryMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        AssetCategory category = new AssetCategory();
        BeanUtils.copyProperties(dto, category);
        category.setId(id);

        if (dto.getParentId() != null && !dto.getParentId().equals(exist.getParentId())) {
            Integer level = 1;
            if (dto.getParentId() != 0) {
                AssetCategory parent = assetCategoryMapper.selectById(dto.getParentId());
                if (parent == null) {
                    throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
                }
                level = parent.getLevel() + 1;
            }
            category.setLevel(level);
        }

        assetCategoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        checkAdminPermission();
        AssetCategory category = assetCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }

        LambdaQueryWrapper<AssetCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(AssetCategory::getParentId, id);
        if (assetCategoryMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILDREN);
        }

        LambdaQueryWrapper<Asset> assetWrapper = new LambdaQueryWrapper<>();
        assetWrapper.eq(Asset::getCategoryId, id);
        if (assetMapper.selectCount(assetWrapper) > 0) {
            throw new BusinessException("该分类下存在资产，无法删除");
        }

        assetCategoryMapper.deleteById(id);
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

    private AssetCategoryVO convertToVO(AssetCategory category) {
        AssetCategoryVO vo = new AssetCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
