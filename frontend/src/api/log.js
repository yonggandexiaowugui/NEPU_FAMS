import request from './request'

export function getOperationLogList(params) {
  return request({
    url: '/log/operation/page',
    method: 'get',
    params
  })
}

export function getOperationLogDetail(id) {
  return request({
    url: `/log/operation/${id}`,
    method: 'get'
  })
}

export function deleteOperationLog(id) {
  return request({
    url: `/log/operation/${id}`,
    method: 'delete'
  })
}

export function clearOperationLog() {
  return request({
    url: '/log/operation/clear',
    method: 'delete'
  })
}

export function exportOperationLog(params) {
  return request({
    url: '/log/operation/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
