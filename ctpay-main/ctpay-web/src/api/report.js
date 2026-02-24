import request from '@/utils/request'

// 获取统计报表
// 统计类型：1-代理、2-商户、3-通道、4-平台、5-时间
export function getReportList(query) {
  return request({
    url: '/orderMainEntity/report',
    method: 'get',
    params: query
  })
}


// 码商首页统计
export function getReportMerchant(data) {
  return request({
    url: '/report/homeReport',
    method: 'post',
    data: data
  })
}

// 代理首页统计 type 1-今日、2-昨日、3-本周
export function getAgentHomeReport(query) {
  return request({
    url: '/report/agentHomeReport',
    method: 'get',
    params: query
  })
}

// 代理对账报表
export function getReportForAgent(data) {
  return request({
    url: '/report/reportForAgent',
    method: 'post',
    data: data
  })
}

// 代理对账报表
//@ApiOperation("代理统计报表日期选择")
// @PreAuthorize("@ss.hasPermi('system:eport:reportForAgent')")
export function reportForAgentByDate(data) {
  return request({
    url: '/report/reportForAgentByDate',
    method: 'post',
    data: data
  })
}

// 佣金列表
// @PreAuthorize("@ss.hasPermi('system:eport:commissionList')")
export function commissionList(data) {
  return request({
    url: '/report/commissionList',
    method: 'post',
    data: data
  })
}


// *****************代理统计通道报表*****************
// 代理查询通道管理
// system:eport:agentQrcodeOrder
export function getAgentQrcodeOrder(data) {
  return request({
    url: '/report/agentQrcodeOrder',
    method: 'post',
    data: data
  })
}

// 代理查询通道管理统计
export function getAgentQrReport(data) {
  return request({
    url: '/report/agentQrReport',
    method: 'post',
    data: data
  })
}

// *****************码商对账报表*****************
export function reportForMerchant(data) {
  return request({
    url: '/report/reportForMerchant',
    method: 'post',
    data: data
  })
}

// *****************统计报表*****************

// 统计报表 --类型：1-商户；2-日期；3-通道
export function reportByCondition(data) {
  return request({
    url: '/report/reportByCondition',
    method: 'post',
    data: data
  })
}

