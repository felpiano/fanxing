<!--
** 码商通道码列表
-->
<template>
   <div class="app-container">
      <search-bar
         :searchData="searchData"
         :query="tableConfig.searchQuery"
         @on-search-change="doSearch"
         @on-reset="resetTable">
      </search-bar>
      <km-table
         :table="tableConfig"
         @tableChangeCurrent="tableChangeCurrent"
         @tableChangeSize="tableChangeSize">
         <!--<template v-slot:table-after>
         <el-button v-hasPermi="['system:merchantQrcode:add']"
               type="primary" size="mini" @click="handleAdd">
               <i class="el-icon-plus"></i>添加
            </el-button>
         </template>-->
         <template v-slot:status="{ row }">
            <el-switch :width="50"
               v-model="row.status"
               :active-value="0"
               :inactive-value="1"
               active-text="开启"
               inactive-text="关闭"
               @change="handleStatusChange(row)">
            </el-switch>
         </template>
         <template v-slot:operation="{ row }">
            <!--<el-button
               v-hasPermi="['system:merchantQrcode:add']"
               type="primary"
               class="min-button"
               @click="handleUpdate(row)">
               编辑
            </el-button>-->
            <el-button
               v-hasPermi="['system:merchantQrcode:delete']"
               type="danger"
               class="min-button"
               @click="handleDelete(row)">
               删除
            </el-button>
         </template>
      </km-table>

      <el-dialog title="选择通道" append-to-body :visible.sync="isVisible" width="20%" @close="closeDialog" :destroy-on-close="true">
         <el-form ref="form" :model="form" :rules="rule" label-width="60px" class="dialog-form-height">
            <el-form-item label="通道" prop="channelCode">
               <el-select v-model="form.channelCode" placeholder="请选择通道">
                  <el-option
                     v-for="item in channelList"
                     :key="item.channelId"
                     :label="item.channelName"
                     :value="item.channelCode">
                  </el-option>
               </el-select>
            </el-form-item>
         </el-form>
         <div slot="footer" class="dialog-footer">
            <el-button @click="closeDialog">关闭</el-button>
            <el-button type="primary" @click="handleSubmit">确定</el-button>
         </div>
      </el-dialog>

      <alipayScanUpdate ref="alipaycodeUpdate" @submit="submitAdd"></alipayScanUpdate>
      <union-pay-update ref="unionPayUpdate" @submit="submitAdd"></union-pay-update>
   </div>
</template>

<script>
import {getChannelListAll} from '@/api/channel'
import {getMerchantQrcodeList, updateMerchantQrcodeStatus, deleteMerchantQrcode} from '@/api/merchantQrcode'
import alipayScanUpdate from './components/alipaycodeUpdate.vue';
import unionPayUpdate from './components/unionPayUpdate.vue';

