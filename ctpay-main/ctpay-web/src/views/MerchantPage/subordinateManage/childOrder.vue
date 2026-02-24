<!--
** 下级订单列表
-->

<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table
      :table="tableConfig"
      @tableChangeCurrent="tableChangeCurrent"
      @tableChangeSize="tableChangeSize">
      <template v-slot:merchantName="{ row }">
        <div class="flex">
          <p class="mr10 ellipsis" style="width: 180px">{{ row.merchantName }}</p>
          <el-button v-hasPermi="['system:merchant:allParent']" type="text" size="mini" @click="lookAllParent(row)">查看上级码商</el-button>
        </div>
      </template>
      <template v-slot:orderAmount="{ row }">
        <p class="text-o text-center">{{ row.orderAmount }}</p>
      </template>
      <template v-slot:orderStatus="{ row }">
        <div class="text-center">
          <el-tag  size="mini" v-if="row.orderStatus === 0" type="warning">待支付</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 1" type="success">支付成功</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 2" type="danger">订单超时</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 3" type="danger">已关闭</el-tag>
        </div>
      </template>
      <template v-slot:callbackStatus="{ row }">
        <div class="text-center">
          <el-tag  size="mini" v-if="row.callbackStatus === 0" type="warning">等待回调</el-tag>
          <el-tag  size="mini" v-else-if="row.callbackStatus === 1" type="success">回调成功</el-tag>
          <el-tag  size="mini" v-else-if="row.callbackStatus === 2" type="danger">回调失败</el-tag>
        </div>
      </template>
    </km-table>

    <!--所有上级码商-->
    <all-parent ref="allParentRef"></all-parent>
  </div>
</template>

<script>
import moment from 'moment'
import { getChannelListAll } from '@/api/channel'
import { getInOrderDetailList } from '@/api/order'
import allParent from '@/views/order/components/allParent.vue'

export default {
  components: { allParent },
  name: 'subOrderList',
  data() {
    return {
      searchData: [
        {
          type: 'select',
          model: 'channelId',
          title: '通道',
          placeholder: '请选择通道',
          clearable: true,
          option: []
        },
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
          type: 'input',
          model: 'merchantName',
          title: '码商名称',
          placeholder: '请输入码商名称',
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
          model: 'orderStatus',
          title: '订单状态',
          placeholder: '全部',
          clearable: true,
          option: [
            { label: '待支付', value: '0'},
            { label: '支付成功', value: '1'},
            { label: '订单超时', value: '2'},
            { label: '已关闭', value: '3'}
          ]
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          channelId: '',
          shopOrderNo: '',
          tradeNo: '',
          merchantName: '',
          orderStatus: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          {
            value: 'merchantName',
            slot: 'merchantName',
            label: '码商名称',
            width: 180
          },
          {
            value: 'channelName',
            label: '通道名称',
            width: 120
          },
          {
            value: 'tradeNo',
            label: '平台单号',
            width: 200
          },
          {
            value: 'shopOrderNo',
            label: '商户单号',
            width: 180
          },
          {
            value: 'orderAmount',
            label: '交易金额',
            align: 'center',
            width: 120
          },
          {
            value: 'fixedAmount',
            label: '支付金额',
            align: 'center',
            width: 100
          },
          {
            value: 'fixedAmount',
            label: '支付金额',
            align: 'center',
            width: 100
          },
          {
            value: 'myFee',
            label: '我的佣金',
            align: 'center',
            width: 100
          },
          {
            value: 'childFee',
            label: '下级佣金',
            align: 'center',
            width: 100
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
          {
            value: 'orderStatus',
            slot: 'orderStatus',
            label: '订单状态',
            align: 'center',
            width: 100
          },
          {
            value: 'callbackStatus',
            slot: 'callbackStatus',
            label: '回调状态',
            align: 'center',
            width: 100
          },
          {
            value: 'orderRemark',
            label: '备注',
            width: 180
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
  mounted() {
    this.doSearch()
    this.getChannelList()
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
    getChannelList() {
      getChannelListAll({}).then(res => {
        const list = res.data || {}
        this.$set(this.searchData[0], 'option', list.map(item => {
          const data = {
            label: item.channelName,
            value: item.id
          }
          return data
        }))
      })
    },
    // 查看上级码商
    lookAllParent(row) {
      this.$refs.allParentRef.openDialog(row)
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        channelId: '',
        shopOrderNo: '',
        tradeNo: '',
        merchantName: '',
        orderStatus: '',
        startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
      }
      this.searchData[this.searchData.length - 1].initialDate = [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')]
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