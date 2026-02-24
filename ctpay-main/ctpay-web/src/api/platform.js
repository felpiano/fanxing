import request from '@/utils/request'

// 获取第三方平台列表
export function getPlatformEntityList(data) {
  return request({
    url: '/platformEntity/list',
    method: 'post',
    data: data
  })
}

// 修改第三方平台状态
export function upateStatus(query) {
  return request({
    url: '/platformEntity/upateStatus',
    method: 'get',
    params: query
  })
}

// 新增第三方平台
export function addPlatformEntity(data) {
  return request({
    url: '/platformEntity/add',
    method: 'post',
    data: data
  })
}

// 修改第三方平台
export function updatePlatformEntity(data) {
  return request({
    url: '/platformEntity/update',
    method: 'post',
    data: data
  })
}

// 获取第三方平台不分页列表
export function getPlatformListAll(data) {
  return request({
    url: '/platformEntity/listNoPage',
    method: 'post',
    data: data
  })
}

// 删除第三方平台
export function deletePlatform(query) {
  return request({
    url: '/platformEntity/deletePlatform',
    method: 'get',
    params: query
  })
}

// 调整第三方平台余额
export function changePlatBalance(data) {
  return request({
    url: '/platformEntity/trimPlatBalance',
    method: 'post',
    data: data
  })
}

// 查询第三方平台的通道列表
export function getPlatformChannel(query) {
  return request({
    url: '/platformEntity/mcList',
    method: 'get',
    params: query
  })
}

// 新增第三方平台的通道列表
export function addPlatChannel(data) {
  return request({
    url: '/platformEntity/addPlatChannel',
    method: 'post',
    data: data
  })
}

// 修改第三方平台的通道列表
export function updatePlatChannel(data) {
  return request({
    url: '/platformEntity/updatePlatChannel',
    method: 'post',
    data: data
  })
}

// 批量保存平台通道
export function saveBatchPlatChannel(data) {
  return request({
    url: '/platformEntity/saveBatchPlatChannel',
    method: 'post',
    data: data
  })
}


// 获取第三方平台账单列表
export function getChargeRecordsList(data) {
  return request({
    url: '/platformChargeRecordsEntity/list',
    method: 'post',
    data: data
  })
}


// 解绑tg账号
export function unBindTelegramGroup(query) {
  return request({
    url: '/platformEntity/unBindTelegramGroup',
    method: 'get',
    params: query
  })
}

// 平台绑定tg群组
export function bindPtTelegramGroup(query) {
  return request({
    url: '/telegram/bindPtTelegramGroup',
    method: 'get',
    params: query
  })
}


// 平台通道列表接口
export function getMcListAll(query) {
  return request({
    url: '/platformEntity/mcListAll',
    method: 'get',
    params: query
  })
}

// 通道配置--获取列表
export function getMcListByChannel(data) {
  return request({
    url: '/platformEntity/mcListByChannel',
    method: 'post',
    data: data
  })
}


