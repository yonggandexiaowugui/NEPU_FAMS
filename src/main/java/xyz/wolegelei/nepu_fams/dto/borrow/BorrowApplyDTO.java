package xyz.wolegelei.nepu_fams.dto.borrow;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

@Data
public class BorrowApplyDTO implements Serializable {

    private Long assetId;

    private String purpose;

    private LocalDate expectedReturnDate;
}