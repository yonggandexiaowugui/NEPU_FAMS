package xyz.wolegelei.nepu_fams.vo.asset;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AssetCategoryVO implements Serializable {

    private Long id;

    private String name;

    private Long parentId;

    private Integer level;

    private Integer sort;

    private List<AssetCategoryVO> children;
}
