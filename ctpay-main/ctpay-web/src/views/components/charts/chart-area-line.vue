<!--
 * 区域面积折线图-柱状图
 -->
<template>
  <div class="chart-area-line" v-loading="loading">
    <NoData v-if="!noData.length"></NoData>
    <VueECharts
      v-else
      auto-resize
      :options="chartOption"
      @click="handleClick">
    </VueECharts>
  </div>
</template>

<script>
  import VueECharts from '@/components/VueEcharts/index'
  import NoData from '@/components/NoData/index.vue';
  export default {
    name: "chart-area-line",
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
      },
      yAxisName: {
        type: String,
        default: ''
      },
      // 是否要展示区域
      areaStyle: {
        type: Boolean,
        default: false
      },
      // 是否平滑曲线显示
      smooth: {
        type: Boolean,
        default: false
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
        const color = ['rgb(0,147,250)', 'rgb(77,250,157)', 'rgb(245,134,37)', 'rgb(241,106,135)', 'rgb(204,255,102)', 'rgb(117,121,255)', 'rgb(255,162,88)', 'rgb(204,0,204)', 'rgb(153,255,204)', 'rgb(153,102,0)', 'rgb(153,153,204)']
        let seriesData = [];
        let dualAxis = 0
        chartData.length && chartData.forEach((item, i) => {
          if ( item.dualAxis !== undefined && item.dualAxis ) {
            dualAxis += 1
          }
          if (!item.type || item.type === 'line') {
            // 获取到对应颜色的面积图颜色
            const color1 = `rgba(${color[i].slice(4, color[i].length - 1)}, 0.4)`
            const color2 = `rgba(${color[i].slice(4, color[i].length - 1)}, 0.1)`
            seriesData.push({
              name: item.name,
              type: item.type ? item.type : 'line',
              symbol: item.symbol ? item.symbol : 'none', //折线图上的圆点
              smooth: !!this.smooth, // 是否平滑曲线显示
              data: item.y,
              yAxisIndex: item.dualAxis ? (item.dualAxisIndex || 1) : 0, //两个y轴
              areaStyle: { // 面积图
                color: {
                  type: 'linear',
                  x: 0,
                  y: 1,
                  x2: 0,
                  y2: 0,
                  colorStops: [
                    {offset: 1, color: color1},
                    {offset: 0, color: color2}
                  ]
                },
                opacity: this.areaStyle ? 1 : 0
              }
            })
          } else { // bar
            seriesData.push({
              name: item.name,
              type: item.type,
              barWidth: item.barWidth ? item.barWidth : 20,
              data: item.y,
              stack: item.stack ? item.stack : '', //堆叠
              yAxisIndex: item.dualAxis ? (item.dualAxisIndex || 1) : 0, //两个y轴
              itemStyle: item.itemStyle
            })
          }
        })

        let yAxis = [{
          name: this.yAxisName ? `(${this.yAxisName})` : '',
          type: "value",
          // y轴刻度标签
          axisLabel: {
            color: 'rgba(102,102,102,1)',
            margin: 22
          },
          //网格
          splitLine: {
            show: true,
            lineStyle: {
              color: 'rgba(102,102,102, .32)'
            }
          },
          // Y轴轴线
          axisLine: {
            show: false,
            lineStyle: {
              color: 'rgba(102,102,102, .32)'
            }
          },
          // 刻度
          axisTick: {
            show: false,
            length: 9,
            lineStyle: {
              color: 'rgba(102,102,102, .32)'
            }
          }
        }]

        if (dualAxis) {
          for(let i = 0; i < dualAxis; i++) {
            yAxis.push({
              name: '',
              type: "value",
              axisLabel: {
                color: 'rgba(102,102,102,1)',
                margin: 22,
                formatter: '{value}'
              },
              //网格
              splitLine: {
                show: true,
                lineStyle: {
                  color: 'rgba(102,102,102, .32)'
                }
              },
              axisLine: {
                show: false,
                lineStyle: {
                  color: 'rgba(102,102,102, .32)'
                }
              },
              axisTick: {
                show: false
              }
            })
          }
        }

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
            // formatter: (params) => {
            //   let param = params[0];
            //   return `<div style="color: #50FFA2; font-size: 12px">
            //         <p>${param.axisValue}</p>
            //         ${params.map(item=> `
            //           <p style="color: ${item.color}">
            //             ${item.seriesName}:
            //             ${item.data && item.data.hasOwnProperty('value') ? item.data.value : item.data}${item.data?.unit ? item.data.unit : dualAxis ? '' : this.yAxisName}
            //            </p>
            //         `).join('')}
            //     </div>`
            // },
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
            type: "category",
            data: chartData && chartData[0] && chartData[0].x,
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
          yAxis: dualAxis ? yAxis : yAxis[0],
          series: seriesData,
          color: color
        };
      },
      handleClick (params) {
        if (params.componentType === 'xAxis') {
          this.$emit('handle-click', params.value)
        }
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
