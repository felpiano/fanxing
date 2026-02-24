<!--
  * @Description: 查询条件封装
  * @LastEditorDescription:
 -->

<template>
  <div v-if="searchData.length" class="tf-search-box">
    <slot name="before"></slot>
    <div
      v-for="(item, index) in searchDataIsVisible"
      :key="'search_' + index"
      :style="{'margin-left': !item.title && '-12px'}"
      class="from">
      <!--  select   -->
      <div v-if="item.type === 'select'" class="select">
        <span
          v-if="item.title"
          :class="['text', (item.option && item.option.length === 0) && 'disabled-text']">
          {{item.title}}
        </span>
        <el-select
          :disabled="item.option && item.option.length === 0"
          v-model="query[item.model]"
          :clearable="item.clearable"
          :filterable="item.filterable"
          :placeholder="item.placeholder ? item.placeholder : '请选择'"
          popper-class="tf-search-box__select"
          size="small"
          :style="Object.assign({width: item.width || '174px'}, item.style)"
          @change="emitSearchChange(item)"
          >
          <el-option
            v-for="i in item.option"
            :key="i.value"
            :label="i.label"
            :disabled="i.disabled"
            :value="i.value">
          </el-option>
        </el-select>
      </div>
      <!--cascader 联动选择（地区） -->
      <div v-if="item.type === 'cascader'" class="select">
        <span v-if="item.title" class="text">{{item.title}}</span>
        <el-cascader
          v-model="query[item.model]"
          :props="item.props"
          :options="item.options"
          :clearable="!item.noAllowClear"
          separator=" "
          @change="(value) => cascaderChange(value, item)">
        </el-cascader>
      </div>
      <!--  日期选择器：年月日：date, 年：year, 月month, 周week   -->
      <div v-else-if="item.type === 'datePicker'" class="date-picker">
        <span v-if="item.title" class="text">{{item.title}}</span>
        <el-date-picker
          :type="item.dateType"
          v-model="item.initialDate"
          :editable="false"
          :clearable="!item.noAllowClear"
          :picker-options="pickerOptions"
          size="small"
          placeholder="请选择"
          :prefix-icon="item.prefix"
          :style="Object.assign({width: item.width || '174px'}, item.style)"
          @change="(value) =>handleDatepicker(value, item)">
        </el-date-picker>
      </div>
      <!--  时间范围选择器   -->
      <div v-else-if="item.type === 'daterange'" class="date-range-picker">
        <span v-if="item.title" class="text">{{item.title}}</span>
        <el-date-picker
          :type="item.type"
          v-model="item.initialDate"
          :editable="false"
          :clearable="!item.noAllowClear"
          :picker-options="item.pickerOptions || pickerOptions"
          size="small"
          value-format="yyyy-MM-dd"
          :style="Object.assign({width: item.width || '220px'}, item.style)"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :range-separator="item.rangeSeparator || '-'"
          @change="(value) =>handleDateRange(value, item)">
        </el-date-picker>
      </div>
       <!--  时间范围选择器   -->
       <div v-else-if="item.type === 'datetimerange'" class="date-range-picker">
        <span v-if="item.title" class="text">{{item.title}}</span>
        <el-date-picker
          :type="item.type"
          v-model="item.initialDate"
          :editable="false"
          :clearable="!item.noAllowClear"
          :picker-options="item.pickerOptions || pickerOptions1"
          size="small"
          value-format="yyyy-MM-dd HH:mm:ss"
          :style="Object.assign({width: item.width || '335px'}, item.style)"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :default-time="['00:00:00', '23:59:59']"
          :range-separator="item.rangeSeparator || '-'"
          @change="(value) =>handleDateRange(value, item)">
        </el-date-picker>
      </div>
      <div v-else-if="item.type === 'monthrange'" class="date-range-picker">
        <span v-if="item.title" class="text">{{item.title}}</span>
        <el-date-picker
          :type="item.type"
          v-model="item.model"
          :editable="false"
          :clearable="!item.noAllowClear"
          :picker-options="pickerOptions"
          size="small"
          :style="Object.assign({width: item.width || '220px'}, item.style)"
          start-placeholder="开始月份"
          end-placeholder="结束月份"
          @change="(value) =>handleDateRange(value, item)">
        </el-date-picker>
      </div>
      <!--  input   -->
      <div v-else-if="item.type === 'input'" class="input">
        <span class="text" v-show="item.title !== null">{{ item.title || '关键字: '}}</span>
        <el-input
          v-model="query[item.model || 'keywords']"
          :maxLength="50"
          :type="item.inputType || 'text'"
          :clearable="item.clearable"
          :placeholder="item.placeholder"
          :disabled="item.disabled || false"
          suffix-icon="el-icon-search"
          size="small"
          :style="Object.assign({width: item.width || '200px'}, item.style)"
        />
      </div>
      <!--  input-range -->
      <div v-else-if="item.type === 'inputRange'" class="input">
        <span class="text">{{ item.title }}</span>
        <el-input
          type="number"
          :controls="false"
          v-model="query[item.modelName[0]]"
          :clearable="item.clearable"
          :placeholder="item.placeholder[0]"
          size="small"
          :style="Object.assign({width: item.width || '160px'}, item.style)"
        />
        <span class="s-text">-</span>
        <el-input
          type="number"
          v-model="query[item.modelName[1]]"
          :controls="false"
          :clearable="item.clearable"
          :placeholder="item.placeholder[1]"
          size="small"
          :style="Object.assign({width: item.width || '160px'}, item.style)"
        />
      </div>
    </div>
    <!-- 操作按钮 -->
    <div v-if="!hiddenSearch" class="search-button">
      <el-button class="search-btn" type="primary" size="small" @click="doSearch"><i class="el-icon-search"></i>查询</el-button>
      <el-button v-if="isReset" class="search-btn" type="primary" size="small" @click="doReset"><i class="el-icon-refresh"></i>重置</el-button>
      <slot name="after"></slot>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'search-bar',
    props: {
      searchData: {
        type: Array,
        default () {
          return []
        }
      },
      query: {
        type: Object,
        default () {
          return {}
        }
      },
      hiddenSearch: {
        type: Boolean,
        default: false
      },
      isReset: {
        type: Boolean,
        default: true
      }
    },
    data () {
      return {
        pickerOptions: {
          disabledDate (time) {
            return time > new Date()
          }
        },
        pickerOptions1: {
          disabledDate (time) {
            return time > (new Date().getTime()  + 24 * 60 * 60 * 1000)
          }
        },
      }
    },
    computed: {
      searchDataIsVisible () {
        return this.searchData.filter(t => !t.hide)
      }
    },
    created () {
      this.initOptions()
    },
    watch: {
      searchData (val) {
        val.map(item => {
          // 监听时间选择切换时禁止不可选时间
          if (!item.disabledDate || item.disabledDate === '') return
          if (item.type === 'datePicker') {
            this.pickerOptions = {
              disabledDate (time) {
                return time > new Date(item.disabledDate)
              }
            }
          }
          return item
        })
      }
    },
    methods: {
      doSearch () {
        this.$emit('on-search-change')
      },
      doReset () {
        this.$emit('on-reset')
      },
      // 获取单个下拉框数据
      initOptions () {
        this.searchData.map(async item => {
          // 需要请求接口来换取下拉框数据
          if (item.api) {
            try {
              this.$set(item, 'loading', true)
              let query = this.$lodash.cloneDeep(item.query) || {}
              // 请求接口
              let res = await item.api(query)
              let data = []
              if (res instanceof Array) {
                data = res
              } else {
                data = res.rows || res.data
              }
              if (item.type === 'select') {
                const curItemOption = data.map(obj => {
                  if (obj.defaultFlag && obj.defaultFlag == '1' && item.isDefaultValue) {
                    this.$set(this.query, item.model, obj[item.keyValueName ? item.keyValueName[1] : 'id'])
                  }
                  return Object.assign(obj, {
                    value: obj[item.keyValueName ? item.keyValueName[1] : 'id'],
                    label: obj[item.keyValueName ? item.keyValueName[0] : 'name'],
                    // defaultFlag: obj.defaultFlag ? obj.defaultFlag : '0'
                  })
                });
                // 需要增加自定义下拉选项时，可在组件外部通过option回调函数处理，回调参数为接口返回的列表数据
                this.$set(item, 'option', typeof item.customOption === 'function' ? item.customOption(curItemOption) : curItemOption)
              }
              if (item.relationKey) {
                this.emitSearchChange(item, true)
              }
            } finally {
              item.loading = false
            }
          }

          if (item.type === 'daterange' && item.initialDate) {
            this.query[item.modelName[0]] = item.initialDate[0]
            this.query[item.modelName[1]] = item.initialDate[1]
          }
          // 如果是时间选择，根据要求限制
          if (!item.disabledDate || item.disabledDate === '') return
          if (item.type === 'datePicker') {
            this.pickerOptions = {
              disabledDate (time) {
                return time > new Date(item.disabledDate)
              }
            }
          }
        })
      },
      // 日期选择器
      handleDatepicker (val, item) {
        if (val === null) {
          this.query[item.model] = new Date();
        } else {
          this.$nextTick(() => {
            this.query[item.model] = val
          })
        }
      },
      // 时间范围选择器
      handleDateRange (val, item) {
        if (val === null) {
          this.query[item.modelName[0]] = this.query[item.modelName[1]] = null
          return
        }
        const minDate = new Date(val[0]).getTime()
        const maxDate = new Date(val[1]).getTime()
        let times = maxDate - minDate
        if (item.type === 'daterange' && item.disabledDate && item.disabledDate !== '') {
          const disableTime = Number(item.disabledDate - 1)*24*60*60*1000
          if (times > disableTime) {
            val[1] = val[0]
            this.$message.warning(`时间选择不能超过${item.disabledDate}天`)
          }
        }
        if (item.type === 'monthrange' && item.disabledDate && item.disabledDate !== '') {
          const disableTime = Number(item.disabledDate)*30*24*60*60*1000
          if (times > disableTime) {
            val[1] = val[0]
            this.$message.warning(`时间选择不能超过${item.disabledDate}个月`)
          }
        }
        let startTime = val[0]
        let endTime = val[1]
        if (!val || val.length === 0) {
          this.query[item.modelName[0]] = this.query[item.modelName[1]] = null
        } else {
          this.query[item.modelName[0]] = startTime
          this.query[item.modelName[1]] = endTime
        }
      },
      /**
       * 获取关联的单个下拉框数据
       * @param val 关联父级的下拉值
       * @param relationItem  关联的下拉项
       * @param isFirst
       */
      async querySingleOptions(val, relationItem, isFirst) {
        if (relationItem && relationItem.relationApi) {
          let list = []
          //如果默认回填第一个，先清空的话文字会闪动，所以加!relationItem.noValueSelectedFirst
          if (!isFirst && !relationItem.noValueSelectedFirst) {
            this.$set(this.query, relationItem.model, undefined)
          }
          if (val) {
            let params = {}
            if (relationItem.querySetKey) {
              params = {
                [relationItem.querySetKey] : val
              }
            } else {
              params = {
                [relationItem.queryKey] : val
              }
            }
            let res = await relationItem.relationApi(params)
            if (res instanceof Array) {
              list = res
            } else {
              list = res.list || res.rows
            }
            list = list.map(obj => {
              if (isFirst && obj.defaultFlag && obj.defaultFlag === '1' && relationItem.isDefaultValue) {
                this.$set(this.query, relationItem.model, obj[relationItem.keyValueName ? relationItem.keyValueName[1] : 'id'])
              }
              return {
                value: obj[relationItem.keyValueName ? relationItem.keyValueName[1] : 'id'],
                label: obj[relationItem.keyValueName ? relationItem.keyValueName[0] : 'name'],
              }
            })
            //start 关联的下拉没有回填值时，默认选择第一个
            let firstValue = list.length ? list[0].value : undefined
            if(relationItem.noValueSelectedFirst) {
              //没有值时，等接口获取完抛方法调查询，否则自己页面调查询
              if(isFirst && !this.query[relationItem.model]) {
                this.$set(this.query, relationItem.model, firstValue)
                isFirst && this.$emit(relationItem.emitName)
              }else if(!isFirst) {
                this.$set(this.query, relationItem.model, firstValue)
              }
            }//  end 默认选择第一个
          }
          if (relationItem.type !== 'select') return false
          // 需要增加自定义下拉选项时，可在组件外部通过option回调函数处理，回调参数为接口返回的列表数据
          this.$set(relationItem, 'option', typeof relationItem.customOption === 'function' ? relationItem.customOption(list) : list)

          if (relationItem.relationKey) {
            let relationItemArr = relationItem.relationKey.split(',')
            relationItemArr.forEach(itemKey => {
              let items = this.searchData.find(obj => {
                return obj.model === itemKey
              })
              if (!items) return false;
              if (items.relationApi) {
                this.querySingleOptions(this.query[relationItem.model], items, isFirst)
              } else {
                this.querySingleOptions(null, items, isFirst )
              }
            })
          }
        }
      },
      emitSearchChange(row, isFirst = false) {
        if (row && row.relationKey) {
          let relationItemArr = row.relationKey.split(',')
          relationItemArr.forEach(itemKey => {
            let relationItem = this.searchData.find(obj => {
              return obj.model === itemKey
            })
            if (relationItem) {
              return this.querySingleOptions(this.query[row.model], relationItem, isFirst)
            }
          })
        }
      },
      cascaderChange(val, item) {
        this.query[item.model] = val
      }
    }
  }
</script>

<style lang="scss">
  .tf-search-box {
    width: 100%;
    background: #fff ;
    display: flex;
    flex-wrap: wrap;

    .from {
      margin-right: 15px;
      margin-bottom: 15px;
      .select, .date-picker, .date-range-picker, .input {
        .text {
          padding-right: 8px;
          font-size: 14px;
        }
        .s-text {
          padding: 0 8px;
          font-size: 14px;
        }
      }

      .date-range-picker {
        .el-input__icon {
          line-height: 24px;
        }
      }

      .input {
        .el-input {
          width: auto
        }
      }
    }

    // button
    .search-button {
      margin-bottom: 15px;
      .search-btn {
        padding: 8px 15px;
        border-radius: 2px;
        border: none
      }
    }
  }
</style>
