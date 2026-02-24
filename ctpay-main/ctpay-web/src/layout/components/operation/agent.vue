<template>
  <div class="flex-center agent-nav">
    <el-button
      v-hasPermi="['system:order:createTest']"
      type="success" size="small"
      @click="handleTestOrder">
      测试收银台
    </el-button>

    <p class="agent-balance">
      <span :class="{'text-red': agent.balance < 100}">余额：</span>
      <b :class="[agent.balance < 100 ? 'text-red' : 'text-green1']">{{ agent.balance || 0}}</b>
    </p>

    <el-button type="primary" plain size="mini" class="min-button" @click="refreshBalance"><i class="el-icon-refresh"></i>刷新</el-button>

    <!--创建测试单-->
    <createTestOrder ref="createTestOrder"></createTestOrder>
  </div>
</template>

<script>
import createTestOrder from '@/views/order/components/createTestOrder.vue'
import {getAgentDetail} from '@/api/agent'

export default {
  components: {
    createTestOrder
  },
  data() {
    return {
      agent: {
        balance: 0
      }
    }
  },
  mounted() {
    this.getAgent()
  },
  methods: {
    getAgent() {
      getAgentDetail({}).then(res => {
        this.agent = res.data || {}
      })
    },
    // 创建测试订单
    handleTestOrder() {
      this.$refs.createTestOrder.openDialog()
    },
    // 刷新余额
    refreshBalance() {
      this.getAgent()
    }
  }
}
</script>

<style scoped lang="scss">
  .agent-nav {
    .agent-balance {
      margin: 0 15px;
      span {
        color: #666;
        font-size: 14px;
      }
      .text-red {
        color: #ff0000;
      }
      b {
        font-size: 16px;
      }
    }

  }
</style>
