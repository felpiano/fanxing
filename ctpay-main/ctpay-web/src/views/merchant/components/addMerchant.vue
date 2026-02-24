<!--
** 新增码商
-->
<template>
  <el-dialog title="添加码商" append-to-body :visible.sync="isVisible" width="30%" @close="closeDialog">
    <el-form ref="form" :model="form" :rules="rule" label-width="100px">
      <!--<el-form-item label="所属码商" prop="parentId">
        <el-select v-model="form.parentId" placeholder="请选择所属码商" class="w100">
          <el-option
            v-for="item in merchantList"
            :key="item.userId"
            :label="item.userName"
            :value="item.userId"
          ></el-option>
        </el-select>
      </el-form-item>-->
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="form.userName" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item label="登录密码" prop="loginPassword">
        <el-input type="password" show-password v-model="form.loginPassword" placeholder="请输入登录密码"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input type="password" show-password v-model="form.confirmPassword" placeholder="请输入确认密码"></el-input>
      </el-form-item>
      <el-form-item label="账号状态" prop="status">
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
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {addMerchantEntity, getMerchantaAll} from '@/api/merchant'

export default {
  name: "addMerchant",
  data() {
    return {
      isVisible: false,
      merchantList: [],
      form: {},
      rule: {
        userName: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2,  message: '用户名最少两个字符', trigger: 'blur'}
          // { pattern: /^[a-zA-Z0-9]{1,}$/, message: '登录名只能包含字母、数字', trigger: ['change', 'blur'] }
        ],
        loginPassword: [
          { required: true, message: '请输入登录密码', trigger: 'blur' },
          { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, message: '密码必须包含大小写字母和数字，且长度不少于6位', trigger: ['change', 'blur'] }
        ],
        confirmPassword: [
          { required: true, message: '请输入确认密码', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '请选择状态', trigger: 'change' }
        ],
        orderPermission: [
          { required: true, message: '请选择接单状态', trigger: 'change' }
        ]
      },
      submitLoading: false
    }
  },
  methods: {
    openDialog() {
      // this.getMerchantList()
      this.form = {
        parentId: '',
        userName: '',
        merchantName: '',
        loginPassword: '',
        confirmPassword: '',
        status: 0,
        orderPermission: 0
      }
      this.isVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.loginPassword !== this.form.confirmPassword) {
            this.$message.error('两次密码输入不一致')
            return
          }
          this.submitLoading = true
          this.form.merchantName = this.form.userName
          addMerchantEntity(this.form).then(res => {
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
    // 获取所有码商---码商可以选择上级码商
    getMerchantList() {
      getMerchantaAll({}).then(res => {
        this.merchantList = res.data || []
      })
    },
    closeDialog() {
      this.isVisible = false
      this.$refs.form.resetFields()
    }
  }
}
</script>
