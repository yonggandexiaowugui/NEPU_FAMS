package xyz.wolegelei.nepu_fams.dto.repair;

import java.io.Serializable;
import lombok.Data;

@Data
public class RepairAssignDTO implements Serializable {

    private Long orderId;

    private Long assigneeId;

    private String priority;
}