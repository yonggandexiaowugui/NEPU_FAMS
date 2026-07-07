package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.*;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapApplyDTO;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapApprovalDTO;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapQueryDTO;
import xyz.wolegelei.nepu_fams.entity.Asset;
import xyz.wolegelei.nepu_fams.entity.BorrowApplication;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.entity.Scrapplication;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.AssetMapper;
import xyz.wolegelei.nepu_fams.mapper.BorrowApplicationMapper;
import xyz.wolegelei.nepu_fams.mapper.CollegeMapper;
import xyz.wolegelei.nepu_fams.mapper.ScrapplicationMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.ScrapplicationService;
import xyz.wolegelei.nepu_fams.vo.scrap.ScrapplicationVO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapplicationServiceImpl implements ScrapplicationService {

    private final ScrapplicationMapper scrapplicationMapper;
    private final AssetMapper assetMapper;
    private final SysUserMapper sysUserMapper;
    private final CollegeMapper collegeMapper;
    private final BorrowApplicationMapper borrowApplicationMapper;

    private static final List<String> ACTIVE_BORROW_STATUSES = Arrays.asList(
            BorrowStatus.PENDING_COLLEGE.getCode(),
            BorrowStatus.PENDING_SUPER.getCode(),
            BorrowStatus.APPROVED.getCode(),
            BorrowStatus.BORROWED.getCode(),
            BorrowStatus.RETURNING.getCode()
    );

    private static final List<String> ACTIVE_SCRAP_STATUSES = Arrays.asList(
            ScrapStatus.PENDING.getCode(),
            ScrapStatus.PENDING_COLLEGE.getCode(),
            ScrapStatus.PENDING_SUPER.getCode()
    );

    @Override
    @Transactional
    public void apply(ScrapApplyDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!RoleConstants.COLLEGE_ADMIN.equals(currentUser.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }

        if (!asset.getCollegeId().equals(currentUser.getCollegeId())) {
            throw new BusinessException("只能发起本学院资产的报废申请");
        }

        if (AssetStatus.SCRAPPED.getCode().equals(asset.getStatus())) {
            throw new BusinessException("已报废的资产不能再次申请报废");
        }

        LambdaQueryWrapper<BorrowApplication> borrowWrapper = new LambdaQueryWrapper<>();
        borrowWrapper.eq(BorrowApplication::getAssetId, dto.getAssetId());
        borrowWrapper.in(BorrowApplication::getStatus, ACTIVE_BORROW_STATUSES);
        Long borrowCount = borrowApplicationMapper.selectCount(borrowWrapper);
        if (borrowCount > 0) {
            throw new BusinessException("该资产存在未完成的领用申请，请先归还后再申请报废");
        }

        LambdaQueryWrapper<Scrapplication> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Scrapplication::getAssetId, dto.getAssetId());
        existWrapper.in(Scrapplication::getStatus,
                ScrapStatus.PENDING.getCode(),
                ScrapStatus.PENDING_COLLEGE.getCode(),
                ScrapStatus.PENDING_SUPER.getCode());
        Long existCount = scrapplicationMapper.selectCount(existWrapper);
        if (existCount > 0) {
            throw new BusinessException("该资产已有待审核的报废申请");
        }

        Scrapplication application = new Scrapplication();
        application.setAssetId(dto.getAssetId());
        application.setCollegeId(currentUser.getCollegeId());
        application.setProposerId(currentUserId);
        application.setReason(dto.getReason());
        application.setStatus(ScrapStatus.PENDING_COLLEGE.getCode());
        application.setIsDeleted(0);

        scrapplicationMapper.insert(application);
    }

    @Override
    public IPage<ScrapplicationVO> page(ScrapQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        Page<Scrapplication> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Scrapplication> wrapper = buildQueryWrapper(dto);

        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            wrapper.eq(Scrapplication::getCollegeId, currentUser.getCollegeId());
            if (!Boolean.TRUE.equals(dto.getApproved()) && !StringUtils.hasText(dto.getStatus())) {
                wrapper.in(Scrapplication::getStatus, ScrapStatus.PENDING.getCode(), ScrapStatus.PENDING_COLLEGE.getCode());
            }
        } else if (RoleConstants.SUPER_ADMIN.equals(role)) {
            if (!Boolean.TRUE.equals(dto.getApproved()) && !StringUtils.hasText(dto.getStatus())) {
                wrapper.in(Scrapplication::getStatus, ScrapStatus.PENDING_SUPER.getCode());
            }
        } else {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        wrapper.orderByDesc(Scrapplication::getCreateTime);

        IPage<Scrapplication> applicationPage = scrapplicationMapper.selectPage(page, wrapper);
        return applicationPage.convert(this::convertToVO);
    }

    @Override
    public ScrapplicationVO getById(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Scrapplication application = scrapplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ResultCode.SCRAP_NOT_FOUND);
        }

        checkDataPermission(application, currentUser);

        return convertToVO(application);
    }

    @Override
    @Transactional
    public void approve(ScrapApprovalDTO dto) {
        handleApproval(dto, true);
    }

    @Override
    @Transactional
    public void reject(ScrapApprovalDTO dto) {
        if (!StringUtils.hasText(dto.getApprovalOpinion())) {
            throw new BusinessException("驳回时必须填写审批意见");
        }
        handleApproval(dto, false);
    }

    private void handleApproval(ScrapApprovalDTO dto, boolean approved) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Scrapplication application = scrapplicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException(ResultCode.SCRAP_NOT_FOUND);
        }

        String role = currentUser.getRole();
        String status = application.getStatus();

        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            if (!application.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException("只能处理本学院报废申请");
            }
            if (!ScrapStatus.PENDING_COLLEGE.getCode().equals(status) && !ScrapStatus.PENDING.getCode().equals(status)) {
                throw new BusinessException("学院管理员只能处理待学院审核的申请");
            }
            application.setCollegeApproverId(currentUserId);
            application.setCollegeApprovalOpinion(dto.getApprovalOpinion());
            application.setApproverId(currentUserId);
            application.setApprovalOpinion(dto.getApprovalOpinion());
            application.setStatus(approved ? ScrapStatus.PENDING_SUPER.getCode() : ScrapStatus.REJECTED.getCode());
        } else if (RoleConstants.SUPER_ADMIN.equals(role)) {
            if (!ScrapStatus.PENDING_SUPER.getCode().equals(status) && !ScrapStatus.PENDING.getCode().equals(status)) {
                throw new BusinessException("超级管理员只能处理待校级审核的申请");
            }
            application.setSuperApproverId(currentUserId);
            application.setSuperApprovalOpinion(dto.getApprovalOpinion());
            application.setApproverId(currentUserId);
            application.setApprovalOpinion(dto.getApprovalOpinion());
            application.setStatus(approved ? ScrapStatus.APPROVED.getCode() : ScrapStatus.REJECTED.getCode());
        } else {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        scrapplicationMapper.updateById(application);

        if (approved && ScrapStatus.APPROVED.getCode().equals(application.getStatus())) {
            Asset asset = assetMapper.selectById(application.getAssetId());
            if (asset != null) {
                asset.setStatus(AssetStatus.SCRAPPED.getCode());
                asset.setUserId(null);
                assetMapper.updateById(asset);
            }
        }
    }

    @Override
    public IPage<ScrapplicationVO> myApplicationList(ScrapQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Page<Scrapplication> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Scrapplication> wrapper = buildQueryWrapper(dto);
        wrapper.eq(Scrapplication::getProposerId, currentUserId);
        wrapper.orderByDesc(Scrapplication::getCreateTime);

        IPage<Scrapplication> applicationPage = scrapplicationMapper.selectPage(page, wrapper);
        return applicationPage.convert(this::convertToVO);
    }

    @Override
    public List<ScrapplicationVO> export(ScrapQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.COLLEGE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        LambdaQueryWrapper<Scrapplication> wrapper = buildQueryWrapper(dto);

        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            wrapper.eq(Scrapplication::getCollegeId, currentUser.getCollegeId());
        }

        wrapper.orderByDesc(Scrapplication::getCreateTime);

        List<Scrapplication> list = scrapplicationMapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Scrapplication application = scrapplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ResultCode.SCRAP_NOT_FOUND);
        }

        if (!application.getProposerId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己发起的申请");
        }

        if (!ScrapStatus.PENDING_COLLEGE.getCode().equals(application.getStatus())
                && !ScrapStatus.PENDING.getCode().equals(application.getStatus())
                && !ScrapStatus.REJECTED.getCode().equals(application.getStatus())) {
            throw new BusinessException("只有待学院审核或已驳回状态的申请可以删除");
        }

        scrapplicationMapper.deleteById(id);
    }

    private LambdaQueryWrapper<Scrapplication> buildQueryWrapper(ScrapQueryDTO dto) {
        LambdaQueryWrapper<Scrapplication> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Scrapplication::getStatus, dto.getStatus());
        }
        if (dto.getCollegeId() != null) {
            wrapper.eq(Scrapplication::getCollegeId, dto.getCollegeId());
        }
        if (dto.getProposerId() != null) {
            wrapper.eq(Scrapplication::getProposerId, dto.getProposerId());
        }
        if (StringUtils.hasText(dto.getAssetName())) {
            List<Asset> assets = assetMapper.selectList(
                    new LambdaQueryWrapper<Asset>().like(Asset::getName, dto.getAssetName())
            );
            List<Long> assetIds = assets.stream().map(Asset::getId).collect(Collectors.toList());
            if (!assetIds.isEmpty()) {
                wrapper.in(Scrapplication::getAssetId, assetIds);
            } else {
                wrapper.in(Scrapplication::getAssetId, -1L);
            }
        }

        return wrapper;
    }

    private void checkDataPermission(Scrapplication application, SysUser currentUser) {
        String role = currentUser.getRole();
        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            if (!application.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        } else if (RoleConstants.USER.equals(role)) {
            if (!application.getProposerId().equals(currentUser.getId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }

    private ScrapplicationVO convertToVO(Scrapplication application) {
        ScrapplicationVO vo = new ScrapplicationVO();
        BeanUtils.copyProperties(application, vo);

        vo.setStatusName(ScrapStatus.getLabelByCode(application.getStatus()));

        Asset asset = assetMapper.selectById(application.getAssetId());
        if (asset != null) {
            vo.setAssetNo(asset.getAssetNo());
            vo.setAssetName(asset.getName());
        }

        SysUser proposer = sysUserMapper.selectById(application.getProposerId());
        if (proposer != null) {
            vo.setProposerName(proposer.getRealName());
        }

        fillApproverNames(application, vo);

        if (application.getCollegeId() != null) {
            College college = collegeMapper.selectById(application.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }

        return vo;
    }

    private void fillApproverNames(Scrapplication application, ScrapplicationVO vo) {
        if (application.getApproverId() != null) {
            SysUser approver = sysUserMapper.selectById(application.getApproverId());
            if (approver != null) {
                vo.setApproverName(approver.getRealName());
            }
        }
        if (application.getCollegeApproverId() != null) {
            SysUser approver = sysUserMapper.selectById(application.getCollegeApproverId());
            if (approver != null) {
                vo.setCollegeApproverName(approver.getRealName());
            }
        }
        if (application.getSuperApproverId() != null) {
            SysUser approver = sysUserMapper.selectById(application.getSuperApproverId());
            if (approver != null) {
                vo.setSuperApproverName(approver.getRealName());
            }
        }
    }
}
