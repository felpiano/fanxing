import request from './request.js'

// 设置安全码
export function setSafeCode(query) {
  return request('GET', '/merchantEntity/setSafeCode', query)
}

// 获取谷歌验证码
export function getGoogleSecret(query) {
  return request('GET', '/common/getGoogleSecret', query)
}

// 绑定谷歌验证码
export function bindGoogle(query) {
  return request('GET', '/merchantEntity/bindGoogle', query)
}

// 码商-成功（回调）
export function repairInOrder(query) {
  return request('GET', '/inOrderEntity/repair', query)
}

// 码商自己开工停工
export function stopWrokByMerchant(query) {
  return request('GET', '/merchantEntity/stopWrokByMerchant', query)
}

// 佣金转余额
// hasPermi('system:merchant:trimFreezeToBalance')
export function trimFreezeToBalance(query) {
  return request('GET', '/merchantEntity/trimFreezeToBalance', query)
}

// 码商查看自身通道列表
export function getSelfChannel(query) {
  return request('GET', '/merchantChannelEntity/selfChannel', query)
}

// 码商帐变列表--资金明细
export function getAmountRecords(data) {
  return request('POST', '/merchantAmountRecordsEntity/list', data)
}

// 码商-获取订单列表
export function getInOrderList(data) {
  return request('POST', '/inOrderEntity/list', data)
}

// 码商--获取订单统计
export function getInOrderTotal(data) {
  return request('POST', '/inOrderEntity/listTatal', data)
}

// 码商通道列表--获取对应的码商列表
export function getQrcodeList(data) {
  return request('POST', '/merchantQrcodeEntity/list', data)
}

// 新增或修改码
export function addMerchantQrcode(data) {
  return request('POST', '/merchantQrcodeEntity/add', data)
}

// 删除码
export function deleteMerchantQrcode(query) {
  return request('GET', '/merchantQrcodeEntity/delete', query)
}

// 修改码进单状态
export function updateMerchantQrcodeStatus(query) {
  return request('GET', '/merchantQrcodeEntity/update', query)
}

// *****************码商对账报表*****************
export function reportForMerchant(data) {
  return request('POST', '/report/reportForMerchant', data)
}

// 获取不分页码商列表
export function getMerchantAll(data) {
  return request('POST', '/merchantEntity/listNoPage', data)
}

// 获取所有下级码商列表
export function getMerchantChildAllList(data) {
  return request('POST', '/merchantChild/allChildList ', data)
}

// 添加下级码商
export function addMerchantChild(query) {
  return request('GET', '/merchantChild/addChild', query)
}

// 修改下级码商
export function updateMerchantChild(data) {
  return request('POST', '/merchantChild/updateChild', data)
}

// 删除码商
export function deleteMerchantChild(query) {
  return request('GET', '/merchantChild/delete', query)
}

// 码商通道列表
export function getChannelList(query) {
  return request('GET', '/merchantChild/channelList', query)
}

// 修改码商通道列表
export function channelUpdate(data) {
  return request('POST', '/merchantChild/channelUpdate', data)
}

// 码商给下级码商转移金额
export function trimBalanceMerchantChild(query) {
  return request('GET', '/merchantChild/trimBalance', query)
}

// 码商点击未收到付款
// 'system:order:unPaid'
export function orderUnPaid(query) {
  return request('GET', '/inOrderEntity/unPaid', query)
}

// 是否需要账号
export function needAccountNumber(query) {
  return request('GET', '/merchantQrcodeEntity/needAccountNumber', query)
}

// 通道码
// {id: ''}
export function getQrcodeId(query) {
  return request('GET', '/merchantQrcodeEntity/getById', query)
}

// 码商开启或关闭来单提醒
export function orderRemindMerchant(query) {
  return request('’GET', '/merchantEntity/orderRemind', query)
}

// 码商开启或关闭自动刷新
export function orderAutoRefresh(query) {
  return request('’GET', '/merchantEntity/orderAutoRefresh', query)
}

