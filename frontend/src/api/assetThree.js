import request from './request'

export function uploadThreeModel(data) {
  return request({
    url: '/asset-three/model/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function bindAssetModel(data) {
  return request({
    url: '/asset-three/model/bind',
    method: 'post',
    data
  })
}

export function getAssetModel(assetId) {
  return request({
    url: `/asset-three/model/${assetId}`,
    method: 'get'
  })
}

/**
 * 首页 3D 资产展厅：拉取当前用户可见的、已绑定 GLB 模型的资产卡片数据。
 */
export function listModelAssets() {
  return request({
    url: '/asset-three/model/assets',
    method: 'get'
  })
}
