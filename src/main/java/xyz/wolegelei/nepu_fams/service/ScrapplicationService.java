package xyz.wolegelei.nepu_fams.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapApplyDTO;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapApprovalDTO;
import xyz.wolegelei.nepu_fams.dto.scrap.ScrapQueryDTO;
import xyz.wolegelei.nepu_fams.vo.scrap.ScrapplicationVO;

import java.util.List;

public interface ScrapplicationService {

    void apply(ScrapApplyDTO dto);

    IPage<ScrapplicationVO> page(ScrapQueryDTO dto);

    ScrapplicationVO getById(Long id);

    void approve(ScrapApprovalDTO dto);

    void reject(ScrapApprovalDTO dto);

    IPage<ScrapplicationVO> myApplicationList(ScrapQueryDTO dto);

    List<ScrapplicationVO> export(ScrapQueryDTO dto);

    void delete(Long id);
}
