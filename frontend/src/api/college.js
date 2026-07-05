import request from './request'

export function getCollegeList(params) {
  return request({
    url: '/college/list',
    method: 'get',
    params
  })
}

export function getAllColleges() {
  return request({
    url: '/college/all',
    method: 'get'
  })
}

export function addCollege(data) {
  return request({
    url: '/college',
    method: 'post',
    data
  })
}

export function updateCollege(id, data) {
  return request({
    url: `/college/${id}`,
    method: 'put',
    data
  })
}

export function deleteCollege(id) {
  return request({
    url: `/college/${id}`,
    method: 'delete'
  })
}
