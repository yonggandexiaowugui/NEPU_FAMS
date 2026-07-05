package xyz.wolegelei.nepu_fams.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OverviewStatsVO implements Serializable {

    private Integer totalAssetCount;

    private BigDecimal totalAssetValue;

    private Integer collegeCount;

    private Integer categoryCount;

    private Integer inUseCount;

    private Integer idleCount;

    private Integer repairingCount;

    private Integer scrappedCount;
}
