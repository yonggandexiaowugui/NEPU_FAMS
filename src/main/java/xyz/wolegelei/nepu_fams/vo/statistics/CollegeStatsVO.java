package xyz.wolegelei.nepu_fams.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CollegeStatsVO implements Serializable {

    private Long collegeId;

    private String collegeName;

    private Integer assetCount;

    private BigDecimal totalValue;

    private Integer idleCount;

    private Integer inUseCount;

    private Integer repairingCount;

    private Integer scrappedCount;
}
