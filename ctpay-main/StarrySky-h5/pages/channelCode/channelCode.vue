<template>
	<view class="container">
		<uni-nav-bar 
			class="nav-bar"
			title="通道码管理"
			right-text="新增"
			:border="false"
			@clickRight="clickRight">
		</uni-nav-bar>
		<scroll-view class="scroll-list-x" scroll-x="true" scroll-with-animation="true">
			<view class="s-tabs-view">
				<view class="s-tabs" v-for="(item, index) in tabList" :key="'_' + item.id">
					<text @click="selectTabs(item.id)"
						:class="['s-tabs-text',{'is-active': searchQuery.channelId === item.id}]">
						{{item.channelName}}
					</text>
				</view>
			</view>
		</scroll-view>
		
		<scroll-view scroll-y="true" class="scroll-list" @scrolltolower="scrolltolower" refresher-enabled="true" @refresherrefresh="onRefresh" :refresher-triggered="isRefreshing"
			:refresher-background="'#f3f3f3'">
			<view class="scroll-list-box">
				<noData v-if="!list.length"></noData>
				<template v-else>
					<view class="card-box list-item" v-for="(item, index) in list" :key="'_' + index">
						<template v-if="item.channelId === 1108">
							<view class="flex-between">
								<view><text class="text-gray">账号备注：</text>{{item.accountRemark}}</view>
								<view>
									<text class="switch-close">关闭</text>
									<switch :checked="item.status === 0 ? true : false" style="transform:scale(0.9)" @change="handleStatusChange(item)"></switch>
									<text class="switch-open">进单</text>
								</view>
							</view>
						</template>
						<template v-else>
							<view class="flex-between">
								<view v-if="item.channelId === 1107"><text class="text-gray">钱包编号：</text>{{item.accountNumber}}</view>
								<view v-else><text class="text-gray">账号：</text>{{item.accountNumber}}</view>
								<view>
									<text class="switch-close">关闭</text>
									<switch :checked="item.status === 0 ? true : false" style="transform:scale(0.9)" @change="handleStatusChange(item)"></switch>
									<text class="switch-open">进单</text>
								</view>
							</view>
							<view v-if="backIdList.includes(item.channelId)">
								<view class="ellipsis"><text class="text-gray">银行卡姓名：</text>{{item.nickName}}</view>
								<view class="ellipsis" style="padding: 3px 0"><text class="text-gray">所属银行：</text>{{item.uid}}</view>
								<view class="ellipsis"><text class="text-gray">银行支行：</text>{{item.accountRemark}}</view>
							</view>
							<view v-if="item.channelId === 1122">
								<view class="ellipsis"><text class="text-gray">姓名：</text>{{item.nickName}}</view>
								<view class="ellipsis"  style="padding: 4px 0 0 0"><text class="text-gray">支付链接：</text>{{item.accountRemark}}</view>
							</view>
							<view class="flex-between" v-else>
								<view class="w50 ellipsis"><text class="text-gray">姓名：</text>{{item.nickName}}</view>
								<view class="w50 ellipsis"><text class="text-gray">账号备注：</text>{{item.accountRemark}}</view>
							</view>
						</template>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">成功总数(今)：</text><text>{{item.successCount || 0}}</text></view> 
							<view class="w50 ellipsis"><text class="text-gray">成功金额(今)：</text><text>{{item.successAmount || 0}}</text></view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">成功总数(昨)：</text><text>{{item.yesSuccessCount || 0}}</text></view> 
							<view class="w50 ellipsis"><text class="text-gray">成功金额(昨)：</text><text>{{item.yesSuccessAmount || 0}}</text></view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">最大金额：</text>{{item.maxAmount}}</view>
							<view class="w50 ellipsis"><text class="text-gray">每日限额：</text>{{item.dayLimit}}</view>
						</view>
						<view><text class="text-gray">添加时间：</text>{{item.createTime}}</view>
						<view class="list-item-bottom-btn">
							<button class="mini-btn btn-primary" @click="handleUpdate(item)">编辑</button>
							<button class="mini-btn btn-danger" @click="handleDelete(item)">删除</button>
						</view>
					</view>
				</template>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { getQrcodeList, updateMerchantQrcodeStatus, deleteMerchantQrcode } from '@/api/merchant.js'
	import { getChannelListAll } from '@/api/channel.js'
	import { getMerchantDetail } from '@/api/login.js'
	import noData from '@/pages/components/noData.vue'
	
	export default {
		components: {
			noData
		},
		data() {
			return {
				tabList: [],
				searchQuery: {
					channelId: ''
				},
				page: {
					total: 0,
					pageNum: 1,
					pageSize: 15
				},
				list: [],
				isRefreshing: false,
				agentId: '',
				backIdList: [1102, 1113, 1114, 1115, 1116, 1118]
			};
		},
		onShow() {
			this.init()
		},
		onLoad() {
			this.getChannelList()
		},
		methods: {
			init() {
				this.list = []
				this.page.pageNum = 1
				this.page.total = 0
				this.getList()
				this.geDetail()
			},
			getChannelList() {
				getChannelListAll({}).then(res => {
					this.tabList = res.data || []
					this.searchQuery.channelId = this.tabList[1].id
				})
			},
			geDetail() {
				getMerchantDetail({}).then(res => {
					this.agentId = res.data.agentId
				})
			},
			getList() {
				uni.showLoading()
				getQrcodeList({
					pageSize: this.page.pageSize,
					pageNum: this.page.pageNum,
					...this.searchQuery
				}).then(res => {
					const list = res.data.records || []
					this.list = this.list.concat(list)
					this.page.total = res.data.total || 0
				}).finally(() => {
					uni.hideLoading()
					this.isRefreshing = false
				})
			},
			 // 状态
			handleStatusChange(item) {
				updateMerchantQrcodeStatus({
					id: item.id,
					status: item.status === 0 ? 1 : 0
				}).then(() => {
					uni.showToast({
						title: '更改成功',
						icon: 'none'
					})
					this.init()
				})
			},
			clickRight() {
				let channelName = ''
				this.tabList.forEach(item => {
					if (this.searchQuery.channelId === item.id) {
						channelName = item.channelName
					}
				})
				const row = {
					isAdd: '0',
					title: channelName,
					channelId: this.searchQuery.channelId,
					agentId: this.agentId
				}
				uni.setStorageSync('tempData', row );
				uni.navigateTo({
					url: '/pages/channelCode/updateCode'
				})
			},
			// 编辑
			handleUpdate(row) {
				const data = {
					...row,
					isAdd: '1',
					title: row.channelName,
					agentId: this.agentId
				}
				uni.setStorageSync('tempData', data )
				uni.navigateTo({
					url: '/pages/channelCode/updateCode'
				})
			},
			// 删除
			handleDelete(row) {
				uni.showModal({
					title: '温馨提示',
					content: `是否确认删除账号【${row.accountNumber}】的通道码?`,
					success: (res) => {
						if (res.confirm) {
							deleteMerchantQrcode({id: row.id}).then(() => {
								uni.showToast({
									title: '删除成功',
									icon: 'none'
								})
								this.init()
							})
						}
					}
				})
			},
			selectTabs(channelId) {
				if (this.searchQuery.channelId === channelId) return
				this.searchQuery.channelId = channelId
				this.init()
			},
			// 上拉加载更多
			scrolltolower(e) {
				if (this.page.pageNum * this.page.pageSize < this.page.total) {
					this.page.pageNum += 1
					this.getList()
				}
			},
			// 下拉刷新
			onRefresh() {
				if (this.isRefreshing) return
				this.isRefreshing = true
				this.init()
			}
		}
	}
</script>

<style lang="scss" scoped>

</style>
