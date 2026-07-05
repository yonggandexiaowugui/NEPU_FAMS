package xyz.wolegelei.nepu_fams.vo.statistics;

import lombok.Data;

import java.io.Serializable;

@Data
public class InventoryStatsVO implements Serializable {

    private Integer totalCount;

    private Integer checkedCount;

    private Integer profitCount;

    private Integer lossCount;

    private Integer matchCount;
}
