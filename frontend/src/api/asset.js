import request from './request'

export function getAssetList(params) {
  return request({
    url: '/asset/page',
    method: 'get',
    params
  })
}

export function getAsset(id) {
  return request({
    url: `/asset/${id}`,
    method: 'get'
  })
}

export function addAsset(data) {
  return request({
    url: '/asset',
    method: 'post',
    data
  })
}

export function updateAsset(id, data) {
  return request({
    url: `/asset/${id}`,
    method: 'put',
    data
  })
}

export function deleteAsset(id) {
  return request({
    url: `/asset/${id}`,
    method: 'delete'
  })
}

export function exportAsset(params) {
  return request({
    url: '/asset/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importAsset(data) {
  return request({
    url: '/asset/import',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
