package xyz.wolegelei.nepu_fams.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.wolegelei.nepu_fams.dto.log.OperationLogQueryDTO;
import xyz.wolegelei.nepu_fams.entity.OperationLog;
import xyz.wolegelei.nepu_fams.vo.log.OperationLogVO;

import java.util.List;

public interface OperationLogService {

    IPage<OperationLogVO> page(OperationLogQueryDTO dto);

    OperationLogVO getById(Long id);

    void add(OperationLog operationLog);

    List<OperationLogVO> export(OperationLogQueryDTO dto);
}
