<!--
** 所有码商列表页--展示所有下级码商
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
        <div class="flex-center" style="margin-bottom: 10px">
          <p style="font-size: 14px">佣金划转：</p>
          <el-switch
            :width="50"
            v-model="commissionType"
            :active-value="0"
            :inactive-value="1"
            active-text="开启"
            inactive-text="关闭"
            @change="updateCommissionType">
          </el-switch>
        </div>
      </template>
      <template v-slot:userName="{ row }">
        <div>
          <p class="w100 ellipsis text-center">{{ row.userName }}</p>
          <el-button v-if="row.merchantLevel != 1" v-hasPermi="['system:merchant:allParent']" type="text" size="mini" class="min-button" @click="lookAllParent(row)">查看上级码商</el-button>
        </div>
      </template>
      <template v-slot:balance="{ row }">
        <div>
          <p>{{ (row.balance - row.baseDeposit).toFixed(2) || 0}}</p>
          <el-button
            v-hasPermi="['system:merchant:updateAmount']"
            type="success"
            class="min-button"
            @click="handleUpdateAmount(row, 'balance')">
            划转
          </el-button>
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
      <template v-slot:weight="{ row }">
        <div v-if="isAgent">
          <el-input-number
            size="mini"
            :controls="false"
            v-model="row.weight"
            @change="handleUpdateWeight(row)"
            class="weight-input">
          </el-input-number>
        </div>
        <p v-else>{{ row.weight }}</p>
      </template>
      <template v-slot:telegram="{ row }">
        <div class="text-center">
          <p class="w100 ellipsis">{{ row.telegram }}</p>
          <el-button type="primary" icon="el-icon-edit" circle class="min-button" @click="editTelegram(row)"></el-button>
        </div>
      </template>
      <template v-slot:telegramGroup="{ row }">
        <div class="text-center">
          <p class="w100 ellipsis">{{ row.telegramGroup }}</p>
          <el-button v-hasPermi="['system:merchant:bindTg']" type="primary" icon="el-icon-edit" circle class="min-button" @click="handlebindTG(row)"></el-button>
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
          <el-tag  size="mini" v-if="row.workStatus === 0" type="success">开工</el-tag>
          <el-tag  size="mini" v-else type="danger">未开工</el-tag>
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
          <el-tag  size="mini" v-if="row.orderPermission === 0" type="success">开启</el-tag>
          <el-tag  size="mini" v-else type="danger">关闭</el-tag>
        </div>
      </template>
      <template v-slot:transAmount="{ row }">
        <el-switch
          v-if="isAgent"
          :width="50"
          v-model="row.transAmount"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus(row, 'transAmount')">
        </el-switch>
        <div class="text-center" v-else>
          <el-tag  size="mini" v-if="row.transAmount === 0" type="success">开启</el-tag>
          <el-tag  size="mini" v-else type="danger">关闭</el-tag>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:merchantQrcode:list']" type="primary" class="min-button" @click="handleChannel(row)">通道</el-button>
        <el-button v-hasPermi="['system:googleSecret:clear']" type="danger" class="min-button" @click="handleClearGoogle(row)">清谷歌</el-button>
        <el-button v-hasPermi="['system:merchantChannel:list']" type="success" class="min-button" @click="handleOrderSet(row)">接单设置</el-button>
        <el-button v-hasPermi="['system:googleSecret:clear']" type="success" class="min-button" @click="handleUnlockUser(row)">解除锁定</el-button>
        <el-button v-hasPermi="['system:merchant:update']" type="primary" class="min-button" @click="handleEditor(row)">编辑</el-button>
        <el-button v-hasPermi="['system:merchantChild:delete']" type="danger" class="min-button" @click="handleDeleteUser(row)">删除</el-button>
      </template>
    </km-table>

    <!-- 操作金额 -->
    <changeAmount ref="updateAmount" @refresh="getList"></changeAmount>

    <!-- 查看上级码商 -->
    <all-parent ref="allParentRef"></all-parent>

    <!--佣金转余额-->
    <update-freeze-amount ref="updateFreezeAmountRef" @refresh="getList"></update-freeze-amount>

    <!--接单设置-->
    <orderSet ref="orderSet" @refresh="getList"></orderSet>

    <!--tg-->
    <tgAdmin ref="tgAdmin" @updateList="getList"></tgAdmin>

    <!--绑定-->
    <bindTgGroup ref="bindTgGroup" @updateList="getList"></bindTgGroup>

    <!--编辑码商-->
    <updateMerchant ref="updateMerchant" @refresh="getList"></updateMerchant>

    <ChannelList ref="ChannelListRef"/>

 </div>
</template>

<script>
import {clearGoogle, getAllMerchantyList, queryCommissionType, updateMerchantEntity, unlockUser, deleteMerchant} from '@/api/merchant';
import {getAgentDetail} from '@/api/agent'
import {getChannelListAll} from '@/api/channel'
import changeAmount from './components/changeAmount.vue';
import allParent from '@/views/order/components/allParent.vue';
import orderSet from './components/orderSet.vue';
import UpdateFreezeAmount from './components/updateFreezeAmount.vue';
import tgAdmin from '@/views/shop/components/tgAdmin.vue';
import bindTgGroup from '@/views/shop/components/bindTgGroup.vue';
import updateMerchant from '@/views/merchant/components/updateMerchant.vue'
import ChannelList from '@/views/merchant/components/channelList.vue'

