<template>
	<view class="container my-page">
		<view class="flex-between profile-div">
			<view class="flex-center">
				<image class="profile-img" src="@/static/profile.png"></image>
				<view class="user-div">
					<view class="user-name"> {{userInfo.userName}} </view>
					<view class="user-grade">
						{{Number(accountInfo.merchantLevel) - 1}}级
					</view>
				</view>
			</view>
			<view class="work-status">
				<text class="switch-close">关闭</text>
				<switch :checked="accountInfo.workStatus === 0 ? true : false" @change="changeStatus"></switch>
				<text class="switch-open">开工</text>
			</view>
		</view>
		<view class="flex-between account-info">
			<view class="account-info_item">
				<view class="text-center amount">{{accountInfo.balance}}</view>
				<view class="text-center title">余额<text class="unit">(元)</text></view>
			</view>
			<view class="account-info_item">
				<view class="text-center amount">{{(accountInfo.balance - accountInfo.baseDeposit).toFixed(2)}}</view>
				<view class="text-center title">接单余额<text class="unit">(元)</text></view>
			</view>
			<view class="account-info_item">
				<view class="text-center amount">{{accountInfo.baseDeposit}}</view>
				<view class="text-center title">押金<text class="unit">(元)</text></view>
			</view>
			<view class="account-info_item">
				<view class="text-center amount">{{accountInfo.childBalance}}</view>
				<view class="text-center title">下级余额<text class="unit">(元)</text></view>
			</view>
		</view>
		<view class="flex-between freeze-amount">
			<view class="account-info_item">
				<view class="text-center amount">{{accountInfo.totalBalance}}</view>
				<view class="text-center title">总余额<text class="unit">(元)</text></view>
			</view>
			<view class="flex account-info_item zhuan">
				<view>
					<view class="text-center amount">{{accountInfo.freezeAmount}}</view>
					<view class="text-center title">佣金<text class="unit">(元)</text></view>
				</view>
				<button class="mini-btn btn" @click="handleUpdateAmount">转余额</button>
			</view>
		</view>
		<view class="status-view">
			<view class="flex-between">
				<view class="status-view_box">
					<view class="text-center">来单提示</view>
					<view class="flex-center status-view_type">
						<text class="switch-close">关闭</text>
						<switch :checked="accountInfo.orderRemind === 0 ? true : false" @change="changeOrderRemind"></switch>
						<text class="switch-open">开启</text>
					</view>
				</view>
				<view class="status-view_box">
					<view class="text-center">自动刷新</view>
					<view class="flex-center status-view_type">
						<text class="switch-close">关闭</text>
						<switch :checked="accountInfo.autoRefresh === 0 ? true : false" @change="changeAutoRefresh"></switch>
						<text class="switch-open">开启</text>
					</view>
				</view>
			</view>
			<view class="remark-text" style="padding-top: 18px">温馨提示：来单提示处于关闭状态下，自动刷新无效</view>
		</view>
		<view class="my-list">
			<view class="flex-between my-list-item" @click="handleRouter('rate')">
				<view class="flex">
					<svg width="20" height="20" t="1733312016486" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="22427"><path d="M179.2 256a25.6 25.6 0 0 1 0-51.2h665.6a25.6 25.6 0 1 1 0 51.2v51.2H921.6V153.6H102.4v153.6h76.8V256z m51.2 0v567.1424c0 11.4688 11.008 21.6576 25.6 21.6576h512c14.592 0 25.6-10.1888 25.6-21.6576v-267.52a25.6 25.6 0 1 1 51.2 0v267.52c0 40.704-34.816 72.8576-76.8 72.8576H256c-41.984 0-76.8-32.1536-76.8-72.8576V358.4H102.4a51.2 51.2 0 0 1-51.2-51.2V153.6a51.2 51.2 0 0 1 51.2-51.2h819.2a51.2 51.2 0 0 1 51.2 51.2v153.6a51.2 51.2 0 0 1-51.2 51.2h-76.8v72.2432a25.6 25.6 0 1 1-51.2 0V256h-563.2z m399.5136 171.7248a25.6 25.6 0 1 1 36.1984 36.1984l-253.44 253.44a25.6 25.6 0 1 1-36.1472-36.2496l253.44-253.44zM614.4 742.4a76.8 76.8 0 1 1 0-153.6 76.8 76.8 0 0 1 0 153.6z m0-51.2a25.6 25.6 0 1 0 0-51.2 25.6 25.6 0 0 0 0 51.2z m-204.8-153.6a76.8 76.8 0 1 1 0-153.6 76.8 76.8 0 0 1 0 153.6z m0-51.2a25.6 25.6 0 1 0 0-51.2 25.6 25.6 0 0 0 0 51.2z" fill="#D64668" p-id="22428"></path></svg>
					通道费率
				</view>
				<uni-icons type="right" size="14" color="#5f5e5e"></uni-icons>
			</view>
			<!--<view class="flex-between my-list-item" @click="handleRouter('amount')">
				<view>资金明细</view>
				<uni-icons type="right" size="14" color="#5f5e5e"></uni-icons>
			</view>-->
			<view class="flex-between my-list-item" @click="handleRouter('report')">
				<view class="flex">
					<svg width="20" height="20" t="1733311863155" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="20548"><path d="M723.968 585.216H310.784c-17.408 0-29.184 11.776-29.184 29.184 0 17.408 11.776 29.184 29.184 29.184h413.184c17.408 0 29.184-11.776 29.184-29.184-0.512-17.92-12.288-29.184-29.184-29.184zM723.968 252.928H310.784c-17.408 0-29.184 11.776-29.184 29.184 0 17.408 11.776 29.184 29.184 29.184h413.184c17.408 0 29.184-11.776 29.184-29.184-0.512-17.92-12.288-29.184-29.184-29.184zM723.968 418.816H310.784c-17.408 0-29.184 11.776-29.184 29.184 0 17.408 11.776 29.184 29.184 29.184h413.184c17.408 0 29.184-11.776 29.184-29.184-0.512-17.408-12.288-29.184-29.184-29.184z" fill="#FFBB12" p-id="20549"></path><path d="M823.808 69.632h-609.28c-37.888 0-69.12 30.72-69.12 69.12v667.136c0 24.576 16.384 46.592 40.448 52.736 5.632 1.536 11.264 3.072 17.408 4.608 40.448 10.24 94.72 21.504 137.728 21.504 78.848 0 109.568-37.888 173.568-37.888s111.104 37.888 181.76 37.888c39.936 0 87.04-9.728 123.904-18.944 8.192-2.048 16.384-4.608 23.552-6.656 23.552-6.656 39.424-28.16 39.424-52.736V129.024c0.512-32.768-26.624-59.392-59.392-59.392z m-123.392 765.952c-82.432 0-115.2-39.936-178.688-39.936S423.424 834.56 340.992 834.56c-29.184 0-62.976-5.12-94.72-11.264-26.112-5.12-45.056-28.16-45.056-54.784v-609.28c0-21.504 17.408-38.4 38.4-38.4h551.936c21.504 0 38.4 17.408 38.4 38.4v611.328c0 26.624-18.432 49.664-44.544 54.784-27.648 5.632-57.856 10.24-84.992 10.24z" fill="#2E77ED" p-id="20550"></path></svg>
					对账报表
				</view>
				<uni-icons type="right" size="14" color="#5f5e5e"></uni-icons>
			</view>
			<view class="flex-between my-list-item" @click="handleRouter('sub')">
				<view class="flex">
					<svg width="20" height="20" t="1733312640824" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="35754"><path d="M896.064 661.376h-53.504V567.616c0-25.088-30.464-44.8-69.248-44.8H522.688V426.624H704c35.328 0 64-28.736 64-64V128.128c0-35.328-28.672-64.064-64-64.064h-384c-35.328 0-64 28.736-64 64v234.624c0 35.328 28.672 64 64 64h181.376v96.128h-250.88c-38.848 0-69.184 19.776-69.184 44.864v93.76H128c-35.328 0-64.064 28.736-64.064 64v170.688c0 35.328 28.736 64 64 64h128c35.328 0 64-28.672 64-64v-170.688c0-35.264-28.672-64-64-64h-53.44V567.616c0-11.136 20.48-23.616 48-23.616h250.88v117.376H448c-35.328 0-64 28.736-64 64v170.688c0 35.328 28.672 64 64 64h128.064c17.152 0 33.28-6.656 45.312-18.816a63.232 63.232 0 0 0 18.56-45.184v-170.688c0-35.264-28.736-64-64-64h-53.376V544h250.624c22.656 0 48 10.112 48 23.68v93.696H768c-35.328 0-64 28.736-64 64v170.688c0 35.328 28.672 64 64 64h128c35.264 0 64-28.672 64-64v-170.688a63.936 63.936 0 0 0-63.936-64zM298.688 362.624V128.064c0-11.84 9.6-21.376 21.376-21.376h384c11.84 0 21.312 9.6 21.312 21.376v234.688a21.312 21.312 0 0 1-21.312 21.248h-384a21.312 21.312 0 0 1-21.376-21.376z m-21.312 362.752v170.688a21.312 21.312 0 0 1-21.376 21.376H128A21.312 21.312 0 0 1 106.752 896v-170.688c0-11.776 9.6-21.312 21.376-21.312h128c11.776 0 21.312 9.536 21.312 21.312z m320 0v170.816a21.312 21.312 0 0 1-21.248 21.248H448A21.312 21.312 0 0 1 426.688 896v-170.688c0-11.776 9.536-21.312 21.312-21.312h128c11.776 0 21.312 9.536 21.312 21.312z m320 170.688a21.312 21.312 0 0 1-21.312 21.376h-128a21.312 21.312 0 0 1-21.312-21.376v-170.688c0-11.776 9.536-21.312 21.376-21.312h128c11.776 0 21.312 9.536 21.312 21.312v170.688z" fill="#FF6633" p-id="35755"></path></svg>
					下级管理
				</view>
				<uni-icons type="right" size="14" color="#5f5e5e"></uni-icons>
			</view>
			<view class="flex-between my-list-item" @click="handleRouter('setPwd')">
				<view class="flex">
					<svg width="21" height="21" t="1733312314285" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="31047"><path d="M725.243 673.28A210.683 210.683 0 1 0 514.56 883.656 210.53 210.53 0 0 0 725.243 673.28z" fill="#F4CA1C" p-id="31048"></path><path d="M806.241 387.098h-31.37v-82.335c0-136.99-118.364-248.443-263.854-248.443S247.168 167.772 247.168 304.763v82.335h-24.29a110.29 110.29 0 0 0-110.238 110.09V852.48a110.29 110.29 0 0 0 110.239 110.08H806.24A110.29 110.29 0 0 0 916.48 852.48V497.188a110.285 110.285 0 0 0-110.239-110.09zM314.64 304.763c0-99.84 88.1-181.069 196.378-181.069S707.4 204.923 707.4 304.763v82.335H314.64v-82.335zM849.01 852.48a42.788 42.788 0 0 1-42.768 42.711H222.88a42.788 42.788 0 0 1-42.768-42.711V497.188a42.788 42.788 0 0 1 42.768-42.711H806.24a42.788 42.788 0 0 1 42.768 42.71V852.48zM625.408 708.73a33.746 33.746 0 0 0-44.544 17.071 87.424 87.424 0 1 1-79.672-122.783 33.628 33.628 0 0 0 24.002 62.234l65.94-16a48.348 48.348 0 0 0 35.574-58.327l-16.03-65.859a33.73 33.73 0 0 0-65.536 15.913l0.307 1.255a154.527 154.527 0 1 0 97.075 210.98 33.67 33.67 0 0 0-17.116-44.483z" fill="#595BB3" p-id="31049"></path></svg>
					修改密码
				</view>
				<uni-icons type="right" size="14" color="#5f5e5e"></uni-icons>
			</view>
			<view class="flex-between my-list-item" @click="handleRouter('safe')">
				<view class="flex">
					<svg width="22" height="22" t="1733312226370" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="30028"><path d="M894.569 357.803l-46.117 46.117c13.97 38.401 21.589 79.851 21.589 123.08 0 198.823-161.177 360-360 360s-360-161.177-360-360 161.177-360 360-360c67.597 0 130.842 18.631 184.882 51.037l43.546-43.546C672.72 131.798 594.277 107 510.042 107c-231.96 0-420 188.04-420 420s188.04 420 420 420 420-188.04 420-420c0-60.197-12.664-117.435-35.473-169.197z" fill="#3659C6" p-id="30029"></path><path d="M510.042 287c-65.642 0-118.982 52.634-120.139 118v62h-29.861c-33.137 0-60 26.863-60 60v150c0 33.137 26.863 60 60 60h300c33.137 0 60-26.863 60-60V527c0-33.137-26.863-60-60-60h-29.861v-62c-1.157-65.366-54.497-118-120.139-118z m0 60.354c32.307 0 58.615 25.618 59.758 57.646v62H450.284v-62c1.143-32.028 27.451-57.646 59.758-57.646z" fill="#3659C6" p-id="30030"></path><path d="M795.361 245.092l-189.904 28.923 120-207.846zM480.042 527h60v150h-60z" fill="#F99332" p-id="30031"></path></svg>
					安全码
				</view>
				<uni-icons type="right" size="14" color="#5f5e5e"></uni-icons>
			</view>
		</view>
		<view class="logout-btn" @click="logOut">退出登录</view>
		
		<update-amount ref="updateAmountRef" @refresh="getDetail"></update-amount>
	</view>
