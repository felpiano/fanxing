<!--
** 新增码商
-->
<template>
  <el-dialog title="添加下级码商" append-to-body :visible.sync="isVisible" width="30%" @close="closeDialog">
    <el-form ref="form" :model="form" :rules="rule" label-width="120px">
      <el-form-item label="用户名" prop="loginName">
        <el-input v-model="form.loginName" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item label="登录密码" prop="password">
        <el-input type="password" show-password v-model="form.password" placeholder="请输入登录密码"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input type="password" show-password v-model="form.confirmPassword" placeholder="请输入确认密码"></el-input>
      </el-form-item>
      <!--<el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="0">正常</el-radio>
          <el-radio :label="1">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="接单状态" prop="orderPermission">
        <el-radio-group v-model="form.orderPermission">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>-->
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { addMerchantChild } from '@/api/merchantChild'

export default {
  name: "addMerchant",
  data() {
    return {
      isVisible: false,
      merchantList: [],
      form: {},
      rule: {
        loginName: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          // { pattern: /^[a-zA-Z0-9]{1,}$/, message: '登录名只能包含字母、数字', trigger: ['change', 'blur'] }
        ],
        password: [
          { required: true, message: '请输入登录密码', trigger: 'blur' },
          { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, message: '密码必须包含大小写字母和数字，且长度不少于6位', trigger: ['change', 'blur'] }
        ],
        confirmPassword: [
          { required: true, message: '请输入确认密码', trigger: 'blur' }
        ]
      },
      submitLoading: false
    }
  },
  methods: {
    openDialog() {
      this.form = {
        loginName: '',
        password: '',
        confirmPassword: '',
        status: 0,
        orderPermission: 0,
        workStatus: 0
      }
      this.isVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.password !== this.form.confirmPassword) {
            this.$message.error('两次密码输入不一致')
            return
          }
          this.submitLoading = true
          addMerchantChild(this.form).then(res => {
            this.submitLoading = false
            this.$message.success('添加成功')
            this.closeDialog()
            this.$emit('refresh')
          }).catch(() => {
            this.submitLoading = false
          })
        }
      })
    },
    closeDialog() {
      this.isVisible = false
      this.$refs.form.resetFields()
    }
  }
}
</script>
