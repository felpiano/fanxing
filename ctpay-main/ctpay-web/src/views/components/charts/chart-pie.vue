<template>
  <div class="chart-pie" v-loading="loading">
    <NoData v-if="!chartPieData.length && !loading"></NoData>
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
    name: "chart-pie",
    components: {
      VueECharts,
      NoData
    },
    props: {
      loading: {
        type: Boolean,
        default: false
      },
      chartPieData: {
        type: Array,
        required: true,
        default: () => {
          return []
        }
      },
      // 曲线图的额外的配置项
      extraChartOption: {
        type: Object,
        default() {
          return {}
        }
      },
      seriesType: {
        type: Number,
        default: 1
      }
    },
    data(vm) {
      return {
        chartOption: {}
      }
    },
    watch: {
      chartPieData: {
        handler(val, oldVal) {
          this.chartOption = this.$lodash.merge({}, this.getChartOption(val), this.extraChartOption)
        },
        immediate: true
      }
    },
    methods: {
      getChartOption(chartPieData) {
        let seriesData = []
        // 常规饼图
        let series_1 = [{
          type: 'pie',
          label: {
            formatter: '{b|{b}}\n{d|{d}%} ',
            rich: {
              b: {
                color: '#fff',
                lineHeight: 22,
                fontSize: 12,
                align: 'center'
              },
              d: {
                fontSize: 14,
                fontWeight: 600,
                lineHeight: 22,
                align: 'center',
              }
            }
          },
          radius: [0, '68%'],
          center: ['50%', '45%'],
          data: chartPieData,
        }]

        // 中间空心
        let series_2 = [{
          type: 'pie',
          label: {
            formatter: '{b|{b}} {c|{c}} ', // {d|{d}%}
            rich: {
              b: {
                color: '#333',
                lineHeight: 22,
                fontSize: 12,
                align: 'center'
              },
              c: {
                fontSize: 14,
                fontWeight: 600,
                lineHeight: 22,
                align: 'center',
              },
              d: {
                fontSize: 14,
                fontWeight: 600,
                lineHeight: 22,
                align: 'center',
              }
            }
          },
          radius: ['36%', '68%'],
          center: ['50%', '56%'],
          data: chartPieData,
        }]

        if (this.seriesType === 1) {
          seriesData = series_1
        } else if (this.seriesType === 2) {
          seriesData = series_2
        }

        return {
          legend: {
            show: true,
            type: 'scroll',
            top: 10,
            right: 10,
            itemGap: 15,
            itemWidth: 14,
            itemHeight: 6
          },
          grid: {
            show: false,
            top: 30,
            bottom: 10
          },
          tooltip: {
            trigger: 'item',
            formatter: '{b} : {c} {d}%'
          },
          color: ['#f58625', '#f16a87', '#0093fa', '#4dfa9d', '#ccff66', '#7579ff', '#ffa258', '#cc00cc', '#99ffcc', '#996600', '#9999cc'],
          series: seriesData
        }
      }
    }
  }
</script>

<style scoped lang="scss">
  .chart-pie {
    position: relative;
    height: 100%;
    width: 100%
  }
  .no-data-tip {
    position: relative;
    width: 100%;
    height: 100%;
    text-align: center;
    > p {
      width: 100%;
      height: 100%;
      padding-top: 12%;
      color: rgba(0,0,0,.68)
    }
  }
</style>
