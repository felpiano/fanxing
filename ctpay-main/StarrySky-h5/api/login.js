import request from './request.js'

// 登录
export function login(data) {
	return request('POST', '/login', data)
}

// 注册
export function register(data) {
	return request('POST', '/register', data)
}

// 退出方法
export function logout() {
  return request('POST', '/logout')
}

// 获取用户详细信息
export function getInfo() {
  return request('GET', '/getInfo')
}

// 获取本人信息
export function getMerchantDetail() {
  return request('GET', '/merchantEntity/getDetail')
}

// 用户密码重置
export function updateUserPwd(data) {
  return request('PUT',  '/system/user/profile/updatePwd', data)
}