<!--
  * @Description: 表格封装
  * table.data：表格数据
  * table.loading: 加载
  * table.columns：表头数据
  *   label: 表头标题
      value: 对应数据的字段名
      width: 自定义宽  Number/String
      fixed: 是否固定在左侧，右侧， true为左侧  left/right
      sortable: 排序。 Boolean/String.  如果要接口请求排序-'custom', 在table上监听sort-change事件即可
      formatter: 格式化内容
      show-overflow-tooltip  当内容过长时显示...，鼠标hover会展示tooltip
 -->
<template>
  <div :class="['tf-table', autoHeight ? 'tf-table-auto' : 'tf-table-flex']">
    <div class="tf-table__btn">
      <div class="table-after_left">
        <slot name="table-after"></slot>
      </div>
      <div class="table-after_right">
        <el-popover trigger="hover" v-model="isVisible">
          <div slot="reference" class="checkbox-column_icon"><i class="el-icon-s-operation"></i></div>
          <div class="checkbox-column">
            <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">全选</el-checkbox>
            <el-checkbox-group v-model="tableColumn" @change="handleChecked" class="checkbox-column_group">
              <el-checkbox v-for="(item, index) in tableColumnsList" :label="item.value" :key="index">{{item.label}}</el-checkbox>
            </el-checkbox-group>
          </div>
        </el-popover>
      </div>
    </div>
    <el-table
      ref="table"
      class="tf-el-table"
      v-bind="$attrs"
      v-on="$listeners"
      stripe
      border
      v-loading="table.loading"
      :data="table.data"
      row-key="id"
      :height="autoHeight ? false : tableHeight"
      @selection-change="handleSelect"
      >
      <el-table-column
        v-for="(item, index) in table.columns"
        :align="item.align"
        :type="item.type"
        :columns="table.columns"
        :column-key="item.value"
        :key="'table_' + index"
        :prop="item.value"
        :label="item.label"
        :min-width="item.width"
        :fixed="item.fixed"
        :sortable="item.sortable"
        :formatter="item.formatter"
        :show-overflow-tooltip="item.tooltip === false ? false : true">
        <template slot="header" v-if="item.tooltip">
          <el-tooltip effect="dark" :content="item.tooltip" placement="top">
            <p>{{ item.label }}<i class="el-icon-question"></i></p>
          </el-tooltip>
        </template>
        <template v-if="item.slot && item.slot !== ''" v-slot="{ row, $index }">
          <slot :name="item.slot" :row="row" :index="$index"></slot>
        </template>
        <el-table-column
          v-for="(i, index) in  item.children"
          :align="i.align"
          :key="index"
          :label="i.label"
          :prop="i.value"
          :min-width="i.width"
          :formatter="i.formatter">
          <template v-if="i.slot && i.slot !== ''" v-slot="{ row, $index }">
            <slot :name="i.slot" :row="row" :index="$index"></slot>
          </template>
        </el-table-column>
      </el-table-column>
    </el-table>
    <!--分页-->
    <el-pagination
      v-if="table.hasPagination"
      class="table-pagination"
      v-show="table.data && table.data.length"
      :hide-on-single-page="false"
      background
      layout="total, prev, pager, next, sizes, jumper"
      :total="table.pagination.total"
      :page-size="table.pagination.pageSize"
      :current-page="table.pagination.pageNum"
      @current-change="changeCurrent"
      @size-change="changeSize">
    </el-pagination>
  </div>

</template>

