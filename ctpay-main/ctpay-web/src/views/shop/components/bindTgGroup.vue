<!--
** 绑定tg群组
-->
<template>
  <el-dialog title="绑定tg群组" :visible.sync="bindTgGroupVisible" width="30%" @close="closeDialog">
    <el-form ref="bindTgGroupForm" :model="bindTgGroupForm" :rules="bindTgGroupRules" label-width="100px">
      <el-form-item label="tg群组名称" prop="groupName">
        <el-input v-model="bindTgGroupForm.groupName" placeholder="请输入tg群组名称"></el-input>
      </el-form-item>
      <el-form-item label="tg群组ID" prop="groupId">
        <el-input v-model="bindTgGroupForm.groupId" placeholder="请输入tg群组ID"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="danger" @click="unBindTgGroup" :loading="loading" v-if="isUnbind">解 绑</el-button>
      <el-button @click="closeDialog">取 消</el-button>
      <el-button type="primary" @click="bindTgGroup" :loading="loading">确 定</el-button>
    </div>
  </el-dialog>
</template>


<script>
import {bindTelegramGroup, unBindTelegramGroup} from '@/api/shop'
import { unBindMTelegramGroup, updateMerchantEntity } from '@/api/merchant'

export default {
  data() {
    return {
      loading: false,
      bindTgGroupVisible: false,
      bindTgGroupForm: {
        userName: '',
        groupName: '',
        groupId: ''
      },
      bindTgGroupRules: {
        groupName: [
          { required: true, message: '请输入群组名称', trigger: 'blur' }
        ],
        groupId: [
          { required: true, message: '请输入群组ID', trigger: 'blur' }
        ]
      },
      type: 'shop',
      isUnbind: false
    }
  },
  methods: {
    open(row, type) {
      this.bindTgGroupForm = {
        userId: row.userId,
        groupName: row.telegramGroup && row.telegramGroup.split('/')[1],
        groupId: row.telegramGroup && row.telegramGroup.split('/')[0]
      }
      this.isUnbind = row.telegramGroup && row.telegramGroup !== ''
      this.type = type
      this.bindTgGroupVisible = true
    },
    bindTgGroup() {
      this.$refs.bindTgGroupForm.validate(valid => {
        if (valid) {
          this.loading = true
          let apiUrl = ''
          if (this.type === 'shop') {
            apiUrl = bindTelegramGroup
          } else {
            apiUrl = updateMerchantEntity
          }
          apiUrl({
            userId: this.bindTgGroupForm.userId,
            telegramGroup: this.bindTgGroupForm.groupId + '/' + this.bindTgGroupForm.groupName,
          }).then(res => {
            this.loading = false
            this.$message.success('绑定成功')
            this.closeDialog()
            this.$emit('updateList')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    },
    unBindTgGroup() {
      this.$confirm(`确认解绑tg群组吗？`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then(res => {
        let apiUrl = ''
        if (this.type === 'shop') {
          apiUrl = unBindTelegramGroup
        } else {
          apiUrl = unBindMTelegramGroup
        }
        apiUrl({userId: this.bindTgGroupForm.userId}).then(res => {
          this.$message.success('解绑成功')
          this.$emit('updateList')
          this.closeDialog()
        })
      })
    },
    closeDialog() {
      this.bindTgGroupVisible = false
      this.$refs.bindTgGroupForm.resetFields()
    }
  }
}
</script>
