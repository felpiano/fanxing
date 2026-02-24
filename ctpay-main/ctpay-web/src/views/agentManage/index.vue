<!--
** 代理列表
-->
<template>
  <div class="app-container">
    <search-bar
      :searchData="searchData"
      :query="tableConfig.searchQuery"
      @on-search-change="doSearch"
      @on-reset="resetTable">
    </search-bar>
    <km-table :table="tableConfig" @tableChangeCurrent="tableChangeCurrent" @tableChangeSize="tableChangeSize">
      <template v-slot:table-after>
        <el-button type="primary" size="mini" @click="addAgent" v-hasPermi="['system:agent:add']">
          <i class="el-icon-plus"></i>
          添加代理
        </el-button>
      </template>
      <template v-slot:balance="{ row }">
        <div class="flex-center column">
          <p class="text-red1 text-center">{{ row.balance || 0}}</p>
          <el-button v-hasPermi="['system:agent:saveBalance']" type="success" class="min-button" size="mini" @click="handleRecharge(row)">增减</el-button>
        </div>
      </template>
      <template v-slot:status="{ row }">
        <el-switch
          :width="50"
          v-model="row.status"
          :active-value="0"
          :inactive-value="1"
          active-text="正常"
          inactive-text="禁用"
          @change="updateChannelStatus(row)">
        </el-switch>
      </template>
      <template v-slot:googleSecret="{ row }">
        <div class="text-center w100">
          <el-button v-if="row.googleSecret && row.googleSecret !== ''" type="text" size="mini" @click="handleGoogleSecret(row)">点击查看</el-button>
          <p v-else>--</p>
        </div>
      </template>
      <template v-slot:uid="{ row }">
        <div class="text-center w100">
          <el-button v-if="row.uid && row.uid !== ''" type="text" size="mini" @click="handleUid(row)">点击查看</el-button>
          <p v-else>--</p>
        </div>
      </template>
      <template v-slot:operation="{ row }">
        <el-button v-hasPermi="['system:googleSecret:clear']" type="danger" class="min-button" @click="handleClearGoogle(row)">清谷歌</el-button>
        <el-button v-hasPermi="['system:agent:update']"
          type="primary"
          class="min-button"
          size="mini"
          @click="handleEditor(row)">
          编辑
        </el-button>
        <el-button v-hasPermi="['system:agent:product']"
          type="primary"
          class="min-button"
          size="mini"
          @click="handleProduct(row)">
          费率
        </el-button>
        <el-button v-hasPermi="['system:agent:delete']"
          type="danger"
          class="min-button"
          size="mini"
          @click="handleDelete(row)">
          删除
        </el-button>
      </template>
    </km-table>

     <!--新增代理-->
     <el-dialog :title="isAdd ? '新增代理' : '编辑代理'" :visible.sync="isVisible" width="30%" @close="closeDialog">
      <el-form :model="addForm" :rules="rules" ref="addForm" label-width="100px">
        <el-form-item label="用户名称" prop="userName">
          <el-input v-model="addForm.userName" placeholder="请输入用户名称" :disabled="!isAdd"></el-input>
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickName">
          <el-input v-model="addForm.nickName" placeholder="请输入用户昵称"></el-input>
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input show-password v-model="addForm.password" :placeholder="isAdd ? '请输入登录密码' : '不修改请留空'"></el-input>
        </el-form-item>
        <el-form-item label="登录白名单IP" prop="allowLoginIp">
          <div class="flex-center">
            <div v-for="(item, index) in whiteLoginIpList" :key="index" class="mr10 mb10">
              <el-input style="width: 160px" v-model="item.ip" placeholder="登录IP白名单"></el-input>
              <el-button class="ml10" type="danger" size="small" @click="deleteWhiteIp(index)">删除</el-button>
            </div>
            <el-button class="mb10" type="primary" size="small" @click="addWhiteIp">添加</el-button>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitLoading">提交</el-button>
      </div>
    </el-dialog>

    <!--充值-->
    <update-balance ref="updateBalance" @refresh="getList"></update-balance>

    <!--费率-->
    <config-rate ref="configRateRef"></config-rate>
  </div>
</template>

<script>
import {addAgentEntity, deleteAgent, getAgentEntityList, upateStatus, updateAgentEntity} from "@/api/agent"
import {clearGoogle} from '@/api/merchant'
import useClipboard from "vue-clipboard3"
import updateBalance from './components/updateBalance.vue'
import configRate from './components/configRate.vue'

