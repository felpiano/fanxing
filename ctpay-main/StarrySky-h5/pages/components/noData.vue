<template>
	<view class="no-data">
		<image class="no-data-image" src="@/static/no-data.png"></image>
		<view class="no-data-text">暂无数据</view>
	</view>
</template>

<script>
</script>

<style lang="scss" scoped>
	.no-data {
		width: 100%;
		padding-top: 38px;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		&-image {
			width: 170px;
			height: 73.8px;
			
		}
		&-text {
			text-align: center;
			color: #999;
			font-size: 14px;
			font-weight: 500;
		}
	}
</style>