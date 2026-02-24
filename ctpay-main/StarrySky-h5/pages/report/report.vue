<!--对账报表-->
<template>
	<view class="container report">
		<view class="search-bar">
			<view class="flex-center">
				<uni-easyinput type="number" class="w50" v-model="searchQuery.merchantId" placeholder="请输入用户编号" @change="getList"></uni-easyinput>
				<uni-easyinput class="w50" v-model="searchQuery.merchantName" placeholder="请输入用户名称" @change="getList"></uni-easyinput>
			</view>
			<uni-datetime-picker placeholder="请选择时间" type="datetimerange" :clear-icon="true" v-model="dateRange" />
		</view>
		<scroll-view class="scroll-list"
			scroll-y="true" refresher-enabled="true" 
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh">
			<view class="scroll-list-box">
				<noData v-if="!list.length"></noData>
				<template v-else>
					<view class="card-box list-item" v-for="(item, index) in list" :key="'_' + index">
						<view v-if="item.userName === '合计'">
							<view class="text-blue" style="font-size: 16px; font-weight: 700; margin-bottom: 6px">合计</view>
							<view><text class="text-gray">今日上分：</text>{{item.chargeMoney}}</view>
						</view>
						<view v-else class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">用户名称：</text>{{item.userName}}<text style="font-size: 12px; color: #666">({{item.userId}})</text></view>
							<view class="w50 ellipsis"><text class="text-gray">今日上分：</text>{{item.chargeMoney}}</view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">今日余额：</text>{{item.todayBalance}}</view>
							<view class="w50 ellipsis"><text class="text-gray">昨日余额：</text>{{item.yesBalance}}</view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">交易笔数：</text>{{item.totalCount}}</view>
							<view class="w50 ellipsis"><text class="text-gray">成功笔数：</text>{{item.successCount}}</view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">总交易金额：</text>{{item.totalMoney}}</view>
							<view class="w50 ellipsis"><text class="text-gray">成功金额：</text>{{item.successMoney}}</view>
						</view>
						<view class="flex-between">
							<view class="w50 ellipsis"><text class="text-gray">佣金：</text>{{item.merchantFee || '/'}}</view>
							<view class="w50 ellipsis"><text class="text-gray">成功率：</text>{{(item.successRate * 100).toFixed(2) + '%'}}</view>
						</view>
					</view>
				</template>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { reportForMerchant } from '@/api/merchant.js'
	import noData from '@/pages/components/noData.vue'
	import moment from 'moment'
	
	export default {
		components: {
			noData
		},
		data() {
			return {
				dateRange: [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
				searchQuery: {
					merchantId: '',
					merchantName: '',
					startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
					endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
				},
				isRefreshing: false,
				list: [],
			};
		},
		watch: {
			'dateRange': {
				handler(val) {
					this.searchQuery.startTime = val.length ? val[0] : ''
					this.searchQuery.endTime =  val.length ? val[1] : ''
					this.getList()
				},
				deep: true
			}
		},
		onShow() {
			this.getList()
		},
		methods: {
			getList() {
				uni.showLoading()
				reportForMerchant(this.searchQuery).then(res => {
					let list = res.data || []
					if (list.length) {
						const newList = [list.pop()]
						this.list = newList.concat(list)
						console.log(this.list)
					}
				}).finally(() => {
					uni.hideLoading()
					this.isRefreshing = false
				})
			},
			// 下拉刷新
			onRefresh() {
				if (this.isRefreshing) return
				this.isRefreshing = true
				this.getList()
			},
		}
	}
</script>

<style lang="scss">

</style>
