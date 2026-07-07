package xyz.wolegelei.nepu_fams.vo.inventory;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InventoryAnalysisVO implements Serializable {

    private Long taskId;

    private Integer totalCount;

    private Integer matchCount;

    private Integer profitCount;

    private Integer lossCount;

    private String summary;

    private String suggestion;

    private List<InventoryDiffVO> differences;
}
