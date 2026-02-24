<!--
** 中额额金条
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
          当前订单总金额：<b class="text-bg-black">￥{{ overviewData.totalAmount || 0}}</b> 元
          成交总金额：<b class="text-bg-green">￥{{ overviewData.successAmount || 0}}</b> 元
          当前订单总数：<b class="text-bg-black">{{ overviewData.totalCount || 0}}</b> 单
          成交订单总数：<b class="text-bg-green">{{ overviewData.successCount || 0 }}</b> 单
          成功率：<b class="text-bg-blue">{{ overviewData.successRate }}</b> %
        </div>
      </template>
      <template v-slot:qrcodeType="{ row }">
         <p class="ellipsis" v-if="row.qrcodeType === 0"> {{ row.qrcodeValue }}</p>
         <p class="ellipsis" v-else-if="row.qrcodeType === 2">无码</p>
         <ImagePreview v-else :src="row.qrcodeUrl" :width="50" :height="50"></ImagePreview>
      </template>
      <template v-slot:unionCode="{ row }">
        <p class="text-o text-center">{{ row.unionCode }}</p>
      </template>
      <template v-slot:orderStatus="{ row }">
        <div class="text-center">
          <el-tag  size="mini" v-if="row.orderStatus === 0" type="warning">待支付</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 1" type="success">支付成功</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 2" type="danger">订单超时</el-tag>
          <el-tag  size="mini" v-else-if="row.orderStatus === 3" type="danger">已关闭</el-tag>
          <el-tag size="mini" v-else-if="row.orderStatus === 4" type="danger">待退回</el-tag>
          <el-tag size="mini" v-else-if="row.orderStatus === 5" type="danger">已退回</el-tag>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button type="primary" class="min-button" @click="handleDetail(row)">详情</el-button>
        <el-button v-hasPermi="['system:order:repair']" v-if="row.orderStatus !== 1" type="success" class="min-button" @click="handleRepair(row)">确认</el-button>
        <el-button v-hasPermi="['system:order:unPaid']" v-if="row.orderStatus === 0" type="danger" class="min-button" @click="handleUnPaid(row)">未收到</el-button>
      </template>
    </km-table>

    <!--查看详情-->
    <detailOrder ref="detailOrder"></detailOrder>

     <!--安全码-->
     <SafeCode ref="safeCode" @submit="handleRepairOrder"></SafeCode>

  </div>
</template>

<script>
import { getInOrderList, getInOrderTotal, orderUnPaid, repairInOrder } from '@/api/order'
import moment from 'moment'
import detailOrder from '@/views/order/components/detailOrder.vue'
import ImagePreview from '@/components/ImagePreview'
import { orderStatusOption } from '@/utils/dictView'

export default {
  name: 'goldBarMedium',
  components: {
    detailOrder,
    ImagePreview
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
          type: 'input',
          model: 'accountNumber',
          title: '收款码账号',
          placeholder: '请输入收款码账号',
          clearable: true
        },
        {
          type: 'input',
          model: 'accountRemark',
          title: '账号备注',
          placeholder: '请输入账号备注',
          clearable: true
        },
        {
          type: 'input',
          model: 'payer',
          title: '付款人姓名',
          placeholder: '请输入付款人姓名',
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
          option: orderStatusOption
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          channelId: '1120',
          shopOrderNo: '',
          tradeNo: '',
          accountNumber: '',
          accountRemark: '',
          payer: '',
          orderStatus: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          {
            value: 'channelName',
            label: '通道名称',
            width: 120
          },
          {
            value: 'nickName',
            label: '收款码名称',
            width: 100
          },
          {
            value: 'accountNumber',
            label: '收款码账号',
            width: 120
          },
          {
            value: 'accountNumber',
            label: '收款码账号',
            width: 120
          },
          {
            value: 'uid',
            label: '收款账号UID',
            width: 150
          },
          {
            value: 'accountRemark',
            label: '账号备注',
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
            width: 200
          },
          {
            value: 'payer',
            label: '付款人姓名',
            width: 120
          },
          {
            value: 'orderAmount',
            label: '订单金额',
            align: 'center',
            width: 120
          },
          {
            value: 'fixedAmount',
            label: '实际支付金额',
            align: 'center',
            width: 120
          },
          {
            value: 'qrcodeType',
            slot: 'qrcodeType',
            align: 'center',
            label: '收款二维码',
            width: 140
          },
          {
            value: 'unionCode',
            slot: 'unionCode',
            label: '口令',
            align: 'center',
            width: 80
          },
          {
            value: 'orderTime',
            label: '创建时间',
            align: 'center',
            width: 160
          },
          {
            value: 'finishTime',
            label: '支付时间',
            align: 'center',
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
            value: 'orderRemark',
            label: '订单备注',
            width: 140
          },
          {
            label: '操作',
            align: 'center',
            slot: 'operation',
            fixed: 'right',
            width: 140
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
        totalAmount: 0,
        successAmount: 0,
        totalNum: 0,
        successNum: 0,
        successRate: 0
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
      getInOrderList({
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
      getInOrderTotal({ ...this.tableConfig.searchQuery}).then(res => {
        this.overviewData = res.data || {}
        this.overviewData.successRate = this.overviewData.successRate ? (this.overviewData.successRate * 100).toFixed(2) : 0
      })
    },
    // 查看详情
    handleDetail(row) {
      this.$refs.detailOrder.openDialog(row)
    },
    // 确认
    handleRepair(row) {
      this.repairOrder = row
      this.repairOrder.type = 0
      this.$refs.safeCode.show(row)
    },
    handleRepairOrder(safeCode) {
      repairInOrder({
        id: this.repairOrder.id,
        safeCode: safeCode,
        type: this.repairOrder.type
      }).then(() => {
        this.$message.success('操作成功')
        this.doSearch()
      })
    },
    // 未收到
    handleUnPaid(row) {
      this.$prompt('请输入安全码', {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputValidator: (value) => {
          if (/<|>|"|'|\||\\/.test(value)) {
            return "不能包含非法字符：< > \" ' \\\ |"
          }
        },
      }).then(({ value }) => {
        orderUnPaid({
          id: row.id,
          safeCode: value
        }).then(res => {
          this.$message.success('操作成功')
          this.getTableData()
        })
      })
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
        channelId: '1120',
        shopOrderNo: '',
        tradeNo: '',
        accountNumber: '',
        accountRemark: '',
        payer: '',
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
