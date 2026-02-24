<!--
**  码商费率 -- 基础通道配置
-->
<template>
  <el-dialog :title="`设置下级码商【${userName}】费率`" :visible.sync="isVisible" width="58%">
    <km-table :table="tableConfig" style="height:460px" :isSearch="false" border>
      <template v-slot:table-after>
        <div>
          <div class="flex-center">
            <p style="margin-bottom: 15px">码商费率：</p>
            <el-input-number v-model="channelRate" :controls="false" size="small" style="margin: 0 10px 15px 0"></el-input-number>
            <el-button type="primary" size="mini" @click="hanndleBatch">一键设置所有费率</el-button>
          </div>
           <p class="remark-text" style="margin-bottom: 10px">备注：如给通道的汇率是百分之5，则输入框信息填入5</p>
        </div>
      </template>
      <template v-slot:channelRate="{ row }">
        <div class="flex-center">
          <el-input-number v-model="row.channelRate" :controls="false" size="mini" style="width: 100px"></el-input-number>%
        </div>
      </template>
      <!-- <template v-slot:merchantOvertime="{ row }">
        <div class="flex-center">
          <el-input-number v-model="row.merchantOvertime" :controls="false" size="mini"></el-input-number>
        </div>
      </template>
      <template v-slot:baseDeposit="{ row }">
        <div class="flex-center">
          <el-input-number v-model="row.baseDeposit" :controls="false" size="mini"></el-input-number>
        </div>
      </template> -->
      <template v-slot:operation="{ row }">
        <el-button type="primary" size="mini" @click="handleSaveRate(row)">保存</el-button>
      </template>
    </km-table>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">关闭</el-button>
      <!-- <el-button type="primary" @click="hanndleSubmit" :loading="submitLoading">保存全部</el-button> -->
    </div>
  </el-dialog>
</template>

<script>
import {getChannelList, channelUpdate} from '@/api/merchantChild'

export default {
  name: 'basicConfigRate',
  data() {
    return {
      isVisible: false,
      tableConfig:{
        loading: false,
        data: [],
        columns: [
          { label: "编号", value: "channelId", width: 60 },
          { label: "渠道名称", value: "channelName", width: 160 },
          { label: "码商费率%", value: "channelRate", slot: "channelRate", width: 140 },
          { label: "订单超时时长(分钟)", value: "merchantOvertime", width: 100 },
          { label: "押金(元)", value: "baseDeposit", width: 80 },
          { label: "最小接单金额", value: "minAmount", width: 100 },
          { label: "最大接单金额", value: "maxAmount", width: 100 },
          { value: 'operation', label: '操作', slot: 'operation', align: 'center', width: 80 }
        ],
      },
      submitLoading: false,
      channelRate: 0,
      userName: ''
    }
  },
  methods: {
    openDialog(item) {
      this.channelRate = 0
      this.tableConfig.data = []
      this.isVisible = true
      this.userName = item.userName
      this.getList(item.userId)
    },
    getList(userId) {
      this.tableConfig.loading = true
      getChannelList({ merchantId: userId }).then(res => {
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    hanndleSubmit() {
      this.submitLoading = true
      channelUpdate(this.tableConfig.data).then(() => {
        this.submitLoading = false
        this.$message.success('保存成功')
        this.$emit('refresh')
        this.isVisible = false
      }).catch(() => {
        this.submitLoading = false
      })
    },
    // 一键设置所有费率
    hanndleBatch() {
      this.tableConfig.data.forEach(item => {
        item.channelRate = this.channelRate
      })
    },
    // 保存当前费率
    handleSaveRate(row) {
      this.submitLoading = true
      channelUpdate(row).then(() => {
        this.$message.success('保存成功')
      }).finally(() => {
        this.submitLoading = false
      })
    }
  }
}
</script>
