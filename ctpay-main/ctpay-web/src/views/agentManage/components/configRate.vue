<!--
**  费率
-->
<template>
  <el-dialog :title="userName + '代理支付产品费率设置'" :visible.sync="isVisible" width="465px">
    <p class="remark-text">备注：如给产品的汇率是百分之5，则输入框信息填入 5</p>
    <km-table :table="tableConfig" style="height:460px" :isSearch="false" border>
      <template v-slot:costRate="{ row }">
        <div class="flex-center">
          <el-input-number v-model="row.costRate" :controls="false" size="mini" style="width: 105px"></el-input-number>%
        </div>
      </template>
    </km-table>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">关闭</el-button>
      <el-button type="primary" @click="hanndleSubmit" :loading="submitLoading">保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {getAgentChannelList, saveAgentChannel} from '@/api/agent'

export default {
  name: 'basicConfigRate',
  data() {
    return {
      isVisible: false,
      tableConfig:{
        loading: false,
        data: [],
        columns: [
          { label: "产品ID", value: "channelId", width: 120 },
          { label: "产品名称", value: "channelName", width: 160 },
          { label: "费率%", value: "costRate", slot: "costRate", width: 143 }
        ],
      },
      submitLoading: false,
      userName: ''
    }
  },
  methods: {
    open(item) {
      this.tableConfig.data = []
      this.isVisible = true
      this.userName = item.userName
      this.getList(item.userId)
    },
    getList(userId) {
      this.tableConfig.loading = true
      getAgentChannelList({ agentId: userId }).then(res => {
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    hanndleSubmit() {
      this.submitLoading = true
      saveAgentChannel(this.tableConfig.data).then(() => {
        this.submitLoading = false
        this.$message.success('保存成功')
        this.isVisible = false
      }).catch(() => {
        this.submitLoading = false
      })
    }
  }
}
</script>
