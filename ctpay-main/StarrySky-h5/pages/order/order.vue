<template>
	<view class="container order-list">
		<view class="search-bar">
			<view class="flex-center">
				<uni-data-select class="w50" v-model="searchQuery.channelId" :localdata="channelList" @change="doSearch" placeholder="请选择通道"></uni-data-select>
				<uni-data-select class="w50" v-model="searchQuery.orderStatus" :localdata="orderStatusOption" @change="doSearch" placeholder="请选择订单状态"></uni-data-select>
			</view>
			<uni-easyinput v-model="orderNumber" placeholder="请输入平台单号/商户单号"></uni-easyinput>
			<uni-datetime-picker placeholder="请选择时间" type="datetimerange" :clear-icon="true" v-model="dateRange" />
		</view>
		<view class="total-view">
			当前订单总金额：￥<b class="text-red">{{ overviewData.totalAmount || 0}} </b> 元；
			成交总金额：￥<b class="text-green">{{ overviewData.successAmount || 0}} </b> 元；
			当前订单总数：<b class="text-red">{{ overviewData.totalCount || 0}} </b> 单；
			成交订单总数：<b class="text-green">{{ overviewData.successCount || 0 }} </b> 单；
			成功率：<b class="text-blue">{{ overviewData.successRate }} </b> %
		</view>
		<scroll-view class="scroll-list" :lower-threshold="80"
			scroll-y="true" refresher-enabled="true" 
			:refresher-triggered="isRefreshing"
			@scrolltolower="scrolltolower" 
			@refresherrefresh="onRefresh">
			<view class="scroll-list-box">
				<noData v-if="!list.length"></noData>
				<template v-else>
					<view class="card-box list-item" v-for="(item, index) in list" :key="'_' + index">
						<view class="flex-between">
							<view><text class="text-gray">通道名称：</text>{{item.channelName}}</view>
							<view v-if="item.orderStatus === 0" class="order-status text-warning">待支付</view>
							<view v-if="item.orderStatus === 1" class="order-status text-green">支付成功</view>
							<view v-if="item.orderStatus === 2" class="order-status text-red">订单超时</view>
							<view v-if="item.orderStatus === 3" class="order-status text-red">已关闭</view>
							<view v-if="item.orderStatus === 4" class="order-status text-red">待退回</view>
							<view v-if="item.orderStatus === 5" class="order-status text-red">已退回</view>
						</view>
						<view class="flex-between">
							<view><text class="text-gray">平台单号： </text>{{item.tradeNo}}</view>
							<view @click="handleCopy(item.tradeNo, '平台单号')" class="text-blue">复制</view>
						</view>
						<view class="flex-between">
							<view><text class="text-gray">商户单号： </text>{{item.shopOrderNo}}</view>
							<view @click="handleCopy(item.shopOrderNo, '商户单号')" class="text-blue">复制</view>
						</view>
						<view class="flex-between">
							<view class="w50"><text class="text-gray">订单金额：</text><b class="text-blue">{{item.orderAmount}}</b></view>
							<view class="w50"><text class="text-gray">实际支付：</text><b class="text-blue">{{item.fixedAmount}}</b></view>
						</view>
						<view class="flex-between">
							<view><text class="text-gray">收款名称：</text>{{item.nickName}}</view>
							<view><text class="text-gray">账号备注：</text>{{item.accountRemark || '--'}}</view>
						</view>
						<view><text class="text-gray">收款账号：</text>{{item.accountNumber || '--'}}</view>
						<view class="flex-between">
							<view><text class="text-gray">付款人：</text>{{item.payer || '--'}}</view>
							<view><text class="text-gray">口令：</text><text class="text-warning">{{item.unionCode}}</text></view>
						</view>
						<view class="flex-between">
							<view><text class="text-gray">创建时间：</text>{{item.orderTime}}</view>
						</view>
						<view><text class="text-gray">更新时间：</text>{{item.finishTime}}</view>
						<view><text class="text-gray">订单备注：</text>{{item.orderRemark}}</view>
		
						<view class="list-item-bottom-btn" style="justify-content: center">
							<button style="width: 45%" v-if="item.orderStatus !== 1" class="mini-btn btn-primary" @click="handleRepair(item)">确认</button>
							<!--<button style="width: 45%; margin-left: 8%" v-if="item.orderStatus === 0" class="mini-btn btn-danger" @click="handleUnPaid(item)">未收到</button>-->
						</view>
					</view>
				</template>
			</view>
		</scroll-view>
		
		<safeCode ref="safeCodeRef" @refresher="doSearch"></safeCode>
		
	</view>
</template>

