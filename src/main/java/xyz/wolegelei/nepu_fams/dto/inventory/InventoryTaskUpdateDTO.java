package xyz.wolegelei.nepu_fams.dto.inventory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class InventoryTaskUpdateDTO implements Serializable {

    private String name;

    private Long collegeId;

    private Long categoryId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private List<Long> assigneeIds;
}