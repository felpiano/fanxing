<!--
** 代理--确认收款
-->
<template>
  <el-dialog
    title="收款确认"
    :visible.sync="isVisible"
    width="30%"
    @close="closeDialog">
    <el-form ref="form" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="平台单号" prop="tradeNo">
        <el-input v-model="form.tradeNo" readonly></el-input>
      </el-form-item>
      <el-form-item label="商户单号" prop="shopOrderNo">
        <el-input v-model="form.shopOrderNo" readonly></el-input>
      </el-form-item>
      <el-form-item label="交易金额(元)" prop="orderAmount">
        <el-input v-model="form.orderAmount" readonly></el-input>
      </el-form-item>
      <el-form-item label="支付金额(元)" prop="fixedAmount">
        <el-input v-model="form.fixedAmount" readonly></el-input>
      </el-form-item>
      <el-form-item label="谷歌验证码" prop="code">
        <el-input v-model="form.code" placeholder="请输入谷歌验证码" maxlength="6"></el-input>
      </el-form-item>
    </el-form>  
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { repairAgentOrder } from '@/api/order'

export default {
  name: 'repairAgentOrder',
  data() {
    return {
      isVisible: false,
      loading: false,
      form: {
        id: '',
        code: '',
        tradeNo: '',
        shopOrderNo: ''
      },
      rules: {
        tradeNo: [
          { required: true, message: '请输入平台单号', trigger: 'blur' }
        ],
        shopOrderNo: [
          { required: true, message: '请输入商户单号', trigger: 'blur' }
        ],
        orderAmount: [
          { required: true, message: '请输入交易金额', trigger: 'blur' }
        ],
        fixedAmount: [
          { required: true, message: '请输入支付金额', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入谷歌验证码', trigger: 'blur' }
        ],
      }
    }
  },
  methods: {
    openDialog(row) {
      this.form = {
        id: row.id,
        code: '',
        tradeNo: row.tradeNo,
        shopOrderNo: row.shopOrderNo,
        orderAmount: row.orderAmount,
        fixedAmount: row.fixedAmount
      },
      this.isVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return
        this.loading = true
        repairAgentOrder({
          id: this.form.id,
          code: this.form.code
        }).then(res => {
          this.$message.success('补单成功')
          this.closeDialog()
          this.$emit('refresh')
        }).finally(() => {
          this.loading = false
        })
      })
    },
    closeDialog() {
      this.isVisible = false
      this.$refs.form.resetFields()
    }
  }
}
</script>