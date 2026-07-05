package xyz.wolegelei.nepu_fams.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CollegeVO implements Serializable {

    private Long id;

    private String name;

    private String code;
}
