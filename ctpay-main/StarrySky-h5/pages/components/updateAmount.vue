<!--
** 资金变动
-->
<template>
	<uni-popup ref="popup" type="center" :animation="true" :is-mask-click="false" :mask-click="false">
		<view class="popup-body">
			<view class="title">{{title}}</view>
			<uni-forms ref="form" :model="form" :rules="rules" :label-position="'left'" :label-width="88">
				<uni-forms-item label="现有金额" required name="balance">
					<uni-easyinput trim disabled v-model="form.balance"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="操作金额" required name="changeAmount">
					<uni-easyinput type="number" trim placeholder="请输入操作金额" v-model="form.changeAmount"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="谷歌验证码" required name="code">
					<uni-easyinput trim placeholder="请输入谷歌验证码" v-model="form.code"></uni-easyinput>
				</uni-forms-item>
				<uni-forms-item label="备注" name="remark">
					<uni-easyinput trim placeholder="请输入备注" v-model="form.remark"></uni-easyinput>
				</uni-forms-item>
			</uni-forms>
			<view class="flex popup-btn">
				<button class="btn" @click="close">取消</button>
				<button class="btn btn-primary" @click="handleSubmit">确认</button>
			</view>
		</view>
	</uni-popup>
</template>

<script>
	import { trimFreezeToBalance, trimBalanceMerchantChild } from '@/api/merchant.js'
	export default {
		name: 'updateAmount',
		props: {
			title: {
				type: String,
				default: '佣金转余额'
			}
		},
		data() {
			return {
				form: {
					userId: '',
					balance: 0,
					changeAmount: 0,
					remark: '',
					code: ''
				},
				rules: {
					code: {rules: [{ required: true, errorMessage: '请输入谷歌验证码'}]},
					changeAmount: {rules: [{ required: true, errorMessage: '请输入操作金额'}]},
				},
				type: null
			}
		},
		methods: {
			openPopup(balance, type, userId) {
				const userInfo = uni.getStorageSync('FanXing')
				this.type = type
				if (type === 'zyye') {
					this.form = {
						userId: userId,
						changeAmount: 0,
						balance: balance,
						remark: '',
						code: ''
					}
				} else {
					this.form = {
						userId: userInfo.userId,
						changeAmount: 0,
						balance: balance,
						remark: '',
						code: ''
					}
				}
				this.$refs.popup.open()
			},
			handleSubmit() {
				this.$refs.form.validate().then(() => {
					// if(this.form.changeAmount === 0 || this.form.changeAmount < 0) {
					// 	return uni.showToast({
					// 		title: '金额不能小于0',
					// 		icon: 'none'
					// 	})
					// }
					if (!this.type) {
						if (this.form.changeAmount > this.form.balance) {
							return uni.showToast({
								title: '操作金额不能大于现有金额',
								icon: 'none'
							})
						}
					}
					uni.showLoading()
					if (!this.type) {
						const query = {
							userId: this.form.userId,
							changeAmount: this.form.changeAmount,
							remark: this.form.remark,
							code: this.form.code
						}
						trimFreezeToBalance(query).then(res => {
							uni.showToast({
								title: '操作成功',
								icon: 'none'
							})
							this.$emit('refresh')
						}).finally(() => {
							this.close()
							uni.hideLoading()
						})
					} else if (this.type === 'zyye') {
						const query = {
							childId: this.form.userId,
							amount: this.form.changeAmount,
							remark: this.form.remark,
							code: this.form.code
						}
						trimBalanceMerchantChild(query).then(res => {
							uni.showToast({
								title: '操作成功',
								icon: 'none'
							})
							this.$emit('refresh')
						}).finally(() => {
							this.close()
							uni.hideLoading()
						})
					}
				})
			},
			close() {
				this.$refs.popup.close()
			}
		}
	}
</script>

<style>
</style>