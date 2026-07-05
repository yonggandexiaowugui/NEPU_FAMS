package xyz.wolegelei.nepu_fams.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import xyz.wolegelei.nepu_fams.common.AssetStatus;
import xyz.wolegelei.nepu_fams.dto.asset.AssetAddDTO;
import xyz.wolegelei.nepu_fams.dto.asset.AssetQueryDTO;
import xyz.wolegelei.nepu_fams.dto.asset.AssetUpdateDTO;
import xyz.wolegelei.nepu_fams.vo.asset.AssetVO;

import java.util.List;

public interface AssetService {

    IPage<AssetVO> page(AssetQueryDTO dto);

    AssetVO getById(Long id);

    void add(AssetAddDTO dto);

    void update(Long id, AssetUpdateDTO dto);

    void delete(Long id);

    List<AssetVO> export(AssetQueryDTO dto);

    void updateStatus(Long id, AssetStatus status);

    String getAssetNo();
}
