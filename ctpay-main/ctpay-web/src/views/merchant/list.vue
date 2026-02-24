<!--
** 码商列表页
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table :table="tableConfig" @tableChangeCurrent="tableChangeCurrent" @tableChangeSize="tableChangeSize" :toHeight="90">
      <template v-slot:table-after v-if="isAgent">
        <div class="flex-center">
          <el-button v-hasPermi="['system:merchant:add']"
            type="primary" size="mini" @click="handleAddMerchant">
            <i class="el-icon-plus"></i>添加
          </el-button>
          <el-button v-hasPermi="['system:merchant:onkeyOpen']"
            type="success" size="mini" @click="handleStopStatus(0)">
            <i class="el-icon-video-play"></i>一键接单
          </el-button>
          <el-button v-hasPermi="['system:merchant:onkeyOpen']"
            type="danger" size="mini" @click="handleStopStatus(1)">
            <i class="el-icon-video-pause"></i>一键停止接单
          </el-button>
        </div>
      </template>
      <template v-slot:totalBalance="{ row }">
        <div>
          <p>{{ row.totalBalance || 0}}</p>
          <el-button
            v-hasPermi="['system:merchant:updateAmount']"
            type="success"
            class="min-button"
            @click="handleUpdateAmount(row, 'totalBalance')">
            增减
          </el-button>
        </div>
      </template>
      <template v-slot:useBalance="{ row }">
        <div>
          <p>{{ (row.totalBalance - row.baseDeposit).toFixed(2) || 0}}</p>
        </div>
      </template>
      <template v-slot:freezeAmount="{ row }">
        <div>
          <p>{{ row.freezeAmount }}</p>
          <el-button
            v-hasPermi="['system:merchant:trimFreezeToBalance']"
            type="success" class="min-button"
             @click="handleUpdateAmount(row, 'freezeAmount')">
             转余额
          </el-button>
        </div>
      </template>
      <template v-slot:baseDeposit="{ row }">
        <div class="w100">
          <p >{{ row.baseDeposit || 0}}</p>
        </div>
      </template>
      <template v-slot:weight="{ row }">
        <div>
          <el-input-number v-if="isAgent"
            size="mini"
            :controls="false"
            v-model="row.weight"
            @change="handleUpdateWeight(row)"
            class="weight-input">
          </el-input-number>
          <p v-else>{{ row.weight }}</p>
        </div>
      </template>
      <template v-slot:status="{ row }">
        <el-switch v-if="isAgent"
          :width="50"
          v-model="row.status"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'status')">
        </el-switch>
        <div class="text-center" v-else>
          <el-tag  size="mini" v-if="row.status === 0" type="success">开启</el-tag>
          <el-tag  size="mini" v-else type="danger">关闭</el-tag>
        </div>
      </template>
      <template v-slot:workStatus="{ row }">
        <el-switch v-if="isAgent"
          :width="50"
          v-model="row.workStatus"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'workStatus')">
        </el-switch>
        <div class="text-center" v-else>
          <el-tag size="mini" v-if="row.workStatus === 0" type="success">开工</el-tag>
          <el-tag size="mini" v-else type="danger">未开工</el-tag>
        </div>
      </template>
      <template v-slot:orderPermission="{ row }">
        <el-switch v-if="isAgent"
          :width="50"
          v-model="row.orderPermission"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'orderPermission')">
        </el-switch>
        <div class="text-center" v-else>
          <el-tag size="mini" v-if="row.orderPermission === 0" type="success">开启</el-tag>
          <el-tag size="mini" v-else type="danger">关闭</el-tag>
        </div>
      </template>
      <template v-slot:transAmount="{ row }">
        <el-switch v-if="isAgent"
          :width="50"
          v-model="row.transAmount"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'transAmount')">
        </el-switch>
        <div class="text-center" v-else>
          <el-tag size="mini" v-if="row.transAmount === 0" type="success">开启</el-tag>
          <el-tag size="mini" v-else type="danger">关闭</el-tag>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:googleSecret:clear']" type="danger" class="min-button" @click="handleClearGoogle(row)">清谷歌</el-button>
        <el-button v-hasPermi="['system:merchantChannel:list']" type="primary" class="min-button" @click="handleRate(row)">费率</el-button>
        <el-button v-hasPermi="['system:merchantChannel:list']" type="success" class="min-button" @click="handleOrderSet(row)">接单设置</el-button>
        <!--<el-button v-if="!row.telegramGroup || row.telegramGroup ===''" type="primary" class="min-button" @click="handlebindTG(row, '1')">绑定TG</el-button>
        <el-button v-else type="warning" class="min-button" @click="handlebindTG(row, '2')">解绑TG</el-button>-->
        <el-button v-hasPermi="['system:merchant:update']"
          type="primary" class="min-button"
          @click="handleEditor(row)">
          编辑
        </el-button>
        <el-button v-hasPermi="['system:merchant:delete']"
          type="danger" class="min-button"
          @click="deleteItem(row)">
           删除
        </el-button>
      </template>
    </km-table>

    <!-- 新增码商 -->
    <addMerchant ref="addMerchant" @refresh="doSearch"></addMerchant>

    <!-- 操作金额 -->
    <changeAmount ref="updateAmount" @refresh="getList"></changeAmount>

    <!--费率 ----  就是基础通道费率配置--->
    <basicConfigRate ref="basiceConfigRate" @refresh="getList"></basicConfigRate>

    <!--编辑码商-->
    <updateMerchant ref="updateMerchant" @refresh="getList"></updateMerchant>

    <!--绑定-->
    <bindTgGroup ref="bindTgGroup" @updateList="getList"></bindTgGroup>

    <!--接单设置-->
    <orderSet ref="orderSet" @refresh="getList"></orderSet>

    <!--佣金转余额-->
    <update-freeze-amount ref="updateFreezeAmountRef" @refresh="getList"></update-freeze-amount>

 </div>
