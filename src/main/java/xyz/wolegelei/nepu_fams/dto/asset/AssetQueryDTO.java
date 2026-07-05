package xyz.wolegelei.nepu_fams.dto.asset;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.wolegelei.nepu_fams.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssetQueryDTO extends PageQuery {

    private String keyword;

    private Long categoryId;

    private String status;

    private Long collegeId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;
}