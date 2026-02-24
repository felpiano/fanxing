<!--
** 支付设置--支付产品
** 代理通道
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table :table="tableConfig" @tableChangeCurrent="tableChangeCurrent" @tableChangeSize="tableChangeSize">
      <template v-slot:showChannel="{ row }">
        <el-switch
          :width="55"
          v-model="row.showChannel"
          :active-value="0"
          :inactive-value="1"
          active-text="开启"
          inactive-text="关闭"
          @change="updateShowStatus(row)">
        </el-switch>
      </template>
      <template v-slot:status="{ row }">
        <div>
          <el-button
            type="primary"
            class="min-button"
            size="mini"
            v-if="row.status === 0"
            @click="changeStatus(row, 1)">
            启用
          </el-button>
          <el-button
            type="danger"
            class="min-button"
            size="mini"
            v-else
            @click="changeStatus(row, 0)">
            禁止
          </el-button>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:agent:saveProduct']" type="primary" class="min-button" @click="handleChannel(row)">渠道配置</el-button>
        <el-button v-hasPermi="['system:agent:saveProduct']" type="danger" class="min-button" @click="clearRate(row)">一键清除费率</el-button>
      </template>
    </km-table>

    <!-- 渠道配置 -->
     <channelConfig ref="channelConfig" @updateList="getList"></channelConfig>
  </div>
</template>

<script>
import {getAgentChannelList, onekeyClearRate} from '@/api/agent'
import channelConfig from './components/channelConfig.vue';

export default {
  name: 'payProduct',
  components: {
    channelConfig
  },
  data() {
    return {
      searchData: [
        {

          type: 'input',
          model: 'channelCode',
          title: '通道代码',
          placeholder: '请输入通道代码',
        },
        {
          type: 'input',
          model: 'channelName',
          title: '通道名称',
          placeholder: '请输入通道名称',
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          channelCode: '',
          channelName: '',
        },
        data: [],
        columns: [
          {
            value: 'channelId',
            label: '通道ID'
          },
          {
            value: 'channelName',
            label: '产品名称'
          },
          {
            value: 'channelCode',
            label: '通道代码'
          },
          // {
          //   value: 'showChannel',
          //   label: '通道显示',
          //   slot: 'showChannel',
          //   align: 'center'
          // },
          // {
          //   value: 'status',
          //   label: '状态',
          //   slot: 'status',
          //   align: 'center'
          // },
          {
            value: 'operation',
            slot: 'operation',
            align: 'center',
            label: '操作'
          }
        ],
        hasPagination: false,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      },
    };
  },
  mounted() {
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getList();
    },
    getList() {
      this.tableConfig.loading = true
      getAgentChannelList({
        ...this.tableConfig.searchQuery,
        pageNum: this.tableConfig.pagination.pageNum,
        pageSize: this.tableConfig.pagination.pageSize
      }).then(res => {
        this.tableConfig.data = res.data || []
        // this.tableConfig.pagination.total = res.data.total || 0
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    // 渠道配置
    handleChannel(row) {
      this.$refs.channelConfig.openDialog(row)
    },
    clearRate(row) {
      this.$confirm(`一键清除所有的2，3，4，5级码商【${row.channelName}】通道的费率，是否确认？`, '一键清除费率', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        onekeyClearRate({
          channelId: row.channelId
        }).then(res => {
          this.$message.success('清除成功')
        }).finally(() => {
          this.loading = false
        })
      })
    },
    // 重置搜索条件
    resetTable() {
      this.tableConfig.searchQuery = {
        channelCode: '',
        channelName: '',
      };
      this.doSearch()
    },
    // 分页
    tableChangeCurrent(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageNum: e.current
      })
      this.getList()
    },
    tableChangeSize(e) {
      this.$set(this.tableConfig, 'pagination', {
        ...this.tableConfig.pagination,
        pageSize: e.pageSize
      })
      this.getList()
    }
  }
}
</script>
