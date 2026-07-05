package xyz.wolegelei.nepu_fams.dto.asset;

import java.io.Serializable;
import lombok.Data;

@Data
public class AssetCategoryDTO implements Serializable {

    private String name;

    private Long parentId;

    private Integer sort;
}