<template>
	<uni-popup ref="popup" type="center" :animation="true" :is-mask-click="false" :mask-click="false">
		<view class="popup-body">
			<view class="title">{{title}}</view>
			<uni-forms ref="form" :model="form" :rules="rules" :label-position="'left'" :label-width="96">
				<uni-forms-item label="用户名" required name="loginName" v-if="isAdd">
					<uni-easyinput trim v-model="form.loginName" placeholder="请输入用户名"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="用户名" required name="userName" v-else>
					<uni-easyinput trim disabled v-model="form.userName"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="密码" :required="isAdd" name="password" v-if="isAdd">
					<uni-easyinput type="password" trim placeholder="请输入密码" v-model="form.password"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="密码" name="loginPassword" v-else>
					<uni-easyinput type="password" trim placeholder="不修改请留空" v-model="form.loginPassword"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="确认密码" :required="isAdd" name="confirmPassword">
					<uni-easyinput type="password" trim :placeholder="isAdd ? '请确认密码' : '请确认密码，不修改请留空'" v-model="form.confirmPassword"></uni-easyinput>
				</uni-forms-item>
				<template v-if="!isAdd">
					<uni-forms-item label="安全码" name="safeCode" >
						<uni-easyinput type="password" trim placeholder="修改安全码，不修改请留空" v-model="form.safeCode"></uni-easyinput>
					</uni-forms-item>
					<uni-forms-item label="最低接单金额" name="minAmount">
						<uni-easyinput type="number" trim placeholder="请输入最低接单金额" v-model="form.minAmount"></uni-easyinput>
					</uni-forms-item>
					<uni-forms-item label="登录IP白名单" name="whiteLoginIp">
						<uni-easyinput type="textarea" trim placeholder="请输入登录IP白名单" v-model="form.whiteLoginIp"></uni-easyinput>
					</uni-forms-item>
				</template>
			</uni-forms>
			<view class="flex popup-btn">
				<button class="btn" @click="close">取消</button>
				<button class="btn btn-primary" @click="handleSubmit">确认</button>
			</view>
		</view>
	</uni-popup>
</template>

<script>
	import { addMerchantChild, updateMerchantChild } from '@/api/merchant.js'
	export default {
		data() {
			return {
				title: '添加下级',
				isAdd: true,
				form: {
					loginName: '',
				},
				rules: {
					loginName: {rules: [{ required: true, errorMessage: '请输入用户名'}]},
					password: {
						rules: [
							{ required: true, errorMessage: '请输入密码'},
							{ validateFunction: (rule,value,data,callback) => {
								if(!(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/.test(value))) {
									callback('密码必须包含大小写字母和数字，且长度不少于6位')
								}
								return true
							}}
						],
					},
					
				}
			}
		},
		methods: {
			openPopup(row) {
				this.isAdd = row ? false : true
				if (row) {
					this.title = '编辑下级'
					this.form = JSON.parse(JSON.stringify(row))
					this.form.safeCode = ''
					this.form.password = ''
					this.form.confirmPassword = ''
					this.$set(this.rules, 'confirmPassword', {rules: [{ required: false, errorMessage: '请输入确认密码'}]})
				} else {
					this.form = {
						loginName: '',
						password: '',
						confirmPassword: ''
					}
					this.$set(this.rules, 'confirmPassword', {rules: [{ required: true, errorMessage: '请输入确认密码'}]})
				}
				this.$refs.popup.open()
			},
			handleSubmit() {
				this.$refs.form.validate().then(() => {
					if (this.form.password !== this.form.confirmPassword) {
						return uni.showToast({
							title: '两次密码输入不一致',
							icon: 'none'
						})
					}
					uni.showLoading()
					if (this.isAdd) {
						addMerchantChild({
							loginName: this.form.loginName,
							password: this.form.password
						}).then(res => {
							uni.showToast({
								title: '添加成功',
								icon: 'none'
							})
							this.$emit('refresh')
						}).finally(() => {
							uni.hideLoading()
							this.close()
						})
					} else {
						// 假如修改了安全码，判断是否符合正则
						if (this.form.safeCode && !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/.test(this.form.safeCode)) {
							return uni.showToast({ 
								title: '安全码必须包含大小写字母和数字，且长度不少于6位',
								icon: 'none'
							})
						}
						const form = JSON.parse(JSON.stringify(this.form))
						if (!form.loginPassword || form.loginPassword === '') {
							delete form.loginPassword
							delete form.confirmPassword
						}
						updateMerchantChild(form).then(res => {
							uni.showToast({
								title: '编辑成功',
								icon: 'none'
							})
							this.$emit('refresh')
						}).finally(() => {
							uni.hideLoading()
							this.close()
						})
					}
				})
			},
			close() {
				this.$refs.form.clearValidate()
				this.$refs.popup.close()
			}
		},
	}
</script>

<style>
</style>