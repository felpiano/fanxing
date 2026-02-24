<!--
** 代理身份查看--夜间卡卡大额
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table :table="tableConfig" @tableChangeCurrent="tableChangeCurrent" @tableChangeSize="tableChangeSize" :toHeight="90">
      <template v-slot:table-after>
        <div class="flex-center table-overview">
          <p>账号总数：<b class="text-bg-red">{{ overviewData.totalCount || 0}}</b> 个</p>
          <p>账号开启总数：<b class="text-bg-green">{{ overviewData.openCount || 0}}</b> 个</p>
          <p>可接单码商总数：<b class="text-bg-black">{{ overviewData.acceptCount || 0}}</b> 个</p>
          <p>可进单二维码总数：<b class="text-bg-green">{{ overviewData.useCount || 0 }}</b> 个</p>
        </div>
      </template>
      <template v-slot:enterOrderStatus="{ row }">
        <el-tag size="mini" v-if="row.enterOrderStatus === 0" type="success">开启</el-tag>
        <el-tag size="mini" v-else type="danger">关闭</el-tag>
      </template>
      <!-- <template v-slot:operation="{ row }">
        <el-button type="danger" class="min-button" @click="handleDelete(row)">删除</el-button>
      </template> -->
    </km-table>

  </div>
</template>

<script>
import { getAgentQrcodeOrder, getAgentQrReport } from '@/api/report'
import moment from "moment/moment";

export default {
  name: 'UnionPayQrcode',
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'accountNumber',
          title: '银行卡账号',
          placeholder: '请输入银行卡账号',
          clearable: true,
          width: '180px',
        },
        {
          type: 'input',
          model: 'nickName',
          title: '银行卡姓名',
          placeholder: '请输入银行卡姓名',
          clearable: true,
          width: '175px',
        },
        {
          type: 'input',
          model: 'merchantName',
          title: '码商名称',
          placeholder: '请输入码商名称',
          clearable: true,
          width: '170px',
        },
        {
          type: 'select',
          model: 'enterOrderStatus',
          title: '状态',
          placeholder: '全部',
          clearable: true,
          width: '100px',
          option: [
            { label: '开启', value: '0'},
            { label: '关闭', value: '1'},
          ]
        },
        {
          type: 'select',
          model: 'delFlag',
          title: '删除状态',
          placeholder: '全部',
          clearable: true,
          width: '100px',
          option: [
            { label: '正常', value: '0'},
            { label: '删除', value: '1'},
          ]
        },
        {
          type: 'daterange',
          initialDate: [moment().startOf('day').format('YYYY-MM-DD'), moment().endOf('day').format('YYYY-MM-DD')],
          modelName: ['startDate', 'endDate'],
          title: '时间选择',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          channelId: '1115',
          accountNumber: '',
          merchantName: '',
          enterOrderStatus: '',
          delFlag: '',
          startDate: moment().startOf('day').format('YYYY-MM-DD'),
          endDate: moment().endOf('day').format('YYYY-MM-DD')
        },
        data: [],
        columns: [
          {
            value: 'channelName',
            label: '通道名称',
            width: 140
          },
          {
            value: 'merchantName',
            label: '码商名称',
            width: 120
          },
          {
            value: 'accountNumber',
            label: '银行卡账号',
            width: 180
          },
          {
            value: 'nickName',
            label: '银行卡姓名',
            width: 120
          },
          {
            value: 'accountRemark',
            label: '所属银行',
            width: 140
          },
          {
            value: 'dayLimit',
            label: '每日限额',
            align: 'center',
            width: 120
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
            value: 'totalCount',
            label: '总条数',
            align: 'center',
            width: 100,
            formatter: row => row.totalCount || '--'
          },
          {
            value: 'successCount',
            label: '成功总数',
            align: 'center',
            width: 100,
            formatter: row => row.successCount || '--'
          },
          {
            value: 'totalAmount',
            label: '总金额',
            align: 'center',
            width: 120,
            formatter: row => row.totalAmount || '--'
          },
          {
            value: 'successAmount',
            label: '成功金额',
            align: 'center',
            width: 120,
            formatter: row => row.successAmount || '--'
          },
          {
            value: 'successRate',
            label: '成功率',
            align: 'center',
            width: 80,
            formatter: row => row.successRate * 100 + '%' || '--'
          },
          {
            value: 'enterOrderStatus',
            slot: 'enterOrderStatus',
            align: 'center',
            label: '进单状态',
            width: 80
          },
          {
            value: 'createTime',
            label: '添加时间',
            width: 160
          }
        ],
        hasPagination: true,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      },
      repairOrder: {},
      overviewData: {
        totalCount: 0,
        openCount: 0,
        acceptCount: 0,
        useCount: 0
      }
    }
  },
  mounted() {
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getTableData()
      this.getOrderTotal()
    },
    getTableData() {
      this.tableConfig.loading = true
      getAgentQrcodeOrder({
        ...this.tableConfig.searchQuery,
        pageNum: this.tableConfig.pagination.pageNum,
        pageSize: this.tableConfig.pagination.pageSize
      }).then(res => {
        this.tableConfig.data = res.data?.records || []
        this.tableConfig.pagination.total = res.data?.total || 0
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    // 统计
    getOrderTotal() {
      getAgentQrReport({ ...this.tableConfig.searchQuery}).then(res => {
        this.overviewData = res.data || {}
      })
    },
    // 删除
    handleDelete(row) {
      this.$confirm('是否确认删除该收款码数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {

      })
    },
    // 查看二维码
    handleClick(row) {
      this.$refs.imagePreview.show(row.qrcodeUrl)
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        channelId: '1115',
        accountNumber: '',
        enterOrderStatus: '',
        delFlag: '',
        merchantName: '',
        startDate: moment().startOf('day').format('YYYY-MM-DD'),
        endDate: moment().endOf('day').format('YYYY-MM-DD')
      }
      this.searchData[this.searchData.length - 1].initialDate = [moment().startOf('day').format('YYYY-MM-DD'), moment().endOf('day').format('YYYY-MM-DD')]
      this.doSearch()
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

<style lang="scss" scoped>
.table-overview {
  padding: 10px 0;
  background-color: #f5f7fa;
  p {
    padding-left: 6px
  }
}
</style>
