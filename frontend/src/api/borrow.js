import request from './request'

export function getBorrowList(params) {
  return request({
    url: '/borrow/page',
    method: 'get',
    params
  })
}

export function getMyBorrowList(params) {
  return request({
    url: '/borrow/my',
    method: 'get',
    params
  })
}

export function applyBorrow(data) {
  return request({
    url: '/borrow/apply',
    method: 'post',
    data
  })
}

export function getBorrowDetail(id) {
  return request({
    url: `/borrow/${id}`,
    method: 'get'
  })
}

export function approveBorrow(id, data) {
  return request({
    url: `/borrow/approve/${id}`,
    method: 'post',
    data
  })
}

export function returnBorrow(id) {
  return request({
    url: `/borrow/return/${id}`,
    method: 'post'
  })
}

export function cancelBorrow(id) {
  return request({
    url: `/borrow/cancel/${id}`,
    method: 'post'
  })
}
