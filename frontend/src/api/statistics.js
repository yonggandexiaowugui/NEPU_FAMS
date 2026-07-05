import request from './request'

export function getOverviewStats() {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

export function getStatusStats() {
  return request({
    url: '/statistics/status',
    method: 'get'
  })
}

export function getCategoryStats() {
  return request({
    url: '/statistics/category',
    method: 'get'
  })
}

export function getCollegeStats() {
  return request({
    url: '/statistics/college',
    method: 'get'
  })
}

export function getInventoryStats() {
  return request({
    url: '/statistics/inventory',
    method: 'get'
  })
}

export function getTrendStats(params) {
  return request({
    url: '/statistics/trend',
    method: 'get',
    params
  })
}
