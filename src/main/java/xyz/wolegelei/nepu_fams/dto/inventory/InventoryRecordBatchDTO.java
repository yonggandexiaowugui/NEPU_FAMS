package xyz.wolegelei.nepu_fams.dto.inventory;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class InventoryRecordBatchDTO implements Serializable {

    private Long taskId;

    private List<InventoryRecordDTO> records;
}