<!--
** 下级管理--下级服务商
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
        <div class="w100">
          <div class="flex-center" v-if="$store.getters.merchantLevel !== 5">
            <el-button v-hasPermi="['system:merchantChild:addChild']"
              type="primary" size="mini" @click="handleAddMerchant">
              <i class="el-icon-plus"></i>添加
            </el-button>
            <el-button v-hasPermi="['system:merchantChild:onkeyOff']"
              type="success" size="mini" @click="handleStopOrder(0)">
              <i class="el-icon-video-pause"></i>开启下级接单
            </el-button>
            <el-button v-hasPermi="['system:merchantChild:onkeyOff']"
              type="danger" size="mini" @click="handleStopOrder(1)">
              <i class="el-icon-video-play"></i>关闭下级接单
            </el-button>
          </div>
        </div>
      </template>
      <template v-slot:status="{ row }">
        <el-switch
          :width="50"
          v-model="row.status"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'status')">
        </el-switch>
      </template>
      <template v-slot:workStatus="{ row }">
        <el-switch
          :width="50"
          v-model="row.workStatus"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'workStatus')">
        </el-switch>
      </template>
      <template v-slot:orderPermission="{ row }">
        <el-switch
          :width="50"
          v-model="row.orderPermission"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'orderPermission')">
        </el-switch>
      </template>
      <template v-slot:teamStatus="{ row }">
        <el-switch
          :width="50"
          v-model="row.teamStatus"
          :active-value="0"
          :inactive-value="1"
          :active-text="'正常'"
          :inactive-text="'禁用'"
          @change="changeStatus(row, 'teamStatus')">
        </el-switch>
      </template>
      <template v-slot:totalBalance="{ row }">
        <div>
          <p>{{ row.totalBalance ? (row.totalBalance - (row.baseDeposit || 0)).toFixed(2) : 0 }}</p>
          <!--v-if="row.showTrimBalance === 0"-->
          <el-button
            v-hasPermi="['system:merchantChild:trimBalance']"
            type="success" class="min-button"
             @click="handleUpdateAmount(row)">
             转移余额
          </el-button>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:merchantChild:channelList']"
          type="primary" class="min-button"
          @click="handleRate(row)">
          费率
        </el-button>
        <el-button v-hasPermi="['system:merchantChild:updateChild']"
          type="primary" class="min-button"
          @click="handleEditor(row)">
          编辑
        </el-button>
        <el-button v-hasPermi="['system:merchantChild:delete']"
          type="danger" class="min-button"
          @click="deleteItem(row)">
           删除
        </el-button>
      </template>
    </km-table>

      <!-- 新增下级码商 -->
      <addMerchant ref="addMerchant" @refresh="doSearch"></addMerchant>

      <!--转移余额-->
      <transferBalance ref="transferBalance" @refresh="getList"></transferBalance>

      <!--费率-->
      <basicConfigRate ref="basicConfigRate" @refresh="getList"></basicConfigRate>

      <!--编辑-->
      <updateMerchant ref="updateMerchant" @refresh="getList"></updateMerchant>

  </div>
</template>

