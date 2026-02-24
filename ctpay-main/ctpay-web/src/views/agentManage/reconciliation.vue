<!--
** 代理对账列表--展示代理下码商的对账信息
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
import { reportForAgentByDate } from '@/api/report'
import moment from 'moment';

export default {
  name: 'Reconciliation',
  data() {
    return {
      searchData: [
        {
          type: 'daterange',
          initialDate: [moment().startOf('day').format('YYYY-MM-DD'), moment().endOf('day').format('YYYY-MM-DD')],
          modelName: ['startTime', 'endTime'],
          title: '时间选择',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        },
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
        /*{
          type: 'input',
          model: 'shopId',
          title: '商户编号',
          inputType: 'number',
          placeholder: '请输入商户编号',
          clearable: true
        },
        {
          type: 'input',
          model: 'shopName',
          title: '商户名称',
          placeholder: '请输入商户名称',
          clearable: true
        }*/
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          shopId: '',
          shopName: '',
          merchantId: '',
          merchantName: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD'),
          endTime: moment().endOf('day').format('YYYY-MM-DD')
        },
        data: [],
        columns: [
          { label: '码商编号', value: 'userId' },
          { label: '码商名称', value: 'userName' },
         /* { label: '商户编号', value: 'shopId' },
          { label: '商户名称', value: 'shopName' },*/
          { label: '累计上分', value: 'chargeMoney', align: 'center' },
          { label: '开始余额(元)', value: 'yesBalance', align: 'center', tooltip: '开始时间的昨日余额', width: 110 },
          { label: '结束余额(元)', value: 'todayBalance', align: 'center', tooltip: '结束时间的当日余额', width: 110 },
          { label: '交易笔数', value: 'totalCount', align: 'center', formatter: (row) => { return row.totalCount ? row.totalCount : '0' }},
          { label: '成功笔数', value: 'successCount', align: 'center', formatter: (row) => { return row.successCount ? row.successCount : '0' }},
          { label: '总交易金额(元)', value: 'totalMoney', align: 'center', width: 100, formatter: (row) => { return row.totalMoney ? row.totalMoney : '0' }},
          { label: '成功金额(元)', value: 'successMoney', align: 'center', width: 90, formatter: (row) => { return row.successMoney ? row.successMoney : '0' }},
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
      reportForAgentByDate({...this.tableConfig.searchQuery}).then(res => {
        this.tableConfig.loading = false
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
     // 重置
     resetTable() {
      this.tableConfig.searchQuery = {
        shopId: '',
        shopName: '',
        merchantId: '',
        merchantName: '',
        startTime: moment().startOf('day').format('YYYY-MM-DD'),
        endTime: moment().endOf('day').format('YYYY-MM-DD')
      }
      this.searchData[0].initialDate = [moment().startOf('day').format('YYYY-MM-DD'), moment().endOf('day').format('YYYY-MM-DD')]
      this.getTableData()
    }
  }
}
</script>
