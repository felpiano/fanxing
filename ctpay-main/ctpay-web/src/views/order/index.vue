<!--
** 订单列表--交易管理
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table :table="tableConfig" @tableChangeCurrent="tableChangeCurrent" @tableChangeSize="tableChangeSize" :autoHeight="true" :row-class-name="tableRowClassName">
      <template v-slot:table-after>
        <div>
          <el-button type="primary" plain
            icon="el-icon-download"
            size="mini"
            @click="exportList"
          >导出</el-button>
          <div class="flex-between">
            <div class="flex-center">
              <div class="flex-center table-overview" style="width: auto">
                <p>成功金额：<b class="text-bg-green">￥{{ overviewData.totalAmount || 0}}</b> 元，</p>
                <p>成功笔数：<b class="text-bg-green">{{ overviewData.totalCount || 0}}</b>笔</p>
              </div>
              <div class="flex-center">
                <el-button style="margin: 0 0 0 15px" v-hasPermi="['system:agent:blackIpList']" size="mini" type="primary" @click="handleBlack">黑名单列表</el-button>
                <el-switch style="margin-left: 15px"
                  title="1分钟自动刷新"
                  :width="75"
                  v-model="autoRefresh"
                  active-value="1"
                  inactive-value="0"
                  active-text="自动刷新"
                  inactive-text="刷新关闭"
                  @change="changeRefresh">
                </el-switch>
              </div>
            </div>
            <div class="flex-center weight-type" v-if="$store.getters.identity === 3">
              <p>轮询方式: </p>
              <el-radio-group v-model="weightType" @change="changeWeightType">
                <el-radio label="0">二维码</el-radio>
                <el-radio label="1">码商通道</el-radio>
                <el-radio label="2">二维码权重</el-radio>
                <el-radio label="3">码商通道权重</el-radio>
              </el-radio-group>
            </div>
          </div>
        </div>
      </template>
      <template v-slot:merchantName="{ row }">
        <div class="flex">
          <p class="mr10 ellipsis" style="width: 160px">{{ row.merchantName }}</p>
          <el-button v-hasPermi="['system:merchant:allParent']" type="text" size="mini" @click="lookAllParent(row)">查看上级码商</el-button>
        </div>
      </template>
      <template v-slot:orderAmount="{ row }">
        <p class="text-o text-center">{{ row.orderAmount }}</p>
      </template>
      <template v-slot:fixedAmount="{ row }">
        <p class="text-o text-center">{{ row.fixedAmount }}</p>
      </template>
      <template v-slot:accountNumber="{ row }">
        <div v-if="[1113, 1114, 1115, 1116, 1118].indexOf(row.channelId) > -1" style="display: flex; align-items: center;font-size: 12px">
          <p>姓名：<span class="font-bold">{{ row.nickName }}</span></p>；
          <p style="padding-left: 6px">卡号：<span class="font-bold">{{ row.accountNumber }}</span></p>
          <p style="padding-left: 6px">银行：<span class="font-bold">{{ row.uid}}</span></p>
        </div>
        <div v-else style="display: flex; align-items: center;font-size: 12px">
          <p>收款人：<span class="font-bold">{{ row.nickName }}</span></p>；
          <p style="padding-left: 6px">收款账号：<span class="font-bold">{{ row.accountNumber }}</span></p>
        </div>
      </template>
      <template v-slot:unionCode="{ row }">
        <p class="text-o text-center">{{ row.unionCode }}</p>
      </template>
      <template v-slot:orderStatus="{ row }">
        <div class="text-center">
          <el-tag size="mini" v-if="row.orderStatus === 0" type="warning">待支付</el-tag>
          <el-tag size="mini" v-else-if="row.orderStatus === 1" type="success">支付成功</el-tag>
          <el-tag size="mini" v-else-if="row.orderStatus === 2" type="danger">订单超时</el-tag>
          <el-tag size="mini" v-else-if="row.orderStatus === 3" type="danger">已关闭</el-tag>
          <el-tag size="mini" v-else-if="row.orderStatus === 4" type="danger">待退回</el-tag>
          <el-tag size="mini" v-else-if="row.orderStatus === 5" type="danger">已退回</el-tag>
        </div>
      </template>
      <template v-slot:callbackStatus="{ row }">
        <div class="text-center">
          <el-tag size="mini" v-if="row.callbackStatus === 0" type="warning">等待回调</el-tag>
          <el-tag size="mini" v-else-if="row.callbackStatus === 1" type="success">回调成功</el-tag>
          <el-tag size="mini" v-else-if="row.callbackStatus === 2" type="danger">回调失败</el-tag>
        </div>
      </template>
      <template v-slot:receiptImg="{ row }">
        <p class="ellipsis" v-if="!row.receiptImg || row.receiptImg === ''">--</p>
        <ImagePreview v-else :src="row.receiptImg" :width="50" :height="50"></ImagePreview>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:order:detail']" type="primary" class="min-button" @click="handleDetail(row)">详情</el-button>
        <el-button v-if="row.showPusBlack === 0" v-hasPermi="['system:agent:putBlackClientIp']" type="danger" class="min-button" @click="handlePutBlack(row)">拉黑IP</el-button>
        <el-button v-if="row.orderStatus !== 1 && row.orderStatus !== 3" v-hasPermi="['system:order:repair']" type="success" class="min-button" @click="handleRepair(row)">确认收款</el-button>
        <el-button v-if="row.orderStatus === 0" v-hasPermi="['system:order:unPaid']" type="danger" class="min-button" @click="handleUnPaid(row)">未收到</el-button>
        <el-button v-if="row.orderStatus < 3" v-hasPermi="['system:order:czorder']" type="danger" class="min-button" @click="handleCzOrder(row)">冲正</el-button>
        <el-button v-if="judgment(row)"
          v-hasPermi="['system:order:backOrder']"
          type="success" class="min-button"
          @click="handleBackOrder(row)">处理未成交订单</el-button>
      </template>
    </km-table>

    <!--查看详情-->
    <detailOrder ref="detailOrder"></detailOrder>

    <!--确认收款-->
    <repair-order ref="repairOrderRef" @refresh="getTableData"></repair-order>

    <!--黑名单-->
    <black-ip-list ref="blackIpListRef" @refresh="getTableData"></black-ip-list>

    <!--所有上级码商-->
    <all-parent ref="allParentRef"></all-parent>

    <!--处理未成交订单-->
    <back-order ref="backOrderRef" @refresh="getTableData"></back-order>
  </div>
