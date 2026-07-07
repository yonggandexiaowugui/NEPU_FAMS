import request from './request'

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export function addUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export function resetUserPassword(id, data) {
  return request({
    url: `/user/${id}/password`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}
