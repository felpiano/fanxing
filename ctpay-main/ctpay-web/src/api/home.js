import request from '@/utils/request'

// 通道、商户、产品数
export function getHomeInfo(query) {
  return request({
    url: '/home/homeInfo',
    method: 'get',
    params: query
  })
}

// 昨日和今日交易数据
export function getTraceData(query) {
  return request({
    url: '/home/traceData',
    method: 'get',
    params: query
  })
}

// 排行
export function getHomeSort(query) {
  return request({
    url: '/home/homeSort',
    method: 'get',
    params: query
  })
}
