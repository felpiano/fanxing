<template>
   <div class="flex-center merchant-nav">
    <el-button type="primary" size="mini" class="min-button" @click="handleChannel">通道费率</el-button>
    <el-button type="primary" size="mini" class="min-button" @click="handleAmount">转移余额</el-button>
    <div class="flex-center" style="margin: 0 15px">
      <!-- <p class="merchant-balance">
        <span>总余额：</span>
        <b class="text-green1">{{ merchant.totalBalance || 0}}</b>
      </p> -->
      <p class="merchant-balance">
        <span>可用余额：</span>
        <b class="text-green1">{{ merchant.useBalance || 0}}</b>
      </p>
      <!-- <p class="merchant-balance">
        <span>押金：</span>
        <b class="text-green1">{{ merchant.baseDeposit || 0}}</b>
      </p>
      <p class="merchant-balance">
        <span>下级余额：</span>
        <b class="text-green1">{{ merchant.childBalance || 0}}</b>
      </p> -->
      <el-button type="primary" plain size="mini" class="min-button" @click="handleMore">余额详情</el-button>

      <el-button type="primary" plain size="mini" class="min-button" @click="refreshBalance"><i class="el-icon-refresh"></i>刷新</el-button>
    </div>
    <el-switch
      :width="50"
      v-model="merchant.workStatus"
      :active-value="0"
      :inactive-value="1"
      active-text="开工"
      inactive-text="停工"
      @change="updateWorkStatus">
    </el-switch>
    <el-switch
      :width="75"
      v-model="merchant.orderRemind"
      :active-value="0"
      :inactive-value="1"
      active-text="来单提示"
      inactive-text="来单提示"
      @change="updateOrderRemind">
    </el-switch>

    <el-dialog title="资产明细" :visible.sync="isVisible" width="30%">
        <el-descriptions title="" :column="1" border :labelStyle="{'width': '138px'}">
          <el-descriptions-item label="总余额：">{{ merchant.totalBalance || 0 }} 元</el-descriptions-item>
          <el-descriptions-item label="可用余额：">{{ merchant.useBalance || 0 }} 元</el-descriptions-item>
          <el-descriptions-item label="押金：">{{ merchant.baseDeposit || 0 }} 元</el-descriptions-item>
          <el-descriptions-item label="下级余额：">{{ merchant.childBalance || 0 }} 元</el-descriptions-item>
        </el-descriptions>
    </el-dialog>

     <el-dialog title="转移余额" :visible.sync="isAmount" width="30%">
       <el-form :model="changeForm" :rules="changeRules" ref="changeForm" label-width="120px">
         <el-form-item label="转移用户ID" prop="toId">
           <el-input v-model="changeForm.toId"placeholder="请输入转移用户的ID" ></el-input>
         </el-form-item>
         <el-form-item label="转移金额" prop="amount">
           <el-input-number :min="0" v-model="changeForm.amount" placeholder="请输入金额" :controls="false" class="w100"></el-input-number>
         </el-form-item>
         <el-form-item label="谷歌验证码" prop="code">
           <el-input v-model="changeForm.code" placeholder="请输入谷歌验证码" maxlength="6"></el-input>
         </el-form-item>
       </el-form>
       <div slot="footer" class="dialog-footer">
         <el-button @click="closeDialog">关闭</el-button>
         <el-button type="primary" @click="submitAmount" :loading="loading">确认</el-button>
       </div>
     </el-dialog>
  </div>
</template>

<script>
import {getMerchantDetail, orderRemindMerchant, stopWrokByMerchant} from '@/api/merchant'
import { trimBalanceFree } from '@/api/merchantChild'
import { EventBus } from '@/utils/event-bus.js'

export default {
  data() {
    return {
      isVisible: false,
      isAmount: false,
      loading: false,
      merchant: {
        orderRemind: 0,
        workStatus: 0,
        balance: 0
      },
      changeForm: {
        toId: '',
        amount: null,
        code: ''
      },
      changeRules: {
        toId: [
          { required: true, message: '请输入转移的用户ID', trigger: 'blur' }
        ],
        amount: [
          { required: true, message: '请输入金额', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入谷歌验证码', trigger: 'blur' }
        ],
      }
    }
  },
  mounted() {
    this.getMerchant()
    // 更新余额
    EventBus.$on('updateBalance', () => {
      this.getMerchant()
    })
  },
  beforeDestroy() {
    EventBus.$off('updateBalance', () => {
      this.getMerchant()
    })
  },
  methods: {
    getMerchant() {
      getMerchantDetail({}).then(res => {
        let merchant = res.data || {}
        merchant.totalBalance = (merchant.balance + merchant.childBalance).toFixed(2)
        merchant.useBalance = (merchant.balance - merchant.baseDeposit).toFixed(2)
        this.merchant = merchant
        this.$store.commit('SET_MERCHANTLEVEL', merchant.merchantLevel)
        this.$store.commit('SET_AGENTID', merchant.agentId)
      })
    },
    updateWorkStatus(e) {
      this.$confirm(`确定${e === 1 ? '停工' : '开工'}吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        stopWrokByMerchant({workStatus: e}).then(() => {
          this.$message.success('操作成功')
          this.getMerchant()
        })
      }).catch(() => {
        this.merchant.workStatus = e === 1 ? 0 : 1
      })
    },
    updateOrderRemind(e) {
      this.$confirm(`确定${e === 1 ? '关闭来单提示' : '开启来单提示'}吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        orderRemindMerchant({orderRemind: e}).then(() => {
          this.$message.success('操作成功')
          this.getMerchant()
        })
      }).catch(() => {
        this.merchant.orderRemind = e === 1 ? 0 : 1
      })
    },
    refreshBalance() {
      this.getMerchant()
    },
    handleMore() {
      this.isVisible = true
    },
    handleChannel() {
      this.$router.push({path: '/MerchantPage/accountManage/msChannelInfo'})
    },
    handleAmount() {
      this.changeForm = {
        toId: '',
        amount: null,
        code: ''
      }
      this.isAmount = true

    },
    submitAmount() {
      this.$refs.changeForm.validate((valid) => {
        if (!valid) return
        this.loading = true
        trimBalanceFree(this.changeForm).then(res => {
          this.getMerchant()
          this.closeDialog()
          this.$message.success('转移成功')
        }).finally(() => {
          this.loading = false
        })
      })
    },
    closeDialog() {
      this.loading = false
      this.isAmount = false
      this.$refs.changeForm.resetFields()
    }
  }
}
</script>

<style lang="scss" scoped>
  .merchant-nav {
    margin-right: 0px;

    .el-switch {
      margin-right: 15px;
    }

    .merchant-balance {
      color: #5a5e66;
      font-size: 14px;
      margin-right: 8px;
      span {
        color: #666;
      }
      .text-green1 {
        font-size: 16px
      }
    }
  }
</style>