export default {
    name: "index",
    components: {
      updateBalance,
      configRate
    },
    data(vm) {
      return {
        searchData: [
          {
            type: 'input',
            model: 'userName',
            title: '用户名称',
            placeholder: '请输入用户名称',
            clearable: true
          },
          {
            type: 'input',
            model: 'nickName',
            title: '用户昵称',
            placeholder: '请输入用户昵称',
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
            nickName: '',
            userName: '',
            status: ''
          },
          data: [],
          columns: [
            {
              value: 'userId',
              label: '用户ID'
            },
            {
              value: 'userName',
              label: '用户名称'
            },
            {
              value: 'nickName',
              label: '用户昵称'
            },
            {
              value: 'balance',
              label: '余额',
              slot: 'balance',
              align: 'center'
            },
            {
              value: 'googleSecret',
              slot: 'googleSecret',
              label: '谷歌密钥',
              align: 'center'
            },
            {
              value: 'uid',
              slot: 'uid',
              label: '登录URL',
              align: 'center'
            },
            {
              value: 'status',
              label: '状态',
              slot: 'status',
              align: 'center'
            },
            {
              value: 'lastLoginIp',
              label: '最后登录IP',
              width: 140
            },
            {
              value: 'lastLoginTime',
              label: '最后登录时间',
              width: 160
            },
            {
              label: '操作',
              slot: 'operation',
              align: 'center',
              width: 200
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
        addForm: {
          userName: '',
          nickName: '',
          password: '',
          status: 0,
          allowLoginIp: ''
        },
        rules: {
          nickName: [
            { required: true, message: '请输入用户昵称', trigger: ['change', 'blur'] }
          ],
          userName: [
            { required: true, message: '请输入用户名称', trigger: ['change', 'blur'] },
            // { pattern: /^[a-zA-Z0-9]{1,}$/, message: '名称只能包含字母、数字', trigger: ['change', 'blur'] }
          ]
        },
        isAdd: true,
        submitLoading: false,
        whiteLoginIpList: [] // 登录白名单
      }
    },
    mounted() {
      this.doSearch()
    },
    methods: {
      doSearch() {
        this.tableConfig.pagination.pageNum = 1
        this.getList()
      },
      // 代理列表
      getList() {
        this.tableConfig.loading = true
        getAgentEntityList({
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
      // 新增代理
      addAgent() {
        this.isAdd = true
        this.addForm = {
          userName: '',
          nickName: '',
          password: '',
          status: 0,
          allowLoginIp: ''
        },
        this.rules.password = [
          { required: true, message: '请输入登录密码', trigger: ['change', 'blur'] },
            // 正则验证，必须有数字和字母，字母必须有大小写
          { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, message: '密码必须包含大小写字母和数字，且长度不少于6位', trigger: ['change', 'blur'] }
        ]
        this.whiteLoginIpList = []
        this.isVisible = true
      },
      // 添加登录IP白名单
      addWhiteIp() {
        this.whiteLoginIpList.push({ip: ''})
      },
      // 删除登录IP白名单
      deleteWhiteIp(index) {
        this.whiteLoginIpList.splice(index, 1)
      },
      submitAdd() {
        this.$refs.addForm.validate((valid) => {
          if (!valid) return
          this.submitLoading = true
          if (this.whiteLoginIpList.length > 0) {
            this.addForm.allowLoginIp = this.whiteLoginIpList.map(item => item.ip).join(';')
          } else {
            this.addForm.allowLoginIp = ''
          }
          if(this.isAdd) {
            addAgentEntity({
              ...this.addForm
            }).then(res => {
              this.$message.success('新增成功')
              this.doSearch()
              this.closeDialog()
            }).finally(() => {
              this.submitLoading = false
            })
          } else {
            if (this.addForm.password && this.addForm.password !== '' && !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/.test(this.addForm.password)) {
              this.$message.error('密码必须包含大小写字母和数字，且长度不少于6位')
              this.submitLoading = false
              return
            }
            updateAgentEntity({
              ...this.addForm
            }).then(res => {
              this.$message.success('编辑成功')
              this.getList()
              this.closeDialog()
            }).finally(() => {
              this.submitLoading = false
            })
          }
        })
      },
      // 关闭弹窗
      closeDialog() {
        this.isVisible = false
        this.$refs.addForm.clearValidate()
      },
      // 清谷歌
      handleClearGoogle(row) {
        this.$confirm(`确认清除${row.userName}【${row.userId}】的谷歌密钥吗？`, '温馨提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
        }).then(res => {
          clearGoogle({userId: row.userId}).then(() => {
            this.$message.success('清除成功');
            this.getList();
          });
        })
      },
      // 查看谷歌验证码
      handleGoogleSecret(row) {
        this.$confirm(`${row.googleSecret}`, '谷歌密钥', {
          confirmButtonText: '复制',
          cancelButtonText: '取消',
        }).then(() => {
          const { toClipboard } = useClipboard()
          toClipboard(row.googleSecret)
          this.$message.success('复制成功')
        })
      },
      // 查看登录密钥
      handleUid(row) {
        this.$confirm(`https://admin.jszdf.xyz/#/login?xhssf=${row.uid}`, '登录URL', {
          confirmButtonText: '复制',
          cancelButtonText: '取消',
        }).then(() => {
          const { toClipboard } = useClipboard()
          toClipboard(`https://admin.jszdf.xyz/#/login?xhssf=${row.uid}`)
          this.$message.success('复制成功')
        })
      },
      // 更新状态
      updateChannelStatus(row) {
        if (!this.$store.getters.permissions.includes('system:agent:update')) {
          return this.$message.warning('暂无操作权限')
        }
        upateStatus({
          userId: row.userId,
          status: row.status
        }).then(res => {
          this.$message.success('状态更新成功')
          this.getList()
        })
      },
      // 充值
      handleRecharge(row) {
        this.$refs.updateBalance.openDialog(row)
      },
      // 编辑
      handleEditor(row) {
        this.isAdd = false
        this.addForm = {
          userId: row.userId,
          userName: row.userName,
          nickName: row.nickName,
          password: '',
          status: row.status,
          allowLoginIp: row.allowLoginIp,
        },
        this.rules.password = [
          { required: false, message: '请输入登录密码', trigger: ['change', 'blur'] }
        ]
        this.whiteLoginIpList = row.allowLoginIp ? row.allowLoginIp.split(';').map(item => ({ip: item})) : []
        this.isVisible = true
      },
      // 产品设置
      handleProduct(row) {
        this.$refs.configRateRef.open(row)
      },
      // 删除
      handleDelete(row) {
        this.$confirm(`确认删除${row.userName}【${row.userId}】代理吗？`, '温馨提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
        }).then(res => {
          deleteAgent({userId: row.userId}).then(res => {
            this.$message.success('删除成功')
            this.doSearch()
          })
        })
      },
      // 重置
      resetTable() {
        this.tableConfig.searchQuery = {
          nickName: '',
          userName: '',
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
