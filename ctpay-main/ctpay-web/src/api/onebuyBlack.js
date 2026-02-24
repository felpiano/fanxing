import request from '@/utils/request'

// 查询一码通黑名单列表
export function listOnebuyBlack(query) {
  return request({
    url: '/system/onebuyBlack/list',
    method: 'post',
    data: query
  })
}

// 查询一码通黑名单详细
export function getOnebuyBlack(id) {
  return request({
    url: '/system/onebuyBlack/getById',
    method: 'get',
    params: {id :id}
  })
}

// 新增一码通黑名单
export function addOnebuyBlack(data) {
  return request({
    url: '/system/onebuyBlack/add',
    method: 'post',
    data: data
  })
}

// 修改一码通黑名单
export function updateOnebuyBlack(data) {
  return request({
    url: '/system/onebuyBlack/update',
    method: 'post',
    data: data
  })
}

// 删除一码通黑名单
export function delOnebuyBlack(ids) {

  return request({
    url: '/system/onebuyBlack/delete',
    method: 'get',
    params: {ids :ids}
  })
}
