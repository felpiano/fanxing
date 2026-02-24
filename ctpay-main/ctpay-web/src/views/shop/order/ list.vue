<!--
** 商户--交易记录
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
          总金额:<b class="text-red">￥0.00</b>元
          完成总金额:<b class="text-red">￥0.00</b>元
          成功率:<b class="text-red">0%</b>
          总手续费:<b class="text-red">0.00</b>元
          总收入:<b class="text-red">0.00</b>元
        </div>
      </template>
      <template v-slot:orderAmount="{ row }">
        <p class="text-o text-center">{{ row.orderAmount }}</p>
      </template>
      <template v-slot:orderStatus="{ row }">
        <div class="flex-center">
          <el-tag  size="mini" v-if="row.orderStatus === 0" type="warning">待支付</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 1" type="success">支付成功</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 2" type="danger">订单超时</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 3" type="danger">已关闭</el-tag>
        </div>
      </template>
      <template v-slot:callbackStatus="{ row }">
        <div class="flex-center">
          <el-tag  size="mini" v-if="row.callbackStatus === 0" type="warning">等待回调</el-tag>
          <el-tag  size="mini" v-else-if="row.callbackStatus === 1" type="success">回调成功</el-tag>
          <el-tag  size="mini" v-else-if="row.callbackStatus === 2" type="danger">回调失败</el-tag>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:order:detail']" type="text" @click="handleDetail(row)">详情</el-button>
      </template>
    </km-table>

    <!--查看详情-->
    <detailOrder ref="detailOrder"></detailOrder>

  </div>
</template>

<script>
import {getInOrderDetailList} from '@/api/order'
import {getChannelListAll} from '@/api/channel'
import moment from 'moment'
import detailOrder from '@/views/order/components/detailOrder.vue'

export default {
  name: 'order',
  components: {
    detailOrder
  },
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'tradeNo',
          title: '平台单号',
          placeholder: '请输入平台单号',
          clearable: true
        },
        {
          type: 'input',
          model: 'shopOrderNo',
          title: '商户单号',
          placeholder: '请输入商户单号',
          clearable: true
        },
        {
          type: 'datetimerange',
          initialDate: [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
          modelName: ['startTime', 'endTime'],
          title: '时间选择',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        },
        {
          type: 'select',
          model: '',
          title: '支付方式',
          placeholder: '请选择支付方式',
          clearable: true,
          option: []
        },
        {
          type: 'select',
          model: 'orderStatus',
          title: '支付状态',
          placeholder: '全部',
          clearable: true,
          option: [
            { label: '等待支付', value: '0'},
            { label: '支付成功', value: '1'},
            { label: '关闭订单', value: '3'}
          ]
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          shopOrderNo: '',
          tradeNo: '',
          channelName: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          {
            value: 'tradeNo',
            label: '平台单号',
            width: 200
          },

          {
            value: 'shopOrderNo',
            label: '商户单号',
            width: 200
          },
          {
            value: 'orderAmount',
            label: '金额',
            align: 'center',
            width: 120
          },
          {
            value: '',
            label: '收入',
            width: 160
          },
          {
            value: '',
            label: '手续费',
            width: 160
          },
          {
            value: '',
            label: '支付渠道',
            width: 120
          },
          {
            value: 'orderStatus',
            slot: 'orderStatus',
            label: '订单状态',
            width: 120
          },
          {
            value: '补单orderRemark',
            label: '备注',
            width: 140
          },
          {
            value: 'orderTime',
            label: '创建时间',
            width: 160
          },
          {
            value: 'finishTime',
            label: '更新时间',
            width: 160
          },
        ],
        hasPagination: true,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      },
      repairOrder: {}
    }
  },
  mounted() {
    this.getChannelList()
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getTableData()
    },
    getTableData() {
      this.tableConfig.loading = true
      getInOrderDetailList({
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
     // 获取通道
     getChannelList() {
      getChannelListAll({}).then(res => {
        let channelList = res.data || []
        this.$set(this.searchData[5], 'option', channelList.map(item => {
          return {
            label: item.channelName,
            value: item.channelCode
          }
        }))
      })
    },
    // 查看详情
    handleDetail(row) {
      this.$refs.detailOrder.openDialog(row)
    },
    // 导出
    exportList() {
      this.download('orderMainEntity/export', {
        ...this.tableConfig.searchQuery
      }, `订单数据_${moment().format('YYYY-MM-DD HHmmss')}.xlsx`)
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        shopOrderNo: '',
        tradeNo: '',
        channelName: '',
        startTime: '',
        endTime: ''
      }
      this.searchData[this.searchData.length - 1].initialDate = []
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