<script>
import addMerchant from './components/addMerchant'
import transferBalance from './components/transferBalance'
import basicConfigRate from './components/basicConfigRate.vue'
import updateMerchant from './components/updateMerchant'
import { getMerchantChildAllList, updateMerchantChild, deleteMerchantChild, onkeyOff } from '@/api/merchantChild'
import { getMerchantaAll } from '@/api/merchant'
export default {
  name: 'subordinateManage',
  components: {
    addMerchant,
    transferBalance,
    basicConfigRate,
    updateMerchant
  },
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'userName',
          title: '用户名',
          placeholder: '请输入用户名',
          clearable: true
        },
        {
          type: 'select',
          model: 'status',
          title: '开工状态',
          placeholder: '全部',
          clearable: true,
          option: [
            { label: '开工', value: '0'},
            { label: '未开工', value: '1'},
          ]
        },
        {
          type: 'select',
          model: 'orderPermission',
          title: '接单权限',
          placeholder: '全部',
          clearable: true,
          option: [
            { label: '开启',  value: 0 },
            { label: '关闭', value: 1 }
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
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          userName: '',
          status: '',
          orderPermission: '',
          parentPath: ''
        },
        data: [],
        columns: [
          {
            value: 'userId',
            label: 'ID',
            width: 80
          },
          {
            value: 'userName',
            label: '用户名',
            width: 120
          },
          {
            value: 'merchantLevel',
            label: '层级',
            width: 80,
            align: 'center',
            formatter: row => row.merchantLevel ? (Number(row.merchantLevel) - 1) : '--'
          },
          {
            value: 'parentName',
            label: '上级',
            align: 'center',
            width: 120
          },
          {
            value: 'totalBalance',
            slot: 'totalBalance',
            label: '余额',
            width: 120,
            align: 'center'
          },
          {
            value: 'baseDeposit',
            label: '押金',
            width: 120,
            align: 'center',
            formatter: row => row.baseDeposit || 0
          },
          {
            value: 'minAmount',
            label: '最低接单金额',
            align: 'center',
            width: 100
          },
          {
            value: 'status',
            slot: 'status',
            label: '账号状态',
            align: 'center',
            width: 90
          },
          {
            value: 'workStatus',
            slot: 'workStatus',
            label: '开工状态',
            align: 'center',
            width: 90
          },
          {
            value: 'orderPermission',
            slot: 'orderPermission',
            label: '接单权限',
            width: 100,
            align: 'center'
          },
          // {
          //   value: 'teamStatus',
          //   slot: 'teamStatus',
          //   label: '团队接单状态',
          //   align: 'center',
          //   width: 100
          // },
          // {
          //   value: '',
          //   label: '昨日跑量',
          //   width: 120,
          //    align: 'center'
          // },
          // {
          //   value: '',
          //   label: '今日跑量',
          //   width: 120,
          //    align: 'center'
          // },
          {
            value: 'createTime',
            label: '添加时间',
            width: 160
          },
          {
            value: 'operation',
            label: '操作',
            align: 'center',
            fixed: 'right',
            slot: 'operation',
            width: 220
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
    this.getMerchantaAllView()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getList()
    },
    getList() {
      this.tableConfig.loading = true
      getMerchantChildAllList({
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
    getMerchantaAllView() {
      getMerchantaAll({
        parentId: this.$store.getters.id
      }).then(res => {
        let option = res.data || []
        this.searchData[3].option = option.map(item => {
          return {
            label: item.userName,
            value: item.parentPath
          }
        })
      })
    },
    // 添加
    handleAddMerchant() {
      this.$refs.addMerchant.openDialog()
    },
    handleStopOrder(status) {
      this.$confirm(`确认是否${status === 0 ? '开启' : '关闭'}所有下级接单权限`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        onkeyOff({
          orderPermission: status
        }).then(res => {
          this.$message.success('操作成功')
          this.doSearch()
        })
      })
    },
    // 修改接单状态
    changeStatus(row, key) {
      updateMerchantChild({
        userId: row.userId,
        [key]: row[key]
      }).then(res => {
        this.$message.success('操作成功')
        this.getList()
      })
    },
    // 转移余额
    handleUpdateAmount(row) {
      this.$refs.transferBalance.openDialog(row)
    },
    // 费率
    handleRate(row) {
      this.$refs.basicConfigRate.openDialog(row)
    },
    // 编辑
    handleEditor(row) {
      this.$refs.updateMerchant.openDialog(row)
    },
    // 删除
    deleteItem(row) {
      this.$confirm(`确认删除${row.userName}【${row.userId}】下级码商吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMerchantChild({
          userId: row.userId
        }).then(res => {
          this.$message.success('删除成功')
          this.getList()
        })
      })
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

