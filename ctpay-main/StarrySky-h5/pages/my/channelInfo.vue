<!--通道费率-->
<template>
	<view class="container channel-rate">
		<view class="channel-rate-head">
			<view class="channel-name">通道名称</view>
			<view class="channel-code">通道编码</view>
			<view class="channel-rates">通道费率(%)</view>
		</view>
		<view class="channel-rate-body">
			<view class="rate-item" v-for="item in channelRateList" :key="item.id">
				<view class="channel-name">{{item.channelName}}</view>
				<view class="channel-code">{{item.channelCode}}</view>
				<view class="channel-rates">{{item.channelRate}}%</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { getSelfChannel } from '@/api/merchant.js'
	
	export default {
		data() {
			return {
				channelRateList: []
			};
		},
		onShow() {
			this.getChannelInfo()
		},
		methods: {
			getChannelInfo() {
				uni.showLoading()
				getSelfChannel({}).then(res => {
					this.channelRateList = res.data || []
				}).finally(() => {
					uni.hideLoading()
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.channel-rate {
		background-color: #fff;
		position: relative;
		overflow-y: auto;
		color: #2c3e50;
		&-head {
			display: table;
			padding: 8px 8px 0 8px;
			> view {
				display: inline-block;
				line-height: 40px;
				text-align: center;
				background-color: #f6f8fa;
				border-bottom: 1px solid #dfe2e5;
				border-left: 1px solid #dfe2e5;
				border-top: 1px solid #dfe2e5;
				&:last-child {
					border-right: 1px solid #dfe2e5;
				}
			}
		}
		&-body {
			width: 100%;
			padding: 0 8px;
			.rate-item {
				width: 100%;
				display: flex;
				> view {
					border-bottom: 1px solid #dfe2e5;
					border-left: 1px solid #dfe2e5;
					padding: 7px 0 7px 15px;
					&:last-child {
						border-right: 1px solid #dfe2e5;
					}
				}
			}
		}
		.channel-name, .channel-code {
			width: 37%
		}
		.channel-rates {
			width: 26%
		}
	}

</style>
