

<template>
  <el-dialog :title="title" :visible.sync="isVisible" :width="'1280px'" @close="closeDialog">
    <div style="height: 600px">
      <search-bar
        :searchData="searchData"
        :query="tableConfig.searchQuery"
        @on-search-change="doSearch"
        @on-reset="resetTable">
      </search-bar>
      <km-table :table="tableConfig" @tableChangeCurrent="tableChangeCurrent" @tableChangeSize="tableChangeSize" style="height:500px">
        <template v-slot:status="{ row }">
          <el-tag size="mini" v-if="row.status === 0" type="success">开启</el-tag>
          <el-tag size="mini" v-else type="danger">关闭</el-tag>
        </template>
        <template v-slot:operation="{ row }">
          <el-button
            v-hasPermi="['system:merchantQrcode:delete']"
            type="danger"
            class="min-button"
            @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </km-table>
    </div>
  </el-dialog>
</template>

<script>
import { getMerchantQrcodeList, deleteMerchantQrcode } from '@/api/merchantQrcode'
import { getChannelListAll } from '@/api/channel'

export default {
  name: 'ChannelList',
  data() {
    return {
      isVisible: false,
      title: '',
      searchData: [
        {
          type: 'select',
          model: 'channelId',
          title: '通道',
          placeholder: '全部',
          clearable: true,
          option: []
        },
        {
          type: 'input',
          model: 'accountNumber',
          title: '收款码账号',
          placeholder: '请输入收款码账号',
          clearable: true
        },
        {
          type: 'input',
          model: 'nickName',
          title: '收款码昵称',
          placeholder: '请输入收款码昵称',
          clearable: true
        },
        {
          type: 'select',
          model: 'status',
          title: '接单状态',
          placeholder: '全部',
          clearable: true,
          option: [
            {
              label: '启用',
              value: 0
            },
            {
              label: '禁用',
              value: 1
            }
          ]
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          merchantId: '',
          channelId: '',
          accountNumber: '',
          nickName: '',
          status: ''
        },
        data: [],
        columns: [
          {
            value: 'id',
            label: '编号',
            width: 80
          },
          {
            value: 'channelName',
            label: '通道名称',
            width: 140
          },
          {
            value: 'accountNumber',
            label: '收款码账号',
            width: 120
          },
          {
            value: 'nickName',
            label: '收款码昵称',
            width: 120
          },
          {
            value: 'stauts',
            slot: 'status',
            align: 'center',
            label: '进单状态',
            width: 100
          },
          {
            value: 'dayLimit',
            label: '每日限额',
            align: 'center',
            width: 100
          },
          {
            value: 'maxAmount',
            label: '最大金额',
            align: 'center',
            width: 100
          },
          {
            value: 'minAmount',
            label: '最小金额',
            align: 'center',
            width: 100
          },
          {
            value: 'successCount',
            label: '成功总数（今）',
            align: 'center',
            width: 120,
            formatter: row => row.successCount || '--'
          },
          {
            value: 'successAmount',
            label: '成功金额（今）',
            align: 'center',
            width: 120,
            formatter: row => row.successAmount || '--'
          },
          {
            value: 'yesSuccessCount',
            label: '成功总数（昨）',
            align: 'center',
            width: 120
          },
          {
            value: 'yesSuccessAmount',
            label: '成功金额（昨）',
            align: 'center',
            width: 120
          },
          {
            value: 'createTime',
            label: '添加时间',
            width: 160
          },
          {
            slot: 'operation',
            label: '操作',
            align: 'center',
            fixed: 'right',
            width: 80
          }
        ],
        hasPagination: true,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      }
    }
  },
  methods: {
    openDialog(row) {
      this.tableConfig.searchQuery.merchantId = row.userId
      this.getChannelList()
      this.doSearch()
      this.isVisible = true
    },
    doSearch(){
      this.tableConfig.pagination.pageNum = 1
      this.getTableData()
    },
    getTableData() {
      this.tableConfig.loading = true
      getMerchantQrcodeList({
        ...this.tableConfig.searchQuery,
        pageNum: this.tableConfig.pagination.pageNum,
        pageSize: this.tableConfig.pagination.pageSize
      }).then(res => {
        this.tableConfig.data = res.data.records
        this.tableConfig.pagination.total = res.data.total
        this.tableConfig.loading = false
      }).catch(() => {
        this.tableConfig.loading = false
      })
    },
    getChannelList() {
      getChannelListAll({}).then(res => {
        const list = res.data || {}
        this.channelList = list
        this.$set(this.searchData[0], 'option', list.map(item => {
          return {
            label: item.channelName,
            value: item.id
          }
        }))
      })
    },
    // 删除
    handleDelete(row) {
      this.$confirm(`是否确认删除编号【${row.id}】通道码?`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMerchantQrcode({id: row.id}).then(() => {
          this.$message.success('删除成功')
          this.getTableData()
        })
      })
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        channelId: '',
        accountNumber: '',
        nickName: '',
        status: ''
      }
      this.doSearch()
    },
    closeDialog() {
      this.resetTable()
      this.isVisible = false
    },
    // 分页
    tableChangeCurrent(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageNum: e.current
      })
      this.getTableData()
    },
    tableChangeSize(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageSize: e.pageSize
      })
      this.getTableData()
    }
  }
}
</script>

<style scoped lang="scss">

</style>