</template>

<script>
import { getInOrderDetailList, getAgentOrderTotal, agentOrderUnPaid, czOrder } from '@/api/order'
import {getChannelListAll} from '@/api/channel'
import { putBlackClientIp, updateAgentEntity, getAgentDetail } from '@/api/agent'
import moment from 'moment'
import detailOrder from './components/detailOrder.vue'
import RepairOrder from './components/repairOrder.vue'
import BlackIpList from './components/blackIpList.vue'
import AllParent from './components/allParent.vue'
import backOrder from './components/backOrder.vue'
import { orderStatusOption } from  '@/utils/dictView'

export default {
  name: 'order',
  components: {
    detailOrder,
    RepairOrder,
    BlackIpList,
    AllParent,
    backOrder
  },
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'shopName',
          title: '商户名称',
          placeholder: '请输入商户名称',
          clearable: true
        },
        {
          type: 'input',
          model: 'tradeNo',
          title: '平台单号',
          width: '220px',
          placeholder: '请输入平台单号',
          clearable: true
        },
        {
          type: 'input',
          model: 'shopOrderNo',
          title: '商户单号',
          width: '220px',
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
          model: 'channelId',
          title: '通道名称',
          placeholder: '请选择通道名称',
          clearable: true,
          option: []
        },
        {
          type: 'select',
          model: 'orderStatus',
          title: '订单状态',
          placeholder: '全部',
          clearable: true,
          option: orderStatusOption
        },
        {
          type: 'select',
          model: 'callbackStatus',
          title: '通知状态',
          placeholder: '全部',
          clearable: true,
          option: [
            { label: '等待回调', value: '0'},
            { label: '回调成功', value: '1'},
            { label: '回调失败', value: '2'}
          ]
        },
        {
          type: 'input',
          model: 'parentMerchantName',
          title: '零级码商名称',
          placeholder: '请输入零级码商名称',
          clearable: true
        },
        {
          type: 'input',
          model: 'memberIp',
          title: '会员IP',
          placeholder: '请输入会员IP',
          clearable: true
        },
        {
          type: 'input',
          model: 'recipient',
          title: '收款人',
          placeholder: '请输入收款人',
          clearable: true
        },
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          shopName: '',
          tradeNo: '',
          shopOrderNo: '',
          merchantName: '',
          channelId: '',
          orderStatus: '',
          callbackStatus: '',
          agentName: '',
          parentMerchantName: '',
          memberIp: '',
          recipient: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          {
            value: 'shopName',
            label: '所属商户',
            width: 120
          },
          {
            value: 'firstUserName',
            label: '零级码商',
            width: 120
          },
          {
            value: 'merchantName',
            slot: 'merchantName',
            label: '码商名称',
            width: 180
          },
          {
            value: 'merchantBalance',
            label: '码商余额(元)',
            align: 'center',
            width: 120
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
            width: 200
          },
          {
            value: 'payer',
            label: '支付用户',
            width: 120
          },
          {
            value: 'orderAmount',
            slot: 'orderAmount',
            label: '交易金额(元)',
            align: 'center',
            width: 100
          },
          {
            value: 'fixedAmount',
            slot: 'fixedAmount',
            label: '支付金额(元)',
            align: 'center',
            width: 100
          },
          {
            value: 'firstMerchantFee',
            label: '1级码商佣金(元)',
            align: 'center',
            width: 120
          },
          {
            value: 'orderStatus',
            slot: 'orderStatus',
            label: '订单状态',
            align: 'center',
            width: 90
          },
          {
            value: 'callbackStatus',
            slot: 'callbackStatus',
            label: '回调状态',
            align: 'center',
            width: 90
          },
          {
            value: 'accountNumber',
            slot: 'accountNumber',
            label: '收款信息',
            width: 180
          },
          {
            value: 'unionCode',
            label: '口令',
            slot: 'unionCode',
            align: 'center',
            width: 80
          },
          {
            value: 'receiptImg',
            slot: 'receiptImg',
            align: 'center',
            label: '回执单',
            width: 140
          },
          {
            value: 'clientIp',
            label: '访问信息',
            width: 120
          },
          {
            value: 'memberIp',
            label: '会员IP',
            width: 120
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
            value: 'orderRemark',
            label: '备注',
            width: 180
          },
          {
            label: '操作',
            align: 'center',
            slot: 'operation',
            fixed: 'right',
            width: 280
          }
        ],
        hasPagination: true,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      },
      autoRefresh: '0',
      timer: null,
      overviewData: {
        totalAmount: 0,
        totalCount: 0
      },
      weightType: '0',
      tempWeight: '0'
    }
  },
  mounted() {
    if(this.$store.getters.identity === 1) {
      this.searchData.push({
        type: 'input',
        model: 'agentName',
        title: '码商代理',
        placeholder: '请输入码商代理名称',
        clearable: true
      })
    }
    this.getChannelList()
    this.doSearch()
    this.autoRefresh = localStorage.getItem('autoRefresh') === null ? '0' : '1'
    if (this.autoRefresh == 1) {
      this.timer = setInterval(() => {
        this.doSearch()
      }, 1000 * (1 * 60))
    }
  },
  beforeDestroy() {
    clearInterval(this.timer)
    this.timer = null
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getTableData()
      if(this.$store.getters.identity === 3) {
        this.getAgentDetailInfo()
      }
    },
    getAgentDetailInfo() {
      getAgentDetail({}).then(res => {
        this.weightType = res.data.weightType + ''
        this.tempWeight = res.data.weightType + ''
      })
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
      this.getTotal()
    },
    getTotal() {
      getAgentOrderTotal({
        ...this.tableConfig.searchQuery
      }).then(res => {
        this.overviewData = res.data || {}
      })
    },
     // 获取通道
     getChannelList() {
      getChannelListAll({}).then(res => {
        let channelList = res.data || []
        this.$set(this.searchData[5], 'option', channelList.map(item => {
          return {
            label: item.channelName,
            value: item.id
          }
        }))
      })
    },
    // 判断按钮
    judgment(row) {
      // admin身份，订单状态为待支付，回调状态为等待回调，订单时间比现在时间晚20分钟
      return this.$store.getters.identity === 1 && row.orderStatus === 0 && row.callbackStatus === 0 && moment().diff(moment(row.orderTime), 'minutes') > 10
    },
    handleBackOrder(row) {
      this.$refs.backOrderRef.openDialog(row)
    },
    // 查看详情
    handleDetail(row) {
      this.$refs.detailOrder.openDialog(row)
    },
    // 拉黑IP
    handlePutBlack(row) {
      if (!row.clientIp || row.clientIp === '') {
        return this.$message.warning('此订单暂无访问IP')
      }
      this.$confirm(`是否确认拉黑【${row.clientIp}】IP?`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        putBlackClientIp({
          clientIp: row.clientIp,
          type: 1
        }).then(() => {
          this.$message.success('拉黑成功')
          this.getTableData()
        })
      })
    },
    handleRepair(row) {
      this.$refs.repairOrderRef.openDialog(row)
    },
    // 未收到
    handleUnPaid(row) {
      this.$prompt('请输入谷歌验证码', {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputValidator: (value) => {
          if (/<|>|"|'|\||\\/.test(value)) {
            return "不能包含非法字符：< > \" ' \\\ |"
          }
        },
      }).then(({ value }) => {
        agentOrderUnPaid({
          id: row.id,
          code: value
        }).then(res => {
          this.$message.success('操作成功')
          this.getTableData()
        })
      })
    },
    // 黑名单
    handleBlack() {
      this.$refs.blackIpListRef.openDialog()
    },
    // 冲正
    handleCzOrder(row) {
      this.$prompt('请输入谷歌验证码', {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputValidator: (value) => {
          if (/<|>|"|'|\||\\/.test(value)) {
            return "不能包含非法字符：< > \" ' \\\ |"
          }
        },
      }).then(({ value }) => {
        czOrder({
          id: row.id,
          code: value
        }).then(res => {
          this.$message.success('操作成功')
          this.getTableData()
        })
      })
    },
    // 查看上级码商
    lookAllParent(row) {
      this.$refs.allParentRef.openDialog(row)
    },
    // 自动刷新
    changeRefresh(val) {
      localStorage.removeItem('autoRefresh')
      clearInterval(this.timer)
      this.timer = null
      if (val === '1') {
        this.timer = setInterval(() => {
          this.getTableData()
        }, 1000 * 60)
        localStorage.setItem('autoRefresh', '1')
        this.getTableData()
      }
    },
    // 轮询方式
    changeWeightType(val) {
      this.$confirm('确认更改轮询方式吗？', '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.tableConfig.loading = true
        this.weightType = val
        this.tempWeight = val
        updateAgentEntity({
          weightType: val,
          userId: this.$store.getters.id
        }).then(() => {
          this.$message.success('修改成功')
        }).finally(() => {
          this.tableConfig.loading = false
        })
      }).catch(action => {
        // 点击“取消” 或 关闭弹窗
        if (action === 'cancel') {
          this.weightType = this.tempWeight
        } else {
          this.weightType = this.tempWeight
          console.log('用户关闭了弹窗');
        }
      });
    },
    // 已关闭状态的订单，整列显示红色
    tableRowClassName({ row }) {
      if (row.orderStatus === 3) {
        return 'red-row'
      }
      return ''
    },
    // 导出
    exportList() {
      if (this.tableConfig.data.length === 0) {
        this.$message.warning('暂无数据导出')
        return
      }
      this.download('inOrderDetailEntity/orderExport', {
        ...this.tableConfig.searchQuery
      }, `订单列表明细${moment().format('YYYY-MM-DD HHmmss')}.xlsx`)
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        shopName: '',
        tradeNo: '',
        shopOrderNo: '',
        merchantName: '',
        channelId: '',
        orderStatus: '',
        callbackStatus: '',
        agentName: '',
        parentMerchantName: '',
        memberIp: '',
        recipient: '',
        startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
      }
      this.searchData[4].initialDate = [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')]
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

<style lang="scss">
.el-table__row.red-row td.el-table__cell{
  background-color: #ffcdcd !important;
}
.el-table--striped .el-table__body tr.el-table__row--striped.red-row td.el-table__cell {
  background-color: #ffcdcd
}

.weight-type {
  margin-left: 10px;
  p {
    font-size: 14px;
    margin-right: 12px
  }
}
</style>
