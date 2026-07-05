package xyz.wolegelei.nepu_fams.dto.scrap;

import java.io.Serializable;
import lombok.Data;

@Data
public class ScrapApprovalDTO implements Serializable {

    private Long applicationId;

    private String approvalOpinion;
}