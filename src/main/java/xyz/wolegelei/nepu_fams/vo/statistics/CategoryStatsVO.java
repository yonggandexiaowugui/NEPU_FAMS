package xyz.wolegelei.nepu_fams.vo.statistics;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CategoryStatsVO implements Serializable {

    private Long categoryId;

    private String categoryName;

    private Integer assetCount;

    private BigDecimal totalValue;

    private BigDecimal percentage;
}
