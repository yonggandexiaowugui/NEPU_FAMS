package xyz.wolegelei.nepu_fams.asset.three.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_3d_scene")
public class ThreeScene {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sceneName;

    private String sceneLayoutJson;

    private Long collegeId;

    private Long createBy;

    private LocalDateTime createTime;
}
