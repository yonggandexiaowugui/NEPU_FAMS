package xyz.wolegelei.nepu_fams.dto.asset;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AssetUpdateDTO implements Serializable {

    private String name;

    private Long categoryId;

    private String specification;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

    private String status;

    private String location;

    private Long collegeId;

    private Long userId;

    private String responsiblePerson;

    private String remark;
}