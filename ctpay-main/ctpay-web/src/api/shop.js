import request from '@/utils/request'

/**********************商户********************/
// 获取商户列表
export function getShopEntityList(data) {
  return request({
    url: '/shopEntity/list',
    method: 'post',
    data: data
  })
}

// 获取商户列表-不分页
export function getShopListAll(data) {
  return request({
    url: '/shopEntity/listNoPage',
    method: 'post',
    data: data
  })
}

// 根据id获取商户详情
export function getShopDetail(query) {
  return request({
    url: '/shopEntity/getById',
    method: 'get',
    params: query
  })
}

// 新增商户
export function addShopEntity(data) {
  return request({
    url: '/shopEntity/add',
    method: 'post',
    data: data
  })
}

// 修改商户
export function updateShopEntity(data) {
  return request({
    url: '/shopEntity/update',
    method: 'post',
    data: data
  })
}

// 修改商户收银台姓名
export function updateMemberFlag(query) {
  return request({
    url: '/shopEntity/updateMemberFlag',
    method: 'get',
    params: query
  })
}

// 删除商户
export function deleteShop(query) {
  return request({
    url: '/shopEntity/deleteAgentShop',
    method: 'get',
    params: query
  })
}

// 更新商户状态
export function upateStatus(query) {
  return request({
    url: '/shopEntity/upateStatus',
    method: 'get',
    params: query
  })
}

// 查看密钥
export function showSecret(query) {
  return request({
    url: '/shopEntity/showSecret',
    method: 'get',
    params: query
  })
}

// 修改密钥
export function resetScret(query) {
  return request({
    url: '/shopEntity/resetScret',
    method: 'get',
    params: query
  })
}

// 商户基础通道列表
export function getShopBaseList(query) {
  return request({
    url: '/shopBaseChannelEntity/list',
    method: 'get',
    params: query
  })
}

// 修改商户基础通道列表
export function updateBatchShopBase(data) {
  return request({
    url: '/shopBaseChannelEntity/updateBatch',
    method: 'post',
    data: data
  })
}

// 商户绑定tg群组
export function bindTelegramGroup(query) {
  return request({
    url: '/shopEntity/bingTg',
    method: 'get',
    params: query
  })
}

// 解绑商户tg群组
export function unBindTelegramGroup(query) {
  return request({
    url: '/shopEntity/unBindTelegramGroup',
    method: 'get',
    params: query
  })
}

// 一键解绑商户tg群组
export function unBindTelegramGroupOneKey(query) {
  return request({
    url: '/shopEntity/unBindTelegramGroupOneKey',
    method: 'get',
    params: query
  })
}

// 资金明细列表
export function getShopAmountRecordsList(data) {
  return request({
    url: '/shopAmountRecordsEntity/list',
    method: 'post',
    data: data
  })
}

// 充值余额
export function shopChargeAmount(query) {
  return request({
    url: '/shopEntity/chargeAmount',
    method: 'get',
    params: query
  })
}

// 获取商户码商关联列表
export function shopMerchantRelationList(data) {
  return request({
    url: '/shopMerchantRelation/list',
    method: 'post',
    data: data
  })
}

// 修改商户码商关联关系的状态
export function shopMerchantRelationUpdateStatus(query) {
  return request({
    url: '/shopMerchantRelation/updateStatus',
    method: 'get',
    params: query
  })
}
