<!--
** 支付宝编辑，新增
-->
<template>
	<view class="container update-page">
		<uni-forms ref="formRef" :model="form" :rules="rules" :label-position="'top'" :label-width="80">
			<uni-forms-item :label="title1" required name="nickName" v-if="form.channelId !== 1108">
				<uni-easyinput trim :placeholder="`请输入${title1}`"  v-model="form.nickName"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item :label="title2" required name="accountNumber" v-if="form.channelId !== 1108 && accountNeed === 0">
				<uni-easyinput trim :placeholder="`请输入${title2}`" v-model="form.accountNumber" :disabled="!isAdd"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="所属银行" required name="uid" v-if="backIdList.includes(form.channelId)">
				<uni-easyinput trim placeholder="请输入所属银行" v-model="form.uid"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="UID" required name="uid" v-if="form.channelId == '1110'">
				<uni-easyinput trim placeholder="请输入收款支付宝UID" v-model="form.uid"></uni-easyinput>
			  <p class="remark-text">请 <span class="text-red pointer" @click="checkUid">点击这里</span> 扫码查看UID，然后将UID复制粘贴到上方输入框中</p>
			</uni-forms-item>
			<uni-forms-item label="支付链接" required name="accountRemark" v-if="form.channelId == '1122'">
				<uni-easyinput trim placeholder="请复制支付链接到输入框中" v-model="form.accountRemark"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="银行支行" name="accountRemark" v-if="backIdList.includes(form.channelId)">
				<uni-easyinput trim placeholder="请输入银行支行" v-model="form.accountRemark"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="账号备注" name="accountRemark" v-else>
				<uni-easyinput trim placeholder="请输入账号备注" v-model="form.accountRemark"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="传码方式" required name="qrcodeType">
				<uni-data-select v-model="form.qrcodeType" :localdata="qrcodeTypeList" :clear="false"></uni-data-select>
			</uni-forms-item>
			<uni-forms-item label="二维码" required name="qrcodeValue" v-if="form.qrcodeType == 0">
				<uni-easyinput trim placeholder="请输入个人二维码解析结果" v-model="form.qrcodeValue"></uni-easyinput>
				<p class="remark-text">请 <span class="text-red pointer" @click="toUrl">点击这里</span> 解析二维码，然后将解析结果复制粘贴到上方输入框中</p>
			</uni-forms-item>
			<uni-forms-item label="二维码" required name="qrcodeUrlObj" v-else-if="form.qrcodeType == 1">
				<uni-file-picker
					v-model="form.qrcodeUrlObj"
					fileMediatype="image"
					:auto-upload="false"
					:sourceType="['album', 'camera']"
					return-type="Object"
					:limit="1"
					mode="grid"
					title=""
					@select="selectUpload"
				/>
			</uni-forms-item>
			<uni-forms-item label="笔数上限" required name="countLimit">
				<uni-easyinput type="number" trim placeholder="收款笔数上限（为0则不限制）" v-model="form.countLimit"></uni-easyinput>
				<view class="remark-text">收款笔数上限（为0则不限制）</view>
			</uni-forms-item>
			<uni-forms-item label="安全码" required name="safeCode">
				<uni-easyinput trim type="password" placeholder="请输入您设置的安全码" v-model="form.safeCode"></uni-easyinput>
				<view class="remark-text">如果您还没设置安全码，初次输入就是设置安全码噢～</view>
			</uni-forms-item>
			<uni-forms-item label="日限额" name="dayLimit">
				<uni-easyinput type="number" trim placeholder="每日限额（为0则不限制）" v-model="form.dayLimit"></uni-easyinput>
				<view class="remark-text">每日限额（为0则不限制）</view>
			</uni-forms-item>
			<uni-forms-item label="最小金额" name="minAmount" v-if="agentId == '10548'">
				<uni-easyinput type="number" trim placeholder="单笔最小金额" v-model="form.minAmount"></uni-easyinput>
			</uni-forms-item>
			<uni-forms-item label="最大金额" name="maxAmount" v-if="agentId == '10548'">
				<uni-easyinput type="number" trim placeholder="单笔最大金额" v-model="form.maxAmount"></uni-easyinput>
			</uni-forms-item>
		</uni-forms>
		<view>
			<button class="btn btn-primary" @click="handleSubmit">提交</button>
		</view>
		
		<uni-popup ref="UIDPopup" type="center" :animation="true">
			<view class="uid-code">
				<img src="@/static/uid-code.jpg"/>
				<view class="uid-code-red">【截图】当前二维码页面，打开手机支付宝【扫一扫】，点击右下角【相册】，选择截图的二维码照片，点击【确定】后扫码获得2088UID</view>
				<button class="btn btn-primary" @click="handleClose">关闭</button>
			</view>
		</uni-popup>
	</view>
