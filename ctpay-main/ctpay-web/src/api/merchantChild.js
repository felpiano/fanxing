import request from '@/utils/request'

// 添加下级码商
export function addMerchantChild(query) {
  return request({
    url: '/merchantChild/addChild',
    method: 'get',
    params: query
  })
}

// 获取码商列表
export function getMerchantList(data) {
  return request({
    url: '/merchantChild/list',
    method: 'post',
    data: data
  })
}

// 获取所有下级码商列表
export function getMerchantChildAllList(data) {
  return request({
    url: '/merchantChild/allChildList ',
    method: 'post',
    data: data
  })
}

// 修改下级码商
export function updateMerchantChild(data) {
  return request({
    url: '/merchantChild/updateChild',
    method: 'post',
    data: data
  })
}

// 码商给下级码商转移金额
export function trimBalanceMerchantChild(query) {
  return request({
    url: '/merchantChild/trimBalance',
    method: 'get',
    params: query
  })
}

// 码商通道列表
export function getChannelList(query) {
  return request({
    url: '/merchantChild/channelList',
    method: 'get',
    params: query
  })
}

// 修改码商通道列表
export function channelUpdate(data) {
  return request({
    url: '/merchantChild/channelUpdate',
    method: 'post',
    data: data
  })
}

export function channelUpdateByMerchantLevel(data) {
  return request({
    url: '/merchantChild/channelUpdateByMerchantLevel',
    method: 'post',
    data: data
  })
}

// 删除码商
export function deleteMerchantChild(query) {
  return request({
    url: '/merchantChild/delete',
    method: 'get',
    params: query
  })
}

// 一键关闭下级所有码商
export function onkeyOff(query) {
  return request({
    url: '/merchantChild/onkeyOff',
    method: 'get',
    params: query
  })
}

//下级转移自己的余额-可以给任何人
// 权限：system:merchantChild:trimBalance
export function trimBalanceFree(query) {
  return request({
    url: '/merchantChild/trimBalanceFree',
    method: 'get',
    params: query
  })
}
