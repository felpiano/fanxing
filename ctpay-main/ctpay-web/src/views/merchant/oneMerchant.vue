<!--
** 码商帐变列表--资金明细
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
        <el-button
          type="primary"
          plain
          icon="el-icon-download"
          size="mini"
          @click="exportList"
        >导出</el-button>
      </template>
    </km-table>
  </div>
</template>

<script>
import { getMerchantaAll, getOneMerchantList } from '@/api/merchant'
import moment from 'moment'

export default {
  name: 'shopRecords',
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'orderNo',
          title: '订单号',
          width: '220px',
          placeholder: '请输入帐变相关订单号',
          clearable: true
        },
        {
          type: 'input',
          model: 'userName',
          title: '码商名称',
          width: '130px',
          placeholder: '请输入码商名称',
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
            { label: '冲正', value: '4'},
            { label: '扣款', value: '5'}
          ]
        },
        {
          type: 'select',
          model: 'amountType',
          title: '金额类型',
          placeholder: '全部',
          width: '130px',
          clearable: true,
          option: [
            { label: '余额', value: '1'},
            { label: '冻结金额', value: '2'},
          ]
        },
        {
          type: 'select',
          model: 'parentPath',
          filterable: true,
          title: '码商层级',
          placeholder: '请选择码商',
          clearable: true,
          option: []
        },
        {
          type: 'select',
          model: 'merchantLevel',
          title: '层级数',
          placeholder: '全部',
          width: '100px',
          clearable: true,
          option: [
            { label: '0', value: '1'},
            { label: '1', value: '2'},
            { label: '2', value: '3'},
            { label: '3', value: '4'},
            { label: '4', value: '5'},
          ]
        },
        {
          type: 'datetimerange',
          initialDate: [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
          modelName: ['startTime', 'endTime'],
          title: '时间选择',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        },
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          userId: '',
          userName: '',
          orderNo: '',
          changeType: '',
          parentId: '',
          merchantLevel: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          {
            value: 'orderNo',
            label: '订单号',
            width: 200
          },
          {
            value: 'userId',
            label: '用户ID',
            width: 100
          },
          {
            value: 'userName',
            label: '码商名称',
            width: 130
          },
          //资金类型
          {
            value: 'amountType',
            label: '金额类型',
            width: 100,
            formatter: (row) => {
              const amountTypeMap = {
                1: '余额',
                2: '冻结金额',
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
              if (row.changeType == '2' && Number(row.changeAmount) < 0) {
                return '扣款'
              } else {
                const changeTypeMap = {
                  1: '充值',
                  2: '上分',
                  3: '转移'
                }
                return changeTypeMap[row.changeType]
              }
            }
          },
          {
            value: 'beforeAmount',
            label: '变更前金额(元)',
            align: 'center',
            width: 120
          },
          {
            value: 'afterAmount',
            label: '变更后金额(元)',
            align: 'center',
            width: 120
          },
          {
            value: 'changeAmount',
            label: '变更金额(元)',
            align: 'center',
            width: 120
          },
          {
            value: 'createTime',
            label: '变更时间',
            width: 160
          },
          {
            value: 'remarks',
            label: '备注',
            width: 440,
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
    // 代理, admin
    if (this.$store.getters.identity !== 5) {
      this.searchData.unshift({
          type: 'input',
          model: 'userId',
          title: '用户ID',
          inputType: 'number',
          placeholder: '请输入用户ID',
          clearable: true
        },
        {
          type: 'input',
          model: 'userName',
          title: '用户名称',
          placeholder: '请输入用户名称',
          clearable: true
        })
      this.tableConfig.columns.unshift({
          value: 'userId',
          label: '用户ID',
          width: 80
        },
        {
          value: 'userName',
          label: '用户名称',
          width: 120
        })
    }
    this.getMerchantaAllView()
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getTableData()
    },
    getTableData() {
      this.tableConfig.loading = true
      getOneMerchantList({
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
          if (column.property !== 'changeBefore' && column.property !== 'changeAfter' && column.property !== 'changeAmount') {
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
    getMerchantaAllView() {
      getMerchantaAll({
        parentId: this.$store.getters.id
      }).then(res => {
        let option = res.data || []
        if (this.$store.getters.identity !== 5) {
          this.searchData[6].option = option.map(item => {
            return {
              label: item.userName,
              value: item.parentPath
            }
          })
        } else {
          this.searchData[4].option = option.map(item => {
            return {
              label: item.userName,
              value: item.parentPath
            }
          })
        }
      })
    },
    resetTable() {
      this.tableConfig.searchQuery = {
        userId: '',
        userName: '',
        orderNo: '',
        changeType: '',
        parentId: '',
        startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
      }
      this.searchData[this.searchData.length - 1].initialDate = [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')]
      this.doSearch()
    },
    // 导出
    exportList() {
      if (this.tableConfig.data.length === 0) {
        this.$message.warning('暂无数据导出')
        return
      }
      this.download('merchantAmountRecordsEntity/oneMerchantExport', {
        ...this.tableConfig.searchQuery
      }, `码商账变${moment().format('YYYY-MM-DD HHmmss')}.xlsx`)
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
