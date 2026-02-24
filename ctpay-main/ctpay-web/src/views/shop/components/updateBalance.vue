<template>
   <!--商户资金操作-->
   <el-dialog :title="dialogInfo.title" :visible.sync="dialogInfo.isVisible" width="30%" @close="closeDialog">
      <el-form :model="changeForm" :rules="changeRules" ref="changeForm" label-width="120px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="changeForm.userId" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="用户账号" prop="userName">
          <el-input v-model="changeForm.userName" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="当前余额(元)" prop="balance">
          <el-input v-model="changeForm.balance" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="操作金额" prop="amount">
          <el-input-number :min="0" v-model="changeForm.amount" placeholder="请输入金额" :controls="false" class="w100"></el-input-number>
        </el-form-item>
        <el-form-item label="谷歌验证码" prop="code">
          <el-input v-model="changeForm.code" placeholder="请输入谷歌验证码" maxlength="6"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" autosize v-model="changeForm.remark" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="submitAmout" :loading="dialogInfo.loading">立即调整</el-button>
      </div>
    </el-dialog>
</template>

<script>
import {shopChargeAmount} from '@/api/shop'

export default {
  name: 'updateBalance',
  data() {
    return {
      dialogInfo: {
        title: '余额操作',
        isVisible: false,
        loading: false
      },
      changeForm: {},
      changeRules: {
        userId: [
          { required: true, message: '请输入用户ID', trigger: 'blur' }
        ],
        userName: [
          { required: true, message: '请输入用户账号', trigger: 'blur' }
        ],
        balance: [
          { required: true, message: '请输入当前余额', trigger: 'blur' }
        ],
        amount: [
          { required: true, message: '请输入金额', trigger: 'blur' }
        ],
        code: [
        { required: true, message: '请输入谷歌验证码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    openDialog(row) {
      this.dialogInfo.isVisible = true
      this.changeForm = {
        userId: row.userId,
        userName: row.userName,
        balance: row.balance || 0,
        amount: 0,
        code: ''
      }
    },
    submitAmout() {
      this.$refs.changeForm.validate((valid) => {
        if (valid) {
          // 只有代理才有权限操作
          if (!this.$store.getters.roles.includes('agent') && !this.$store.getters.identity === 3) {
            this.$message.error('暂无权限操作')
            return
          }
          this.dialogInfo.loading = true
          shopChargeAmount(this.changeForm).then(res => {
            this.$message.success('操作成功')
            this.dialogInfo.loading = false
            this.closeDialog()
            this.$emit('refresh')
          }).catch(() => {
            this.dialogInfo.loading = false
          })
        }
      })
    },
    closeDialog() {
      this.dialogInfo.isVisible = false
      this.$refs.changeForm.resetFields()
    }
  }
}
</script>
