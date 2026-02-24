<!--
** 代理帐变列表
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table
      :table="tableConfig"
      @tableChangeCurrent="tableChangeCurrent"
      @tableChangeSize="tableChangeSize"
      :toHeight="90"
      show-summary
      :summary-method="getSummaries">
      <template v-slot:table-after>
      </template>
    </km-table>
  </div>
</template>

<script>
import {getAgentAmountRecordsList} from '@/api/agent'
import moment from 'moment'

export default {
  name: 'shopRecords',
  data() {
    return {
      searchData: [
      {
          type: 'input',
          model: 'userId',
          title: '代理ID',
          inputType: 'number',
          placeholder: '请输入代理ID',
          clearable: true
        },
        {
          type: 'input',
          model: 'userName',
          title: '代理名称',
          placeholder: '请输入代理名称',
          clearable: true
        },
        {
          type: 'input',
          model: 'orderNo',
          title: '订单号',
          placeholder: '请输入帐变相关订单号',
          clearable: true
        },
        {
          type: 'select',
          model: 'changeType',
          title: '账变类型',
          placeholder: '全部',
          clearable: true,
          option: [
            { label: '充值', value: '1'},
            { label: '上分', value: '2'},
            { label: '转移', value: '3'},
          ]
        },
        // {
        //   type: 'select',
        //   model: 'amountType',
        //   title: '金额类型',
        //   placeholder: '全部',
        //   clearable: true,
        //   option: [
        //     { label: '余额', value: '1'},
        //     { label: '冻结金额', value: '2'},
        //     { label: '押金', value: '3'},
        //   ]
        // },
        {
          type: 'datetimerange',
          initialDate: [],
          modelName: ['startTime', 'endTime'],
          title: '时间范围',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          userId: '',
          userName: '',
          orderNo: '',
          changeType: '1',
          amountType: '',
          startTime: '',
          endTime: ''
        },
        data: [],
        columns: [
          {
            value: 'userId',
            label: '代理ID',
            width: 80
          },
          {
            value: 'userName',
            label: '代理名称',
            width: 120
          },
          {
            value: 'orderNo',
            label: '订单号',
            width: 180
          },
          {
            value: 'amountType',
            label: '金额类型',
            width: 100,
            formatter: (row) => {
              const amountTypeMap = {
                1: '余额',
                3: '押金'
              }
              return amountTypeMap[row.amountType]
            }
          },
          {
            value: 'changeType',
            label: '账变类型',
            width: 100,
            formatter: (row) => {
              const changeTypeMap = {
                1: '充值',
                2: '上分',
                3: '转移'
              }
              return changeTypeMap[row.changeType]
            }
          },
          {
            value: 'beforeAmount',
            label: '变更前金额(元)',
            width: 120
          },
          {
            value: 'afterAmount',
            label: '变更后金额(元)',
            width: 120
          },
          {
            value: 'changeAmount',
            label: '变更金额(元)',
            width: 120
          },
          {
            value: 'createTime',
            label: '变更时间',
            width: 160
          },
          {
            value: 'notes',
            label: '备注',
            formatter: (row) => {
              return row.remarks ? row.remarks : row.notes
            }
          }
        ],
        hasPagination: true,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      }
    }
  },
  mounted() {
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getTableData()
    },
    getTableData() {
      this.tableConfig.loading = true
      getAgentAmountRecordsList({
        ...this.tableConfig.searchQuery,
        pageNum: this.tableConfig.pagination.pageNum,
        pageSize: this.tableConfig.pagination.pageSize
      }).then(res => {
        this.tableConfig.data = res.data.records || []
        this.tableConfig.pagination.total = res.data.total || 0
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    // 合计
    getSummaries(param) {
      const { columns, data } = param;
      const sums = []
      if (data.length && columns.length) {
        columns.forEach((column, index) => {
          if (index === 0) {
            sums[index] = '合计';
            return;
          }
          // 只统计变更前余额、变更后余额、变更金额
          if (column.property !== 'changeAmount') {
            sums[index] = '';
          } else {
            const values = data.map(item => Number(item[column.property]));
            if (!values.every(value => isNaN(value))) {
              sums[index] = values.reduce((prev, curr) => {
                const value = Number(curr);
                if (!isNaN(value)) {
                  return prev + curr;
                } else {
                  return prev;
                }
              }, 0);
              sums[index] = (sums[index]).toFixed(2)
              sums[index] += '元';
            } else {
              sums[index] = '';
            }
          }
        })
        return sums;
      } else {
        return sums
      }
    },
    resetTable() {
      this.tableConfig.searchQuery = {
        tradeNo: '',
        orderNo: '',
        shopName: '',
        userId: '',
        purseType: '',
        changeType: '',
        startTime: '',
        endTime: ''
      }
      this.searchData[this.searchData.length - 1].initialDate = []
      this.doSearch()
    },
    // 导出
    exportList() {
      this.download('shopChargeRecordsEntity/export', {
        ...this.tableConfig.searchQuery
      }, `代理账变${moment().format('YYYY-MM-DD HHmmss')}.xlsx`)
    },
    // 分页
    tableChangeCurrent(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageNum: e.current
      })
      this.getTableData()
    },
    tableChangeSize(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageSize: e.pageSize
      })
      this.getTableData()
    }
  }
}
</script>
