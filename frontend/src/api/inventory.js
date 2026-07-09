import request from './request'

export function getInventoryTaskList(params) {
  return request({
    url: '/inventory/task/page',
    method: 'get',
    params
  })
}

export function getInventoryTaskDetail(id) {
  return request({
    url: `/inventory/task/${id}`,
    method: 'get'
  })
}

export function createInventoryTask(data) {
  return request({
    url: '/inventory/task',
    method: 'post',
    data
  })
}

export function updateInventoryTask(id, data) {
  return request({
    url: `/inventory/task/${id}`,
    method: 'put',
    data
  })
}

export function deleteInventoryTask(id) {
  return request({
    url: `/inventory/task/${id}`,
    method: 'delete'
  })
}

export function startInventoryTask(id) {
  return request({
    url: `/inventory/task/start/${id}`,
    method: 'post'
  })
}

export function completeInventoryTask(id) {
  return request({
    url: `/inventory/task/complete/${id}`,
    method: 'post'
  })
}

export function archiveInventoryTask(id) {
  return request({
    url: `/inventory/task/archive/${id}`,
    method: 'post'
  })
}

export function getInventoryRecordList(params) {
  return request({
    url: '/inventory/record/page',
    method: 'get',
    params
  })
}

export function submitInventoryRecord(data) {
  return request({
    url: '/inventory/record',
    method: 'post',
    data
  })
}

export function batchSubmitInventoryRecord(data) {
  return request({
    url: '/inventory/record/batch',
    method: 'post',
    data
  })
}

export function getInventoryDiffList(params) {
  return request({
    url: '/inventory/diff',
    method: 'get',
    params
  })
}

export function analyzeInventoryDiff(params) {
  return request({
    url: '/inventory/diff/analyze',
    method: 'get',
    params
  })
}

export function getInventoryAiAnalysis(params) {
  return request({
    url: '/inventory/diff/ai-analysis',
    method: 'get',
    params
  })
}

export function confirmInventoryDiff(taskId, assetIds) {
  return request({
    url: `/inventory/diff/confirm/${taskId}`,
    method: 'post',
    data: assetIds
  })
}