export default {
   name: 'merchantQrcode',
   components: {
      alipayScanUpdate,
      unionPayUpdate
   },
   data(){
      return {
         searchData: [
            {
               type: 'select',
               model: 'channelId',
               title: '通道',
               placeholder: '全部',
               clearable: true,
               option: []
            },
            {
               type: 'input',
               model: 'accountNumber',
               title: '收款码账号',
               placeholder: '请输入收款码账号',
               clearable: true
            },
            {
               type: 'input',
               model: 'nickName',
               title: '收款码昵称',
               placeholder: '请输入收款码昵称',
               clearable: true
            },
            {
               type: 'select',
               model: 'status',
               title: '接单状态',
               placeholder: '全部',
               clearable: true,
               option: [
                  {
                     label: '启用',
                     value: 0
                  },
                  {
                     label: '禁用',
                     value: 1
                  }
               ]
            }
         ],
         tableConfig: {
            loading: false,
            searchQuery: {
               channelId: '',
               accountNumber: '',
               nickName: '',
               status: ''
            },
            data: [],
            columns: [
            {
               value: 'id',
               label: '编号',
               width: 80
            },
            {
               value: 'channelName',
               label: '通道名称',
               width: 140
            },
            {
               value: 'accountNumber',
               label: '收款码账号',
               width: 120
            },
            {
               value: 'nickName',
               label: '收款码昵称',
               width: 120
            },
            {
               value: 'stauts',
               slot: 'status',
               align: 'center',
               label: '进单状态',
               width: 100
            },
            {
               value: 'dayLimit',
               label: '每日限额',
               align: 'center',
               width: 100
            },
            {
               value: 'maxAmount',
               label: '最大金额',
               align: 'center',
               width: 100
            },
            {
               value: 'minAmount',
               label: '最小金额',
               align: 'center',
               width: 100
            },
            {
               value: 'successCount',
               label: '成功总数（今）',
               align: 'center',
               width: 120,
               formatter: row => row.successCount || '--'
            },
            {
               value: 'successAmount',
               label: '成功金额（今）',
               align: 'center',
               width: 120,
               formatter: row => row.successAmount || '--'
            },
            {
               value: 'yesSuccessCount',
               label: '成功总数（昨）',
               align: 'center',
               width: 120
            },
            {
               value: 'yesSuccessAmount',
               label: '成功金额（昨）',
               align: 'center',
               width: 120
            },
            {
               value: 'createTime',
               label: '添加时间',
               width: 160
            },
            {
               slot: 'operation',
               label: '操作',
               align: 'center',
               fixed: 'right',
               width: 120
            }
         ],
            hasPagination: true,
            pagination: {
               pageNum: 1,
               pageSize: 10,
               total: 0
            }
         },
         channelList: [],
         isVisible: false,
         form: {
            channelCode: ''
         },
         rule: {
            channelCode: [
               { required: true, message: '请选择通道', trigger: 'change' }
            ]
         },
         submitLoading: false,
      }
   },
   created() {
      this.doSearch()
      this.getChannelList()
   },
   methods: {
      doSearch(){
         this.tableConfig.pagination.pageNum = 1
         this.getTableData()
      },
      getTableData() {
         this.tableConfig.loading = true
         getMerchantQrcodeList({
            ...this.tableConfig.searchQuery,
            pageNum: this.tableConfig.pagination.pageNum,
            pageSize: this.tableConfig.pagination.pageSize
         }).then(res => {
            this.tableConfig.data = res.data.records
            this.tableConfig.pagination.total = res.data.total
            this.tableConfig.loading = false
         }).catch(() => {
            this.tableConfig.loading = false
         })
      },
      getChannelList() {
         getChannelListAll({}).then(res => {
            const list = res.data || {}
            this.channelList = list
            this.$set(this.searchData[0], 'option', list.map(item => {
                return {
                  label: item.channelName,
                  value: item.id
               }
            }))
         })
      },
      // 添加
      handleAdd() {
         this.isVisible = true
         this.form = {
            channelCode: ''
         }
      },
      handleSubmit() {
         this.$refs.form.validate(valid => {
            if (valid) {
               this.$refs[this.form.channelCode + 'Update'].openDialog(true)
               this.isVisible = false
            }
         })
      },
      submitAdd(isAdd) {
         if (isAdd) {
            this.doSearch()
         } else {
            this.getTableData()
         }
      },
      // 进单状态
      handleStatusChange(row) {
         updateMerchantQrcodeStatus({
            id: row.id,
            status: row.status
         }).then(() => {
            this.$message.success('修改成功')
            this.getTableData()
         })
      },
      // 编辑
      handleUpdate(row) {
         this.$refs[row.channelCode + 'Update'].openDialog(false, row)
      },
      // 删除
      handleDelete(row) {
         this.$confirm(`是否确认删除编号【${row.id}】通道码?`, '温馨提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
         }).then(() => {
            deleteMerchantQrcode({id: row.id}).then(() => {
               this.$message.success('删除成功')
               this.getTableData()
            })
         })
      },
      closeDialog() {
         this.isVisible = false
         this.$refs.form.resetFields()
      },
      // 重置
      resetTable() {
         this.tableConfig.searchQuery = {
            channelId: '',
            accountNumber: '',
            nickName: '',
            status: ''
         }
         this.doSearch()
      },
      // 分页
      tableChangeCurrent(e) {
         this.$set(this.tableConfig, 'pagination', {
         ...this.tableConfig.pagination,
         pageNum: e.current
         })
         this.getTableData()
      },
      tableChangeSize(e) {
         this.$set(this.tableConfig, 'pagination', {
         ...this.tableConfig.pagination,
         pageSize: e.pageSize
         })
         this.getTableData()
      }
   }
}
</script>
