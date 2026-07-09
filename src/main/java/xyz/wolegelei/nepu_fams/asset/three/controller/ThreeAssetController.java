package xyz.wolegelei.nepu_fams.asset.three.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.wolegelei.nepu_fams.asset.three.dto.AssetModelCardVO;
import xyz.wolegelei.nepu_fams.asset.three.dto.BindAssetModelDTO;
import xyz.wolegelei.nepu_fams.asset.three.dto.ThreeSceneDTO;
import xyz.wolegelei.nepu_fams.asset.three.entity.ThreeScene;
import xyz.wolegelei.nepu_fams.asset.three.service.ThreeAssetService;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.vo.asset.AssetVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asset-three")
@RequiredArgsConstructor
public class ThreeAssetController {

    private final ThreeAssetService threeAssetService;

    @PostMapping("/model/upload")
    public Result<Map<String, String>> uploadModel(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(threeAssetService.uploadModel(file));
    }

    @PostMapping("/model/bind")
    public Result<Void> bindAssetModel(@RequestBody BindAssetModelDTO dto) {
        threeAssetService.bindAssetModel(dto.getAssetId(), dto.getModelUrl());
        return Result.success();
    }

    @GetMapping("/model/{assetId}")
    public Result<Map<String, String>> getAssetModel(@PathVariable Long assetId) {
        return Result.success(threeAssetService.getAssetModel(assetId));
    }

    /**
     * 首页 3D 资产展厅：返回当前用户可见的、已绑定 GLB 模型的资产列表。
     */
    @GetMapping("/model/assets")
    public Result<List<AssetModelCardVO>> listModelAssets() {
        return Result.success(threeAssetService.listModelAssets());
    }

    @PostMapping("/scene")
    public Result<Long> addScene(@RequestBody ThreeSceneDTO dto) {
        return Result.success(threeAssetService.addScene(dto));
    }

    @PutMapping("/scene/{id}")
    public Result<Void> updateScene(@PathVariable Long id, @RequestBody ThreeSceneDTO dto) {
        threeAssetService.updateScene(id, dto);
        return Result.success();
    }

    @GetMapping("/scene/list")
    public Result<List<ThreeScene>> listScenes(@RequestParam(required = false) Long collegeId) {
        return Result.success(threeAssetService.listScenes(collegeId));
    }

    @DeleteMapping("/scene/{id}")
    public Result<Void> deleteScene(@PathVariable Long id) {
        threeAssetService.deleteScene(id);
        return Result.success();
    }

    @GetMapping("/scene/{sceneId}/assets")
    public Result<List<AssetVO>> listSceneAssets(@PathVariable Long sceneId) {
        return Result.success(threeAssetService.listSceneAssets(sceneId));
    }
}
