package xyz.wolegelei.nepu_fams.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StatusStatsVO implements Serializable {

    private String status;

    private String statusName;

    private Integer count;

    private BigDecimal percentage;
}
