<!--
** 佣金列表
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table :table="tableConfig">
      <template v-slot:table-after>
      </template>
      <template v-slot:merchantName="{ row }">
        <div class="flex" v-if="row.merchantName !== '合计'">
          <p class="mr10 ellipsis" style="width: 140px">{{ row.merchantName }}</p>
          <el-button v-hasPermi="['system:merchant:allParent']" type="text" size="mini" @click="lookAllParent(row)">查看上级</el-button>
        </div>
      </template>
      <template v-slot:dianwei="{ row }">
        <span v-if="row.merchantName !== '合计'">{{ row.dianwei  || 0}}%</span>
      </template>
      <template v-slot:commissionOne="{ row }">
        <span>{{ row.commissionOne  || 0}}<span v-if="row.merchantName !== '合计'">({{ row.commissionOneRate }}%)</span></span>
      </template>
      <template v-slot:commissionTwo="{ row }">
        <span>{{ row.commissionTwo  || 0}}<span v-if="row.merchantName !== '合计'">({{ row.commissionTwoRate }}%)</span></span>
      </template>
      <template v-slot:commissionThree="{ row }">
        <span>{{ row.commissionThree  || 0}}<span v-if="row.merchantName !== '合计'">({{ row.commissionThreeRate }}%)</span></span>
      </template>
      <template v-slot:commissionFour="{ row }">
        <span>{{ row.commissionFour  || 0}}<span v-if="row.merchantName !== '合计'">({{ row.commissionFourRate }}%)</span></span>
      </template>
      <template v-slot:myCommission="{ row }">
        <span>{{ row.myCommission  || 0}}</span>
      </template>
    </km-table>

    <!--所有上级码商-->
    <all-parent ref="allParentRef"></all-parent>
  </div>
</template>

<script>
import { commissionList } from '@/api/report'
import moment from 'moment';
import {getChannelListAll} from '@/api/channel';
import AllParent from '@/views/order/components/allParent.vue'

export default {
  name: 'CommissionList',
  components: { AllParent },
  data() {
    return {
      searchData: [
        {
          type: 'daterange',
          initialDate: [moment().startOf('day').format('YYYY-MM-DD'), moment().endOf('day').format('YYYY-MM-DD')],
          modelName: ['startTime', 'endTime'],
          title: '时间选择',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        },
        {
          type: 'input',
          model: 'merchantName',
          title: '码商名称',
          placeholder: '请输入码商名称',
          clearable: true
        },
        {
          type: 'input',
          model: 'merchantNameOne',
          title: '下级码商名称',
          placeholder: '请输入下级码商名称',
          clearable: true
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
          shopId: '',
          shopName: '',
          merchantId: '',
          merchantName: '',
          merchantNameOne: '',
          channelId: '',
          startTime: moment().startOf('day').format('YYYY-MM-DD'),
          endTime: moment().endOf('day').format('YYYY-MM-DD')
        },
        data: [],
        columns: [
          { label: '码商名称', value: 'merchantName', slot: 'merchantName', width: 160},
          { label: '零级码商名称', value: 'merchantNameOne', width: 120 },
          { label: '通道名称', value: 'channelName', width: 120 },
          { label: '点位', value: 'dianwei' , align: 'center', slot: 'dianwei' , width: 100},
          { label: '总跑量', value: 'totalAmount', align: 'center',width: 120 },
          { label: '总成功单数', value: 'totalCount', align: 'center', width: 100},
          { label: '零级佣金', value: 'commissionOne', align: 'center', slot: 'commissionOne', width: 160 },
          { label: '一级佣金', value: 'commissionTwo', align: 'center', slot: 'commissionTwo' , width: 160},
          { label: '二级佣金', value: 'commissionThree', align: 'center', slot: 'commissionThree', width: 160 },
          { label: '三级佣金', value: 'commissionFour', align: 'center', slot: 'commissionFour', width: 160 },
          { label: '我的佣金', value: 'myCommission', align: 'center', slot: 'myCommission', width: 160 }
        ]
      }
    }
  },
  mounted() {
    this.getChannelList();
    this.doSearch()
  },
  methods: {
    doSearch() {
      this.getTableData()
    },
    getTableData() {
      this.tableConfig.loading = true
      commissionList({...this.tableConfig.searchQuery}).then(res => {
        this.tableConfig.loading = false
        this.tableConfig.data = res.data || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
    // 获取通道
    getChannelList() {
      getChannelListAll({}).then(res => {
        let channelList = res.data || []
        this.$set(this.searchData[3], 'option', channelList.map(item => {
          return {
            label: item.channelName,
            value: item.id
          }
        }))
      })
    },
    // 查看上级码商
    lookAllParent(row) {
      this.$refs.allParentRef.openDialog(row)
    },
     // 重置
     resetTable() {
      this.tableConfig.searchQuery = {
        shopId: '',
        shopName: '',
        merchantId: '',
        merchantName: '',
        merchantNameOne: '',
        channelId: '',
        startTime: moment().startOf('day').format('YYYY-MM-DD'),
        endTime: moment().endOf('day').format('YYYY-MM-DD')
      }
      this.searchData[0].initialDate = [moment().startOf('day').format('YYYY-MM-DD'), moment().endOf('day').format('YYYY-MM-DD')]
      this.getTableData()
    }
  }
}
</script>
