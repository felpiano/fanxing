<!--
** 安全码验证
-->

<template>
  <el-dialog title="敏感操作，请验证安全码" append-to-body
    :visible.sync="isVisible"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :before-close="handleClose"
    width="23%">
    <el-form ref="form" :model="form" :rules="rule" label-width="0px">
      <el-form-item prop="safeCode">
        <el-input v-model="form.safeCode" placeholder="请输入安全码" show-password></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="handleSubmit">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'SafeCode',
  props: {
  },
  data() {
    return {
      isVisible: false,
      form: {
        safeCode: ''
      },
      rule: {
        safeCode: [
          { required: false, message: '请输入安全码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    show() {
      this.form.safeCode = ''
      this.isVisible = true
    },
    handleClose() {
      this.isVisible = false
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.isVisible = false
        this.$emit('submit', this.form.safeCode)
      })
    }
  }
}
</script>

  