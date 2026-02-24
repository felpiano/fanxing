import request from '@/utils/request'

// 获取代理列表
export function getAgentEntityList(data) {
  return request({
    url: '/agentEntity/list',
    method: 'post',
    data: data
  })
}

// 获取代理不分页列表
export function getAgentEntityListAll(data) {
  return request({
    url: '/agentEntity/listNoPage',
    method: 'post',
    data: data
  })
}

// 新增代理列表
export function addAgentEntity(data) {
  return request({
    url: '/agentEntity/add',
    method: 'post',
    data: data
  })
}

// 修改状态
export function upateStatus(query) {
  return request({
    url: '/agentEntity/upateStatus',
    method: 'get',
    params: query
  })
}

// 删除
export function deleteAgent(query) {
  return request({
    url: '/agentEntity/deleteAgent',
    method: 'get',
    params: query
  })
}

// 修改代理
export function updateAgentEntity(data) {
  return request({
    url: '/agentEntity/update',
    method: 'post',
    data: data
  })
}

// 充值余额
export function chargeAmount(query) {
  return request({
    url: '/agentEntity/chargeAmount',
    method: 'get',
    params: query
  })
}

// 代理获取自身信息
export function getAgentDetail(query) {
  return request({
    url: '/agentEntity/getDetail',
    method: 'get',
    params: query
  })
}

// 产品 --代理通道
export function getAgentChannelList(query) {
  return request({
    url: '/agentEntity/listAgentChannel',
    method: 'get',
    params: query
  })
}

// 修改产品
export function updateAgentChannel(data) {
  return request({
    url: '/agentEntity/updateAgentChannel',
    method: 'post',
    data: data
  })
}

// 管理员设置代理费率
export function saveAgentChannel(data) {
  return request({
    url: '/agentEntity/saveAgentChannel',
    method: 'post',
    data: data
  })
}

// 获取代理帐变列表
export function getAgentAmountRecordsList(data) {
  return request({
    url: '/agentAmountRecordsEntity/list',
    method: 'post',
    data: data
  })
}


// 拉黑IP
export function putBlackClientIp(query) {
  return request({
    url: '/agentEntity/putBlackClientIp',
    method: 'get',
    params: query
  })
}

// 黑名单列表
export function getBlackIpList(query) {
  return request({
    url: '/agentEntity/blackIpList',
    method: 'get',
    params: query
  })
}


// 一键清除所有费率
export function onekeyClearRate(query) {
  return request({
    url: '/agentEntity/onekeyClearRate',
    method: 'get',
    params: query
  })
}

