<!--
** 一键配置通道
-->
<template>
  <el-dialog title="一键配置" :visible.sync="isVisible" width="30%">
    <km-table :table="tableConfig" style="height:486px" :isSearch="false" border>
      <template v-slot:platName="{ row }">
        <div class="w100 text-center">
          {{ row.platName }}【{{row.channelName}}】
        </div>
      </template>
      <template v-slot:config="{ row }">
        <el-checkbox v-model="row.config">配置</el-checkbox>
      </template>
    </km-table>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">关闭</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
    </div>
  </el-dialog>
</template>


<script>
import {getMcListAll} from '@/api/platform'
import {settingShopChannel} from '@/api/shop'

export default {
  name: "configChannel",
  data() {
    return {
      isVisible: false,
      submitLoading: false,
      tableConfig: {
        loading: false,
        data: [],
        columns: [
          { label: "渠道名称", value: "platName", slot: 'platName', align: "center"},
          { label: "是否配置", value: "config", slot: "config", align: "center", width: 140},
        ]
      }
    }
  },
  methods:{
    open() {
      this.isVisible = true
      this.tableConfig.data = []
      this.getListAll()
    },
    getListAll() {
      this.tableConfig.loading = true
      getMcListAll({}).then(res => {
        let list = res.data || []
        this.tableConfig.data = list.map(item => {
          item.config = false
          return item
        })
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    handleSubmit() {
      let list = this.tableConfig.data.filter(item => item.config)
      let newList = this.$lodash(list)
      newList.map(item => {
        delete item.config
        return item
      })
      this.submitLoading = true
      settingShopChannel(newList).then(() => {
        this.$message.success('配置成功')
        this.isVisible = false
      }).finally(() => {
        this.submitLoading = false
      })
    }
  }
}
</script>

