import request from '@/utils/request'

// 码商通道列表--获取对应的码商列表
export function getMerchantQrcodeList(data) {
  return request({
    url: '/merchantQrcodeEntity/list',
    method: 'post',
    data: data
  })
}

// 新增或修改码
export function addMerchantQrcode(data) {
  return request({
    url: '/merchantQrcodeEntity/add',
    method: 'post',
    data: data
  })
}

// 删除码
export function deleteMerchantQrcode(query) {
  return request({
    url: '/merchantQrcodeEntity/delete',
    method: 'get',
    params: query
  })
}

// 修改码进单状态
export function updateMerchantQrcodeStatus(query) {
  return request({
    url: '/merchantQrcodeEntity/update',
    method: 'get',
    params: query
  })
}

// 统计码商所有通道码
export function allChildQrcodeTotal(data) {
  return request({
    url: '/merchantChild/allChildQrcodeTotal',
    method: 'post',
    data: data
  })
}

// 是否需要账号
export function needAccountNumber(query) {
  return request({
    url: '/merchantQrcodeEntity/needAccountNumber',
    method: 'get',
    params: query
  })
}
