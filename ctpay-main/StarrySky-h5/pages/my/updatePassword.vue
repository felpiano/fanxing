<!--
** 修改密码
-->
<template>
	<view class="container set-safe">
		<uni-forms ref="formRef" :model="form" :rules="rules" :label-position="'top'" :label-width="80">
			<uni-forms-item label="旧登录密码" required name="oldPassword">
				<uni-easyinput trim type="password" placeholder="请输入旧登录密码" v-model="form.oldPassword"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="新密码" required name="newPassword">
				<uni-easyinput trim type="password" placeholder="请输入新密码" v-model="form.newPassword"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="确认密码" required name="confirmPassword">
				<uni-easyinput trim type="password" placeholder="请确认密码" v-model="form.confirmPassword"></uni-easyinput>
			</uni-forms-item>
		</uni-forms>
		<view>
			<button class="btn btn-primary" @click="handleSubmit">提交</button>
		</view>
	</view>
</template>

<script>
	import { updateUserPwd } from '@/api/login.js'
	export default {
		data() {
			return {
				form: {
					userId: '',
					userName: '',
					oldPassword: '',
					newPassword: '',
					confirmPassword: ''
				},
				rules: {
					oldPassword: {rules: [{ required: true, errorMessage: '请输入旧登录密码'}]},
					newPassword: {rules: [{ required: true, errorMessage: '请输入新密码'}]},
					confirmPassword: {rules: [{ required: true, errorMessage: '请确认密码'}]}
				},
				
			};
		},
		methods: {
			handleSubmit() {
				if (this.form.newPassword !== this.form.confirmPassword) {
					return uni.showToast({
						title: '两次密码不一致',
						icon: 'none'
					})
				}
				this.$refs.formRef.validate().then(() => {
					uni.showLoading()
					updateUserPwd({
						oldPassword: this.form.oldPassword,
						newPassword: this.form.newPassword
					}).then(res => {
						uni.showToast({
							title: '修改成功',
							icon: 'none'
						})
						uni.navigateBack({delta: 1})
					}).finally(() => {
						uni.hideLoading()
					})
				})
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
