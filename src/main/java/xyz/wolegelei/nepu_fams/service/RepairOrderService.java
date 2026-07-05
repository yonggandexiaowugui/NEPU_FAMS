package xyz.wolegelei.nepu_fams.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.wolegelei.nepu_fams.dto.repair.RepairAssignDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairProgressDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairQueryDTO;
import xyz.wolegelei.nepu_fams.dto.repair.RepairSubmitDTO;
import xyz.wolegelei.nepu_fams.vo.repair.RepairOrderDetailVO;
import xyz.wolegelei.nepu_fams.vo.repair.RepairOrderVO;

public interface RepairOrderService {

    void submit(RepairSubmitDTO dto);

    IPage<RepairOrderVO> page(RepairQueryDTO dto);

    RepairOrderDetailVO getById(Long id);

    void assign(RepairAssignDTO dto);

    void updateProgress(RepairProgressDTO dto);

    IPage<RepairOrderVO> myAssignedList(RepairQueryDTO dto);

    void delete(Long id);
}
