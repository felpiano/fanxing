<!--
** 团队跑量统计
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
      @tableChangeSize="tableChangeSize">
    </km-table>
  </div>
</template>

<script>
import moment from 'moment'
export default {
  name: 'teamStatistics',
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'userName',
          title: '码商名称',
          placeholder: '请输入码商名称',
          clearable: true
        },
        {
          type: 'select',
          model: 'merchantLevel',
          title: '层级',
          placeholder: '请选择层级',
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
          userName: '',
          merchantLevel: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
          endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
        },
        data: [],
        columns: [
          {
            label: '码商名称',
            prop: 'userName',
            width: 150
          },
          {
            label: '层级',
            prop: 'merchantLevel',
            width: 150
          },
          {
            label: '余额',
            prop: 'balance',
            width: 150
          },
          {
            label: '跑量金额',
            prop: '',
            width: 150
          },
          {
            label: '收款码数量',
            prop: '',
            width: 150
          },
          {
            label: '成功率',
            prop: '',
            width: 150
          },
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
      // getInOrderList({
      //   ...this.tableConfig.searchQuery,
      //   pageNum: this.tableConfig.pagination.pageNum,
      //   pageSize: this.tableConfig.pagination.pageSize
      // }).then(res => {
      //   this.tableConfig.data = res.data?.records || []
      //   this.tableConfig.pagination.total = res.data?.total || 0
      // }).finally(() => {
        this.tableConfig.loading = false
      // })
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        userName: '',
        merchantLevel: '',
        startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')
      }
      this.searchData[this.searchData.length - 1].initialDate = [moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'), moment().endOf('day').format('YYYY-MM-DD HH:mm:ss')]
      this.doSearch()
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
