import request from './request'

export function getRepairList(params) {
  return request({
    url: '/repair/page',
    method: 'get',
    params
  })
}

export function getMyRepairList(params) {
  return request({
    url: '/repair/my',
    method: 'get',
    params
  })
}

export function submitRepair(data) {
  return request({
    url: '/repair/submit',
    method: 'post',
    data
  })
}

export function getRepairDetail(id) {
  return request({
    url: `/repair/${id}`,
    method: 'get'
  })
}

export function assignRepair(id, data) {
  return request({
    url: `/repair/assign/${id}`,
    method: 'post',
    data
  })
}

export function updateRepairProgress(id, data) {
  return request({
    url: `/repair/progress/${id}`,
    method: 'post',
    data
  })
}

export function completeRepair(id) {
  return request({
    url: `/repair/complete/${id}`,
    method: 'post'
  })
}

export function cancelRepair(id) {
  return request({
    url: `/repair/cancel/${id}`,
    method: 'post'
  })
}
