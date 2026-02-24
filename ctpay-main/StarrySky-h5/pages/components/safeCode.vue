<!--
** 安全码验证
-->
<template>
	<uni-popup ref="popup" type="center" :animation="true" :is-mask-click="false" :mask-click="false">
		<view class="safe-code">
			<view class="title">敏感操作，请确认信息后验证安全码</view>
			<view class="mb-10"><text class="text-gray">订单号：</text>{{orderInfo.shopOrderNo}}</view>
			<view class="mb-10"><text class="text-gray">金额：</text>{{orderInfo.fixedAmount}}元</view>
			<view class="mb-10"><text class="text-gray">收款名称：</text>{{orderInfo.nickName}}</view>
			<view class="mb-10"><text class="text-gray">收款账号：</text>{{orderInfo.accountNumber}}</view>
			<view class="mb-10"><text class="text-gray">付款人：</text>{{orderInfo.payer}}</view>
			<view class="mb-10"><text class="text-gray">订单时间：</text>{{orderInfo.orderTime}}</view>
			<uni-forms ref="form" :model="form" :rules="rules">
				<uni-forms-item label="" name="safeCode">
					<uni-easyinput trim type="password" placeholder="请输入安全码" v-model="form.safeCode">
					</uni-easyinput>
				</uni-forms-item>
			</uni-forms>
			<view class="popup-btn">
				<button class="btn" @click="close">取消</button>
				<button class="btn btn-primary" @click="handleSubmit">确认</button>
			</view>
		</view>
	</uni-popup>
</template>

<script>
	import { repairInOrder, orderUnPaid } from '@/api/merchant.js'
	
	export default {
		name: 'safeCode',
		data() {
			return {
				form: {
					id: '',
					safeCode: '',
					type: 0
				},
				rules: {
					safeCode: {rules: [{ required: true, errorMessage: '请输入安全码'}]}
				},
				type: 'Repair',
				orderInfo: {
					shopOrderNo: '',
					fixedAmount: '',
					accountNumber: ''
				}
			}
		},
		mounted() {
			 this.token = uni.getStorageSync('TRADE-TOKEN');
		},
		methods: {
			openPopup(item, type) {
				this.orderInfo = item
				this.form = {
					id: item.id,
					safeCode: '',
					type: 0
				}
				if (type) {
					this.type = type
				}
				this.$refs.popup.open()
			},
			handleSubmit() {
				this.$refs.form.validate().then(() => {
					uni.showLoading()
					if (this.type && this.type === 'Unpaid') {
						orderUnPaid({
							id: this.form.id,
							safeCode: this.form.safeCode
						}).then(res => {
							uni.showToast({
								title: '操作成功',
								icon: 'none'
							})
							this.$emit('refresher')
						}).finally(() => {
							this.close()
							uni.hideLoading()
						})
					} else {
						repairInOrder(this.form).then(res => {
							uni.showToast({
								title: '操作成功',
								icon: 'none'
							})
							this.$emit('refresher')
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

<style lang="scss" scoped>
	.safe-code {
		background: #fff;
		width: 288px;
		padding: 15px 20px;
		border-radius: 4px;
		.title {
			text-align: center;
			padding-bottom: 15px;
		}
		
		.popup-btn {
			display: flex;
			justify-content: flex-end;
			.btn {
				margin: 0 0 0 16px;
				font-size: 14px;
			}
		}
		.mb-10 {
			margin-bottom: 10px
		}
	}
</style>