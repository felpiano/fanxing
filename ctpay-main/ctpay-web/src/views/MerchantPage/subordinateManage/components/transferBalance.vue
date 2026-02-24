<!--
** 转移余额
-->
<template>
  <el-dialog title="转移余额" :visible.sync="isVisible" width="36%" @close="closeDialog">
      <el-form ref="form" label-width="120px" :model="form" :rules="rules">
        <el-form-item label="码商名称" prop="userName">
          <el-input v-model="form.userName" disabled></el-input>
        </el-form-item>
        <el-form-item label="现有余额" prop="balance">
          <el-input v-model="form.balance" disabled></el-input>
        </el-form-item>
        <el-form-item label="转移金额" prop="amount">
          <el-input-number :controls="false" :min="0" v-model="form.amount" placeholder="请输入转移金额" class="w100"></el-input-number>
        </el-form-item>
        <el-form-item label="谷歌验证码" prop="code">
          <el-input v-model="form.code" placeholder="请输入谷歌验证码" maxlength="6"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" autosize v-model="form.remark" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
      </div>
    </el-dialog>
</template>

<script>
import { trimBalanceMerchantChild } from '@/api/merchantChild'
import { EventBus } from '@/utils/event-bus.js'

export default {
  name: "changeAmount",
  data() {
    return {
      isVisible: false,
      loading: false,
      form: {
        childId: '',
        amount: '',
        code: '',
        remark: ''
      },
      rules: {
        userName: [
          { required: true, message: '请输入码商名称', trigger: 'blur' }
        ],
        balance: [
          { required: true, message: '请输入', trigger: 'blur' }
        ],
        amount: [
          { required: true, message: '请输入操作金额', trigger: 'blur' }
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
        userName: row.userName,
        childId: row.userId,
        balance: row.totalBalance - row.baseDeposit,
        amount: '',
        remark: '',
        code: ''
      }
      this.isVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.form.amount <= 0) {
          this.$message.error('金额不能小于0')
          return
        }
        this.loading = true
        const form = this.$lodash.cloneDeep(this.form)
        delete form.balance
        delete form.userName
        trimBalanceMerchantChild(form).then(res => {
          this.closeDialog()
          this.$emit('refresh')
          EventBus.$emit('updateBalance')
          this.$message.success('操作成功')
        }).catch(() => {
          this.closeDialog()
          this.$emit('refresh')
        })
      })
    },
    closeDialog() {
      this.loading = false
      this.isVisible = false
      this.$refs.form.resetFields()
    }
  }
}
</script>