</template>

<script>
import {
  clearGoogle,
  deleteMerchant,
  getMerchantyList,
  stopWrok,
  updateMerchantEntity
} from '@/api/merchant';
import addMerchant from './components/addMerchant.vue';
import changeAmount from './components/changeAmount.vue';
import updateMerchant from './components/updateMerchant.vue';
import basicConfigRate from './components/basicConfigRate.vue';
import bindTgGroup from '../shop/components/bindTgGroup.vue';
import orderSet from './components/orderSet.vue';
import UpdateFreezeAmount from './components/updateFreezeAmount.vue';

export default {
  name: "merchantList",
  components: {
    addMerchant,
    changeAmount,
    updateMerchant,
    basicConfigRate,
    bindTgGroup,
    orderSet,
    UpdateFreezeAmount
  },
  data() {
    return {
      searchData: [
        {
          type: 'input',
          model: 'userId',
          title: '用户ID',
          inputType: 'number',
          width: '140px',
          placeholder: '请输入用户ID',
          clearable: true
        },
        {
          type: 'input',
          model: 'userName',
          title: '码商名称',
          placeholder: '请输入码商名称',
          clearable: true
        },
        {
          type: 'input',
          model: 'parentName',
          title: '上级名称',
          placeholder: '请输入上级名称',
          clearable: true
        },
        {
          type: 'select',
          model: 'workStatus',
          title: '开工状态',
          placeholder: '请选择开工状态',
          clearable: true,
          width: '100px',
          option: [
            { label: '开工', value: '0'},
            { label: '未开工', value: '1'},
          ]
        },
        {
          type: 'select',
          model: 'orderPermission',
          title: '接单权限',
          placeholder: '请选择接单权限',
          clearable: true,
          width: '100px',
          option: [
            { label: '开启', value: '0'},
            { label: '关闭', value: '1'},
          ]
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          userId: '',
          userName: '',
          parentName: '',
          workStatus: '',
          orderPermission: ''
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
            width: 140
          },
          {
            value: 'totalBalance',
            label: '总余额(包含下级余额)',
            slot: 'totalBalance',
            align: 'center',
            width: 120
          },
          {
            value: 'useBalance',
            label: '可用余额',
            slot: 'useBalance',
            align: 'center',
            width: 100
          },
          // {
          //   value: 'prepayment',
          //   label: '押金', // 冻结余额
          //   slot: 'prepayment',
          //   align: 'center',
          //   width: 100
          // },
          {
            value: 'baseDeposit',
            label: '押金',
            tooltip: '取值来源于通道押金设置，多通道时默认取最大值',
            slot: 'baseDeposit',
            align: 'center',
            width: 100
          },
          {
            value: 'freezeAmount',
            slot: 'freezeAmount',
            label: '佣金',
            align: 'center',
            width: 100
          },
          {
            value: 'childFreezeAmount',
            label: '下级佣金汇总',
            align: 'center',
            width: 100
          },
          {
            value: 'minAmount',
            label: '最低接单金额',
            align: 'center',
            width: 100
          },
          // {
          //   value: 'parentName',
          //   label: '上级名称',
          //   width: 100,
          //   formatter: row => row.parentName || '--'
          // },
          // {
          //   value: 'parentPath',
          //   label: '上级层级',
          //   width: 80,
          //   formatter: row => row.parentPath || '--'
          // },
          // {
          //   value: 'weight',
          //   // slot: 'weight',
          //   label: '权重',
          //   align: 'center',
          //   width: 80,
          // },
          {
            value: 'status',
            slot: 'status',
            label: '账号状态',
            align: 'center',
            width: 80
          },
          {
            value: 'workStatus',
            slot: 'workStatus',
            label: '开工状态',
            align: 'center',
            width: 80
          },
          {
            value: 'orderPermission',
            slot: 'orderPermission',
            label: '接单权限',
            align: 'center',
            width: 80
          },
          {
            value: 'transAmount',
            slot: 'transAmount',
            label: '佣金划转',
            align: 'center',
            width: 80
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
  computed: {
    isAgent() {
      return this.$store.getters.identity === 3 && this.$store.getters.roles.includes('agent')
    }
  },
  mounted() {
    if (this.isAgent) {
      this.tableConfig.columns.push({
        value: 'operation',
        label: '操作',
        fixed: 'right',
        align: 'center',
        slot: 'operation',
        width: 280
      })
    }
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getList()
    },
    getList() {
      this.tableConfig.loading = true
      getMerchantyList({
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
    // 添加
    handleAddMerchant() {
      this.$refs.addMerchant.openDialog()
    },
    // 一键停工
    handleStopStatus(workStatus) {
      // 只有代理可以操作
      if (this.$store.getters.identity !== 3) {
        this.$message.error('暂无权限操作');
        return;
      }
      this.$confirm(`确认操作${workStatus ? '一键停止接单' : '一键接单'}吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.tableConfig.loading = true
        stopWrok({
          workStatus: workStatus
        }).then(() => {
          this.$message.success(workStatus ? '一键停止接单成功' : '一键接单成功');
          this.getList();
        }).finally(() => {
          this.tableConfig.loading = false
        });
      });
    },
    // 增减余额
    handleUpdateAmount(row, type) {
      if (type === 'freezeAmount') {
        this.$refs.updateFreezeAmountRef.openDialog(row)
      } else {
        this.$refs.updateAmount.openDialog(row, type)
      }
    },
    // 修改权重
    handleUpdateWeight(row) {
      this.tableConfig.loading = true
      updateMerchantEntity({
        userId: row.userId,
        weight: row.weight
      }).then(() => {
        this.$message.success('权重修改成功');
        this.getList();
      }).finally(() => {
        this.tableConfig.loading = false
      });
    },
    // 修改状态
    changeStatus(row, type) {
      this.tableConfig.loading = true
      updateMerchantEntity({
        userId: row.userId,
        [type]: row[type]
      }).then(() => {
        this.$message.success('修改成功');
        this.getList();
      }).finally(() => {
        this.tableConfig.loading = false
      });
    },
    // 清谷歌
    handleClearGoogle(row) {
      this.$confirm(`确认清除${row.userName}【${row.userId}】的谷歌密钥吗？`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        clearGoogle({userId: row.userId}).then(() => {
          this.$message.success('清除成功');
          this.getList();
        });
      });
    },
    // 费率
    handleRate(row) {
      this.$refs.basiceConfigRate.openDialog(row)
    },
    // 接单设置
    handleOrderSet(row) {
      this.$refs.orderSet.openDialog(row)
    },
    // 绑定TG
    handlebindTG(row, type) {
      if (type === '1') {
        this.$refs.bindTgGroup.open(row, 'shop')
      } else {
        this.$confirm(`确认解绑tg群组【${row.telegramGroup}】吗？`, '温馨提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
        }).then(res => {
          unBindTelegramGroup({userId: row.userId}).then(res => {
            this.$message.success('解绑成功')
            this.getList()
          })
        })
      }
    },
    // 编辑
    handleEditor(row) {
      this.$refs.updateMerchant.openDialog(row)
    },
    // 删除
    deleteItem(row) {
      this.$confirm(`确认删除【${row.userId}】吗?`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMerchant({userId: row.userId}).then(() => {
          this.$message.success('删除成功');
          this.getList();
        });
      });
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        userId: '',
        userName: '',
        parentName: '',
        workStatus: '',
        orderPermission: ''
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

<style lang="scss" scoped>
.weight-input {
  width: 60px;
  ::v-deep .el-input__inner {
    border: none;
    background: transparent;
    text-align: center;
    &:focus {
      outline: none;
      border: 1px solid #1890ff;
    }
  }
}
</style>
