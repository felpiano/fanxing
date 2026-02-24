<!--
** 码商对应通道码详情
-->
<template>
  <el-dialog :title="`${row.merchantName}-通道码列表`" append-to-body :visible.sync="isVisible" width="80%">
    <div style="width: 100%;height: 568px">
      <search-bar
        :searchData="searchData"
        :query="tableConfig.searchQuery"
        @on-search-change="doSearch"
        @on-reset="resetTable">
      </search-bar>
      <km-table
        :table="tableConfig"
        @tableChangeCurrent="tableChangeCurrent"
        @tableChangeSize="tableChangeSize"
        style="height: 518px">
        <template v-slot:status="{ row }">
          <el-tag size="mini" v-if="row.status === 0" type="success">开启</el-tag>
          <el-tag size="mini" v-else type="danger">关闭</el-tag>
        </template>
      </km-table>
    </div>
  </el-dialog>
</template>

<script>
import { getMerchantQrcodeList } from '@/api/merchantQrcode'

export default {
  name: 'channelQrcode',
  data() {
    return {
      isVisible: false,
      row: {},
      searchData: [
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
            value: 'channelName',
            label: '通道名称',
            width: 120
          },
          {
            value: 'accountNumber',
            label: '收款码账号',
            width: 140
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
            width: 80
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
      this.row = row
      this.tableConfig.searchQuery.merchantId = row.merchantId
      this.tableConfig.searchQuery.channelId = row.channelId
      this.isVisible = true
      this.doSearch()
    },
    doSearch() {
      this.getList()
    },
    getList() {
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
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        userName: '',
        status: '',
        orderPermission: '',
        parentPath: ''
      }
      this.doSearch()
    },
    // 分页
    tableChangeCurrent(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageNum: e.current
      })
      this.getList()
    },
    tableChangeSize(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageSize: e.pageSize
      })
      this.getList()
    }
  }
}
</script>