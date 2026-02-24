<script>
import {createdSocket} from 'js/websocket.js'

export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onLoad(option) {
			console.log(option)
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		},
		methods: {
			init() {
				const token = uni.getStorageSync('SANFANG-TOKEN')
				if(!token) {
					const xhssf = sessionStorage.getItem('xhssf')
					uni.redirectTo({
						url: '/pages/login?xhssf=' + xhssf
					})
				} else {
					createdSocket()
				}
			}
		}
	}
</script>

<style lang="scss">
	/*每个页面公共css */
	html, body {
		width: 100%;
		height: 100%;
		overflow: hidden;
		position: relative;
		color: #333;
		margin: 0;
		padding: 0;
		user-select: none;
		font-size: 14px;
		font-family: -apple-system, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto, 'PingFang SC', miui, 'Hiragino Sans GB', 'Microsoft Yahei', sans-serif;
	}
	*,
	*:before,
	*:after {
	  box-sizing: border-box;
	}
	uni-page-body {
		height: 100%;
		background: #f8f8f8;
	}
	uni-page-head {
		border-bottom: 1px solid #f1f1f1
	}
	uni-toast {
	  pointer-events: auto
	}
	uni-switch .uni-switch-wrapper .uni-switch-input {
		height: 22px;
		width: 40px;
		margin: 0;
		&:before {
			width: 38px;
			height: 20px
		}
		&:after {
			height: 20px;
			width: 20px
		}
	}
	.switch-open {
		font-size: 12px;
		padding-left: 4px;
	}
	.switch-close {
		font-size: 12px;
		padding-right: 4px;
	}

	.w100 {
		width: 100%
	}
	.w50 {
		width: 50%
	}

	.popup-body {
		max-height: 520px;
		min-width: 340px;
		background-color: #fff;
		padding: 15px;
		border-radius: 4px;
		overflow-y: auto;
		.title {
			text-align: center;
			font-size: 18px;
			padding-bottom: 15px
		}
		.popup-btn {
			justify-content: flex-end;
			.btn {
				font-size: 14px;
				margin: 0 0 0 15px
			}

		}
	}

	/* 修改整个滚动条 */
	::-webkit-scrollbar {
	  height: 3px; /* 横向滚动条的高度 */
		width: 3px;
	}

	/* 滚动条轨道 */
	::-webkit-scrollbar-track {
	  background: #fff; /* 轨道背景色 */
	  border-radius: 5px; /* 轨道圆角 */
	}

	/* 滑块样式 */
	::-webkit-scrollbar-thumb {
	  background: #bacbfa; /* 滑块颜色 */
	  border-radius: 5px;   /* 滑块圆角 */
	}

	/* 滑块悬停时 */
	::-webkit-scrollbar-thumb:hover {
	  background: #8697af; /* 滑块悬停时的颜色 */
	}

	.nav-bar {
		border-bottom: 1px solid #f1f1f1
	}
	.nav-bar .uni-nav-bar-text {
	  font-size: 16px;
	  font-weight: 700;
	}
	.nav-bar .uni-nav-bar-right-text {
	  font-size: 14px !important;
	  color: #0084ff !important
	}

	.container {
		width: 100%;
		height: 100%;
		position: relative;
		display: flex;
		flex-direction: column;
		overflow: hidden;
	}

	.font12 {
		font-size: 12px;
	}

	.flex {
		display: flex;
	}
	.flex-1 {
		flex: 1
	}
	.flex-center {
		display: flex;
		align-items: center;
		justify-content: center;
	}
	.flex-between {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.ellipsis {
	  white-space: nowrap;
	  overflow: hidden;
	  text-overflow: ellipsis;
	}
	.text-center {
		text-align: center;
	}

	.btn {
		font-size: 15px;
		line-height: 32px
	}

	.mini-btn {
		height: 24px;
		display: inline-block;
		line-height: 24px;
		font-size: 12px;

		&::after {
			display: none;
		}
	}

	.btn-primary {
		background-color: #0084ff;
		color: #fff;
	}
	.btn-danger {
		background-color: #e90409;
		color: #fff;
	}
	.btn-success {
		background-color: #0fce65;
		color: #fff;
	}

	.text-red {
		color: #ff0000;
	}
	.text-green {
		color: #04b350
	}
	.text-blue {
		color: #0030ea;
	}
	.text-warning {
		color: #ff7e10
	}
	.text-gray {
		color: #5e6065
	}

	.search-bar {
		width: 100%;
		background: #fff;
	}

	::v-deep {
		.uni-easyinput__content-input {
			flex: none;
			height: 30px
		}

	}

	.card-box {
		width: 100%;
		border-radius: 4px;
		box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
		padding: 15px;
		background-color: #fff;
	}
	/**scroll-view**/
	.scroll-list{
		width: 100%;
		flex: 1;
		padding: 10px 0;
		position: relative;
		overflow: hidden;

		.scroll-list-box {
			width: 100%;
			height: 100%;
			padding: 0 15px;
		}
	}

	.list-item {
		padding: 8px 10px;
		margin-bottom: 8px;
		> view {
			padding: 3px 0
		}
		.list-item-bottom-btn {
			display: flex;
			justify-content: flex-end;
			margin-top: 6px;
			border-top: 1px solid #dce7f8;
			padding: 6px 0 0 0;
			.mini-btn {
				margin: 0 0 0 15px
			}
		}
	}

	.scroll-list-x {
		white-space: nowrap;
		width: 100%;
		height: 42px;
		position: relative;
		box-shadow: 0 2px 3px 0 rgba(0, 0, 0, 0.2);

	}
	/***tabs*/
	.s-tabs-view {
		width: 100%;
		height: 38px;
		display: flex;
		padding: 0 6px;
		background: #fff;
		align-items: center;
		justify-content: space-between;
		.s-tabs {
			height: 100%;
			text-align: center;
		}
		.s-tabs-text {
			display: inline-block;
			padding: 0 8px;
			line-height: 35px;
			border-bottom: 3px solid #fff;
			font-size: 14px;
			border-radius: 2px;
			&.is-active {
				border-bottom: 3px solid #0030ea;
			}
		}
	}

	.remark-text {
		font-size: 12px;
		color: #555;
		margin-top: 2px
	}

</style>
