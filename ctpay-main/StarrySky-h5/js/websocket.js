import config from '../config/index.js'

//心跳
let heartbeat = null
const timeout = 1000 * 30

let socketTask = null

// 是否创建成功
let isCreated = false
// 是否连接成功
let isConnect = false
// 是否主动断开
let isInitiative = false

// 创建次数
let createdNumber = 1
let createTask = null

// 初始化websocket
export function createdSocket() {
  const token = uni.getStorageSync('SANFANG-TOKEN')
	if (!token) return
	if (socketTask) return
	socketTask = uni.connectSocket({
		url: config.wsUrl + '/ws/orderRemind?Authorization=' + token,
		success: () => {
			isCreated = true
			console.log('创建成功')
			// 停止再次连接
			againCreatedEnd()
		},
		fail: () => {
			isCreated = false
			console.log('创建失败')
			// 重新连接10次，如果还是不成功直接放弃
			againCreated()
		}
	})
	
	openSocket()
}

// 创建websocket连接
export function openSocket() {
	if (isCreated) {
		console.log('WebSocket 开始初始化')
		// 监听 WebSocket 连接打开事件
		socketTask.onOpen((res) => {
			console.info('监听 WebSocket 连接打开事件, 连接成功')
			isConnect = true
			sendSocketMessage()
		})

		// 接收websocket消息及处理
		socketTask.onMessage((res) => {
			// console.log(res)
			const rel = JSON.parse(res.data)
			uni.$emit('updateOrderList', rel)
		})

		// 监听WebSocket错误
		uni.onSocketError((res) => {
			console.info("监听WebSocket错误:" + JSON.stringify(res))
		})

		// 监听 WebSocket 连接关闭事件
		socketTask.onClose(() => {
			console.log('WebSocket 关闭了')
			isConnect = false
			socketTask = null
			// 重新连接
			reconnect()
		})

		// 监听 WebSocket 错误事件
		socketTask.onError((res) => {
			console.log('WebSocket 出错了:', res)
			isInitiative = false
		})
	}
}

// 发送消息
export function sendSocketMessage() {
	// 线停止心跳，再发消息
	heartCheckClose()
	if (!heartbeat) {
		socketTask.send({
			data: 'ping',
			success: () => {
				console.log('发送消息成功')
				// 开启心跳
				heartCheckStart()
			},
			fail: () => {
				console.log('发送消息失败')
			}
		})
	}
}

// 关闭 WebSocket 连接
export function closeSocket() {
	if (!socketTask) return
	socketTask.close({
		reason: '主动关闭',
		success: () => {
			console.log('主动关闭成功')
			socketTask = null
			isCreated = false
			isConnect = false
			isInitiative = true
			heartCheckClose()
			againCreatedEnd()
		},
		fail: () => {
			console.log('主动关闭失败')
			closeSocket()
		}
	})
}

// 如果不主动关闭的话，重新连接
function reconnect() {
	if(!isInitiative) {
		againCreated()
	}
}


// 开启心跳
export function heartCheckStart() {
	heartbeat = setInterval(() => {
		socketTask.send({
			data: 'ping',
			success: () => {
				console.log('发送消息成功')
			},
			fail: () => {
				console.log('发送消息失败')
			}
		})
	}, timeout)
}

// 关闭心跳
export function heartCheckClose() {
	clearInterval(heartbeat)
	heartbeat = null
}

// 重新创建
function againCreated() {
	heartCheckClose()
	createTask = setInterval(() => {
		if (createdNumber < 10) {
			createdNumber++
			createdSocket()
		} else {
			againCreatedEnd()
		}
	}, timeout)
}

// 停止创建
function againCreatedEnd() {
	clearInterval(createTask)
	createTask = null
	createdNumber = 1
}
