import request from './request'

export function getCategoryTree() {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

export function getCategoryList(params) {
  return request({
    url: '/category/list',
    method: 'get',
    params
  })
}

export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/category/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}

export function getCategoryById(id) {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}
