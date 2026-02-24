<!--
** 订单详情
-->
<template>
  <el-dialog
    title="订单详情"
    :visible.sync="dialogVisible"
    width="55%">
    <el-descriptions title="" :column="2" size="small" border :labelStyle="{'width': '98px'}" :contentStyle="{'width': '200px'}">
      <el-descriptions-item label="代理信息：">{{ orderInfo.agentId ? `${orderInfo.agentName}【 ${orderInfo.agentId}】` : '无代理'}}</el-descriptions-item>
      <el-descriptions-item label="商户信息：">{{ orderInfo.shopName }}【{{ orderInfo.shopId }}】</el-descriptions-item>
      <el-descriptions-item label="码商信息">{{ orderInfo.merchantName }}【{{orderInfo.merchantId}}】</el-descriptions-item>
      <el-descriptions-item label="通道名称">{{ orderInfo.channelName }}</el-descriptions-item>
      <el-descriptions-item label="平台单号">{{ orderInfo.tradeNo}}</el-descriptions-item>
      <el-descriptions-item label="商户单号：">{{ orderInfo.shopOrderNo}}</el-descriptions-item>
      <el-descriptions-item label="付款人">{{ orderInfo.payer}}</el-descriptions-item>
      <el-descriptions-item label="访问IP">{{ orderInfo.clientIp}}</el-descriptions-item>
      <el-descriptions-item label="收款人：">{{ orderInfo.nickName}}</el-descriptions-item>
      <el-descriptions-item label="收款账号：">{{ orderInfo.accountNumber}}</el-descriptions-item>
      <el-descriptions-item label="交易金额：">{{ orderInfo.orderAmount || '0'}}</el-descriptions-item>
      <el-descriptions-item label="到账金额：">{{ orderInfo.orderAmount }}</el-descriptions-item>
      <el-descriptions-item label="商户收入：">{{ orderInfo.shopAmount}}</el-descriptions-item>
      <el-descriptions-item label="代理收入：" v-if="$store.getters.identity === 1">{{ orderInfo.agentFee }}</el-descriptions-item>
      <el-descriptions-item label="码商收入：">{{ orderInfo.merchantFee}}</el-descriptions-item>
      <el-descriptions-item label="创建时间：">{{ orderInfo.orderTime}}</el-descriptions-item>
      <el-descriptions-item label="更新时间：">{{ orderInfo.finishTime}}</el-descriptions-item>
      <el-descriptions-item label="返回地址：">
        <div style="max-width: 200px;word-break: break-all;" v-if="orderInfo.callShopUrl && orderInfo.callShopUrl !== ''">
          <div v-if="isCallShow">
            {{ orderInfo.callShopUrl }}
          </div>
          <el-button v-else type="text" size="mini" @click="isCallShow = true">点击查看</el-button>
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="交易信息：">
        <div style="max-width: 200px;word-break: break-all;" v-if="orderInfo.forwordUrl && orderInfo.forwordUrl !== ''">
          <div v-if="isShow">
            <a style="color: #1890ff" target="_blank" :href="orderInfo.forwordUrl">{{ orderInfo.forwordUrl }}</a>
          </div>
          <el-button v-else type="text" size="mini" @click="isShow = true">点击查看</el-button>
        </div>
        <div v-else>--</div>
      </el-descriptions-item>
      <el-descriptions-item label="订单状态：">
        <el-tag  size="mini" v-if="orderInfo.orderStatus === 0">待支付</el-tag>
        <el-tag  size="mini" type="success" v-else-if="orderInfo.orderStatus === 1">支付成功</el-tag>
        <el-tag  size="mini" type="danger" v-else-if="orderInfo.orderStatus === 2">订单超时</el-tag>
        <el-tag  size="mini" type="info" v-else-if="orderInfo.orderStatus === 3">已关闭</el-tag>
        <el-tag size="mini" type="danger" v-else-if="orderInfo.orderStatus === 4" >待退回</el-tag>
        <el-tag size="mini" type="danger" v-else-if="orderInfo.orderStatus === 5">已退回</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="回调状态：">
        <el-tag  size="mini" v-if="orderInfo.callbackStatus === 0">等待回调</el-tag>
        <el-tag  size="mini" type="success" v-else-if="orderInfo.callbackStatus === 1">回调成功</el-tag>
        <el-tag  size="mini" type="danger" v-else-if="orderInfo.callbackStatus === 2">回调失败</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="订单备注：">{{ orderInfo.orderRemark }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script>
import {getInOrderDetailInfo} from '@/api/order'

export default {
  name: "detailOrder",
  data() {
    return {
      dialogVisible: false,
      orderInfo: {},
      isShow: false,
      isCallShow: false
    }
  },
  methods: {
    openDialog(row) {
      this.isShow = false
      this.getOrderDetail(row)
    },
    getOrderDetail(row) {
      getInOrderDetailInfo({id: row.id}).then(res => {
        this.orderInfo = Object.assign({}, row, res.data)
        this.orderInfo.shopAmount = (this.orderInfo.orderAmount - this.orderInfo.shopFee).toFixed(2)
        this.dialogVisible = true
      })
    }
  }
}
</script>
