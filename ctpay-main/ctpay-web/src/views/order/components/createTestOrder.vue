<!--
** 创建测试单
-->

<template>
  <el-dialog
    title="测试收银台"
    :visible.sync="dialogVisible"
    width="30%"
    center>
    <el-form ref="testOrderForm" :model="testOrderForm" :rules="rules" label-width="100px">
      <el-form-item label="商户" prop="mchid">
        <el-select v-model="testOrderForm.mchid" placeholder="请选择商户" @change="selectMid" class="w100">
          <el-option
            v-for="item in shopList"
            :key="item.userId"
            :label="item.shopName"
            :value="item.userId">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="码商" prop="merchantId">
        <el-select v-model="testOrderForm.merchantId" filterable placeholder="请选择商户" class="w100">
          <el-option
            v-for="item in merchantList"
            :key="item.userId"
            :label="item.userName"
            :value="item.userId">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="通道" prop="channel">
        <el-select v-model="testOrderForm.channel" placeholder="请选择通道" class="w100">
          <el-option
            v-for="item in channelList"
            :key="item.channelCode"
            :label="item.channelName"
            :value="item.channelCode">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number :controls="false" v-model="testOrderForm.amount" placeholder="请输入下单金额" class="w100"></el-input-number>
      </el-form-item>
      <el-form-item label="回调地址" prop="notify_url">
        <el-input v-model="testOrderForm.notify_url" placeholder="请输入回调地址"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="testLoading" @click="submitTestOrder">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {getShopBaseList, getShopListAll} from '@/api/shop'
import { createTestByMerchant } from '@/api/order'
import { getMerchantaAll } from '@/api/merchant'

export default {
  name: "createTestOrder",
  data() {
    return {
      channelList: [],
      shopList: [],
      merchantList: [],
      testLoading: false,
      dialogVisible: false,
      testOrderForm: {
        mchid: '',
        merchantId: '',
        amount: 0,
        notify_url: '',
        channel: ''
      },
      rules: {
        mchid: [
          { required: true, message: '选择商户', trigger: 'blur' }
        ],
        merchantId: [
          { required: true, message: '选择码商', trigger: 'blur' }
        ],
        channel: [
          { required: true, message: '选择通道', trigger: 'blur' }
        ],
        amount: [
          { required: true, message: '请输入金额', trigger: 'blur' }
        ],
        notify_url: [
          { required: true, message: '请输入回调地址', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    openDialog() {
      this.testOrderForm = {
        mchid: '',
        amount: 0,
        notify_url: 'https://admin.jszdf.xyz/api',
        channel: ''
      },
      this.getShopList()
      this.getMerchantList()
      this.dialogVisible = true
    },
    getShopList() {
      getShopListAll({}).then(res => {
        this.shopList = res.data || []
      })
    },
    selectMid(e) {
      this.testOrderForm.channel = ''
      getShopBaseList({ shopId: e }).then(res => {
        this.channelList = res.data || []
      })
    },
    // 选择码商
    getMerchantList() {
      getMerchantaAll({}).then(res => {
        this.merchantList = res.data || []
      })
    },
    submitTestOrder() {
      this.$refs.testOrderForm.validate((valid) => {
        if (!valid) return
        // 下单金额不能为0
        if (this.testOrderForm.amount <= 0) {
          this.$message.error('下单金额不能为0')
          return
        }
        this.testLoading = true
        createTestByMerchant({
          ...this.testOrderForm
        }).then(res => {
          const requestUrl = res.data.request_url
          this.$message.success('操作成功，正在跳转链接...')
          window.open(requestUrl)
          this.dialogVisible = false
        }).finally(() => {
          this.testLoading = false
        })
      })
    },
  }
}
</script>
