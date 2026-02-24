<!--
** 上级码商
-->
<template>
  <el-dialog :title="`${tableConfig.merchantName}的上级码商`" :visible.sync="isVisible" width="32%">
    <km-table :table="tableConfig" style="height:460px" :isSearch="false" border>
    </km-table>
  </el-dialog>
</template>

<script>
import { getAllParent } from '@/api/merchant'
export default {
  name: 'AllParent',
  data() {
    return {
      isVisible: false,
      tableConfig: {
        loading: false,
        searchQuery: {},
        data: [],
        merchantName: '',
        columns: [
          { label: '码商ID', value: 'userId', width: 80 },
          { label: '码商名称', value: 'userName'},
          {
            label: '码商层级',
            value: 'merchantLevel',
            width: 100,
            formatter: row => row.merchantLevel ? (Number(row.merchantLevel) - 1) : '--'
          }
        ]
      }
    }
  },
  methods: {
    openDialog(row) {
      this.isVisible = true
      this.tableConfig.merchantName = row.merchantName
      this.getList(row)
    },
    getList(row) {
      this.tableConfig.loading = true
      getAllParent({
        merchantId: row.merchantId
      }).then(res => {
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    }
  }
}
</script>
