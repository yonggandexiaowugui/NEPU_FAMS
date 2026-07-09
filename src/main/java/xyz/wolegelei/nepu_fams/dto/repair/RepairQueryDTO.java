package xyz.wolegelei.nepu_fams.dto.repair;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.wolegelei.nepu_fams.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class RepairQueryDTO extends PageQuery {

    private String status;

    private String assetName;

    private Long assigneeId;

    private Long collegeId;

    private String priority;
}
