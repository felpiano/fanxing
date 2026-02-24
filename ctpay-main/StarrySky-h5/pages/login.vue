<template>
	<view class="container login">
		<view class="login-head">
			<view class="flex-center">
				<image class="login-logo" src="@/static/logo.png"></image>
				<view class="logo-title">繁星三方</view>
			</view>
			<view class="logo-welcome">欢迎登录</view>
		</view>
		
		<view class="login-div">
			<uni-forms ref="loginForm" class="login-form" :model="loginForm" :label-position="'top'" :rules="rules" :label-width="85">
				<uni-forms-item label="账号" name="username" class="login-input">
					<uni-easyinput type="text" trim placeholder="请输入账号" v-model="loginForm.username" :border="false"
						:primaryColor="'#043462'">
					</uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="密码" name="password" class="login-input">
					<uni-easyinput type="password" trim placeholder="请输入密码" v-model="loginForm.password"
						:primaryColor="'#043462'">
					</uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="谷歌验证码" name="code" class="login-input">
					<uni-easyinput type="text" trim placeholder="请输入谷歌验证码" v-model="loginForm.code"
						:primaryColor="'#043462'">
					</uni-easyinput>
				</uni-forms-item>
			</uni-forms>
			<button class="sub_btn" :loading="loading" @click="handleSubmit">登录</button>
		</view>
	</view>
</template>

<script>
	import { login, getInfo } from '@/api/login.js'
	export default {
		data() {
			return {
				loading: false,
				loginForm: {
					username: '',
					password: '',
					code: '',
					uuid: 'mmobile'
				},
				rules: {
					username: {
						rules: [{
							required: true,
							errorMessage: '请输入账号'
						}]
					},
					password: {
						rules: [{
							required: true,
							errorMessage: '请输入密码'
						}]
					}
				},
			};
		},
		onLoad(option) {
			if (option.xhssf) {
				// this.loginForm.uuid = option.xhssf
			}
		},
		methods: {
			handleSubmit() {
				this.$refs.loginForm.validate().then(() => {
					this.loading = true
					login({
						...this.loginForm
					}).then(res => {
						uni.setStorageSync('SANFANG-TOKEN', res.token)
						uni.showLoading()
						getInfo({}).then(res => {
							const user = res.user
							// 不是码商不让登录
							if (user.identity !== 5) {
								uni.hideLoading()
								uni.removeStorageSync('SANFANG-TOKEN')
								return uni.showToast({
									title: '请登录电脑端系统',
									icon: 'none'
								})
							}
							const accountInfo = {
								userId: user.userId,
								userName: user.userName,
								showGoogle: user.showGoogle
							}
							try {
								uni.setStorageSync('FanXing', accountInfo);
							} finally {
								uni.hideLoading()
								uni.showToast({
									title: '登录成功',
									icon: 'none'
								})
								if (!user.showGoogle) {
									uni.redirectTo({
										url: '/pages/home/middle'
									})
								} else {
									// 调用App.vue的初始化方法
									getApp().$vm.init()
									uni.switchTab({
										url: '/pages/order/order'
									})
								}
							}
						})
					}).catch(() => {
						this.loading = false
					})
				})
			}
		}
	}
</script>

<style lang="scss">
	.login {
		position: relative;
		.login-head {
			background: linear-gradient(90deg, #e0eaf4 0%, #e5ebf5 50%, #eff3f6 100%);
			width: 100%;
			height: 22%;
			padding: 10% 15px 15px;
			.login-logo {
				width: 40px;
				height: 40px;
			}
			.logo-title {
				font-size: 26px;
				line-height: 40px;
				padding-left: 8px
			}
			.logo-welcome {
				font-size: 18px;
				line-height: 40px;
				padding-top: 20px;
				text-align: center;
			}
		}
		.login-div {
			width: 100%;
			height: 78%;
			padding: 0 10%;
			background: #eff3f6
		}
		
		
		
		.login-form {
			margin-top: 20px;
		}
		
		.sub_btn {
			width: 100%;
			height: 46px;
			line-height: 46px;
			border: none;
			outline: none;
			color: #fff;
			font-size: 16px;
			border-radius: 4px;
			background: #043462;
			&:after {
				display: none;
			}
		}
	}
	
	::v-deep {
		.uni-forms-item__label {
			color: #333;
			font-weight: 700
		}
	}

</style>
