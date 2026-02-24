<!--
** 报表管理-商户报表
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      :isReset="false">
      <template v-slot:after>
        <el-radio-group v-model="dateType" @input="changeDate" size="mini" style="margin-left: 20px">
          <el-radio-button label="上月"></el-radio-button>
          <el-radio-button label="本月"></el-radio-button>
          <el-radio-button label="近三月"></el-radio-button>
          <el-radio-button label="近六月"></el-radio-button>
          <el-radio-button label="本年"></el-radio-button>
        </el-radio-group>
      </template>
    </search-bar>
    <km-table :table="tableConfig"></km-table>
  </div>
</template>

<script>
import { reportByCondition } from '@/api/report'
import moment from 'moment'

export default {
  name: 'dateReport',
  data() {
    return {
      searchData: [
        {
          type: 'datetimerange',
          initialDate: [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
          modelName: ['startTime', 'endTime'],
          title: '时间选择',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          type: 1,
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          {
            label: '商户名称',
            value: 'userName',
            width: 160
          },
          { label: '交易笔数', value: 'totalCount' },
          { label: '成功笔数', value: 'successCount' },
          { label: '总交易金额(元)', value: 'totalMoney' },
          { label: '成功金额(元)', value: 'successMoney' },
          { label: '成功率(%)', value: 'successRate', formatter: row => (row.successRate * 100).toFixed(2) + '%' }
        ]
      },
      dateType: ''
    }
  },
  mounted() {
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.dateType = ''
      this.getList()
    },
    getList() {
      this.tableConfig.loading = true
      reportByCondition(this.tableConfig.searchQuery).then(res => {
        this.tableConfig.data = res.data || []
        this.tableConfig.loading = false
      }).catch(() => {
        this.tableConfig.loading = false
      })
    },
    changeDate(e) {
      let startTime = '', endTime = ''
      if (e === '上月') {
        startTime = moment().subtract(1, 'month').startOf('month').format('YYYY-MM-DD')
        endTime = moment().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')
      } else if (e === '本月') {
        startTime = moment().startOf('month').format('YYYY-MM-DD')
        endTime = moment().format('YYYY-MM-DD')
      } else if (e === '近三月') {
        startTime = moment().subtract(2, 'month').startOf('month').format('YYYY-MM-DD')
        endTime = moment().endOf('month').format('YYYY-MM-DD')
      } else if (e === '近六月') {
        startTime = moment().subtract(5, 'month').startOf('month').format('YYYY-MM-DD')
        endTime = moment().endOf('month').format('YYYY-MM-DD')
      } else if (e === '本年') {
        startTime = moment().startOf('year').format('YYYY-MM-DD')
        endTime = moment().endOf('year').format('YYYY-MM-DD')
      }
      this.tableConfig.searchQuery.startTime = startTime + ' 00:00:00'
      this.tableConfig.searchQuery.endTime = endTime + ' 23:59:59'
      this.searchData[0].initialDate = [startTime + ' 00:00:00', endTime + ' 23:59:59']
      this.getList()
    }
  }
}
</script>

