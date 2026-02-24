<!--
** 码商佣金转余额
-->
<template>
  <el-dialog title="佣金转余额" :visible.sync="isVisible" width="36%" @close="closeDialog">
      <el-form ref="form" label-width="105px" :model="form" :rules="rules">
        <el-form-item label="码商名称" prop="userName">
          <el-input v-model="form.userName" disabled></el-input>
        </el-form-item>
        <el-form-item label="现有佣金" prop="freezeAmount">
          <el-input v-model="form.freezeAmount" disabled></el-input>
        </el-form-item>
        <el-form-item label="操作金额" prop="changeAmount">
          <el-input-number :controls="false" v-model="form.changeAmount" placeholder="请输入操作金额" class="w100"></el-input-number>
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
        <el-button type="primary" @click="handleSubmit" :loading="loading">确认转余额</el-button>
      </div>
    </el-dialog>
</template>

<script>
import { trimFreezeToBalance } from '@/api/merchant'

export default {
  name: "changeAmount",
  data() {
    return {
      isVisible: false,
      loading: false,
      form: {
        userName: '',
        userId: '',
        freezeAmount: '',
        changeAmount: '',
        remark: '',
        code: ''
      },
      rules: {
        userName: [
          { required: true, message: '请输入码商名称', trigger: 'blur' }
        ],
        freezeAmount: [
          { required: true, message: '请输入', trigger: 'blur' }
        ],
        changeAmount: [
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
        userId: row.userId,
        freezeAmount: row.freezeAmount,
        changeAmount: '',
        remark: '',
        code: ''
      }
      this.isVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.form.changeAmount <= 0) {
          this.$message.error('操作金额必须大于0')
          return
        }
        if (this.form.changeAmount > this.form.freezeAmount) {
          this.$message.error('操作金额不能大于现有佣金')
          return
        }
        this.loading = true
        const form = this.$lodash.cloneDeep(this.form)
        delete form.freezeAmount
        delete form.userName
        trimFreezeToBalance(form).then(res => {
          this.closeDialog()
          this.$emit('refresh')
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
