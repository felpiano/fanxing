<template>
	<view class="container">
		<uni-nav-bar
			class="nav-bar"
			title="设置安全码"
			left-icon="left"
			:border="false"
			@clickLeft="clickLeft">
		</uni-nav-bar>
		<view class="set-safe">
			<view style="text-align: center;;margin-bottom: 10px">为了账户安全考虑，需要设置安全码！</view>
			<uni-forms ref="formRef" :model="form" :rules="rules" :label-position="'top'" :label-width="80">
				<uni-forms-item label="新安全码" required name="safeCode">
					<uni-easyinput trim type="password" placeholder="请输入新安全码" v-model="form.safeCode"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="谷歌验证码" required name="googleSecret">
					<uni-easyinput trim placeholder="请输入谷歌验证码" v-model="form.googleSecret"></uni-easyinput>
				</uni-forms-item>
			</uni-forms>
			<view>
				<button class="btn btn-primary" @click="handleSubmit">提交</button>
			</view>
		</view>
	</view>
</template>

<script>
	import { setSafeCode } from '@/api/merchant.js'
	export default {
		data() {
			return {
				form: {
					safeCode: '',
					googleSecret: ''
				},
				rules: {
					safeCode: {rules: [{ required: true, errorMessage: '请输入新安全码'}]},
					googleSecret: {rules: [{ required: true, errorMessage: '请输入谷歌验证码'}]},
				},
				type: ''
			};
		},
		onLoad(option) {
			if (option.type) {
				console.log(option)
				this.type = option.type
			}
		},
		methods: {
			handleSubmit() {
				this.$refs.formRef.validate().then(() => {
					uni.showLoading()
					setSafeCode(this.form).then(res => {
						uni.showToast({
							title: '修改成功',
							icon: 'none'
						})
						this.clickLeft()
					}).finally(() => {
						uni.hideLoading()
					})
				})
			},
			clickLeft() {
				if (this.type === 'update') {
					uni.navigateBack({delta: 1})
				} else {
					uni.switchTab({
						url: '/pages/order/order'
					})
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
.set-safe {
	background: #fff;
	padding: 8px 15px
}
</style>