<script>
	import noData from '@/pages/components/noData.vue'
	import safeCode from '@/pages/components/safeCode.vue'
	import moment from 'moment'
	import { getChannelListAll } from '@/api/channel.js'
	import { getInOrderList, getInOrderTotal } from '@/api/merchant.js'
	import { getMerchantDetail } from '@/api/login.js'
		
	export default {
		components: {
			noData,
			safeCode
		},
		data() {
			return {
				dateRange: [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
				searchQuery: {
					channelId: '',
					shopOrderNo: '',
					tradeNo: '',
					orderStatus: '',
					startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
					endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
					pageNum: 1,
					pageSize: 15
				},
				orderNumber: '',
				total: 0,
				isRefreshing: false,
				list: [],
				channelList: [],
				orderStatusOption: [
					{ text: '待支付', value: '0'},
					{ text: '支付成功', value: '1'},
					{ text: '订单超时', value: '2'},
					{ text: '已关闭', value: '3'},
					{ text: '待退回', value: '4'},
					{ text: '已退回', value: '5'}
				],
				overviewData: {
					totalAmount: 0,
					successAmount: 0,
					totalNum: 0,
					successNum: 0,
					successRate: 0
				},
				innerAudioContext: null,
				accountInfo: {
					orderRemind: 0,
					autoRefresh: 0
				}
			};
		},
		watch: {
			'orderNumber': {
				handler(val) {
					this.searchQuery.tradeNo = ''
					this.searchQuery.shopOrderNo = ''
					if (val) {
						if(val.indexOf('HB') > -1 && val.indexOf('HB') === 0) {
							this.searchQuery.tradeNo = val
						} else {
							this.searchQuery.shopOrderNo = val
						}
					}
					this.doSearch()
				}
			},
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
			const token = uni.getStorageSync('SANFANG-TOKEN')
			if(!token) {
				const xhssf = sessionStorage.getItem('xhssf')
				uni.redirectTo({
					url: '/pages/login?xhssf=' + xhssf
				})
			} else {
				const userInfo = uni.getStorageSync('FanXing')
				if (!userInfo.showGoogle) {
					uni.redirectTo({
						url: '/pages/home/middle'
					})
				} else {
					this.doSearch()
					this.getDetail()
				}
			}
		},
		onLoad() {
			
		},
		mounted() {
			const token = uni.getStorageSync('SANFANG-TOKEN')
			if (token) {
				this.getChannelList()
				getApp().$vm.init()
				this.innerAudioContext = uni.createInnerAudioContext();
				this.innerAudioContext.src = '/static/mp3/newOrder.mp3';
				this.innerAudioContext.autoplay = false; // 不自动播放
				uni.$on('updateOrderList', this.onUpdateOrder)
			}
		},
		beforeDestroy() {
			// 页面关闭时清除监听
			uni.$off('updateOrderList', this.onUpdateOrder)
		},
		methods: {
			onUpdateOrder() {
				this.playMusic()
				if (this.accountInfo.autoRefresh === 0) {
					this.doSearch()
				}
			},
			getDetail() {
				getMerchantDetail({}).then(res => {
					const data = res.data || {}
					this.accountInfo = {
						orderRemind: data.orderRemind,
						autoRefresh: data.autoRefresh
					}
				})
			},
			doSearch() {
				this.list = []
				this.searchQuery.pageNum = 1
				this.total = 0
				this.init()
			},
			init() {
				this.getList()
				this.getOrderTotal()
			},
			getChannelList() {
				getChannelListAll({}).then(res => {
					const channelList = res.data || []
					this.channelList = channelList.map(item => {
						return {
							text: item.channelName,
							value: item.id
						}
					})
				})
			},
			getList() {
				uni.showLoading()
				getInOrderList({
					...this.searchQuery
					}).then(res => {
					const list = res.data.records || []
					if (this.searchQuery.pageNum = 1) {
						this.list = list
					} else {
						this.list = this.list.concat(list)
					}
					this.total = res.data.total || 0
				}).finally(() => {
					uni.hideLoading()
					this.isRefreshing = false
				})
			},
			// 统计
			getOrderTotal() {
				getInOrderTotal({...this.searchQuery}).then(res => {
					this.overviewData = res.data || {}
					this.overviewData.successRate = this.overviewData.successRate ? (this.overviewData.successRate * 100).toFixed(2) : 0
				})
			},
			handleRepair(item) {
				this.$refs.safeCodeRef.openPopup(item)
			},
			handleUnPaid(item) {
				this.$refs.safeCodeRef.openPopup(item, 'Unpaid')
			},
			playMusic() {
				// 模拟 preload：手动触发加载
				// this.innerAudioContext.onCanplay(() => {});
				this.innerAudioContext.play()
				setTimeout(() => {
					this.innerAudioContext.pause();
				}, 5600);
				
				this.innerAudioContext.onError((err) => {
				  console.error('音频加载失败', err);
				});
			},
			// 复制
			handleCopy(val, msg) {
				if (!val) return
				uni.setClipboardData({
					data: val,
					success: () => {
						uni.showToast({
							title: msg + '复制成功',
							icon: 'none'
						})
					}
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
		}
	}
</script>

<style lang="scss" scoped>
.total-view {
	background-color: #dce7f8;
	padding: 5px 15px;
	line-height: 24px
}
.order-status {
	border: 1px solid #fff;
	border-radius: 4px;
	padding: 2px 3px;
	font-size: 12px;
	&.text-warning {
		border-color: #ff7e10
	}
	&.text-red {
		border-color: #ff0000
	}
	&.text-green {
		border-color: #04b350
	}
}

.order-btn {
	display: flex;
	justify-content: flex-end;
	margin-top: 6px;
	border-top: 1px solid #dce7f8;
	padding-top: 6px !important;
	.btn-primary {
		width: 45px;
		margin: 0
	}
}

.play-btn {
	position: fixed;
	bottom: -800px;
	opacity: 0;
}
</style>
