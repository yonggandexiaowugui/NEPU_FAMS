package xyz.wolegelei.nepu_fams.vo.inventory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InventoryTaskVO implements Serializable {

    private Long id;

    private String name;

    private Long collegeId;

    private String collegeName;

    private Long categoryId;

    private String categoryName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private String statusName;

    private Long creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer assigneeCount;

    private Integer totalCount;

    private Integer checkedCount;
}
