package xyz.wolegelei.nepu_fams.dto.inventory;

import java.io.Serializable;

public class InventoryDiffQueryDTO implements Serializable {

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
