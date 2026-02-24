<!--
** 码商首页
-->
<template>
  <div class="app-container agent-home">
    <div class="card-div">
      <div class="radio-div flex-center">
        <el-radio-group v-model="type">
          <el-radio-button label="1">今日</el-radio-button>
          <el-radio-button label="2">昨日</el-radio-button>
          <el-radio-button label="3">本周</el-radio-button>
        </el-radio-group>
      </div>

      <div class="agent-account">
        <ul class="flex-center">
          <li>
            <CountTo class="count-number" :endVal="overview.totalAmount"></CountTo>
            <p>总交易金额(元)</p>
          </li>
          <li>
            <CountTo class="count-number" :endVal="overview.totalCount"></CountTo>
            <p>总成功订单</p>
          </li>
          <li>
            <CountTo class="count-number" :endVal="overview.childAmount"></CountTo>
            <p>下级总跑量(元)</p>
          </li>
          <li>
            <CountTo class="count-number" :endVal="overview.agentFee"></CountTo>
            <p>总提成金额(元)</p>
          </li>
          <li>
            <CountTo class="count-number" :endVal="overview.merchantBalance"></CountTo>
            <p>码商剩余总余额(元)</p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import {getAgentHomeReport} from '@/api/report'
import CountTo from '@/components/CountTo/index.vue'

export default {
  name: 'AgentPage',
  components: {
    CountTo
  },
  data(){
    return {
      type: '1',
      overview: {}
    }
  },
  watch: {
    type() {
      this.getReport()
    }
  },
  created() {
   this.getReport()
  },
  methods: {
    getReport() {
      getAgentHomeReport({type: this.type}).then(res => {
        let data = res.data || {}
        // 如果参数为null，赋值为0
        for (let key in data) {
          if (data[key] === null) {
            data[key] = 0
          }
        }
        this.overview = data
      })
    }
  }
}
</script>

<style scoped lang="scss">
.agent-home {
  padding: 10px;
  position: relative;
  background: #f7f9fa;
  .radio-div {
    background: #fff;
    border-radius: 5px 5px 0 0;
    padding: 0 15px 30px 0;
    justify-content: center;
  }
  .card-div {
    position: relative;
    z-index: 22;
    background: #fff;
    border-radius: 5px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.1);
    .title-h3 {
      display: flex;
      align-items: center;
      width: 100%;
      font-size: 18px;
      color: #333;
      position: relative;
      padding-bottom: 15px;
      &::before {
        content: '';
        display: block;
        width: 12px;
        height: 12px;
        border-radius: 20px;
        background: #04b350;
        margin-right: 10px;
      }
      .red {
        color: #ff4d4f;
        font-size: 14px;
        margin-left: 10px;
      }
    }
  }

  ul {
    margin-bottom: 20px;
    li {
      width: 18%;
      text-align: center;
      padding: 0 10px;
      background: #fff;
      border-right: 1px solid #f0f0f0;
      // background: #40f58e;
      p {
        font-size: 16px;
        color: #382a2a;
        margin-bottom: 10px;
      }
      .count-number {
        font-size: 26px;
        font-weight: 700;
        color: #04b350;
      }

      &:nth-child(2) {
        .count-number {
          color: #ff6cf3;
        }
        // background: #f5b350;
      }
      &:nth-child(3) {
        .count-number {
          color: #ff4d4f;
        }
        // background: #ff4d4f;
      }
      &:nth-child(4) {
        .count-number {
          color: #1890ff;
        }
        // background: rgba(24, 144, 255);
      }
      &:nth-child(5) {
        .count-number {
          color: #2937ff;
        }
        // background: rgba(24, 144, 255);
      }
      &:last-child {
        border-right: none;
      }
    }
  }
}
</style>
