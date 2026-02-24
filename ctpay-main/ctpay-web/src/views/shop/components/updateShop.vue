<!--
** 编辑商户
-->

<template>
  <el-dialog title="编辑用户" :visible.sync="isVisible" width="38%" @close="closeDialog">
    <el-form :model="form" :rules="rules" ref="form" sizi="small" label-width="120px" class="dialog-form-height">
      <el-form-item label="商户ID" prop="userId">
        <el-input v-model="form.userId" disabled></el-input>
      </el-form-item>
      <el-form-item label="商户名称" prop="userName">
        <el-input v-model="form.userName" placeholder="请输入商户名称" disabled></el-input>
      </el-form-item>
      <el-form-item label="登录密码" prop="loginPassword">
        <el-input type="password" show-password v-model="form.loginPassword" placeholder="不修改请留空"></el-input>
      </el-form-item>
      <el-form-item label="安全码" prop="safeCode">
        <el-input type="password" show-safeCode v-model="form.safeCode" placeholder="修改商户安全码，不修改请留空"></el-input>
      </el-form-item>
      <el-form-item label="提现手续费(元)" prop="wthdrawalFee">
        <el-input-number v-model="form.wthdrawalFee" placeholder="请输入提现手续费" :controls="false" class="w100"></el-input-number>
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
      <el-form-item label="指定码商ID" prop="merchantIdList">
        <div class="flex-center">
          <div v-for="(item, index) in merchantIdList" :key="index" class="mr10 mb10">
            <el-input style="width: 100px" v-model="item.id" placeholder="码商ID"></el-input>
            <el-button class="ml10" type="danger" size="small" @click="deleteMerchantId(index)">删除</el-button>
          </div>
          <el-button class="mb10" type="primary" size="small" @click="addMerchantId">添加</el-button>
        </div>
      </el-form-item>
      <el-form-item label="google验证" prop="googleSecretFlag">
        <el-select v-model="form.googleSecretFlag" placeholder="请选择是否有google验证" class="w100">
          <el-option label="有" :value="0"></el-option>
          <el-option label="无" :value="1"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="最小金额" prop="minAmount">
        <el-input-number v-model="form.minAmount" placeholder="请输入最小金额" :controls="false" class="w100"></el-input-number>
      </el-form-item>
      <el-form-item label="最大金额" prop="maxAmount">
        <el-input-number v-model="form.maxAmount" placeholder="请输入最大金额" :controls="false" class="w100"></el-input-number>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="closeDialog">关闭</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
    </div>
   </el-dialog>
</template>

<script>
import {updateShopEntity} from '@/api/shop'

export default {
  name: 'updateVendor',
  data() {
    return {
      isVisible: false,
      whiteLoginIpList: [],
      merchantIdList: [],
      form: {},
      rules: {
        userId: [
          { required: true, message: '请输入商户Id', trigger: 'blur' }
        ],
        userName: [
          { required: true, message: '请输入商户名称', trigger: 'blur' }
        ],
        // wthdrawalFee: [
        //   { required: true, message: '请输入提现手续费', trigger: 'blur' }
        // ],
        // allowLoginIp: [
        //   { required: true, message: '请输入登录IP白名单', trigger: 'blur' }
        // ],
        // merchantIds: [
        //   { required: true, message: '请输入指定码商ID', trigger: 'blur' }
        // ],
        // telegram: [
        //   { required: true, message: '请输入指定团队（团长ID）', trigger: 'blur' }
        // ],
        // telegramGroup: [
        //   { required: true, message: 'telegram群组ID', trigger: 'blur' }
        // ],
        // googleSecretFlag: [
        //   { required: true, message: '请选择是否有google验证', trigger: 'change' }
        // ],
        // minAmount: [
        //   { required: true, message: '请输入最小金额', trigger: 'blur' }
        // ],
        // maxAmount: [
        //   { required: true, message: '请输入最大金额', trigger: 'blur' }
        // ]
      },
      submitLoading: false,
    }
  },
  methods: {
    openDialog(row) {
      this.form = this.$lodash.cloneDeep(row)
      this.whiteLoginIpList = []
      this.merchantIdList = []
      if (this.form.allowLoginIp) {
        this.whiteLoginIpList = this.form.allowLoginIp.split(';').map(item => ({ip: item}))
      }
      if (this.form.merchantIds) {
        this.merchantIdList = this.form.merchantIds.split(',').map(item => ({id: item}))
      }
      // 登录密码和安全码不可见
      this.form.loginPassword = null
      this.form.safeCode = null
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
    // 添加指定码商ID
    addMerchantId() {
      this.merchantIdList.push({id: ''})
    },
    // 删除指定码商ID
    deleteMerchantId(index) {
      this.merchantIdList.splice(index, 1)
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          this.form.allowLoginIp = this.whiteLoginIpList.map(item => item.ip).join(';')
          // 码商ID不能为空,如果为空则提示
          const isEmty = this.merchantIdList.map(item => item.id === '').includes(true)
          if (isEmty) {
            this.$message.error('指定码商ID不能为空')
            this.submitLoading = false
            return
          }
          // 码商ID不能重复,如果重复则提示
          const isRepeat = this.merchantIdList.map(item => item.id).some((item, index, arr) => arr.indexOf(item) !== index)
          if (isRepeat) {
            this.$message.error('指定码商ID不能重复')
            this.submitLoading = false
            return
          }
          this.form.merchantIds = this.merchantIdList.map(item => item.id).join(',')
          updateShopEntity(this.form).then(() => {
            this.$message.success('编辑成功')
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
