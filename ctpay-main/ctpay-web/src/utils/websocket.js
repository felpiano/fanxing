import {Notification} from 'element-ui'
import store from '@/store'
import {getToken} from '@/utils/auth'
import { EventBus } from '@/utils/event-bus.js'

let token = getToken()

// websocket.js
let socket =  null
let isConnected = false;

// url: websocket链接地址 测试地址 process.env.NODE_ENV === 'development' ? 'ws://192.168.0.102:8090/ws/orderRemind' :
const wsUrl = 'wss://admin.jszdf.xyz/ws/orderRemind'

// 心跳检测时间间隔
const heartCheckTime = 1000 * 30;
// 心跳检测定时器
let heartCheckTimer = null;

// 初始化websocket链接
export function initWebSocket() {
  // 每次都先关闭
  closeWebSocket()
  if (!token) {
    token = getToken()
    reconnect()
  }
  // 创建websocket对象
  socket = new WebSocket(wsUrl + '?Authorization=' + token);
  if (!socket)  {
    reconnect()
  }
  // 监听websocket链接
  socket.onopen = () => {
    console.log('websocket链接成功');
    isConnected = true;
    // 发送消息，启动心跳检测
    socket.send('ping');
    startHeartCheck();
  };
  // 监听websocket消息
  socket.onmessage = (event) => {
    console.log('接收到消息', event.data);
    // 如果返回订单信息，弹框提示您有新的订单，显示订单号，并播放提示音
    const data = JSON.parse(event.data);
    if (data) {
      EventBus.$emit('updateOrderList')
      // 弹框提示
      Notification({
        title: '新订单提示',
        message: `您有新的订单，订单号为${data.message}`,
        type: 'success'
      });
      // 播放提示音
      store.commit('SET_ISPLAY', true);
      // const audio = new Audio(require('@/assets/mp3/newOrder.mp3'));
      // audio.play();
    }
  };
  // 监听websocket关闭
  socket.onclose = () => {
    console.log('websocket关闭');
    isConnected = false;
    socket = null
    // 重新连接
    reconnect();
  };
  // 监听websocket错误
  socket.onerror = () => {
    console.log('websocket错误');
    isConnected = false;
    socket = null
    // 重新连接
    reconnect();
  };
  return socket;
}

// 心跳检测, 连接前先清除之前的心跳检测
function startHeartCheck() {
  clearHeartCheck();
  heartCheckTimer = setInterval(() => {
    // 判断websocket是否连接
    if (socket.readyState === 1) {
      socket.send('ping');
    }
  }, heartCheckTime);
}

// 清除心跳检测
function clearHeartCheck() {
  clearInterval(heartCheckTimer);
  heartCheckTimer = null
}

// 关闭websocket链接
export function closeWebSocket() {
  clearHeartCheck();
  if(socket) {
    socket.close();
    clearTimeout(reconnectTimer);
    isConnected = false;
    socket = null;
  }
}

// 如果没有连接上，每隔5秒重新连接，重试5次后放弃
let reconnectTimer = null;
let reconnectCount = 0;
function reconnect() {
  if (reconnectCount >= 10) {
    return;
  }
  reconnectCount++;
  reconnectTimer = setTimeout(() => {
    initWebSocket();
  }, 5000);
}
