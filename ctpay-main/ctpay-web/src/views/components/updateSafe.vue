<!--
** 修改安全码
-->
<template>
  <el-dialog 
    title="修改安全码" 
    :visible.sync="isVisible" 
    width="30%" 
    :show-close="isClose" 
    :destroy-on-close="true" 
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="closeDialog">
    <el-form ref="form" :model="form" :rules="rule" label-width="108px">
      <el-form-item label="" prop="">
        <div class="remark-text">为了账户安全考虑，需要设置安全码！</div>
      </el-form-item>
      <el-form-item label="新安全码" prop="safeCode">
        <el-input type="password" show-password v-model="form.safeCode" placeholder="请输入新安全码"></el-input>
      </el-form-item>
      <el-form-item label="谷歌验证码" prop="googleSecret">
        <el-input maxlength="6" v-model="form.googleSecret" placeholder="请输入谷歌验证码"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button v-if="isClose" @click="isVisible = false">关闭</el-button>
      <el-button v-else @click="logout">退出系统</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { setSafeCode } from '@/api/merchant'

export default {
  name: "updateSafe",
  data() {
    return {
      isVisible: false,
      submitLoading: false,
      form: {
        safeCode: '',
        googleSecret: '',
      },
      rule: {
        safeCode: [
          { required: true, message: '请输入新安全码', trigger: 'blur' },
          { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, message: '密码必须包含大小写字母和数字，且长度不少于6位', trigger: ['change', 'blur'] }
        ],
        googleSecret: [
          { required: true, message: '请输入谷歌验证码', trigger: 'blur' }
        ]
      },
      isClose: true
    }
  },
  methods: {
    openDialog(isClose) {
      this.isClose = isClose
      this.isVisible = true
    },
    closeDialog() {
      this.$refs.form.resetFields()
      this.isVisible = false
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          setSafeCode(this.form).then(res => {
            this.submitLoading = false
            this.closeDialog()
            this.$message.success('修改成功')
            this.$emit('success')
          }).catch(() => {
            this.submitLoading = false
          })
        }
      })
    },
    logout() {
      this.$store.dispatch('LogOut').then(() => {
        this.$store.dispatch('tagsView/delAllViews')
        this.showGoogle = false
        this.$router.replace({ path: `/login?xhssf=${this.$store.getters.uuid}` })
      })
    }
  }
}
</script>