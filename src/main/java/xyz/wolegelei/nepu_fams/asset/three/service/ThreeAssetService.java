package xyz.wolegelei.nepu_fams.asset.three.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.wolegelei.nepu_fams.asset.three.dto.AssetModelCardVO;
import xyz.wolegelei.nepu_fams.asset.three.dto.ThreeSceneDTO;
import xyz.wolegelei.nepu_fams.asset.three.entity.ThreeScene;
import xyz.wolegelei.nepu_fams.vo.asset.AssetVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ThreeAssetService {

    Map<String, String> uploadModel(MultipartFile file) throws IOException;

    void bindAssetModel(Long assetId, String modelUrl);

    Map<String, String> getAssetModel(Long assetId);

    /**
     * 首页 3D 展厅：返回当前用户可见的、已绑定 GLB 模型的资产卡片数据。
     * 超管/校级管理员可看全部，学院管理员与普通用户只看本学院。
     */
    List<AssetModelCardVO> listModelAssets();

    Long addScene(ThreeSceneDTO dto);

    void updateScene(Long id, ThreeSceneDTO dto);

    List<ThreeScene> listScenes(Long collegeId);

    void deleteScene(Long id);

    List<AssetVO> listSceneAssets(Long sceneId);
}
