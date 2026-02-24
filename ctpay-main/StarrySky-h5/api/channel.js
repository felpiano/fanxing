
import request from './request.js'

// 获取不分页的通道列表
export function getChannelListAll(data) {
  return request('POST', '/channelEntity/listNoPage', data)
}