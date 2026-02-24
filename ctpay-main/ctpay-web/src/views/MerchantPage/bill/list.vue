<!--
** 码商对账列表
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table :table="tableConfig">
      <template v-slot:table-after>
      </template>
    </km-table>
  </div>
</template>

<script>
import { reportForMerchant } from '@/api/report'
import moment from 'moment';

export default {
  name: 'BillList',
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'merchantId',
          title: '码商编号',
          inputType: 'number',
          placeholder: '请输入码商编号',
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
          type: 'select',
          model: 'orderFlag',
          title: '是否跑量',
          placeholder: '请选择',
          clearable: true,
          option: [{label: '是', value: 1}],
        },
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
          merchantId: '',
          merchantName: '',
          orderFlag: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          { label: '码商编号', value: 'userId' },
          { label: '码商名称', value: 'userName' },
          { label: '今日上分', value: 'chargeMoney', align: 'center' },
          { label: '今日余额(元)', value: 'todayBalance', align: 'center' },
          { label: '昨日余额(元)', value: 'yesBalance', align: 'center' },
          { label: '交易笔数', value: 'totalCount', align: 'center' },
          { label: '成功笔数', value: 'successCount', align: 'center' },
          { label: '总交易金额(元)', value: 'totalMoney', align: 'center' },
          { label: '成功金额(元)', value: 'successMoney', align: 'center' },
          { label: '佣金(元)', value: 'merchantFee', align: 'center',  formatter: (row) => { return row.merchantFee ? row.merchantFee : '/'}},
          { label: '成功率(%)', value: 'successRate', align: 'center', formatter: (row) => { return (row.successRate * 100).toFixed(2) + '%' } }
        ]
      }
    }
  },
  mounted() {
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.getTableData()
    },
    getTableData() {
      this.tableConfig.loading = true
      reportForMerchant({...this.tableConfig.searchQuery}).then(res => {
        this.tableConfig.loading = false
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
     // 重置
     resetTable() {
      this.tableConfig.searchQuery = {
        merchantId: '',
        merchantName: '',
        orderFlag: '',
        startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
      }
      this.searchData[3].initialDate = [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')]
      this.getTableData()
    }
  }
}
</script>
