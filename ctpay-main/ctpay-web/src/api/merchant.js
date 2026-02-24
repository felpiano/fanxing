import request from '@/utils/request'

/**********************码商********************/
// 获取一级码商列表
export function getMerchantyList(data) {
  return request({
    url: '/merchantEntity/list',
    method: 'post',
    data: data
  })
}

// 获取码商列表
export function getAllMerchantyList(data) {
  return request({
    url: '/merchantEntity/merchantList',
    method: 'post',
    data: data
  })
}

// 获取不分页码商列表
export function getMerchantaAll(data) {
  return request({
    url: 'merchantEntity/listNoPage',
    method: 'post',
    data: data
  })
}

// 新增码商
export function addMerchantEntity(data) {
  return request({
    url: '/merchantEntity/add',
    method: 'post',
    data: data
  })
}

// 修改码商
export function updateMerchantEntity(data) {
  return request({
    url: '/merchantEntity/update',
    method: 'post',
    data: data
  })
}

// 删除码商
export function deleteMerchant(query) {
  return request({
    url: '/merchantEntity/delete',
    method: 'get',
    params: query
  })
}

// 修改码商余额或押金
// changeType	变更类型：1-余额；3-押金
export function updateAmount(query) {
  return request({
    url: '/merchantEntity/updateAmount',
    method: 'get',
    params: query
  })
}

// 码商一键开工停工
export function stopWrok(query) {
  return request({
    url: '/merchantEntity/stopWrok',
    method: 'get',
    params: query
  })
}

// 清谷歌
export function clearGoogle(query) {
  return request({
    url: '/merchantEntity/clearGoogle',
    method: 'get',
    params: query
  })
}

// 设置安全码
export function setSafeCode(query) {
  return request({
    url: '/merchantEntity/setSafeCode',
    method: 'get',
    params: query
  })
}

// 获取码商本人信息
export function getMerchantDetail(query) {
  return request({
    url: '/merchantEntity/getDetail',
    method: 'get',
    params: query
  })
}

// 码商自己开工停工
export function stopWrokByMerchant(query) {
  return request({
    url: '/merchantEntity/stopWrokByMerchant',
    method: 'get',
    params: query
  })
}

// 码商开启或关闭来单提醒
export function orderRemindMerchant(query) {
  return request({
    url: '/merchantEntity/orderRemind',
    method: 'get',
    params: query
  })
}

// 佣金划转开关：0-开启；1-关闭
export function queryCommissionType(query) {
  return request({
    url: '/merchantEntity/commissionType',
    method: 'get',
    params: query
  })
}

// 码商通道列表
export function getMerchantChannelList(query) {
  return request({
    url: '/merchantChannelEntity/list',
    method: 'get',
    params: query
  })
}


// 修改码商通道列表
export function updateBatchMerchantChannel(data) {
  return request({
    url: '/merchantChannelEntity/updateBatch',
    method: 'post',
    data: data
  })
}

// 修改码商通道列表-单条修改
export function updateChannelRate(data) {
  return request({
    url: '/merchantChannelEntity/updateChannelRate',
    method: 'post',
    data: data
  })
}

// 码商查看自身通道列表
export function getSelfChannel(query) {
  return request({
    url: '/merchantChannelEntity/selfChannel',
    method: 'get',
    params: query
  })
}


// 码商帐变列表--资金明细
export function getMerchantAmountRecordsList(data) {
  return request({
    url: '/merchantAmountRecordsEntity/list',
    method: 'post',
    data: data
  })
}

// 获取我的上级所有码商
export function getAllParent(query) {
  return request({
    url: '/merchantEntity/allParent',
    method: 'get',
    params: query
  })
}

// 佣金转余额
// hasPermi('system:merchant:trimFreezeToBalance')
export function trimFreezeToBalance(query) {
  return request({
    url: '/merchantEntity/trimFreezeToBalance',
    method: 'get',
    params: query
  })
}

// 解绑码商tg群组
export function unBindMTelegramGroup(query) {
  return request({
    url: '/merchantEntity/unBindTelegramGroup',
    method: 'get',
    params: query
  })
}

// 更新接单设置中的团队接单状态
export function agentContrlOrder(query) {
  return request({
    url: '/merchantChannelEntity/agentContrl',
    method: 'get',
    params: query
  })
}

// 一级码商查所有下级资金明细
export function getOneMerchantList(data) {
  return request({
    url: '/merchantAmountRecordsEntity/oneMerchantList',
    method: 'post',
    data: data
  })
}

//解除用户锁定
export function unlockUser(query) {
  return request({
    url: '/merchantEntity/unlockUser',
    method: 'get',
    params: query
  })
}


