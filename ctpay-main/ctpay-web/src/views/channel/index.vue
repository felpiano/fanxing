<!--
** 通道列表
-->
<template>
  <div class="app-container">
    <search-bar
      :search-data="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable"></search-bar>
    <km-table :table="tableConfig" @tableChangeCurrent="tableChangeCurrent" @tableChangeSize="tableChangeSize">
      <template v-slot:table-after>
        <el-button v-hasPermi="['system:channel:add']" type="primary" size="mini" @click="addChannel">
          <i class="el-icon-plus"></i>添加通道
        </el-button>
      </template>
      <template v-slot:status="{ row }">
        <el-switch
          :width="55"
          v-model="row.status"
          :active-value="0"
          :inactive-value="1"
          active-text="正常"
          inactive-text="禁用"
          @change="updateChannelStatus(row)">
        </el-switch>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:channel:update']" type="primary" class="min-button" size="mini" @click="handleUpdate(row)">编辑</el-button>
        <el-button v-hasPermi="['system:channel:delete']"
          type="danger"
          class="min-button"
          size="mini"
          @click="deleteItemChannel(row)">
          删除
        </el-button>
      </template>
    </km-table>

    <!--新增通道-->
    <el-dialog :title="isAdd ? '新增通道' : '编辑通道'" :visible.sync="isVisible" width="30%">
      <el-form :model="addForm" :rules="rules" ref="addForm" label-width="100px">
        <el-form-item label="通道编码" prop="channelCode">
          <el-input v-model="addForm.channelCode" placeholder="请输入通道编码"></el-input>
        </el-form-item>
        <el-form-item label="通道名称" prop="channelName">
          <el-input v-model="addForm.channelName" placeholder="请输入通道名称"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="isVisible = false">关闭</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitLoading">提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addChannelEntity, deleteChannel, getChannelList, upateStatus, updateChannelList} from '@/api/channel'

export default {
    name: "index",
    data(vm) {
      return {
        searchData: [
          {
            type: 'input',
            model: 'channelCode',
            title: '通道编码',
            placeholder: '请输入通道编码',
            clearable: true
          },
          {
            type: 'input',
            model: 'channelName',
            title: '通道名称',
            placeholder: '请输入通道名称',
            clearable: true
          },
          {
            type: 'select',
            model: 'status',
            title: '状态',
            placeholder: '全部',
            clearable: true,
            option: [
              { label: '正常', value: '0'},
              { label: '禁用', value: '1'},
            ]
          }
        ],
        tableConfig: {
          loading: false,
          searchQuery: {
            channelCode: '',
            channelName: '',
            status: ''
          },
          data: [],
          columns: [
            {
              value: 'id',
              label: 'ID',
              width: 100
            },
            {
              value: 'channelCode',
              label: '通道编码'
            },
            {
              value: 'channelName',
              label: '通道名称'
            },
            {
              value: 'status',
              label: '状态',
              slot: 'status',
              width: 160
            },
            {
              label: '操作',
              slot: 'operation',
              width: 180
            },
          ],
          hasPagination: true,
          pagination: {
            pageNum: 1,
            pageSize: 10,
            total: 0
          }
        },
        isVisible: false,
        isAdd: true,
        addForm: {
          channelCode: '',
          channelName: '',
          status: 0,
          userId: vm.$store.getters.id
        },
        rules: {
          channelCode: [
            { required: true, message: '请输入通道编码', trigger: ['change', 'blur'] }
          ],
          channelName: [
            { required: true, message: '请输入通道名称', trigger: ['change', 'blur'] }
          ]
        },
        submitLoading: false,
        isEditor: false,
        editorLoading: false,
        promptForm: {
          warnNotes: ''
        }
      }
    },
    mounted() {
      this.doSearch()
    },
    methods: {
      getList() {
        this.tableConfig.loading = true
        getChannelList({
          ...this.tableConfig.searchQuery,
          pageNum: this.tableConfig.pagination.pageNum,
          pageSize: this.tableConfig.pagination.pageSize
        }).then(res => {
          this.tableConfig.data = res.data.records || []
          this.tableConfig.pagination.total = res.data.total || 0
        }).finally(() => {
          this.tableConfig.loading = false
        })
      },
      // 搜索
      doSearch() {
        this.tableConfig.pagination.pageNum = 1
        this.getList()
      },
      // 新增通道
      addChannel() {
        this.isAdd = true
        this.addForm = {
          channelCode: '',
          channelName: '',
          userId: this.$store.getters.id
        }
        this.isVisible = true
      },
      submitAdd() {
        this.$refs.addForm.validate((valid) => {
          if (!valid) return
          this.submitLoading = true
          if(this.isAdd) {
            addChannelEntity({
              ...this.addForm
            }).then(res => {
              this.$message.success('新增成功')
              this.isVisible = false
              this.doSearch()
            }).finally(() => {
              this.submitLoading = false
            })
          } else {
            updateChannelList(this.addForm).then(res => {
              this.$message.success('编辑成功')
              this.isVisible = false
              this.doSearch()
            }).finally(() => {
              this.submitLoading = false
            })
          }
        })
      },
      // 编辑
      handleUpdate(row) {
        this.isAdd = false
        this.addForm = {
          id: row.id,
          channelCode: row.channelCode,
          channelName: row.channelName,
          userId: this.$store.getters.id
        }
        this.isVisible = true
      },
      // 更新状态
      updateChannelStatus(row) {
        upateStatus({
          id: row.id,
          status: row.status
        }).then(res => {
          this.$message.success('状态更新成功')
          this.getList()
        })
      },
      // 删除通道
      deleteItemChannel(row) {
        this.$confirm(`确认删除${row.channelCode}-${row.channelName}通道吗？`, '温馨提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
        }).then(res => {
          deleteChannel({id: row.id}).then(res => {
            this.$message.success('删除成功')
            this.doSearch()
          })
        })
      },
      // 重置
      resetTable() {
        this.$set(this.tableConfig, 'searchQuery', {
          channelCode: '',
          channelName: '',
          status: ''
        })
        this.doSearch()
      },
      // 分页
      tableChangeCurrent(e) {
        this.$set(this.tableConfig, 'pagination', {
          ...this.tableConfig.pagination,
          pageNum: e.current
        })
        this.getList()
      },
      tableChangeSize(e) {
        this.$set(this.tableConfig, 'pagination', {
          ...this.tableConfig.pagination,
          pageSize: e.pageSize
        })
        this.getList()
      }
    }
  }
</script>

<style scoped>

</style>
