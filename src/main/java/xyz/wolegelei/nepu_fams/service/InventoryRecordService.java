package xyz.wolegelei.nepu_fams.service;

import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordBatchDTO;
import xyz.wolegelei.nepu_fams.dto.inventory.InventoryRecordDTO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryDiffVO;
import xyz.wolegelei.nepu_fams.vo.inventory.InventoryRecordVO;

import java.util.List;

public interface InventoryRecordService {

    List<InventoryRecordVO> listByTaskId(Long taskId);

    void addRecord(InventoryRecordDTO dto);

    void batchAddRecords(InventoryRecordBatchDTO dto);

    List<InventoryDiffVO> getDiffList(Long taskId);

    void confirmDiff(Long taskId, List<Long> assetIds);
}
