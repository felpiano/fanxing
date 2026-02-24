<!--
**编辑tg管理员
-->
<template>
  <el-dialog title="编辑tg管理员" :visible.sync="isVisible" width="500px">
    <km-table  :table="tableConfig" style="height: 480px">
      <template v-slot:table-after>
        <el-button type="primary" @click="addTgUser">添加tg管理员</el-button>
      </template>
      <template v-slot:userId="{ row }">
        <el-input v-model="row.userId" placeholder="请输入tg管理员ID"></el-input>
      </template>
      <template v-slot:userName="{ row }">
        <el-input v-model="row.userName" placeholder="请输入tg管理员名称"></el-input>
      </template>
      <template v-slot:operation="{ row }">
        <el-button type="danger" class="min-button" @click="deleteTgAdmin(row)">删除</el-button>
      </template>
    </km-table>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">取 消</el-button>
      <el-button type="primary" @click="editTgAdmin" :loading="submitLoading">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { updateShopEntity } from '@/api/shop'
import { updateMerchantEntity } from '@/api/merchant'

export default {
  data() {
      return {
        isVisible: false,
        tableConfig: {
          loading: false,
          data: [],
          columns: [
            { label: 'tg管理员ID', value: 'userId', slot: 'userId', width: 160 },
            { label: 'tg管理员名称', value: 'userName' , slot: 'userName', width: 160 },
            { label: '操作', slot: 'operation', align: 'center', width: 60 }
          ]
        },
        editForm: {},
        submitLoading: false,
        type: 'shop',
      }
  },
  methods: {
    open(row, type) {
      this.type = type
      this.editForm = this.$lodash.cloneDeep(row)
      let data = []
      if (row.telegram) {
        let telegramList = row.telegram.split(',')
        telegramList.map(item => {
          const userId = item.split('/')[0]
          const userName = item.split('/')[1]
          data.push({
            userId: userId,
            userName: userName
          })
        })
      }
      this.tableConfig.data = data
      this.isVisible = true
    },
    addTgUser() {
      this.tableConfig.data.push({
        userId: '',
        userName: ''
      })
    },
    deleteTgAdmin(row) {
      this.tableConfig.data = this.tableConfig.data.filter(item => item.userId !== row.userId)
    },
    editTgAdmin() {
      // 判断tableConfig.data中id和userName是否有为空的情况，有就提示
      let flag = false
      this.tableConfig.data.map(item => {
        if (!item.userId || !item.userName) {
          flag = true
        }
      })
      if (flag) {
        this.$message.error('tg管理员ID和名称不能为空')
        return
      }
      this.submitLoading = true
      // 拼接telegram
      let telegram = ''
      this.tableConfig.data.map(item => {
        telegram += item.userId + '/' + item.userName + ','
      })
      telegram = telegram.substring(0, telegram.length - 1)
      this.editForm.telegram = telegram
      let apiUrl = ''
      if (this.type === 'shop') {
        apiUrl = updateShopEntity
      } else {
        apiUrl = updateMerchantEntity
      }
      apiUrl(this.editForm).then(res => {
        this.$message.success('编辑成功')
        this.isVisible = false
        this.$emit('updateList')
      }).finally(() => {
        this.submitLoading = false
      })
    }
  }
}
</script>
