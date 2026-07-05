package xyz.wolegelei.nepu_fams.service;

import xyz.wolegelei.nepu_fams.dto.asset.AssetCategoryDTO;
import xyz.wolegelei.nepu_fams.vo.asset.AssetCategoryVO;

import java.util.List;

public interface AssetCategoryService {

    List<AssetCategoryVO> listTree();

    List<AssetCategoryVO> listAll();

    AssetCategoryVO getById(Long id);

    void add(AssetCategoryDTO dto);

    void update(Long id, AssetCategoryDTO dto);

    void delete(Long id);
}
