package xyz.wolegelei.nepu_fams.dto.borrow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.wolegelei.nepu_fams.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class BorrowQueryDTO extends PageQuery {

    private String status;

    private String assetName;

    private Long applicantId;

    private Long collegeId;

    private Boolean pending;

    private Boolean approved;
}