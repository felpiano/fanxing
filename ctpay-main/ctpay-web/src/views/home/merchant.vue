<!--
** 码商首页
-->
<template>
  <div class="app-container merchat-home">
    <div class="card-div merchat-account">
      <h3 class="title-h3">
        账户信息
      </h3>
      <ul class="flex-center">
        <li>
          <CountTo class="count-number" :endVal="overview.merchantBalance"></CountTo>
          <p>总余额(元)</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.childMerchantBalance"></CountTo>
          <p>下级余额(元)</p>
        </li>
      </ul>
    </div>

    <div class="card-div merchat-account">
      <h3 class="title-h3 today">
        今日统计 <span class="red">（注：只统计成功订单）</span>
      </h3>
      <ul class="flex-center">
        <li>
          <CountTo class="count-number" :endVal="overview.totalCount"></CountTo>
          <p>订单笔数</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.totalAmount"></CountTo>
          <p>总交易金额(元)</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.merchantFee"></CountTo>
          <p>佣金(元)</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.childAmount"></CountTo>
          <p>下级总跑量(元)</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.childTotalCount"></CountTo>
          <p>下级订单笔数</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.yesBalance"></CountTo>
          <p>昨日余额</p>
        </li>
      </ul>
    </div>
    <div class="card-div merchat-account">
      <h3 class="title-h3 yesterday">
        昨日统计 <span class="red">（注：只统计成功订单）</span>
      </h3>
      <ul class="flex-center">
        <li>
          <CountTo class="count-number" :endVal="overview.yesTotalCount"></CountTo>
          <p>订单笔数</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.yesTotalAmount"></CountTo>
          <p>总交易金额(元)</p>
        </li>
        <li>
          <!--总提成金额-->
          <CountTo class="count-number" :endVal="overview.yesMerchantFee"></CountTo>
          <p>佣金(元)</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.yesChildAmount"></CountTo>
          <p>下级总跑量(元)</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.yesChildTotalCount"></CountTo>
          <p>下级订单笔数</p>
        </li>
        <li>
          <CountTo class="count-number" :endVal="overview.beforeYesBalance"></CountTo>
          <p>前日余额</p>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import {getReportMerchant} from '@/api/report'
import CountTo from '@/components/CountTo/index.vue'

export default {
  name: 'MerchantPage',
  components: {
    CountTo
  },
  data() {
    return {
      loading: true,
      overview: {}
    };
  },
  created() {
   this.getReport()
  },
  methods: {
   getReport(){
     getReportMerchant({}).then(res => {
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
};
</script>

<style scoped lang="scss">
.merchat-home {
  padding: 10px;

  .card-div {
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
      &.today {
        &::before {
          background: #0a04b3;
        }
      }
      &.yesterday {
        &::before {
          background: #f5b350;
        }
      }
      .red {
        color: #ff4d4f;
        font-size: 14px;
        margin-left: 10px;
      }
    }
  }

  ul {
    flex-wrap: wrap;
    li {
      width: 16.5%;
      min-width: 130px;
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
          color: #18b0bb;
        }
        // background: #ff4d4f;
      }
      &:last-child {
        border-right: none;
      }
    }
  }
}
</style>
