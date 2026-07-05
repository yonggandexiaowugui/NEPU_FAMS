package xyz.wolegelei.nepu_fams.vo.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {

    private Long id;

    private Long userId;

    private String username;

    private String operation;

    private String type;

    private String typeName;

    private String method;

    private String params;

    private String ip;

    private LocalDateTime createTime;
}