export default {
  name: "AllMerchantList",
  components: {
    updateMerchant,
    changeAmount,
    allParent,
    UpdateFreezeAmount,
    orderSet,
    tgAdmin,
    bindTgGroup,
    ChannelList
  },
  data() {
    return {
      searchData: [
        {
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
          type: 'input',
          model: 'parentPath',
          title: '层级路径',
          placeholder: '请输入层级路径',
          clearable: true
        },
        {
          type: 'select',
          model: 'workStatus',
          title: '开工状态',
          placeholder: '请选择开工状态',
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
          placeholder: '请选择接单权限',
          clearable: true,
          option: [
            { label: '开启', value: '0'},
            { label: '关闭', value: '1'},
          ]
        },
        {
          type: 'select',
          model: 'channelId',
          title: '通道名称',
          placeholder: '请选择通道名称',
          clearable: true,
          option: []
        },
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          userId: '',
          userName: '',
          parentName: '',
          workStatus: '',
          orderPermission: '',
          parentPath: '',
          channelId: ''
        },
        data: [],
        columns: [
          {
            value: 'userId',
            label: '用户ID',
            width: 80
          },
          {
            value: 'userName',
            slot: 'userName',
            align: 'center',
            label: '码商名称',
            width: 120
          },
          {
            value: 'merchantLevel',
            label: '码商层级',
            align: 'center',
            width: 80,
            formatter: row => row.merchantLevel ? (Number(row.merchantLevel) - 1) : '--'
          },
          {
            value: 'balance',
            label: '余额(不包含下级余额)',
            slot: 'balance',
            align: 'center',
            width: 100
          },
          {
            value: 'baseDeposit',
            label: '押金',
            tooltip: '取值来源于通道押金设置，多通道时默认取最大值',
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
            value: 'minAmount',
            label: '最低接单金额',
            align: 'center',
            width: 100
          },
          {
            value: 'parentName',
            label: '上级名称',
            width: 100,
            formatter: row => row.parentName || '--'
          },
          {
            value: 'parentPath',
            label: '层级路径',
            width: 180,
            formatter: row => row.parentPath || '--'
          },
          // {
          //   value: 'weight',
          //   slot: 'weight',
          //   label: '权重',
          //   align: 'center',
          //   width: 100,
          // },
          {
            value: 'telegram',
            slot: 'telegram',
            label: 'tg管理员(ID/名称)',
            align: 'center',
            width: 140
          },
          {
            value: 'telegramGroup',
            slot: 'telegramGroup',
            label: 'tg群组(ID/名称)',
            width: 140
          },
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
          },
          {
            value: 'createTime',
            label: '创建时间',
            width: 160
          },
          {
            value: 'updateTime',
            label: '更新时间',
            width: 160
          },
          {
            value: 'operation',
            label: '操作',
            fixed: 'right',
            align: 'center',
            slot: 'operation',
            width: 350
          }
        ],
        hasPagination: true,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      },
      commissionType: 0
    }
  },
  computed: {
    isAgent() {
      return this.$store.getters.identity === 3 && this.$store.getters.roles.includes('agent')
    }
  },
  mounted() {
    if (this.$store.getters.identity === 3) {
      this.getAgent()
    }
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
      getAllMerchantyList({
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
    getAgent() {
      getAgentDetail({}).then(res => {
        const agent = res.data || {}
        this.commissionType = agent.commissionType
      })
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
    // 获取通道
    getChannelList() {
      getChannelListAll({}).then(res => {
        let channelList = res.data || []
        this.$set(this.searchData[this.searchData.length - 1], 'option', channelList.map(item => {
          return {
            label: item.channelName,
            value: item.id
          }
        }))
      })
    },
    // 划转佣金
    updateCommissionType(type) {
      queryCommissionType({ commissionType: type }).then(res => {
        this.$message.success('操作成功')
      })
    },
    // 增减余额
    handleUpdateAmount(row, type) {
      if (type === 'freezeAmount') {
        this.$refs.updateFreezeAmountRef.openDialog(row)
      } else {
        this.$refs.updateAmount.openDialog(row, type)
      }
    },

  
    // 查看上级码商
    lookAllParent(row) {
      this.$refs.allParentRef.openDialog({
        merchantId: row.userId,
        merchantName: row.userName
      })
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
    // 通道列表
    handleChannel(row) {
      this.$refs.ChannelListRef.openDialog(row)
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
     // 接单设置
     handleOrderSet(row) {
      this.$refs.orderSet.openDialog(row, row.merchantLevel > 1 ? 'allList' : null)
    },
    //解除用户锁定
    handleUnlockUser(row) {
      this.$confirm(`确认解除${row.userName}【${row.userId}】的账号锁定吗？`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        unlockUser({userId: row.userId}).then(() => {
          this.$message.success('解除成功');
          this.getList();
        });
      });
    },
    // 删除码商
    handleDeleteUser(row) {
      this.$confirm(`确认删除${row.userName}【${row.userId}】码商吗？`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMerchant({userId: row.userId}).then(() => {
          this.$message.success('删除成功');
          this.getList();
        })
      })
    },
    // 编辑
    handleEditor(row) {
      this.$refs.updateMerchant.openDialog(row)
    },
     // tg管理员
     editTelegram(row) {
      this.$refs.tgAdmin.open(row, 'merchant')
    },
     // 绑定/解绑TG
     handlebindTG(row) {
      this.$refs.bindTgGroup.open(row, 'merchant')
    },
    // 重置
    resetTable() {
      this.tableConfig.searchQuery = {
        userId: '',
        userName: '',
        parentName: '',
        workStatus: '',
        orderPermission: '',
        parentPath: '',
        channelId: ''
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
  width: 68px;
  // ::v-deep .el-input__inner {
  //   border: none;
  //   background: transparent;
  //   text-align: center;
  //   &:focus {
  //     outline: none;
  //     border: 1px solid #1890ff;
  //   }
  // }
}
</style>
