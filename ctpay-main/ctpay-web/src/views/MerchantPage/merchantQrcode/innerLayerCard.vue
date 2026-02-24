<!--
**  内层卡卡
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
      <template v-slot:table-after>
        <el-button v-hasPermi="['system:merchantQrcode:add']"
                   type="primary" size="mini" @click="handleAdd">
          <i class="el-icon-plus"></i>添加
        </el-button>
      </template>
      <template v-slot:qrcodeType="{ row }">
        <p class="ellipsis" v-if="row.qrcodeType === 0"> {{ row.qrcodeValue }}</p>
        <ImagePreview v-else :src="row.qrcodeUrl" :width="100" :height="100"></ImagePreview>
      </template>
      <template v-slot:status="{ row }">
        <el-switch :width="50"
                   v-model="row.status"
                   :active-value="0"
                   :inactive-value="1"
                   active-text="开启"
                   inactive-text="关闭"
                   @change="handleStatusChange(row)">
        </el-switch>
      </template>
      <template v-slot:operation="{ row }">
        <el-button
          v-hasPermi="['system:merchantQrcode:add']"
          type="primary"
          class="min-button"
          @click="handleUpdate(row)">
          编辑
        </el-button>
        <el-button
          v-hasPermi="['system:merchantQrcode:delete']"
          type="danger"
          class="min-button"
          @click="handleDelete(row)">
          删除
        </el-button>
      </template>
    </km-table>

    <!-- 银联扫码配置 -->
    <bankCardUpdate ref="bankCardUpdate" @submit="submitAdd"></bankCardUpdate>

  </div>
</template>

<script>
import {deleteMerchantQrcode, getMerchantQrcodeList, updateMerchantQrcodeStatus} from '@/api/merchantQrcode'
import ImagePreview from '@/components/ImagePreview'
import bankCardUpdate from './components/bankCardUpdate.vue'

export default {
  name: 'unionPay',
  components: {
    bankCardUpdate,
    ImagePreview
  },
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'nickName',
          title: '银行卡姓名',
          placeholder: '请输入支付宝昵称',
          clearable: true
        },
        {
          type: 'input',
          model: 'accountNumber',
          title: '银行卡账号',
          placeholder: '请输入支付宝账号',
          clearable: true
        },
        {
          type: 'select',
          model: 'status',
          title: '进单状态',
          placeholder: '全部',
          clearable: true,
          option: [
            { label: '启用',  value: 0 },
            { label: '禁用',  value: 1 }
          ]
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          channelId: '1118',
          nickName: '',
          accountNumber: '',
          status: ''
        },
        data: [],
        columns: [
          {
            value: 'id',
            label: '编号',
            width: 80
          },
          {
            value: 'nickName',
            label: '银行卡姓名',
            width: 120
          },
          {
            value: 'accountNumber',
            label: '银行卡卡号',
            width: 160
          },
          {
            value: 'accountRemark',
            label: '所属银行',
            width: 120
          },
          // {
          //   value: 'qrcodeType',
          //   slot: 'qrcodeType',
          //   label: '支付二维码',
          //   width: 140
          // },
          {
            value: 'stauts',
            slot: 'status',
            align: 'center',
            label: '进单状态',
            width: 100
          },
          {
            value: 'dayLimit',
            label: '每日限额',
            align: 'center',
            width: 120
          },
          {
            value: 'maxAmount',
            label: '最大金额',
            align: 'center',
            width: 120
          },
          {
            value: 'minAmount',
            label: '最小金额',
            align: 'center',
            width: 120
          },
          {
            value: 'successCount',
            label: '成功总数（今）',
            align: 'center',
            width: 120,
            formatter: row => row.successCount || '--'
          },
          {
            value: 'successAmount',
            label: '成功金额（今）',
            align: 'center',
            width: 120,
            formatter: row => row.successAmount || '--'
          },
          {
            value: 'yesSuccessCount',
            label: '成功总数（昨）',
            align: 'center',
            width: 120
          },
          {
            value: 'yesSuccessAmount',
            label: '成功金额（昨）',
            align: 'center',
            width: 120
          },
          {
            value: 'createTime',
            label: '添加时间',
            width: 160
          },
          {
            slot: 'operation',
            label: '操作',
            align: 'center',
            fixed: 'right',
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
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getTableData()
    },
    getTableData() {
      this.tableConfig.loading = true
      getMerchantQrcodeList({
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
    handleAdd() {
      this.$refs.bankCardUpdate.openDialog(true, {
        channelId: '1118'
      })
    },
    // 编辑
    handleUpdate(row) {
      this.$refs.bankCardUpdate.openDialog(false, row)
    },
    submitAdd(isAdd) {
      if (isAdd) {
        this.doSearch()
      } else {
        this.getTableData()
      }
    },
    // 进单状态
    handleStatusChange(row) {
      updateMerchantQrcodeStatus({
        id: row.id,
        status: row.status
      }).then(() => {
        this.$message.success('修改成功')
        this.getTableData()
      })
    },
    // 删除
    handleDelete(row) {
      this.$confirm(`是否确认删除编号【${row.id}】通道码?`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMerchantQrcode({id: row.id}).then(() => {
          this.$message.success('删除成功')
          this.getTableData()
        })
      })
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        channelId: '1118',
        nickName: '',
        accountNumber: '',
        status: ''
      }
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