</template>
<script>
	import { getQrcodeDetail, addMerchantQrcode, needAccountNumber } from '@/api/merchant.js'
	
	export default {
		data() {
			return {
				isAdd: true,
				token: '',
				userInfo: {},
				title1: '',
				title2: '',
				accountNeed: 0,
				qrcodeTypeList: [
					{ value: 0, text: "解析地址" },
					{ value: 1, text: "直接上传" },
					{ value: 2, text: "无码" },
				],
				backIdList: [1102, 1113, 1114, 1115, 1116, 1118],
				form: {
					channelId: '',
					merchantId: '',
					nickName: '',
					accountNumber: '',
					qrcodeType: 0,
					qrcodeValue: '',
					qrcodeUrl: '',
					qrcodeUrlObj: [],
					accountRemark: '',
					minAmount: 0,
					maxAmount: 0,
					dayLimit: 0,
					countLimit: 0,
					safeCode: '',
					uid: ''
				},
				rules: {
					nickName: {rules : [
						{ required: true, errorMessage: '请输入姓名', trigger: 'blur' }
					]},
					accountNumber: {rules :[
						{ required: true, errorMessage: '请输入账号', trigger: 'blur' },
					]},
					qrcodeType: {rules :[
						{ required: true, errorMessage: '请选择传码方式', trigger: 'change' }
					]},
					qrcodeValue: {rules :[
						{ required: true, errorMessage: '请输入二维码解析结果', trigger: 'blur' }
					]},
					qrcodeUrlObj: {rules :[
						{ required: true, errorMessage: '请上传二维码', trigger: 'blur' }
					]},
					countLimit:  {rules :[
						{ required: true, errorMessage: '请输入笔数上限', trigger: 'blur' }
					]},
					safeCode:  {rules :[
						{ required: true, errorMessage: '请输入安全码', trigger: 'blur' }
					]},
					uid: {rules :[
						{ required: true, errorMessage: '必填项不能为空', trigger: 'blur' }
					]},
				},
				agentId: ''
			}
		},
		onLoad() {
			const row = uni.getStorageSync('tempData');
			// 设置标题
			uni.setNavigationBarTitle({
				title: (row.isAdd === '1' ? '编辑' : '新增') + row.title,
				success: function() {},
				fail: function(err) {
					console.error('标题设置失败', err);
				}
			});
			this.agentId = row.agentId
			this.getNeedAccount(row.channelId)
			// 判断
			this.form.qrcodeType = 0
			const channelIdList = [1105, 1106, 1110]
			if (this.backIdList.includes(row.channelId)) {
				this.title1 = '银行卡姓名'
				this.title2 = '银行卡账号'
			} else if (row.channelId === 1107) {
				this.title1 = '收款人姓名'
				this.title2 = '钱包编号'
				this.form.qrcodeType = 2
			} else if (row.channelId === 1108) {
				this.form.qrcodeType = 1
			} else if (row.channelId === 1109) {
				this.title1 = '真实姓名'
				this.title2 = '收款账号'
				this.form.qrcodeType = 1
			} else if (channelIdList.includes(row.channelId)) {
				this.title1 = '支付宝姓名'
				this.title2 = '支付宝账号'
				this.rules.accountNumber.rules.push({ validateFunction: (rule,value,data,callback) => {
					// 手机号码 邮箱
					if (value.indexOf('gmail') > -1) {
						if (!(/^[a-zA-Z0-9_.+]+[a-zA-Z0-9]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/).test(value)) {
							callback('请输入正确的支付宝账号')
						}
						return
					} else {
						if (!/^1[3456789]\d{9}$/.test(value) && !/^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/.test(value)) {
							callback('请输入正确的支付宝账号')
						}
						return
					}
				}, trigger: 'blur' })
			} else {
				this.title1 = '姓名'
				this.title2 = '账号'
			}
				
			if (row.isAdd) {
				this.isAdd = row.isAdd === '1' ? false : true
			}
			if (row.id) {
				this.getDetail(row)
			} else { 
				this.userInfo = uni.getStorageSync('FanXing')
				this.form.channelId = row.channelId
				this.form.merchantId = this.userInfo.userId
			}
		},
		mounted() {
			this.token = uni.getStorageSync('SANFANG-TOKEN')
		},
		methods: {
			getDetail(row) {
				this.form = row
				this.form.qrcodeUrlObj = [{
					url: this.form.qrcodeUrl,
					extname: 'png',
					name: '二维码.png'
				}]
			},
			getNeedAccount(channelId) {
				needAccountNumber({ channelId:  channelId}).then(res => {
					this.accountNeed = res.data.accountNeed
				})
			},
			handleSubmit() {
				uni.showLoading()
				this.$refs.formRef.validate().then(() => {
					addMerchantQrcode({
						...this.form
					}).then(res => {
						uni.showToast({
							title: '操作成功',
						 	icon: 'none'
						})
						uni.navigateBack({delta: 1})
					})
				}).finally(() => {
					uni.hideLoading()
				})
			},
			// 上传
			selectUpload(fileObj) {
				uni.showLoading()
				const imgFile = fileObj.tempFiles
				imgFile[0].name = 'file'
				uni.uploadFile({
					url: this.$config.apiUrl + '/common/upload',
					files: imgFile,
					header: {
						'Authorization': 'Bearer ' + this.token
					},
					success: (res) => {
						if (res.statusCode === 200) {
							const data = JSON.parse(res.data)
							if (data.code === 200) {
								this.form.qrcodeUrlObj = imgFile
								this.form.qrcodeUrl = data.url
							} else {
								this.form.qrcodeUrlObj = []
								this.form.qrcodeUrl = {}
								uni.showToast({
									title: data.msg,
								 	icon: 'none'
								})
							}
						}
					},
					complete: () => {
					  uni.hideLoading()
					}
				})
			},
			toUrl() {
				window.open('https://cli.im/deqr')
			},
			checkUid() {
				this.$refs.UIDPopup.open()
			},
			handleClose() {
				this.$refs.UIDPopup.close()
			}
		}
	}
</script>

<style lang="scss" scoped>
	.update-page {
		background: #fff;
		padding: 0 15px 20px 15px;
		overflow-y: auto;
	}
	
	.uid-code {
	  width: 96%;
	  margin: auto;
		padding: 20px 30px;
		border-radius: 4px;
	  > img {
	    display: block;
	    width: 88%;
	    margin: auto
	  }
		&-red {
		  color: #ff0000;
		  text-align: center;
		  line-height: 24px;
		  margin-top: 6px
		}
		.btn {
			margin-top: 20px
		}
	}
	
	
	::v-deep {
		.uni-forms-item {
			margin-bottom: 16px;
			&.is-direction-top {
				.uni-forms-item__label  {
					padding: 0
				}
			}
		}
	}
</style>