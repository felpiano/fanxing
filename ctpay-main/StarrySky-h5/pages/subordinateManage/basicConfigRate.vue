<template>
	<view class="container rate-config">
		<view class="title">设置下级【{{userName}}】的费率</view>
		<view class="list-ul">
			<view class="card-box list-item" v-for="(item, index) in list" :key="'_' + index">
				<view class="flex-between">
					<view><text class="text-gray">渠道名称：</text><text>{{item.channelName}}</text></view> 
					<view><text class="text-gray">编号：</text><text>{{item.channelId}}</text></view>
				</view>
				<view class="flex" style="align-items: center;">
					<text class="text-gray">费率：</text>
					<uni-easyinput trim v-model="item.channelRate" :clearable="false"></uni-easyinput>%
				</view>
				<view class="flex-between">
					<view class="w50 ellipsis"><text class="text-gray">订单超时时长(分钟)：</text><text>{{item.merchantOvertime}}</text></view> 
					<view class="w50 ellipsis"><text class="text-gray">押金(元)：</text><text>{{item.baseDeposit}}</text></view>
				</view>
				<view class="flex-between">
					<view class="w50 ellipsis"><text class="text-gray">最小接单金额：</text><text>{{item.minAmount}}</text></view> 
					<view class="w50 ellipsis"><text class="text-gray">最大接单金额：</text><text>{{item.maxAmount}}</text></view>
				</view>
				<view class="list-item-bottom-btn">
					<button style="width: 50%; margin: auto" class="mini-btn btn-primary" @click="handleSaveRate(item)">保存</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { getChannelList, channelUpdate} from '@/api/merchant.js'
	
	export default {
		data() {
			return {
				userName: '',
				list: []
			};
		},
		onLoad(option){
			if (option.merchantId) {
				this.userName = option.userName
				this.list = []
				this.getList(option.merchantId)
			}
		},
		methods: {
			getList(userId) {
				uni.showLoading()
				getChannelList({ merchantId: userId }).then(res => {
					this.list = res.data || []
				}).finally(() => {
					uni.hideLoading()
				})
			},
			// 保存当前费率
			handleSaveRate(row) {
				if (!(/^\d+(\.\d+)?$/.test(row.channelRate))) {
					return uni.showToast({ title: '请检查费率格式', icon: 'none'})
				}
				uni.showLoading()
				channelUpdate(row).then(() => {
					uni.showToast({ title: '保存成功', icon: 'none'})
				}).finally(() => {
					uni.hideLoading()
				})
			}
		}
	
	}
</script>

<style lang="scss" scoped>
.rate-config {
	.title {
		text-align: center;
		font-size: 15px;
		line-height: 36px
	}
	.list-ul {
		flex: 1;
		padding: 0 15px 15px 15px;
		overflow-y: auto;
		.list-item {
			> view {
				padding: 4px 0
			}
		}
	}
}

::v-deep {
	.uni-easyinput {
		width: 140px;
		flex: none
	}
	.uni-easyinput__content-input {
		height: 25px;
		
	}
}
</style>
