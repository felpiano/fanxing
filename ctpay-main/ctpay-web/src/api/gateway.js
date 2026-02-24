import request from '@/utils/request'

// 获取模板列表
export function getModelEntityList(data) {
  return request({
    url: '/modelEntity/list',
    method: 'post',
    data: data
  })
}

// 获取模板列表不分页列表
export function getModelEntityListAll(data) {
  return request({
    url: '/modelEntity/listNoPage',
    method: 'post',
    data: data
  })
}

// 新增模板
export function addModelEntity(data) {
  return request({
    url: '/modelEntity/add',
    method: 'post',
    data: data
  })
}

// 删除模板
export function deleteModel(query) {
  return request({
    url: '/modelEntity/deleteModel',
    method: 'get',
    params: query
  })
}

// 修改模板状态
export function upateStatus(query) {
  return request({
    url: '/modelEntity/upateStatus',
    method: 'get',
    params: query
  })
}

// 修改模板
export function upateModelEntity(data) {
  return request({
    url: '/modelEntity/update',
    method: 'post',
    data: data
  })
}

// 修改模板通道状态
export function updateMcStatus(query) {
  return request({
    url: '/modelEntity/updateMcStatus',
    method: 'get',
    params: query
  })
}

// 查询模板通道列表
export function getMcList(query) {
  return request({
    url: '/modelEntity/mcList',
    method: 'get',
    params: query
  })
}

