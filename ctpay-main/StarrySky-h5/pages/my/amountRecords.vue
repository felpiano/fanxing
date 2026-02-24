<!--资金明细-->
<template>
	<view class="container amount-records">
		<view class="search-bar">
			<view class="flex-center">
				<uni-data-select style="width: 50%" v-model="searchQuery.changeType" :localdata="changeTypeOption" @change="doSearch" placeholder="请选择账变类型"></uni-data-select>
				<uni-data-select style="width: 50%" v-model="searchQuery.amountType" :localdata="amountTypeOption" @change="doSearch" placeholder="请选择金额类型"></uni-data-select>
			</view>
			<uni-datetime-picker placeholder="请选择时间" type="datetimerange" :clear-icon="true" v-model="dateRange" />
		</view>
		<scroll-view scroll-y="true" class="scroll-list" 
			@scrolltolower="scrolltolower" 
			refresher-enabled="true"
			@refresherrefresh="onRefresh" 
			:refresher-triggered="isRefreshing">
			<view class="scroll-list-box">
				<noData v-if="!list.length"></noData>
				<template v-else>
					<view class="card-box list-item" v-for="(item, index) in list" :key="'_' + index">
						<view class="flex-between">
							<view>订单号： {{item.orderNo || '--'}}</view>
							<view class="text-blue">{{getAmountType(item.amountType)}}-{{getChangeType(item.changeType)}}</view>
						</view>
						<view>变更前金额：{{item.beforeAmount}}</view>
						<view>变更后金额：{{item.afterAmount}}</view>
						<view>变更金额：{{item.changeAmount}}</view>
						<view>变更时间：{{item.createTime}}</view>
						<view>备注：{{item.remarks ? item.remarks : item.notes}}</view>
					</view>
				</template>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import { getAmountRecords } from '@/api/merchant.js'
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
					amountType: '',
					changeType: '',
					startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
					endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
					pageNum: 1,
					pageSize: 15
				},
				changeTypeOption: [
					{ text: '充值', value: '1'},
					{ text: '上分', value: '2'},
					{ text: '转移', value: '3'},
				],
				amountTypeOption: [
					{ text: '余额', value: '1'},
					{ text: '冻结金额', value: '2'},
				],
				total: 0,
				isRefreshing: false,
				list: []
			};
		},
		watch: {
			'dateRange': {
				handler(val) {
					this.searchQuery.startTime = val.length ? val[0] : ''
					this.searchQuery.endTime =  val.length ? val[1] : ''
					this.doSearch()
				},
				deep: true
			}
		},
		onShow() {
			this.doSearch()
		},
		methods: {
			doSearch() {
				this.list = []
				this.searchQuery.pageNum = 1
				this.total = 0
				this.getList()
			},
			getList() {
				uni.showLoading()
				getAmountRecords({
					...this.searchQuery
				}).then(res => {
					const list = res.data.records || []
					this.list = this.list.concat(list)
					this.total = res.data.total || 0
				}).finally(() => {
					uni.hideLoading()
					this.isRefreshing = false
				})
			},
			// 上拉加载更多
			scrolltolower(e) {
				if (this.searchQuery.pageNum * this.searchQuery.pageSize < this.total) {
					this.searchQuery.pageNum += 1
					this.getList()
				}
			},
			// 下拉刷新
			onRefresh() {
				if (this.isRefreshing) return
				this.isRefreshing = true
				this.doSearch()
			},
			getAmountType(type) {
				const amountTypeMap = {
					1: '余额',
					2: '冻结金额',
					3: '押金'
				}
				return amountTypeMap[type]
			},
			getChangeType(type) {
				const changeTypeMap = {
					1: '充值',
					2: '上分',
					3: '转移'
				}
				return changeTypeMap[type]
			}
		}
	}
</script>

<style lang="scss" scoped>
.amount-records {
	
}
</style>
