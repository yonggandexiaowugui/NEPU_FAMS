package xyz.wolegelei.nepu_fams.dto.log;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.wolegelei.nepu_fams.common.PageQuery;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQueryDTO extends PageQuery {

    private String username;

    private String operation;

    private String type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}