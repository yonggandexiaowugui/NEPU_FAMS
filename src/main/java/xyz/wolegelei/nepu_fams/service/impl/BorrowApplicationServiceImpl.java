package xyz.wolegelei.nepu_fams.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import xyz.wolegelei.nepu_fams.common.*;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowApplyDTO;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowApprovalDTO;
import xyz.wolegelei.nepu_fams.dto.borrow.BorrowQueryDTO;
import xyz.wolegelei.nepu_fams.entity.Asset;
import xyz.wolegelei.nepu_fams.entity.BorrowApplication;
import xyz.wolegelei.nepu_fams.entity.BorrowApproval;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.entity.SysUser;
import xyz.wolegelei.nepu_fams.mapper.AssetMapper;
import xyz.wolegelei.nepu_fams.mapper.BorrowApplicationMapper;
import xyz.wolegelei.nepu_fams.mapper.BorrowApprovalMapper;
import xyz.wolegelei.nepu_fams.mapper.CollegeMapper;
import xyz.wolegelei.nepu_fams.mapper.SysUserMapper;
import xyz.wolegelei.nepu_fams.service.BorrowApplicationService;
import xyz.wolegelei.nepu_fams.vo.borrow.BorrowApplicationDetailVO;
import xyz.wolegelei.nepu_fams.vo.borrow.BorrowApplicationVO;
import xyz.wolegelei.nepu_fams.vo.borrow.BorrowApprovalVO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowApplicationServiceImpl implements BorrowApplicationService {

    private final BorrowApplicationMapper borrowApplicationMapper;

    private final BorrowApprovalMapper borrowApprovalMapper;

    private final AssetMapper assetMapper;

    private final SysUserMapper sysUserMapper;

    private final CollegeMapper collegeMapper;

    private static final List<String> NON_TERMINAL_STATUSES = Arrays.asList(
            BorrowStatus.PENDING_COLLEGE.getCode(),
            BorrowStatus.PENDING_SUPER.getCode(),
            BorrowStatus.APPROVED.getCode(),
            BorrowStatus.BORROWED.getCode(),
            BorrowStatus.RETURNING.getCode()
    );

    @Override
    @Transactional
    public void apply(BorrowApplyDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Asset asset = assetMapper.selectById(dto.getAssetId());
        if (asset == null) {
            throw new BusinessException(ResultCode.ASSET_NOT_FOUND);
        }

        if (!AssetStatus.IDLE.getCode().equals(asset.getStatus())) {
            throw new BusinessException("只有闲置状态的资产才能被领用");
        }
        if (currentUser.getCollegeId() == null || !asset.getCollegeId().equals(currentUser.getCollegeId())) {
            throw new BusinessException("只能申请领用本学院资产");
        }

        LambdaQueryWrapper<BorrowApplication> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BorrowApplication::getAssetId, dto.getAssetId());
        existWrapper.in(BorrowApplication::getStatus, NON_TERMINAL_STATUSES);
        Long count = borrowApplicationMapper.selectCount(existWrapper);
        if (count > 0) {
            throw new BusinessException("该资产已有未完成的领用申请");
        }

        BorrowApplication application = new BorrowApplication();
        application.setAssetId(dto.getAssetId());
        application.setUserId(currentUserId);
        application.setCollegeId(currentUser.getCollegeId());
        application.setPurpose(dto.getPurpose());
        application.setExpectedReturnDate(dto.getExpectedReturnDate());
        application.setStatus(BorrowStatus.PENDING_COLLEGE.getCode());
        application.setIsDeleted(0);

        borrowApplicationMapper.insert(application);
    }

    @Override
    public IPage<BorrowApplicationVO> myApplicationList(BorrowQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        Page<BorrowApplication> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<BorrowApplication> wrapper = buildQueryWrapper(dto);
        wrapper.eq(BorrowApplication::getUserId, currentUserId);
        wrapper.orderByDesc(BorrowApplication::getCreateTime);

        IPage<BorrowApplication> applicationPage = borrowApplicationMapper.selectPage(page, wrapper);
        return applicationPage.convert(this::convertToVO);
    }

    @Override
    public IPage<BorrowApplicationVO> pendingApprovalList(BorrowQueryDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        Page<BorrowApplication> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<BorrowApplication> wrapper = buildQueryWrapper(dto);

        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            wrapper.eq(BorrowApplication::getCollegeId, currentUser.getCollegeId());
            if (Boolean.TRUE.equals(dto.getPending())) {
                wrapper.in(BorrowApplication::getStatus, BorrowStatus.PENDING_COLLEGE.getCode(), BorrowStatus.RETURNING.getCode());
            } else if (Boolean.TRUE.equals(dto.getApproved())) {
                wrapper.notIn(BorrowApplication::getStatus, BorrowStatus.PENDING_COLLEGE.getCode(), BorrowStatus.RETURNING.getCode());
            }
        } else if (RoleConstants.SUPER_ADMIN.equals(role)) {
            if (Boolean.TRUE.equals(dto.getPending())) {
                wrapper.in(BorrowApplication::getStatus, BorrowStatus.PENDING_SUPER.getCode(), BorrowStatus.RETURNING.getCode());
            } else if (Boolean.TRUE.equals(dto.getApproved())) {
                wrapper.notIn(BorrowApplication::getStatus, BorrowStatus.PENDING_SUPER.getCode(), BorrowStatus.RETURNING.getCode());
            }
        } else {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        wrapper.orderByDesc(BorrowApplication::getCreateTime);

        IPage<BorrowApplication> applicationPage = borrowApplicationMapper.selectPage(page, wrapper);
        return applicationPage.convert(this::convertToVO);
    }

    @Override
    public BorrowApplicationDetailVO getDetail(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        BorrowApplication application = borrowApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        checkDataPermission(application, currentUser);

        BorrowApplicationDetailVO detailVO = new BorrowApplicationDetailVO();
        BeanUtils.copyProperties(convertToVO(application), detailVO);

        LambdaQueryWrapper<BorrowApproval> approvalWrapper = new LambdaQueryWrapper<>();
        approvalWrapper.eq(BorrowApproval::getApplicationId, id);
        approvalWrapper.orderByAsc(BorrowApproval::getCreateTime);
        List<BorrowApproval> approvalList = borrowApprovalMapper.selectList(approvalWrapper);

        List<BorrowApprovalVO> approvalVOList = approvalList.stream()
                .map(this::convertApprovalToVO)
                .collect(Collectors.toList());

        detailVO.setApprovalHistory(approvalVOList);

        return detailVO;
    }

    @Override
    @Transactional
    public void collegeApprove(BorrowApprovalDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        if (!RoleConstants.COLLEGE_ADMIN.equals(role) && !RoleConstants.SUPER_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        BorrowApplication application = borrowApplicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        if (!BorrowStatus.PENDING_COLLEGE.getCode().equals(application.getStatus())) {
            throw new BusinessException("当前状态不允许学院审批");
        }

        if (RoleConstants.COLLEGE_ADMIN.equals(role) && !application.getCollegeId().equals(currentUser.getCollegeId())) {
            throw new BusinessException("只能审批本学院的申请");
        }

        boolean isPass = Boolean.TRUE.equals(dto.getApproved());
        String opinion = dto.getApprovalOpinion();
        if (!isPass && !StringUtils.hasText(opinion)) {
            throw new BusinessException("驳回时必须填写审批意见");
        }

        BorrowApproval approval = new BorrowApproval();
        approval.setApplicationId(dto.getApplicationId());
        approval.setApproverId(currentUserId);
        approval.setApprovalStatus(isPass ? "PASS" : "REJECT");
        approval.setOpinion(opinion);
        borrowApprovalMapper.insert(approval);

        if (isPass) {
            application.setStatus(BorrowStatus.PENDING_SUPER.getCode());
        } else {
            application.setStatus(BorrowStatus.REJECTED.getCode());
        }
        borrowApplicationMapper.updateById(application);
    }

    @Override
    @Transactional
    public void superApprove(BorrowApprovalDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!RoleConstants.SUPER_ADMIN.equals(currentUser.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        BorrowApplication application = borrowApplicationMapper.selectById(dto.getApplicationId());
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        if (!BorrowStatus.PENDING_SUPER.getCode().equals(application.getStatus())) {
            throw new BusinessException("当前状态不允许校级审批");
        }

        boolean isPass = Boolean.TRUE.equals(dto.getApproved());
        String opinion = dto.getApprovalOpinion();
        if (!isPass && !StringUtils.hasText(opinion)) {
            throw new BusinessException("驳回时必须填写审批意见");
        }

        BorrowApproval approval = new BorrowApproval();
        approval.setApplicationId(dto.getApplicationId());
        approval.setApproverId(currentUserId);
        approval.setApprovalStatus(isPass ? "PASS" : "REJECT");
        approval.setOpinion(opinion);
        borrowApprovalMapper.insert(approval);

        if (isPass) {
            application.setStatus(BorrowStatus.APPROVED.getCode());
        } else {
            application.setStatus(BorrowStatus.REJECTED.getCode());
        }
        borrowApplicationMapper.updateById(application);
    }

    @Override
    @Transactional
    public void confirmBorrow(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        BorrowApplication application = borrowApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能确认自己的领用申请");
        }

        if (!BorrowStatus.APPROVED.getCode().equals(application.getStatus())) {
            throw new BusinessException("当前状态不允许确认领用");
        }

        application.setStatus(BorrowStatus.BORROWED.getCode());
        borrowApplicationMapper.updateById(application);

        Asset asset = assetMapper.selectById(application.getAssetId());
        if (asset != null) {
            asset.setStatus(AssetStatus.IN_USE.getCode());
            asset.setUserId(application.getUserId());
            assetMapper.updateById(asset);
        }
    }

    @Override
    @Transactional
    public void returnApply(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        BorrowApplication application = borrowApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能申请归还自己的领用");
        }

        if (!BorrowStatus.BORROWED.getCode().equals(application.getStatus())) {
            throw new BusinessException("当前状态不允许申请归还");
        }

        application.setStatus(BorrowStatus.RETURNING.getCode());
        borrowApplicationMapper.updateById(application);
    }

    @Override
    @Transactional
    public void confirmReturn(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String role = currentUser.getRole();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.COLLEGE_ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        BorrowApplication application = borrowApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        if (!BorrowStatus.RETURNING.getCode().equals(application.getStatus())) {
            throw new BusinessException("当前状态不允许确认归还");
        }

        if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            if (!application.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException("只能确认本学院的归还申请");
            }
        }

        application.setStatus(BorrowStatus.RETURNED.getCode());
        borrowApplicationMapper.updateById(application);

        Asset asset = assetMapper.selectById(application.getAssetId());
        if (asset != null) {
            asset.setStatus(AssetStatus.IDLE.getCode());
            asset.setUserId(null);
            assetMapper.updateById(asset);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        BorrowApplication application = borrowApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        if (!application.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己的申请");
        }

        if (!BorrowStatus.REJECTED.getCode().equals(application.getStatus())
                && !BorrowStatus.PENDING_COLLEGE.getCode().equals(application.getStatus())
                && !BorrowStatus.PENDING_SUPER.getCode().equals(application.getStatus())) {
            throw new BusinessException("只有待审批或已驳回状态的申请可以取消");
        }

        borrowApplicationMapper.deleteById(id);
    }

    private LambdaQueryWrapper<BorrowApplication> buildQueryWrapper(BorrowQueryDTO dto) {
        LambdaQueryWrapper<BorrowApplication> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(BorrowApplication::getStatus, dto.getStatus());
        }
        if (dto.getApplicantId() != null) {
            wrapper.eq(BorrowApplication::getUserId, dto.getApplicantId());
        }
        if (dto.getCollegeId() != null) {
            wrapper.eq(BorrowApplication::getCollegeId, dto.getCollegeId());
        }
        if (StringUtils.hasText(dto.getAssetName())) {
            List<Asset> assets = assetMapper.selectList(
                    new LambdaQueryWrapper<Asset>().like(Asset::getName, dto.getAssetName())
            );
            List<Long> assetIds = assets.stream().map(Asset::getId).collect(Collectors.toList());
            if (!assetIds.isEmpty()) {
                wrapper.in(BorrowApplication::getAssetId, assetIds);
            } else {
                wrapper.in(BorrowApplication::getAssetId, -1L);
            }
        }

        return wrapper;
    }

    private void checkDataPermission(BorrowApplication application, SysUser currentUser) {
        String role = currentUser.getRole();
        if (RoleConstants.USER.equals(role)) {
            if (!application.getUserId().equals(currentUser.getId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        } else if (RoleConstants.COLLEGE_ADMIN.equals(role)) {
            if (!application.getCollegeId().equals(currentUser.getCollegeId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
        }
    }

    private BorrowApplicationVO convertToVO(BorrowApplication application) {
        BorrowApplicationVO vo = new BorrowApplicationVO();
        BeanUtils.copyProperties(application, vo);

        vo.setStatusName(BorrowStatus.getLabelByCode(application.getStatus()));

        Asset asset = assetMapper.selectById(application.getAssetId());
        if (asset != null) {
            vo.setAssetNo(asset.getAssetNo());
            vo.setAssetName(asset.getName());
        }

        SysUser user = sysUserMapper.selectById(application.getUserId());
        if (user != null) {
            vo.setUserName(user.getRealName());
        }

        if (application.getCollegeId() != null) {
            College college = collegeMapper.selectById(application.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }

        return vo;
    }

    private BorrowApprovalVO convertApprovalToVO(BorrowApproval approval) {
        BorrowApprovalVO vo = new BorrowApprovalVO();
        BeanUtils.copyProperties(approval, vo);

        if ("PASS".equals(approval.getApprovalStatus())) {
            vo.setStatusName("通过");
        } else if ("REJECT".equals(approval.getApprovalStatus())) {
            vo.setStatusName("驳回");
        } else {
            vo.setStatusName(approval.getApprovalStatus());
        }

        SysUser approver = sysUserMapper.selectById(approval.getApproverId());
        if (approver != null) {
            vo.setApproverName(approver.getRealName());
        }

        return vo;
    }
}
