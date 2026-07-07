package xyz.wolegelei.nepu_fams.dto.repair;

import java.io.Serializable;
import lombok.Data;

@Data
public class RepairSubmitDTO implements Serializable {

    private Long assetId;

    private String faultDescription;

    private String priority;

    private String remark;

    private String attachmentUrls;
}
