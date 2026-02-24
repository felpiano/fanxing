<!--
** 商户列表
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
      <template v-slot:table-after>
        <div class="flex-center" v-if="isAgent">
          <el-button type="primary" size="mini" @click="addVendor" v-hasPermi="['system:shop:add']">
            <i class="el-icon-plus"></i>添加
          </el-button>
          <el-button type="danger" plain size="mini" @click="oneClickUnbindTG" v-hasPermi="['system:shop:bindTg']">
            一键解绑TG
          </el-button>
        </div>
      </template>
      <template v-slot:balance="{ row }">
        <div class="flex-center column">
          <p class="text-red1 text-center">{{ row.balance || 0}}</p>
          <el-button v-hasPermi="['system:shop:chargeAmount']" type="success" class="min-button" size="mini" @click="handleRecharge(row)">
            增减
          </el-button>
        </div>
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
          <el-button v-hasPermi="['system:shop:bindTg']" type="primary" icon="el-icon-edit" circle class="min-button" @click="handlebindTG(row)"></el-button>
        </div>
      </template>
      <template v-slot:status="{ row }">
        <div v-if="isAgent || $store.getters.identity === 1" v-hasPermi="['system:shop:updateStatus']">
          <el-switch
            :width="50"
            v-model="row.status"
            :active-value="0"
            :inactive-value="1"
            active-text="正常"
            inactive-text="禁止"
            @change="changeStatus(row)">
          </el-switch>
        </div>
        <div v-else>
          <el-tag size="mini" v-if="row.status === 0" type="primary">正常</el-tag>
          <el-tag size="mini" v-else type="danger">禁止</el-tag>
        </div>
      </template>
      <template v-slot:memberNameFlag="{ row }">
        <div v-if="isAgent || $store.getters.identity === 1" v-hasPermi="['system:shop:updateStatus']">
          <el-switch
            :width="50"
            v-model="row.memberNameFlag"
            :active-value="0"
            :inactive-value="1"
            active-text="开启"
            inactive-text="关闭"
            @change="changeNameStatus(row)">
          </el-switch>
        </div>
        <div v-else>
          <el-tag size="mini" v-if="row.memberNameFlag === 0" type="primary">开启</el-tag>
          <el-tag size="mini" v-else type="danger">关闭</el-tag>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:shop:showSecret']"
          type="success" class="min-button"
          @click="handleUserKey(row)">
          开户信息
        </el-button>
        <el-button v-hasPermi="['system:shopBase:list']"
          type="primary" class="min-button"
          @click="handleBasic(row)">
          支付开关
        </el-button>
        <el-button v-hasPermi="['system:shopBase:list']"
          type="primary" class="min-button"
         @click="handleRate(row)">
          费率
        </el-button>
        <el-button v-hasPermi="['system:shop:update']"
          type="primary" class="min-button" @click="handleEditor(row)">
          编辑
        </el-button>
        <el-button
          v-hasPermi="['system:shop:showSecret']"
         type="success" class="min-button"
         @click="handleDetail(row)">
          码商
        </el-button>
        <el-button v-hasPermi="['system:shop:delete']"
          type="danger" class="min-button" @click="deleteItemShop(row)">
          删除
        </el-button>
      </template>
    </km-table>

    <!-- 安全码验证-->
    <SafeCode ref="safeCode" @submit="handleSecretKey"></SafeCode>

    <!--秘钥-->
    <el-dialog title="商户秘钥" append-to-body :visible.sync="secretKeyInfo.isVisible" width="600px">
      <el-form label-width="120px">
        <el-form-item label="商户ID">
          <p>{{ secretKeyInfo.userId }}</p>
        </el-form-item>
        <el-form-item label="商户秘钥">
         <p>{{ secretKeyInfo.secretKey || '--'}}</p>
        </el-form-item>
        <el-form-item label="下单地址">
         <p>{{ secretKeyInfo.submitOrderUrl || '--'}}</p>
        </el-form-item>
        <el-form-item label="回调地址">
         <p>{{ secretKeyInfo.clientIp || '--'}}</p>
        </el-form-item>
        <!--<el-form-item label="">
          <el-button type="warning" @click="handleSetKey">重置秘钥</el-button>
          <el-button type="primary" @click="handleCopy">复制</el-button>
        </el-form-item>-->
      </el-form>
      <div slot="footer" class="dialog-footer">
          <el-button @click="secretKeyInfo.isVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCopy">复制</el-button>
        </div>
    </el-dialog>

    <!-- 新增商户 -->
    <addShop ref="addShop" @refresh="doSearch"></addShop>

    <!-- 编辑商户-->
     <updateShop ref="updateShop" @refresh="getList"></updateShop>

     <!--支付开关  --  就是基础通道状态配置-->
     <payConfig ref="PayConfig"></payConfig>

     <!--费率 ----  就是基础通道费率配置--->
     <basic-config-rate ref="basiceConfigRate"></basic-config-rate>

     <!--tg-->
    <tgAdmin ref="tgAdmin" @updateList="getList"></tgAdmin>

    <!--绑定-->
    <bindTgGroup ref="bindTgGroup" @updateList="getList"></bindTgGroup>

    <!--充值-->
    <chargeAmountShop ref="chargeAmountShop" @refresh="getList"></chargeAmountShop>

    <bindMerchant ref="bindMerchantRef"/>
  </div>
