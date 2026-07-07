package xyz.wolegelei.nepu_fams.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskCreateDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskQueryDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryTaskUpdateDTO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryTaskDetailVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryTaskVO;

public interface InventoryTaskService {

    IPage<InventoryTaskVO> page(InventoryTaskQueryDTO dto);

    InventoryTaskDetailVO getDetail(Long id);

    void create(InventoryTaskCreateDTO dto);

    void update(Long id, InventoryTaskUpdateDTO dto);

    void delete(Long id);

    void start(Long id);

    void complete(Long id);

    void archive(Long id);
}
