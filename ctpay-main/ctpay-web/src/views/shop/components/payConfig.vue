<!--
**  支付开关 -- 基础通道配置
-->
<template>
  <el-dialog title="商品支付产品" :visible.sync="isVisible" width="42%">
    <km-table :table="tableConfig" style="height:460px" :isSearch="false" border>
      <template v-slot:status="{ row }">
        <el-switch
          :width="55"
          v-model="row.status"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'停用'">
        </el-switch>
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
  name: 'basicConfig',
  data() {
    return {
      isVisible: false,
      tableConfig:{
        loading: false,
        data: [],
        columns: [
          { label: "编号", value: "channelId", width: 80 },
          { label: "通道代码", value: "channelName", width: 160 },
          { label: "产品代码", value: "channelCode", width: 120 },
          { label: "状态", value: "status", slot: "status", align: 'center', width: 100 }
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
        this.$message.success('操作成功')
        this.isVisible = false
      }).catch(() => {
        this.submitLoading = false
      })
    }
  }
}
</script>
