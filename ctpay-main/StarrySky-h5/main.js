import App from './App'
import config from './config/index'

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false

Vue.prototype.$config = config

const href = window.location.href
if (href.indexOf('?') > -1) {
	const params = href.split('?')
	if(params.length > 1) {
		const options = params[1]
		const arr = options.split('=')
		if (arr[0] === 'xhssf') {
			sessionStorage.setItem('xhssf', arr[1])
		}
	}
}

App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif