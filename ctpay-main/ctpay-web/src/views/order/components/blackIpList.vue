<!--
** 黑名单列表
-->
<template>
  <el-dialog title="黑名单列表" :visible.sync="isVisible" width="32%">
    <km-table :table="tableConfig" style="height:460px" :isSearch="false" border>
      <template v-slot:operation="{ row }">
        <el-button type="danger" size="mini" @click="handleDelete(row)">删除</el-button>
      </template>
    </km-table> 
  </el-dialog>
</template>

<script>
import { getBlackIpList, putBlackClientIp } from '@/api/agent'
export default {
  name: 'blackIpList',
  data() {
    return {
      isVisible: false,
      tableConfig: {
        loading: false,
        searchQuery: {},
        data: [],
        columns: [
          { label: 'IP', value: 'clientIp', width: 160 },
          { value: 'operation', label: '操作', slot: 'operation', align: 'center', width: 80 }
        ]
      }
    }
  },
  methods: { 
    openDialog() {
      this.isVisible = true
      this.getList()
    },
    getList() {
      this.tableConfig.loading = true
      getBlackIpList({}).then(res => {
        let list = res.data || []
        this.tableConfig.data = list.map(item => {
          return {
            clientIp: item
          }
        })
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    handleDelete(row) {
      this.$confirm(`确认把IP${row.clientIp}从黑名单中删除吗？`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        putBlackClientIp({
          clientIp: row.clientIp,
          type: 0
        }).then(res => {
          this.$message.success('删除成功')
          this.getList()
          this.$emit('refresh')
        })
      })
    }
  }
}
</script>