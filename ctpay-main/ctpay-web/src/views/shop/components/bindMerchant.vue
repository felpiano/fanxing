<!--
**  指定码商
-->
<template>
  <el-dialog title="绑定码商列表" :visible.sync="isVisible" :width="'520px'">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      :is-reset="false">
    </search-bar>
    <km-table :table="tableConfig" style="height:460px" border>
      <template v-slot:status="{ row }">
        <el-switch
          :width="50"
          v-model="row.status"
          :active-value="0"
          :inactive-value="1"
          :active-text="'正常'"
          :inactive-text="'禁用'"
          @change="changeStatus(row)">
        </el-switch>
      </template>
    </km-table>
  </el-dialog>
</template>

<script>
import { shopMerchantRelationList, shopMerchantRelationUpdateStatus } from '@/api/shop'
export default {
  name: 'bindMerchant',
  data() {
    return {
      isVisible: false,
      searchData: [
        {
          type: 'input',
          model: 'merchantName',
          title: '码商名称',
          placeholder: '请输入码商名称',
          clearable: true
        },
      ],
      tableConfig: {
        loading: false,
        data: [],
        searchQuery: {
          shopId: '',
          merchantName: ''
        },
        columns: [
          { label: "id", value: "id", width: 80 },
          { label: "码商名称", value: "merchantName", width: 80 },
          { label: "状态", value: "status", slot: "status", align: 'center', width: 100 }
        ],
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      }
    }
  },
  mounted() {

  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getList()
    },
    open(item) {
      this.tableConfig.data = []
      this.isVisible = true
      this.tableConfig.searchQuery.shopId = item.userId
      this.tableConfig.pagination.pageNum = 1
      this.getList()
    },
    getList(userId) {
      this.tableConfig.loading = true
      shopMerchantRelationList({
        ...this.tableConfig.searchQuery,
        pageNum: this.tableConfig.pagination.pageNum,
        pageSize: this.tableConfig.pagination.pageSize
      }).then(res => {
        this.tableConfig.data = res.data.records || []
        this.tableConfig.pagination.total = res.data.total || 0
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    changeStatus(row) {
      this.tableConfig.loading = true
      shopMerchantRelationUpdateStatus({
        id: row.id,
        status: row.status,
      }).then(res => {
        this.$message.success('操作成功')
      }).finally(() => {
        this.tableConfig.loading = false
      })
    }
  }
}
</script>

<style scoped lang="scss"></style>
