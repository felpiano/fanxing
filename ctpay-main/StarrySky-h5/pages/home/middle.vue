<template>
	<view class="container">
		<show-google ref="showGooleRef"></show-google>
	</view>
</template>

<script>
	import showGoogle from './components/showGoogle.vue'
		
	export default {
		components: {
			showGoogle
		},
		data() {
			return {
				
			};
		},
		onLoad() {
			uni.showLoading()
			const token = uni.getStorageSync('SANFANG-TOKEN')
			if (token) {
				const userInfo = uni.getStorageSync('FanXing')
				if (!userInfo.showGoogle) {
					setTimeout(() => {
						uni.hideLoading()
						this.$refs.showGooleRef.init()
					})
				}
			}
		},
	}
</script>

<style lang="scss">

</style>
