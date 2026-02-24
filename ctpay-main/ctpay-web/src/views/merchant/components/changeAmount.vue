<!--
** 修改码商余额或押金
-->
<template>
  <el-dialog :title="title" :visible.sync="isVisible" width="36%" @close="closeDialog">
      <el-form ref="form" label-width="120px" :model="form" :rules="rules">
        <el-form-item label="码商名称" prop="userName">
          <el-input v-model="form.userName" disabled></el-input>
        </el-form-item>
        <el-form-item label="现有余额" prop="balance" v-if="form.amountType === 1">
          <el-input v-model="form.balance" disabled></el-input>
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
        <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
      </div>
    </el-dialog>
</template>

<script>
import {updateAmount} from '@/api/merchant'

export default {
  name: "changeAmount",
  data() {
    return {
      isVisible: false,
      title: '',
      loading: false,
      form: {
        amountType: '',
        changeAmount: '',
        remark: ''
      },
      rules: {
        userName: [
          { required: true, message: '请输入码商名称', trigger: 'blur' }
        ],
        balance: [
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
    openDialog(row, type) {
      this.title = type === 'totalBalance' ? `修改余额` : '划转余额'
      this.form = {
        userName: row.userName,
        userId: row.userId,
        amountType: 1,
        balance: row[type],
        changeAmount: '',
        remark: '',
        code: ''
      }
      this.isVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true
        const form = this.$lodash.cloneDeep(this.form)
        delete form.balance
        updateAmount(form).then(res => {
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
