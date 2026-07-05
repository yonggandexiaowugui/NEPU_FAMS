package xyz.wolegelei.nepu_fams.dto.scrap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.wolegelei.nepu_fams.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScrapQueryDTO extends PageQuery {

    private String status;

    private String assetName;

    private Long collegeId;

    private Long proposerId;
}