<script>
  export default {
    name: 'TableIndex',
    props: {
      table: {
        type: Object,
        required: true
      },
      toHeight: {
        type: Number,
        default: 45
      },
      isSearch: {
        type: Boolean,
        default: true
      },
      autoHeight: {
        type: Boolean,
        default: false
      }
    },
    watch: {
      toHeight: {
        handler (val) {
          this.$set(this, 'height', `calc(100% - ${val}px)`)
          if (this.table.hasPagination) {
            this.$set(this, 'tableHeight', this.isSearch ? 'calc(100% - 90px)' : 'calc(100% - 40px)')
          } else {
            this.$set(this, 'tableHeight', this.isSearch ? 'calc(100% - 40px)' : '100%')
          }
        },
        immediate: true,
      }
    },
    data () {
      return {
        height: 'calc(100% - 45px)',
        tableHeight: 'calc(100% - 90px)',
        tableColumn: [],
        checkAll: true,
        isIndeterminate: false,
        isVisible: false,
        tableColumnsList: []
      }
    },
    mounted () {
      // 初始tableColumnsList，默认全选
      this.tableColumnsList = this.$lodash.cloneDeep(this.table.columns)
      this.tableColumn = this.tableColumnsList.map(item => item.value)
    },
    methods: {
      handleSelect (e) {
        this.$emit('selection', e)
      },
      // 改变页数
      changeCurrent (e) {
        this.$emit('tableChangeCurrent', {current: e})
      },
      // 改变每页条数
      changeSize (e) {
        this.$emit('tableChangeSize', {pageSize : e})
      },
      // 选择列
      handleChecked (e) {
        this.table.columns = this.tableColumnsList.filter(item => e.includes(item.value))
        this.isIndeterminate = e.length > 0 && e.length < this.tableColumnsList.length
      },
      // 全选
      handleCheckAllChange(val) {
        this.tableColumn = val ? this.tableColumnsList.map(item => item.value) : []
        this.table.columns = val ? this.tableColumnsList : []
        this.isIndeterminate = false
      }
    }
  }
</script>

<style lang="scss">
  .tf-table {
    width: 100%;
    background: #fff;
    overflow: hidden;
    &:after {
      display: block;
      content: '';
      clear: both;
    }

    .el-table__cell {
      padding: 6px 0
    }
    // table
    .tf-el-table.el-table {
      width: 100%;
      border: 1px solid #dfe6ec;

      .el-table__header-wrapper {
        th {
          background-color: #f5f7fa
        }
      }

      // body
      .el-table__body-wrapper {
        // 滚动条
        &::-webkit-scrollbar {
          background: #D1E0FA
        }
        &::-webkit-scrollbar-thumb {
          background: #D1E0FA;
          -webkit-border-radius: 2px;
        }
      }
    }

    // slot tf-table__btn
    .tf-table__btn {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .el-button {
        padding: 8px 15px;
        border-radius: 2px;
        margin-bottom: 15px;
      }
    }

    // 空数据
    .el-table__empty-text {
      padding-top: 60px;
      line-height: 80px
    }


    /*分页*/
  .table-pagination.is-background {
    height: 45px;
    display: flex;
    padding: 12px 0 0;
    justify-content: flex-end;

    .el-pagination__total {
      height: 32px;
      line-height: 32px;
    }

    .el-pagination__sizes {
      height: 30px;
      line-height: 30px;
      margin-left: 10px;
    }

    .btn-prev, .btn-next, .el-pager>li {
      min-width: 32px;
      height: 32px;
      line-height: 32px;
      background: transparent;
      font-weight: 500;
    }
    .btn-next {
      margin-right: 0
    }
  }
}

.tf-table-flex {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tf-table-auto {
  height: auto;
  overflow-y: auto;
}

.table-after_left {
  position: relative;
  width: 95%
}

.table-after_right {
  position: relative;
}


.checkbox-column_icon {
  font-size: 18px;
  color: #696969;
  &:hover {
    color: #409EFF;
  }
}
.checkbox-column_group {
  display: flex;
  flex-wrap: wrap;
  max-width: 140px;
}

.el-checkbox {
  margin-right: 15px;
  margin-bottom: 3px
}

// 宽度小于768px，.tf-table设置固定高度600px
@media screen and (max-width: 768px) {
  .tf-table {
    flex: none;
    height: 600px;
  }
}


</style>