</template>

<script>
import {
  deleteShop,
  getShopDetail,
  getShopEntityList,
  resetScret,
  showSecret,
  unBindTelegramGroupOneKey,
  upateStatus,
  updateMemberFlag
} from '@/api/shop'
import useClipboard from "vue-clipboard3"
import addShop from './components/addShop.vue';
import updateShop from './components/updateShop.vue';
import payConfig from './components/payConfig.vue';
import basicConfigRate from './components/basicConfigRate.vue';
import tgAdmin from './components/tgAdmin.vue';
import bindTgGroup from './components/bindTgGroup.vue';
import chargeAmountShop from './components/updateBalance.vue';
import bindMerchant from '@/views/shop/components/bindMerchant.vue'

export default {
  name: "index",
  components: {
    addShop,
    updateShop,
    payConfig,
    basicConfigRate,
    tgAdmin,
    bindTgGroup,
    chargeAmountShop,
    bindMerchant
  },
  data(vm) {
    return {
      searchData: [
        {
          type: 'input',
          model: 'userId',
          title: '商户ID',
          inputType: 'number',
          placeholder: '请输入商户ID',
          clearable: true
        },
        {
          type: 'input',
          model: 'userName',
          title: '商户名称',
          placeholder: '请输入商户名称',
          clearable: true
        },
        {
          type: 'input',
          model: 'agentName',
          title: '代理用户名',
          placeholder: '请输入代理用户名',
          clearable: true
        },
        {
          type: 'select',
          model: 'status',
          title: '商户状态',
          placeholder: '请选择一个状态',
          clearable: true,
          option: [
            { label: '正常', value: '0'},
            { label: '禁止', value: '1'},
          ]
        },
        {
          type: 'inputRange',
          modelName: ['minAmount', 'maxAmount'],
          title: '金额范围',
          placeholder: ['请输入最小金额', '请输入最大金额'],
          clearable: true,
        }
      ],
      tableConfig: {
        loading: false,
        searchQuery: {
          userId: '',
          userName: '',
          status: '',
          agentName: '',
          minAmount: '',
          maxAmount: ''
        },
        data: [],
        columns: [
          {
            value: 'userId',
            label: '商户ID',
            width: 80
          },
          {
            value: 'userName',
            label: '商户名称',
            width: 120
          },
          {
            value: 'agentName',
            label: '代理用户',
            width: 120
          },
          {
            value: 'balance',
            slot: 'balance',
            label: '余额(元)',
            align: 'center',
            width: 120
          },
          {
            value: 'minAmount',
            label: '最小金额(元)',
            align: 'center',
            width: 100,
            formatter: (row) => {
              return !row.minAmount ? '无限制' : row.minAmount
            }
          },
          {
            value: 'maxAmount',
            label: '最大金额(元)',
            align: 'center',
            width: 100,
            formatter: (row) => {
              return !row.maxAmount ? '无限制' : row.maxAmount
            }
          },
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
            value: 'lastLoginIp',
            label: '最后登录IP',
            width: 120,
            formatter: (row) => {
              return row.lastLoginIp || '--'
            }
          },
          {
            value: 'lastLoginTime',
            label: '最后登录时间',
            width: 160,
            formatter: (row) => {
              return row.lastLoginTime || '--'
            }
          },
          {
            value: 'status',
            label: '状态',
            align: 'center',
            slot: 'status',
            width: 80
          },
          {
            value: 'memberNameFlag',
            label: '收银台姓名',
            align: 'center',
            slot: 'memberNameFlag',
            width: 120
          }
        ],
        hasPagination: true,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      },
      // 秘钥
      secretKeyInfo: {
        isVisible: false,
        safeCode: '',
        userId: '',
        secretKey: '',
        submitOrderUrl: '',
        clientIp: ''
      }
    }
  },
  computed: {
    // 只有代理才能操作
    isAgent() {
      return this.$store.getters.roles.includes('agent') && this.$store.getters.identity === 3
    }
  },
  mounted() {
    if (this.isAgent) {
      this.searchData.splice(2, 1)
      this.tableConfig.columns.splice(2, 1)
      this.tableConfig.columns.push({
        label: '操作',
        slot: 'operation',
        width: 360,
        align: 'center',
        fixed: 'right'
      })
    }
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.tableConfig.pagination.pageNum = 1
      this.getList()
    },
    // 商户列表
    getList() {
      this.tableConfig.loading = true
      getShopEntityList({
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
    // 新增商户
    addVendor() {
      this.$refs.addShop.openDialog()
    },
    // 编辑
    handleEditor(row) {
      getShopDetail({
        userId: row.userId
      }).then(res => {
        this.$refs.updateShop.openDialog(res.data)
      })
    },
    handleDetail(row) {
      this.$refs.bindMerchantRef.open(row)
    },
    // tg管理员
    editTelegram(row) {
      this.$refs.tgAdmin.open(row, 'shop')
    },
    // 一键解绑TG
    oneClickUnbindTG(){
      this.$confirm('确认解绑所有商户的TG群组吗？', '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then(res => {
        unBindTelegramGroupOneKey().then(res => {
          this.$message.success('解绑成功')
          this.doSearch()
        })
      })
    },
    // 更新商状态-启用 禁用
    changeStatus(row) {
      upateStatus({
        userId: row.userId,
        status: row.status
      }).then(res => {
        this.$message.success('操作成功')
        this.getList()
      })
    },
    changeNameStatus(row) {
      updateMemberFlag({
        userId: row.userId,
        memberFlag: row.memberNameFlag
      }).then(res => {
        this.$message.success('操作成功')
        this.getList()
      })
    },
    // 支付开关
    handleBasic(row) {
      this.$refs.PayConfig.open(row)
    },
    // 费率
    handleRate(row) {
      this.$refs.basiceConfigRate.open(row)
    },
    // 删除商户
    deleteItemShop(row) {
      this.$confirm(`确认删除${row.userName}-${row.shopName} 商户吗？`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then(res => {
        deleteShop({
          userId: row.userId
        }).then(res => {
          this.$message.success('删除成功')
          this.getList()
        })
      })
    },
    handleUserKey(row) {
      this.secretKeyInfo.userId = row.userId
      this.secretKeyInfo.submitOrderUrl = row.submitOrderUrl
      this.secretKeyInfo.clientIp = row.clientIp
      this.handleSecretKey(row.userId)
    },
    // 查看秘钥
    handleSafeCode(row) {
      this.secretKeyInfo.userId = row.userId
      this.$refs.safeCode.show()
    },
    handleSecretKey(userId) {
      showSecret({
        userId: userId,
        safeCode: ''
      }).then(res => {
        this.secretKeyInfo.secretKey = res.data || ''
        this.secretKeyInfo.isVisible = true
      })
    },
    // 重制秘钥
    handleSetKey() {
      this.$confirm(`确认重置商户【${this.secretKeyInfo.userId}】秘钥吗？`, '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      }).then(res => {
        resetScret({
          userId: this.secretKeyInfo.userId
        }).then(res => {
          this.secretKeyInfo.secretKey = res.data || ''
          this.$message.success('重置成功')
        })
      })
    },
    // 复制秘钥
    handleCopy() {
      if (!this.secretKeyInfo.secretKey) {
        this.$message.warning('暂无秘钥')
        return
      }
      const { toClipboard } = useClipboard()
      const text = `用户ID：${this.secretKeyInfo.userId}; 商户秘钥：${this.secretKeyInfo.secretKey}; 下单地址：${this.secretKeyInfo.submitOrderUrl}; 回调地址：${this.secretKeyInfo.clientIp}`
      toClipboard(text)
      this.$message.success('复制成功')
    },
    // 绑定/解绑TG
    handlebindTG(row) {
      this.$refs.bindTgGroup.open(row, 'shop')
    },
    // 充值
    handleRecharge(row) {
      this.$refs.chargeAmountShop.openDialog(row)
    },
    // 查看tg
    handleCheckTG(row) {
      this.$refs.tgAdmin.open(row)
    },
     // 重置
     resetTable() {
      this.tableConfig.searchQuery = {
        userId: '',
        userName: '',
        status: '',
        agentName: '',
        minAmount: '',
        maxAmount: ''
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
