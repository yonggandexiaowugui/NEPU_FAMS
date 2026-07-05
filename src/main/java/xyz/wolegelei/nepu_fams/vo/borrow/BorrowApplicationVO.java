package xyz.wolegelei.nepu_fams.vo.borrow;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BorrowApplicationVO implements Serializable {

    private Long id;

    private Long assetId;

    private String assetNo;

    private String assetName;

    private Long userId;

    private String userName;

    private Long collegeId;

    private String collegeName;

    private String purpose;

    private LocalDate expectedReturnDate;

    private String status;

    private String statusName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
