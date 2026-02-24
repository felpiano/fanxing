<!--
** 渠道信息 - 看代理或者上级给自己的通道费率
-->
<template>
  <div class="app-container">
    <km-table :table="tableConfig">
    </km-table>
  </div>
</template>

<script>
import { getSelfChannel } from '@/api/merchant'

export default {
  name: 'channelInfo',
  data() {
    return {
      tableConfig: {
        loading: false,
        searchQuery: {},
        data: [],
        columns: [
          { label: '通道名称', value: 'channelName', width: 160 },
          { label: '通道编码', value: 'channelCode', width: 160 },
          { label: '通道费率%', value: 'channelRate', width: 160, formatter: (row) => row.channelRate + '%' },
        ]
      }
    }
  },
  mounted() {
    this.getList()
  },
  methods: {
    getList() {
      this.tableConfig.loading = true
      getSelfChannel().then(res => {
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    }
  },
}
</script>
