<!--
**  谷歌验证码
** 只能点击保存接口，并且成功后才能关掉弹框
-->

<template>
	<uni-popup ref="googlePopup" type="center" :animation="true" :is-mask-click="false" :mask-background-color="'rgba(0,0,0,0.8)'">
		<view class="show-google">
			<view class="title">请先绑定谷歌验证码</view>
			<uni-forms :model="googleForm" ref="googleForm" :rules="googleRules" label-width="90px">
				<image class="scret-code" :src="googleForm.googleScretCode"/>
				<uni-forms-item label="谷歌秘钥:" prop="googleScret" style="line-height: 36px">
					<view class="flex-center">
						<view class="">{{ googleForm.googleScret }}</view>
						<button style="margin-left: 10px" class="mini-btn btn-primary" @click="handleCopy">复制</button>
					</view>
				</uni-forms-item>
				<uni-forms-item label="谷歌验证码:" prop="googleCode">
					<uni-easyinput maxlength="6" v-model="googleForm.googleCode" clearable placeholder="请输入谷歌验证码"></uni-easyinput>
				</uni-forms-item>
			</uni-forms>
			<view class="flex-center foot-btn">
				<button class="btn " @click="handleLogout">退出系统</button>
				<button class="btn btn-primary" @click="handleSubmit" :loading="loading">绑定</button>
			</view>
		</view>
	</uni-popup>
</template>

<script>
import { bindGoogle, getGoogleSecret } from '@/api/merchant.js';
import { logout, getInfo } from '@/api/login.js'
export default {
  data() {
    return {
      loading: false,
      showGoogle: false,
      googleForm: {
        googleScret: '',
        googleScretCode: '',
        googleCode: ''
      },
      googleRules: {
        googleCode: {rules: [
          { required: true, errorMessage: '请输入谷歌验证码', trigger: 'blur' }
        ]}
      }
    }
  },
  methods: {
    init() {
      getGoogleSecret({}).then(res => {
        this.$set(this, 'googleForm', (res.data || {}))
				this.$refs.googlePopup.open()
      });
    },
    handleSubmit() {
      this.$refs.googleForm.validate().then(() => {
				this.loading = true
				bindGoogle({
					googleSecret: this.googleForm.googleScret,
					code: this.googleForm.googleCode
				}).then(() => {
					getInfo({}).then((res) => {
						const user = res.user || {}
						const userInfo = {
							userId: user.userId,
							userName: user.userName,
							showGoogle: user.showGoogle
						}
						try {
							uni.setStorageSync('FanXing', userInfo);
						} finally {
							this.loading = false
							uni.showToast({ title: '绑定成功', icon: 'none'})
							this.$refs.googlePopup.close()
							// 调用App.vue的初始化方法
							getApp().$vm.init()
							uni.navigateTo({
								url: '/pages/my/setSafeCode?type=set'
							})
						}
					})
				}).finally(() => {
					
				});
      })
    },
    // 复制
    handleCopy() {
			uni.setClipboardData({
				data: this.googleForm.googleScret,
				success: () => {
					uni.showToast({
						title: '复制成功',
						icon: 'none'
					})
				}
			})
    },
		handleLogout() {
			uni.showLoading()
			logout().then(res => {
				uni.removeStorageSync('TRADE-TOKEN')
				uni.removeStorageSync('tradeAccount')
				const xhssf = sessionStorage.getItem('xhssf')
				uni.redirectTo({
					url: '/pages/login?xhssf=' + xhssf
				})
			}).finally(() => {
				uni.hideLoading()
			})
		}
  }
}
</script>

<style scoped lang="scss">
	.show-google {
		background: #fff;
		border-radius: 4px;
		padding: 10px 15px;
		
		.title {
			text-align: center;
			font-size: 16px;
			font-weight: 700
		}
		.scret-code {
		  display: block;
		  width: 140px;
		  height: 140px;
		  margin: 12px auto;
			img {
				opacity: 1;
			}
		}
		.foot-btn {
			.btn {
				margin: 0 0 0 16px
			}
		}
	}
	
	::v-deep {
		.uni-popup {
			z-index: 9999
		}
		uni-image>img {
			opacity: 1;
		}
	}
</style>
