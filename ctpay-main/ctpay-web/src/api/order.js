import request from '@/utils/request'

// 码商-获取订单列表
export function getInOrderList(data) {
  return request({
    url: '/inOrderEntity/list',
    method: 'post',
    data: data
  })
}

// 后台创建测试单
export function createTest(data) {
  return request({
    url: '/inOrderEntity/createTest',
    method: 'post',
    data: data
  })
}

// 后台创建测试单--指定码商
// hasPermi('system:order:createTest')
export function createTestByMerchant(data) {
  return request({
    url: '/inOrderEntity/createTestByMerchant',
    method: 'post',
    data: data
  })
}

// 码商-成功（回调）
export function repairInOrder(query) {
  return request({
    url: '/inOrderEntity/repair',
    method: 'get',
    params: query
  })
}

// 商户/代理/管理员  获取订单列表
export function getInOrderDetailList(data) {
  return request({
    url: '/inOrderDetailEntity/list',
    method: 'post',
    data: data
  })
}

// 获取订单详情
export function getInOrderDetailInfo(query) {
  return request({
    url: '/inOrderDetailEntity/getById',
    method: 'get',
    params: query
  })
}

//代理--获取订单统计
export function getAgentOrderTotal(data) {
  return request({
    url: '/inOrderDetailEntity/listTotal',
    method: 'post',
    data: data
  })
}


// 码商--获取订单统计
export function getInOrderTotal(data) {
  return request({
    url: '/inOrderEntity/listTatal',
    method: 'post',
    data: data
  })
}

// 代理-补单
export function repairAgentOrder(query) {
  return request({
    url: '/inOrderEntity/agentRepair',
    method: 'get',
    params: query
  })
}

// 处理超时未提交订单
// 'system:order:backOrder'
export function backOrder(query) {
  return request({
    url: '/inOrderEntity/backOrder',
    method: 'get',
    params: query
  })
}

// 代理点击未收到付款
// 'system:order:unPaid'
export function agentOrderUnPaid(query) {
  return request({
    url: '/inOrderEntity/agentUnPaid',
    method: 'get',
    params: query
  })
}

// 码商点击未收到付款
// 'system:order:unPaid'
export function orderUnPaid(query) {
  return request({
    url: '/inOrderEntity/unPaid',
    method: 'get',
    params: query
  })
}

// 冲正
// 权限：system:order:czorder
export function czOrder(query) {
  return request({
    url: 'inOrderEntity/czOrder',
    method: 'get',
    params: query
  })
}


