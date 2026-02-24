<!--
**  费率 -- 基础通道配置
-->
<template>
  <el-dialog title="商户支付渠道" :visible.sync="isVisible" width="40%">
    <p class="remark-text">备注：如给通道的汇率是百分之5，则输入框信息填入 5</p>
    <km-table :table="tableConfig" style="height:460px" :isSearch="false" border>
      <template v-slot:channelRate="{ row }">
        <div class="flex-center">
          <el-input-number v-model="row.channelRate" :controls="false" size="mini" style="width: 125px"></el-input-number>%
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
import {getShopBaseList, updateBatchShopBase} from '@/api/shop'

export default {
  name: 'basicConfigRate',
  data() {
    return {
      isVisible: false,
      tableConfig:{
        loading: false,
        data: [],
        columns: [
          { label: "编号", value: "channelCode", width: 160 },
          { label: "渠道名称", value: "channelName", width: 160 },
          { label: "通道费率%", value: "channelRate", slot: "channelRate", width: 140 }
        ],
      },
      submitLoading: false
    }
  },
  methods: {
    open(item) {
      this.tableConfig.data = []
      this.isVisible = true
      this.getList(item.userId)
    },
    getList(userId) {
      this.tableConfig.loading = true
      getShopBaseList({ shopId: userId }).then(res => {
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    hanndleSubmit() {
      this.submitLoading = true
      updateBatchShopBase(this.tableConfig.data).then(() => {
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
