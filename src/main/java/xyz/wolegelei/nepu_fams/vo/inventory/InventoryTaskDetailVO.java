package xyz.wolegelei.nepu_fams.vo.inventory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.wolegelei.nepu_fams.vo.UserVO;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryTaskDetailVO extends InventoryTaskVO implements Serializable {

    private List<UserVO> assignees;
}