</template>

<script>
	import { getMerchantDetail, logout } from '@/api/login.js'
	import { stopWrokByMerchant, orderRemindMerchant, orderAutoRefresh } from '@/api/merchant.js'
	import updateAmount from '@/pages/components/updateAmount.vue';
	
	export default {
		components: {
			updateAmount
		},
		data() {
			return {
				accountInfo: {
					totalBalance: 0,
					merchantLevel: 0,
					balance: 0,
					baseDeposit: 0,
					freezeAmount: 0,
					childBalance: 0,
					workStatus: 0,
					orderRemind: 0,
					autoRefresh: 0,
				}
			};
		},
		computed: {
			userInfo() {
				return uni.getStorageSync('FanXing')
			}
		},
		onShow() {
			this.getDetail()
		},
		methods: {
			getDetail() {
				uni.showLoading()
				getMerchantDetail({}).then(res => {
					const data = res.data || {}
					this.accountInfo = {
						merchantLevel: data.merchantLevel,
						balance: data.balance,
						baseDeposit: data.baseDeposit,
						freezeAmount: data.freezeAmount,
						childBalance: data.childBalance,
						workStatus: data.workStatus,
						totalBalance: (data.balance - data.baseDeposit + data.baseDeposit + data.childBalance).toFixed(2),
						orderRemind: data.orderRemind,
						autoRefresh: data.autoRefresh
					}
				}).finally(() => {
					uni.hideLoading()
				})
			},
			changeStatus(e) {
				const status = e.detail.value ? 0 : 1
				stopWrokByMerchant({
					workStatus: status
				}).then(() => {
					uni.showToast({ title: '修改成功', icon: 'none'})
				})
			},
			
			changeOrderRemind(e) {
				const status = e.detail.value ? 0 : 1
				orderRemindMerchant({
					orderRemind: status
				}).then(() => {
					uni.showToast({ title: '修改成功', icon: 'none'})
					if (!e.detail.value && this.accountInfo.autoRefresh === 0) {
						this.changeAutoRefresh(e)
						this.accountInfo.autoRefresh = 1
					}
				})
			},
			changeAutoRefresh(e) {
				const status = e.detail.value ? 0 : 1
				orderAutoRefresh({
					autoRefresh: status
				}).then(() => {
					uni.showToast({ title: '修改成功', icon: 'none'})
				})
			},
			handleUpdateAmount() {
				this.$refs.updateAmountRef.openPopup(this.accountInfo.freezeAmount)
			},
			handleRouter(type){
				if (type === 'rate') {
					uni.navigateTo({
						url: '/pages/my/channelInfo'
					})
				} else if (type === 'amount') {
					uni.navigateTo({
						url: '/pages/my/amountRecords'
					})
				} else if (type === 'setPwd'){
					uni.navigateTo({
						url: '/pages/my/updatePassword'
					})
				} else if (type === 'report') {
					uni.navigateTo({
						url: '/pages/report/report'
					})
				} else if (type === 'sub') {
					uni.navigateTo({
						url: '/pages/subordinateManage/index'
					})
				} else {
					uni.navigateTo({
						url: '/pages/my/setSafeCode?type=update'
					})
				}
			},
			// 退出
			logOut() {
				uni.showModal({
					title: '是否确认退出？',
					content: '',
					success: (res) => {
						if (res.confirm) {
							logout({}).then(res => {
								uni.removeStorageSync('SANFANG-TOKEN')
								uni.removeStorageSync('FanXing')
								const xhssf = sessionStorage.getItem('xhssf')
								uni.redirectTo({
									url: '/pages/login?xhssf=' + xhssf
								})
							})
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
.my-page {
	overflow-y: auto;
}
.profile-div {
	background-color: #fff;
	padding: 8px 16px;
	align-items: center;
	.profile-img {
		width: 55px;
		height: 55px;
	}
	.user-div {
		padding: 0 10px
	}
	.user-name {
		font-size: 16px;
		font-weight: 700;
	}
	.user-grade {
		padding: 2px;
		background: linear-gradient(90deg, #11123b, #1500d8);
		font-size: 12px;
		color: #fff;
		border-radius: 4px 0 4px 0;
		text-align: center;
		margin-top: 4px
	}
	.work-status {
		font-size: 12px;
		position: relative;
	}
}
.account-info {
	background-color: #fff;
	margin-top: 1px;
	padding: 14px 26px;
	.title {
		color: #5f5e5e;
		font-size: 12px
	}
	.amount {
		color: #0429b8;
		font-weight: 700;
	}
}

.freeze-amount {
	background: #fff;
	border-top: 1px solid #efefef;
	padding: 10px 20px;
	.amount {
		color: #0fce65;
		font-weight: 700;
	}
	.title {
		color: #5f5e5e;
		font-size: 12px
	}
	.account-info_item {
		width: 50%
	}
	.zhuan {
		align-items: flex-end;
		border-left: 1px solid #f3f3f3;
		padding-left: 10%
	}
	.btn {
		color: #fff;
		background-color: #0fce65;
		margin-left: 12px
	}
}

.my-list {
	padding: 6px 15px;
	background: #fff;
	color: #5f5e5e;
	margin-top: 6px;
	
	&-item {
		padding: 14px 0;
		border-bottom: 1px solid #e5e5e5;
		.icon {
			margin-right: 6px
		}
	}
}
.logout-btn {
	color: #999;
	display: block;
	width: 80%;
	height: 35px;
	line-height: 35px;
	font-size: 12px;
	position: relative;
	margin: 30px auto 40px auto;
	text-align: center;
	border: 1px solid #e5e5e5;
	border-radius: 4px;
} 

.status-view {
	background: #fff;
	padding: 10px 15px;
	border-top: 1px solid #efefef
}
.status-view_box {
	width: 48%
}
.status-view_type {
	padding-top: 8px;
	
}
</style>
