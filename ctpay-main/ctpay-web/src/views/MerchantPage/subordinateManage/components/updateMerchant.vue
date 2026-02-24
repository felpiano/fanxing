<!--
** 编辑下级码商
-->
<template>
  <el-dialog title="编辑码商" append-to-body :visible.sync="isVisible" width="30%" @close="closeDialog" :destroy-on-close="true">
    <el-form ref="form" :model="form" :rules="rule" label-width="120px" class="dialog-form-height">
      <el-form-item label="ID" prop="userId">
        <el-input v-model="form.userId" disabled></el-input>
      </el-form-item>
      <el-form-item label="用户名" prop="userName">
        <el-input v-model="form.userName" disabled></el-input>
      </el-form-item>
      <el-form-item label="登录密码" prop="loginPassword">
        <el-input type="password" show-password v-model="form.loginPassword" placeholder="不修改请留空"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input type="password" show-password v-model="form.confirmPassword" placeholder="请确认密码，不修改请留空"></el-input>
      </el-form-item>
      <el-form-item label="安全码" prop="safeCode">
        <el-input type="password" show-password v-model="form.safeCode" placeholder="修改码商安全码，不修改请留空"></el-input>
      </el-form-item>
      <el-form-item label="最低接单金额" prop="minAmount">
        <el-input-number :controls="false" v-model="form.minAmount" placeholder="请输入最低接单金额"></el-input-number>
      </el-form-item>
      <!-- <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="0">正常</el-radio>
          <el-radio :label="1">禁用</el-radio>
        </el-radio-group>
      </el-form-item> -->
      <el-form-item label="登录IP白名单" prop="whiteLoginIpList">
        <div class="flex-center">
          <div v-for="(item, index) in whiteLoginIpList" :key="index" class="mr10 mb10">
            <el-input style="width: 160px" v-model="item.ip" placeholder="登录IP白名单"></el-input>
            <el-button class="ml10" type="danger" size="small" @click="deleteWhiteIp(index)">删除</el-button>
          </div>
          <el-button class="mb10" type="primary" size="small" @click="addWhiteIp">添加</el-button>
        </div>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">关闭</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
    </div>
 </el-dialog>
</template>

<script>
import { updateMerchantChild } from '@/api/merchantChild'

export default {
  name: "updateMerchant",
  data() {
    return {
      isVisible: false,
      submitLoading: false,
      whiteLoginIpList: [],
      whiteCallbackIpList: [],
      form: {
        userId: '',
        userName: '',
        loginPassword: '',
        confirmPassword: '',
        safeCode: '',
        minAmount: '',
        whiteCallbackIp: '',
        whiteLoginIp: ''
      },
      rule: {
        userId: [
          { required: true, message: '请输入ID', trigger: 'blur' }
        ],
        userName: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        // minAmount: [
        //   { required: true, message: '请输入最低接单金额', trigger: 'blur' }
        // ]
      }
    }
  },
  methods: {
    openDialog(row) {
      this.form = this.$lodash.cloneDeep(row)
      this.whiteLoginIpList = []
      this.whiteCallbackIpList = []
      if (row.whiteLoginIp && row.whiteLoginIp !== '') {
        this.whiteLoginIpList = row.whiteLoginIp.split(',').map(item => ({ip: item}))
      }
      if (row.whiteCallbackIp && row.whiteCallbackIp !== '') {
        this.whiteCallbackIpList = row.whiteCallbackIp.split(',').map(item => ({ip: item}))
      }
      // 登录密码和安全码不可见
      this.form.loginPassword = null
      this.form.safeCode = null
      this.isVisible = true
    },
    addWhiteIp() {
      this.whiteLoginIpList.push({ ip: '' })
    },
    addCallbackIp() {
      this.whiteCallbackIpList.push({ ip: '' })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          this.form.whiteLoginIp = this.whiteLoginIpList.map(item => item.ip).join(',')
          this.form.whiteCallbackIp = this.whiteCallbackIpList.map(item => item.ip).join(',')
          // 假如修改了密码，确认密码不能为空，并且两次密码要一致
          if (this.form.loginPassword) {
            if (!this.form.confirmPassword) {
              this.$message.error('请确认密码')
              this.submitLoading = false
              return
            }
            if (this.form.loginPassword !== this.form.confirmPassword) {
              this.$message.error('两次密码不一致')
              this.submitLoading = false
              return
            }
          }
          // 假如修改了安全码，判断是否符合正则
          if (this.form.safeCode && !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/.test(this.form.safeCode)) {
            this.$message.error('安全码必须包含大小写字母和数字，且长度不少于6位')
            this.submitLoading = false
            return
          }
          const form = this.$lodash.cloneDeep(this.form)
          if (!form.loginPassword || form.loginPassword === '') {
            delete form.loginPassword
            delete form.confirmPassword
          }
          updateMerchantChild(form).then(res => {
            this.$message.success('编辑成功')
            this.$emit('refresh')
            this.isVisible = false
          }).finally(() => {
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
