<!--
**  添加商户
-->
<template>
  <el-dialog title="添加商户" append-to-body :visible.sync="isVisible" width="30%" @close="closeDialog">
    <el-form ref="form" :model="form" :rules="rule" label-width="120px">
      <el-form-item label="商户名称" prop="userName">
        <el-input v-model="form.userName" placeholder="请输入商户名称"></el-input>
      </el-form-item>
      <el-form-item label="登录密码" prop="loginPassword">
        <el-input type="password" show-password v-model="form.loginPassword" placeholder="请输入登录密码"></el-input>
      </el-form-item>
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
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {addShopEntity} from '@/api/shop'

export default {
  name: "addVendor",
  data() {
    return {
      isVisible: false,
      whiteLoginIpList: [],
      form: {
        userName: '',
        shopName: '', // nickname ,和userName一样
        loginPassword: '',
        allowLoginIp: '',
        status: 0
      },
      rule: {
        userName: [
          { required: true, message: '请输入商户名称', trigger: 'blur' },
          // { pattern: /^[a-zA-Z0-9]{1,}$/, message: '商户名称只能包含字母、数字', trigger: ['change', 'blur'] }
        ],
        loginPassword: [
          { required: true, message: '请输入登录密码', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9]{6,}$/, message: '密码只能包含字母、数字，且长度不小于6', trigger: ['change', 'blur'] }
        ]
      },
      submitLoading: false
    }
  },
  methods: {
    openDialog() {
      this.form = {
        userName: '',
        shopName: '',
        loginPassword: '',
        allowLoginIp: '',
        status: 0
      }
      this.whiteLoginIpList = []
      this.isVisible = true
    },
    // 添加登录IP白名单
    addWhiteIp() {
      this.whiteLoginIpList.push({ip: ''})
    },
    // 删除登录IP白名单
    deleteWhiteIp(index) {
      this.whiteLoginIpList.splice(index, 1)
    },
    // 提交
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          this.form.shopName = this.form.userName
          if (this.whiteLoginIpList.length > 0) {
            this.form.allowLoginIp = this.whiteLoginIpList.map(item => item.ip).join(';')
          }
          addShopEntity(this.form).then(res => {
            this.$message.success('添加成功')
            this.closeDialog()
            this.$emit('refresh')
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
