package xyz.wolegelei.nepu_fams.dto.repair;

import java.io.Serializable;
import lombok.Data;

@Data
public class RepairProgressDTO implements Serializable {

    private Long orderId;

    private String status;

    private String repairResult;
}