<!--
 **** 排行榜
 -->
 <template>
  <div class="chart-area-line" v-loading="loading">
    <NoData v-if="!noData.length"></NoData>
    <VueECharts
      v-else
      auto-resize
      :options="chartOption">
    </VueECharts>
  </div>
</template>

<script>
  import VueECharts from '@/components/VueEcharts/index'
  import NoData from '@/components/NoData/index.vue';
  export default {
    name: "chart-rang",
    props: {
      loading: {
        type: Boolean,
        default: false
      },
      // 曲线图的额外的配置项
      extraChartOption: {
        type: Object,
        default() {
          return {}
        }
      },
      chartData: {
        type: Array,
        default() {
          return []
        }
      }
    },
    components: {
      VueECharts,
      NoData
    },
    data() {
      return {
        chartOption: {},
        // 是否有数据
        noData: []
      }
    },
    watch: {
      chartData: {
        handler(val, oldVal) {
          this.noData = val.length ? val.filter(item => item.x && item.x.length || item.y.length) : []
          this.chartOption = this.$lodash.merge({}, this.getChartOption(val), this.extraChartOption)
        },
        immediate: true
      }
    },
    methods: {
      /**
       * @param chartData {Array}
       **/
      getChartOption(chartData = []) {
        let seriesData = [];
        chartData.length && chartData.forEach(item => {
          seriesData.push({
            name: item.name,
            type: 'bar',
            barWidth: item.barWidth ? item.barWidth : 20,
            data: item.data,
            itemStyle: item.itemStyle
          })
        })

        return {
          legend: {
            show: true,
            type: 'scroll',
            top: 10,
            right: 10,
            itemGap: 38,
            itemWidth: 18,
            itemHeight: 8,
            textStyle: {
              color: '#222',
              fontSize: '12'
            },
            selectedMode: false, //图例选择
            data: chartData.map(item => item.name),
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              label: { //对应Y轴的label
                show: false
              },
              crossStyle: {
                type: 'dotted',
                color: 'rgba(102,102,102,0.8)'
              },
              lineStyle: {
                color: 'rgba(102,102,102,0.8)'
              },
            },
            padding: [8, 16],
            backgroundColor: 'rgb(255,255,255)',
            textStyle: {
              color: '#222'
            },
            extraCssText: 'box-shadow: 0 0 6px 2px rgba(102,102,102,0.2), 0 1px 0 0 rgba(255,255,255,1) inset;'
          },
          grid: {
            show: false,
            left: '1%',
            right: '2%',
            bottom: 20,
            top: 40,
            containLabel: true
          },
          xAxis: {
            type: "value",
            axisLabel: {
              interval: 0,
              // rotate: 45,
              color: 'rgba(0, 0, 0, .7)'
            },
            //网格
            splitLine: {
              show: false,
            },
            axisLine: {
              show: false
            },
            axisTick: {
              show: false
            },
            boundaryGap: true,
          },
          yAxis: {
            type: 'category',
            data: chartData && chartData[0] && chartData[0].y,
          },
          series: seriesData,
          color: ['#0093fa', '#4dfa9d', '#f58625', '#f16a87', '#ccff66', '#7579ff', '#ffa258', '#cc00cc', '#99ffcc', '#996600', '#9999cc'],
        };
      }
    }
  }
</script>

<style scoped lang="scss">
  .chart-area-line {
    position: relative;
    height: 100%;
    width: 100%
  }
</style>
