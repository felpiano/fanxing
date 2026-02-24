<!--
** 集合码
-->

<template>
  <el-dialog :title="isAdd ? '添加' : '编辑'" append-to-body :visible.sync="isVisible" width="36%" @close="closeDialog" :destroy-on-close="true">
    <el-form ref="form" :model="form" :rules="rule" label-width="120px" class="dialog-form-height">
      <!-- <el-form-item label="收款姓名" prop="nickName">
        <el-input v-model="form.nickName" placeholder="请输入银行卡姓名"></el-input>
      </el-form-item>
      <el-form-item label="收款账号" prop="accountNumber">
        <el-input v-model="form.accountNumber" placeholder="请输入银行卡账号" :disabled="!isAdd"></el-input>
      </el-form-item> -->
      <el-form-item label="账号备注" prop="accountRemark">
        <el-input v-model="form.accountRemark" placeholder="请输入账号备注"></el-input>
      </el-form-item>
      <el-form-item label="传码方式" prop="qrcodeType">
        <el-select v-model="form.qrcodeType" placeholder="请选择传码方式">
          <el-option label="直接上传" :value="1"></el-option>
          <!-- <el-option label="解析地址" :value="0"></el-option>
          <el-option label="无码" :value="2"></el-option> -->
        </el-select>
      </el-form-item>
      <el-form-item label="二维码" prop="qrcodeValue" v-if="form.qrcodeType == 0">
        <el-input v-model="form.qrcodeValue" placeholder="请输入个人二维码解析结果"></el-input>
        <p class="remark-text">请 <span class="text-red pointer" @click="toUrl">点击这里</span> 解析二维码，然后将解析结果复制粘贴到上方网址栏中</p>
      </el-form-item>
      <el-form-item label="二维码" prop="qrcodeUrl" v-else-if="form.qrcodeType == 1">
        <ImageUpload :multiple="false" :limit="1" :value="form.qrcodeUrl" @input="imageUpload"/>
      </el-form-item>
      <el-form-item label="笔数上限" prop="countLimit">
        <el-input-number :controls="false" v-model="form.countLimit" placeholder="收款笔数上限（为0则不限制）" class="w100"></el-input-number>
        <p class="remark-text">收款笔数上限（为0则不限制）</p>
      </el-form-item>
      <el-form-item label="安全码" prop="safeCode">
        <el-input type="password" show-password v-model="form.safeCode" placeholder="请输入您设置的安全码"></el-input>
        <p class="remark-text">如果您还没设置安全码，初次输入就是设置安全码噢～</p>
      </el-form-item>
      <!--<el-form-item label="最小金额" prop="minAmount" v-if="$store.getters.agentId !== 10053">
        <el-input-number :controls="false" v-model="form.minAmount" placeholder="单笔最小金额" class="w100"></el-input-number>
      </el-form-item>
      <el-form-item label="最大金额" prop="maxAmount" v-if="$store.getters.agentId !== 10053">
        <el-input-number :controls="false" v-model="form.maxAmount" placeholder="单笔最大金额" class="w100"></el-input-number>
      </el-form-item>-->
      <el-form-item label="日限额" prop="dayLimit">
        <el-input-number :controls="false" v-model="form.dayLimit" placeholder="请输入每日限额" class="w100"></el-input-number>
        <p class="remark-text">每日限额（为0则不限制）</p>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">关闭</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {addMerchantQrcode} from '@/api/merchantQrcode'
import ImageUpload from '@/components/ImageUpload'

export default {
  name: "addMerchantScode",
  components: {
    ImageUpload
  },
  data() {
    return {
      isVisible: false,
      submitLoading: false,
      isAdd: true,
      form: {
        nickName: '',
        accountNumber: '',
        qrcodeType: 1,
        qrcodeValue: '',
        qrcodeUrl: '',
        accountRemark: '',
        minAmount: '',
        maxAmount: '',
        dayLimit: '',
        countLimit: '',
        safeCode: ''
      },
      rule: {
        // nickName: [
        //   { required: true, message: '请输入收款姓名', trigger: 'blur' }
        // ],
        // accountNumber: [
        //   { required: true, message: '请输入收款账号', trigger: 'blur' }
        // ],
        qrcodeType: [
          { required: true, message: '请选择传码方式', trigger: 'change' }
        ],
        qrcodeValue: [
          { required: true, message: '请输入二维码解析结果', trigger: 'blur' }
        ],
        qrcodeUrl: [
          { required: true, message: '请上传二维码', trigger: 'blur' }
        ],
        countLimit: [
          { required: true, message: '请输入笔数上限', trigger: 'blur' }
        ],
        safeCode: [
          { required: true, message: '请输入安全码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    openDialog(isAdd, data = {}) {
      this.isAdd = isAdd
      this.isVisible = true
      if (!isAdd) {
        this.form = this.$lodash.cloneDeep(data)
      } else {
        this.form = {
          channelId: data.channelId,
          merchantId: this.$store.getters.id,
          nickName: '',
          accountNumber: '',
          qrcodeType: 1,
          qrcodeValue: '',
          qrcodeUrl: '',
          accountRemark: '',
          minAmount: '',
          maxAmount: '',
          dayLimit: '',
          countLimit: '',
          safeCode: '',
        }
      }
    },
    closeDialog() {
      this.isVisible = false
      this.$refs.form.resetFields()
    },
    handleSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.submitLoading = true
          addMerchantQrcode(this.form).then(() => {
            this.$message.success('操作成功')
            this.$emit('submit', this.isAdd)
            this.closeDialog()
          }).finally(() => {
            this.submitLoading = false
          })
        }
      })
    },
    imageUpload(file) {
      this.form.qrcodeUrl = file.url
    },
    toUrl() {
      window.open('https://cli.im/deqr')
    }
  }
}
</script>
