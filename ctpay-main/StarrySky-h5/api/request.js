import config from '@/config/index'

// 封装请求处理
export default function request(method, url, data){
 return new Promise((resolve, reject) => {
		const token = uni.getStorageSync('SANFANG-TOKEN');
		let header = {}
		if (token) {
			header = {
				'Authorization': 'Bearer ' + token
			}
		}
		uni.request({
			url: config.apiUrl + url,
			method: method,
			data: data,
			header: header,
			success: res => {
				if (res.statusCode === 200) {
					if (res.data.code === 200 || res.data.code === 0) {
						resolve(res.data)
					} else if (res.data.code === 401 || res.data.code === 415) {
						uni.removeStorageSync('SANFANG-TOKEN')
						const xhssf = sessionStorage.getItem('xhssf')
						uni.navigateTo({
							url: '/pages/login?xhssf=' + xhssf
						})
						uni.showToast({
							title: res.data.msg,
							icon: 'none'
						})
						reject(res.data)
					} else if (res.data.code === 500 && res.data.msg === '404') {
						uni.navigateTo({
							url: '/pages/404/404'
						})
					} else {
						uni.showToast({
							title: res.data.msg,
							icon: 'none'
						})
						reject(res.data)
					}
				} else {
					reject(res.data);
				}
			},
			fail: (e) => {
				return reject(e)
			},
			complete: () => {}
		})
 })
}
