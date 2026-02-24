<!--
** 码商对应通道码详情
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
      <template v-slot:operation="{ row }">
        <el-button
          type="primary" class="min-button"
          @click="handleChannel(row)">
          详情
        </el-button>
      </template>
    </km-table>

    <ChannelQrcodeList ref="channelQrcodeRef"></ChannelQrcodeList>
  </div>
</template>

<script>
import { allChildQrcodeTotal } from  '@/api/merchantQrcode'
import { getChannelListAll } from '@/api/channel'
import ChannelQrcodeList from './components/channelQrcodeList.vue';
export default {
  name: 'channelQrcode',
  components: {
    ChannelQrcodeList
  },
  data() {
    return {
      isVisible: false,
      row: {},
      searchData: [
        {
          type: 'input',
          model: 'merchantName',
          title: '码商名称',
          placeholder: '请输入码商名称',
          clearable: true
        },
        {
          type: 'select',
          model: 'channelId',
          title: '通道',
          placeholder: '请选择通道',
          clearable: true,
          option: []
        },
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          channelId: '',
          merchantName: '',
        },
        data: [],
        columns: [
        {
            value: 'merchantId',
            label: '码商ID'
          },
          {
            value: 'merchantName',
            label: '码商名称'
          },
          {
            value: 'channelName',
            label: '通道名称'
          },
          {
            value: 'channelCode',
            label: '通道编码'
          },
          {
            value: 'qrcodeNum',
            label: '挂码数量',
            align: 'center'
          },
          {
            value: 'operation',
            label: '操作',
            align: 'center',
            fixed: 'right',
            slot: 'operation',
            width: 120
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
    this.getChannelList()
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getList()
    },
    getList() {
      this.tableConfig.loading = true
      allChildQrcodeTotal({
        merchantName: this.tableConfig.searchQuery.merchantName
      }).then(res => {
        this.tableConfig.data = res.data?.records || []
        this.tableConfig.pagination.total = res.data?.total || 0
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    getChannelList() {
      getChannelListAll({}).then(res => {
        const list = res.data || {}
        this.$set(this.searchData[1], 'option', list.map(item => {
          const data = {
            label: item.channelName,
            value: item.id
          }
          return data
        }))
      })
    },
    handleChannel(row) {
      this.$refs.channelQrcodeRef.openDialog(row)
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        userName: '',
        status: '',
        orderPermission: '',
        parentPath: ''
      }
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