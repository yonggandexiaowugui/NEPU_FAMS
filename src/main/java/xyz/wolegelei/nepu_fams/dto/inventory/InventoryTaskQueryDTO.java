package xyz.wolegelei.nepu_fams.dto.inventory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.wolegelei.nepu_fams.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryTaskQueryDTO extends PageQuery {

    private String name;

    private String status;

    private Long collegeId;
}