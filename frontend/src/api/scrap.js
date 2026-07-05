import request from './request'

export function getScrapList(params) {
  return request({
    url: '/scrap/page',
    method: 'get',
    params
  })
}

export function getScrapApprovalList(params) {
  return request({
    url: '/scrap/approval/page',
    method: 'get',
    params
  })
}

export function getMyScrapList(params) {
  return request({
    url: '/scrap/my',
    method: 'get',
    params
  })
}

export function applyScrap(data) {
  return request({
    url: '/scrap/apply',
    method: 'post',
    data
  })
}

export function getScrapDetail(id) {
  return request({
    url: `/scrap/${id}`,
    method: 'get'
  })
}

export function approveScrap(id, data) {
  return request({
    url: `/scrap/approve/${id}`,
    method: 'post',
    data
  })
}

export function rejectScrap(id, data) {
  return request({
    url: `/scrap/reject/${id}`,
    method: 'post',
    data
  })
}

export function cancelScrap(id) {
  return request({
    url: `/scrap/cancel/${id}`,
    method: 'post'
  })
}
