<!--
**  谷歌验证码
** 只能点击保存接口，并且成功后才能关掉弹框
-->

<template>
  <el-dialog
    title="请先绑定谷歌验证码"
    :visible.sync="showGoogle"
    width="30%"
    center
    :top="'10%'"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :custom-class="'dialog-width'">
    <el-form :model="googleForm" ref="googleForm" :rules="googleRules" label-width="110px">
      <canvas id="scretCode" style="display: block;margin: auto"></canvas>
      <el-form-item label="谷歌秘钥：" prop="googleScret">
        <div class="flex-center">
          <p class="mr10">{{ googleForm.googleScret }}</p>
          <el-button type="primary" class="min-button" @click="handleCopy">复制</el-button>
        </div>
      </el-form-item>
      <el-form-item label="谷歌验证码：" prop="googleCode">
        <el-input maxlength="6" v-model="googleForm.googleCode" clearable placeholder="请输入谷歌验证码"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="logout">退出系统</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">绑定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {bindGoogle, getGoogleSecret} from '@/api/common';
import useClipboard from "vue-clipboard3"
import QRCode from 'qrcode'

export default {
  data() {
    return {
      loading: false,
      showGoogle: false,
      googleForm: {
        googleScret: '',
        googleScretCode: '',
        googleCode: ''
      },
      googleRules: {
        googleCode: [
          { required: true, message: '请输入谷歌验证码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    init() {
      this.showGoogle = true
      getGoogleSecret({}).then(res => {
        this.googleForm = res.data || {}
        const account = this.$store.getters.name
        const secret = res.data.googleScret
        const issuer = ''
        const otpauth = `otpauth://totp/${encodeURIComponent(issuer)}:${encodeURIComponent(account)}?secret=${secret}&issuer=${encodeURIComponent(issuer)}`
        QRCode.toCanvas(document.getElementById("scretCode"), otpauth, {
          width: 220
        })
      });
    },
    handleSubmit() {
      this.$refs.googleForm.validate(valid => {
        if (valid) {
          this.loading = true
          bindGoogle({
            googleSecret: this.googleForm.googleScret,
            code: this.googleForm.googleCode
          }).then(res => {
            this.$message.success('绑定成功')
            this.showGoogle = false
            this.$emit('success')
          }).finally(() => {
            this.loading = false
          });
        }
      })
    },
    // 复制
    handleCopy() {
      const { toClipboard } = useClipboard()
      toClipboard(this.googleForm.googleScret)
      this.$message.success('复制成功')
    },
    logout() {
      this.$store.dispatch('LogOut').then(() => {
        localStorage.clear()
        this.$store.dispatch('tagsView/delAllViews')
        this.showGoogle = false
        this.$router.replace({ path: `/login?xhssf=${this.$store.getters.uuid}` })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.scret-code {
  display: block;
  width: 200px;
  height: 200px;
  margin: auto
}
</style>
