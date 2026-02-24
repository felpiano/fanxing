import request from '@/utils/request'

// 获取通道列表
export function getChannelList(data) {
  return request({
    url: '/channelEntity/list',
    method: 'post',
    data: data
  })
}

// 获取不分页的通道列表
export function getChannelListAll(data) {
  return request({
    url: '/channelEntity/listNoPage',
    method: 'post',
    data: data
  })
}

// 修改通道
export function updateChannelList(data) {
  return request({
    url: '/channelEntity/update',
    method: 'post',
    data: data
  })
}

// 新增通道
export function addChannelEntity(data) {
  return request({
    url: '/channelEntity/add',
    method: 'post',
    data: data
  })
}

// 修改通道状态
export function upateStatus(query) {
  return request({
    url: '/channelEntity/upateStatus',
    method: 'get',
    params: query
  })
}

// 删除通道
export function deleteChannel(query) {
  return request({
    url: '/channelEntity/deleteChannel',
    method: 'get',
    params: query
  })
}
