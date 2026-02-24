<!--
** 下级管理
-->
<template>
	<view class="container">
		<uni-nav-bar
			class="nav-bar"
			title="下级管理"
			left-icon="left"
			right-text="新增"
			:border="false"
			@clickLeft="clickLeft"
			@clickRight="clickRight">
		</uni-nav-bar>
		<view class="search-bar">
			<view class="flex-center">
				<uni-easyinput class="w50" v-model="searchQuery.userName" placeholder="请输入用户名" @change="doSearch"></uni-easyinput>
				<uni-data-select class="w50" v-model="searchQuery.parentPath" :localdata="parentPathList" @change="doSearch" placeholder="请选择层级"></uni-data-select>
			</view>
			<view class="flex-center">
				<uni-data-select class="w50" v-model="searchQuery.status" :localdata="statusOption" @change="doSearch" placeholder="请选择开工状态"></uni-data-select>
				<uni-data-select class="w50" v-model="searchQuery.orderPermission" :localdata="orderPermission" @change="doSearch" placeholder="请选择接单权限状态"></uni-data-select>
			</view>
		</view>
		
		<scroll-view scroll-y="true" class="scroll-list" @scrolltolower="scrolltolower" refresher-enabled="true" @refresherrefresh="onRefresh" :refresher-triggered="isRefreshing"
			:refresher-background="'#f3f3f3'">
			<view class="scroll-list-box">
				<noData v-if="!list.length"></noData>
				<template v-else>
					<view class="card-box list-item" v-for="(item, index) in list" :key="'_' + index">
						<view class="flex-between">
							<view><text class="text-gray">用户名：</text><text>{{item.userName}}</text></view> 
							<view>
								<text class="switch-close">关闭</text>
								<switch :checked="item.workStatus === 0 ? true : false" style="transform:scale(0.9)" @change="changeStatus(item, 'workStatus')"></switch>
								<text class="switch-open">开工</text>
							</view>
						</view>
						<view class="flex-between" style="margin-top: 6px">
							<view class="flex">
								<view>
									<text class="text-gray">余额：</text>
									<text class="text-green">{{ item.totalBalance ? (item.totalBalance - (item.baseDeposit || 0)).toFixed(2) : 0 }}</text>
								</view>
								<button
									style="margin: 0 0 0 16px; padding: 0 5px" 
									class="mini-btn btn-success" @click="handleUpdateAmount(item)">
									转移余额
								</button>
							</view>
							<view>
								<text class="switch-close">关闭</text>
								<switch :checked="item.orderPermission === 0 ? true : false" style="transform:scale(0.9)" @change="changeStatus(item, 'orderPermission')"></switch>
								<text class="switch-open">接单</text>
							</view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">层级：</text><text>{{item.merchantLevel ? Number(item.merchantLevel) - 1 : '--'}}级</text></view> 
							<view class="w50 ellipsis"><text class="text-gray">上级：</text><text>{{item.parentName}}</text></view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">押金：</text><text>{{item.baseDeposit || 0}}</text></view>
							<view class="w50 ellipsis"><text class="text-gray">最低接单金额：</text><text>{{item.minAmount || 0}}</text></view> 
						</view>
						<view><text class="text-gray">添加时间：</text>{{item.createTime}}</view>
						<view class="list-item-bottom-btn">
							<button class="mini-btn btn-success" @click="handleRate(item)">费率</button>
							<button class="mini-btn btn-primary" @click="handleUpdate(item)">编辑</button>
							<button class="mini-btn btn-danger" @click="handleDelete(item)">删除</button>
						</view>
					</view>
				</template>
			</view>
		</scroll-view>
		
		<updateSubVue ref="updateSubVue" @refresh="refreshList"></updateSubVue>
		
		<updateAmountVue ref="updateAmountRef" @refresh="refreshList" :title="'转移余额'"/>
	</view>
</template>

<script>
	import updateSubVue from './components/updateSub.vue'
	import noData from '@/pages/components/noData.vue'
	import updateAmountVue from '@/pages/components/updateAmount.vue'
	import { 
		getMerchantAll, 
		getMerchantChildAllList, 
		updateMerchantChild,
		deleteMerchantChild
	} from '@/api/merchant.js'
	
	export default {
		components: {
			updateSubVue,
			noData,
			updateAmountVue
		},
		data() {
			return {
				searchQuery: {
					userName: '',
					status: '',
					parentPath: '',
					orderPermission: '',
					pageNum: 1,
					pageSize: 15
				},
				parentPathList: [],
				statusOption: [
					{ text: '开工', value: '0'},
					{ text: '未开工', value: '1'},
				],
				orderPermission: [
					{ text: '开启',  value: 0 },
					{ text: '关闭', value: 1 }
				],
				total: 0,
				list: [],
				isRefreshing: false
			};
		},
		onShow() {
			this.doSearch()
		},
		onLoad() {
			this.getMerchantList()
		},
		methods: {
			doSearch() {
				this.list = []
				this.searchQuery.pageNum = 1
				this.total = 0
				this.getList()
			},
			refreshList() {
				this.doSearch()
				this.getMerchantList()
			},
			getMerchantList() {
				const userInfo = uni.getStorageSync('FanXing');
				getMerchantAll({
					parentId: userInfo.userId
				}).then(res => {
				 let option = res.data || []
					this.parentPathList = option.map(item => {
						return {
							text: item.userName,
							value: item.parentPath
						}
					})
				})
			},
			getList() {
				uni.showLoading()
				getMerchantChildAllList(this.searchQuery).then(res => {
					const list = res.data.records || []
					this.list = this.list.concat(list)
					this.total = res.data.total || 0
				}).finally(() => {
					uni.hideLoading()
					this.isRefreshing = false
				})
			},
			// 修改接单状态
			changeStatus(row, key) {
				uni.showLoading()
				updateMerchantChild({
					userId: row.userId,
					[key]: row[key] === 0 ? 1 : 0
				}).then(res => {
					uni.showToast({ title: '操作成功', icon: 'none' })
					this.doSearch()
				}).finally(() => {
					uni.hideLoading()
				})
			},
			handleRate(item) {
				uni.navigateTo({
					url: '/pages/subordinateManage/basicConfigRate?merchantId=' + item.userId + '&userName=' + item.userName
				})
			},
			handleUpdate(item) {
				this.$refs.updateSubVue.openPopup(item)
			},
			handleUpdateAmount(item) {
				const balance = item.totalBalance ? (item.totalBalance - (item.baseDeposit || 0)).toFixed(2) : 0
				this.$refs.updateAmountRef.openPopup(balance, 'zyye', item.userId)
			},
			// 删除
			handleDelete(row) {
				uni.showModal({
					title: '温馨提示',
					content: `是否确认删除下级【${row.userName}】`,
					success: (res) => {
						if (res.confirm) {
							deleteMerchantChild({userId: row.userId}).then(() => {
								uni.showToast({
									title: '删除成功',
									icon: 'none'
								})
								this.doSearch()
							})
						}
					}
				})
			},
			clickRight() {
				this.$refs.updateSubVue.openPopup()
			},
			clickLeft() {
				uni.navigateBack({delta: 1})
			},
			// 上拉加载更多
			scrolltolower(e) {
				if (this.searchQuery.pageNum * this.searchQuery.pageSize < this.total) {
					this.page.pageNum += 1
					this.getList()
				}
			},
			// 下拉刷新
			onRefresh() {
				if (this.isRefreshing) return
				this.isRefreshing = true
				this.doSearch()
			}
		}
	}
</script>

<style lang="scss">

</style>
