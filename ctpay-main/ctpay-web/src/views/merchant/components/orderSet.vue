<!--
** 接单设置
-->
<template>
  <el-dialog :title="`码商【${userName}】的接单设置`" append-to-body :visible.sync="isVisible" width="84%">
    <km-table :table="tableConfig" style="height:460px" :isSearch="false" border>
      <template v-slot:merchantOvertime="{ row }">
        <el-input-number v-model="row.merchantOvertime" placeholder="订单超时时长" :controls="false" size="mini" style="width: 110px"></el-input-number>
      </template>
      <template v-slot:minAmount="{ row }">
        <el-input-number v-model="row.minAmount" placeholder="最小接单金额" :controls="false" size="mini" style="width: 90px"></el-input-number>
      </template>
      <template v-slot:maxAmount="{ row }">
        <el-input-number v-model="row.maxAmount" placeholder="最大接单金额" :controls="false" size="mini" style="width: 90px"></el-input-number>
      </template>
      <template v-slot:weight="{ row }">
        <el-input-number style="width: 90px"
          size="mini"
          :controls="false"
          v-model="row.weight"
          class="weight-input">
        </el-input-number>
      </template>
      <template v-slot:agentContrl="{ row }">
        <el-switch v-if="!type"
          :width="50"
          v-model="row.agentContrl"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row)">
        </el-switch>
        <div class="text-center" v-else>
          <el-tag size="mini" v-if="row.agentContrl === 0" type="success">开启</el-tag>
          <el-tag size="mini" v-else type="danger">关闭</el-tag>
        </div>
      </template>
      <template v-slot:status="{ row }">
        <el-switch
          :width="50"
          v-model="row.status"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'">
        </el-switch>
      </template>
    </km-table>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">关闭</el-button>
      <el-button type="primary" @click="hanndleSubmit" :loading="submitLoading">保存全部</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {getMerchantChannelList, updateBatchMerchantChannel, agentContrlOrder} from '@/api/merchant'

export default {
  name: 'orderSet',
  data() {
    return {
      isVisible: false,
      tableConfig:{
        loading: false,
        data: [],
        columns: [
          { label: "编号", value: "channelId", width: 80 },
          { label: "渠道名称", value: "channelName", width: 120 },
          { label: "渠道编码(接口标识)", value: "channelCode", width: 120 },
          { label: "费率", value: "channelRate", width: 80 },
          { label: "订单超时时长(分钟)", value: "merchantOvertime", slot: 'merchantOvertime', width: 120 },
          { label: "最小接单金额", value: "minAmount", slot: 'minAmount', width: 100 },
          { label: "最大接单金额", value: "maxAmount", slot:'maxAmount', width: 100 },
          { label: '权重', value: 'weight', slot: 'weight', align: 'center', width: 100 },
          { label: '团队接单状态', tooltip: '可以控制当前码商及其全部下级的接单状态', value: 'agentContrl', slot: 'agentContrl', align: 'center', width: 100 },
          { label: "状态", value: "status", slot: "status", width: 80, align: 'center' }
        ]
      },
      submitLoading: false,
      userName: '',
      type: null
    }
  },
  methods: {
    openDialog(item, type) {
      this.type = type
      this.tableConfig.data = []
      this.isVisible = true
      this.userName = item.userName
      this.getList(item.userId)
    },
    getList(userId) {
      this.tableConfig.loading = true
      getMerchantChannelList({ merchantId: userId }).then(res => {
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    changeStatus(row) {
      agentContrlOrder({
        merchantId: row.merchantId,
        channelId: row.channelId,
        agentContrl: row.agentContrl
      }).then(res => {
        this.$message.success('操作成功')
      })
    },
    hanndleSubmit() {
      // 全部的最大金额不能小于最小金额
      const isPass = this.tableConfig.data.every(item => {
        return item.maxAmount >= item.minAmount
      })
      if (!isPass) {
        this.$message.error('最大接单金额不能小于最小接单金额')
        return
      }
      this.submitLoading = true
      updateBatchMerchantChannel(this.tableConfig.data).then(() => {
        this.submitLoading = false
        this.$message.success('保存成功')
        this.$emit('refresh')
        this.isVisible = false
      }).catch(() => {
        this.submitLoading = false
      })
    },
  }
}
</script>

