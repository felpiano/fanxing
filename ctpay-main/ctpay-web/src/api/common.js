import request from '@/utils/request'

// 获取谷歌验证码
export function getGoogleSecret(query) {
  return request({
    url: '/common/getGoogleSecret',
    method: 'get',
    params: query
  })
}

// 绑定谷歌验证码
export function bindGoogle(query) {
  return request({
    url: '/merchantEntity/bindGoogle',
    method: 'get',
    params: query
  })
}
