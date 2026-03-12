<!--
* 服务商信息 --看自己
-->

<template>
  <div class="app-container">
    <km-table :table="tableConfig">
      <template v-slot:workStatus="{ row }">
        <el-switch
          :width="55"
          v-model="row.workStatus"
          :active-value="0"
          :inactive-value="1"
          :active-text="'开启'"
          :inactive-text="'关闭'"
          @change="changeStatus">
        </el-switch>
      </template>
      <template v-slot:orderPermission="{ row }">
        <el-tag size="mini" v-if="row.orderPermission === 0" type="success">开启</el-tag>
        <el-tag size="mini" v-else type="danger">关闭</el-tag>
      </template>
      <template v-slot:operation="{ row }">
        <el-button
          type="primary"
          class="min-button"
          @click="updatePws(row)">
          <i class="el-icon-edit"></i>
          修改密码
        </el-button>
        <el-button v-hasPermi="['system:merchant:safeCode']"
          type="danger"
          class="min-button"
          @click="handleSafe(row)">
          <i class="el-icon-edit"></i>
          安全码
        </el-button>
      </template>
      <template v-slot:jindanBalance="{ row }">
        <p class="text-center">{{ (row.balance - row.baseDeposit).toFixed(2) || 0 }}</p>
        <el-button
          type="success" class="min-button"
            @click="handleUpdateAmountToMerchant(row)">
           余额转码商
        </el-button>
      </template>
      <template v-slot:freezeAmount="{ row }">
        <p class="text-center">{{ row.freezeAmount || 0 }}</p>
        <el-button
          v-hasPermi="['system:merchant:trimFreezeToBalance']"
          type="success" class="min-button"
            @click="handleUpdateAmount(row)">
            转余额
        </el-button>
      </template>
      <template v-slot:balance="{ row }">
        <p class="text-center">{{ row.balance || 0 }}</p>
      </template>
    </km-table>

    <!--修改密码-->
    <el-dialog title="修改登录密码" :visible.sync="isVisible" width="30%" :destroy-on-close="true" @close="closeDialog">
      <el-form ref="form" :model="form" :rules="rule" label-width="120px">
        <el-form-item label="旧登录密码" prop="oldPassword">
          <el-input type="password" show-password v-model="form.oldPassword" placeholder="请输入旧登录密码"></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input type="password" show-password v-model="form.newPassword" placeholder="请输入新的登录密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input type="password" show-password v-model="form.confirmPassword" placeholder="请确认密码"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="isVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交</el-button>
      </div>
    </el-dialog>

    <!--安全码-->
    <update-safe ref="updateSafe"></update-safe>

    <!--佣金转余额---->
    <update-freeze-amount ref="updateFreezeAmountRef" @refresh="getList"></update-freeze-amount>


     <!--码商余额转他人-->
    <change-amount-to-merchant ref="changeAmountToMerchantRef" @refresh="getList"></change-amount-to-merchant>


 </div>
</template>

<script>
import {getMerchantDetail, stopWrokByMerchant} from '@/api/merchant';
import {updateUserPwd} from "@/api/system/user";
import updateSafe from '@/views/components/updateSafe.vue';
import updateFreezeAmount from '@/views/merchant/components/updateFreezeAmount.vue';
import ChangeAmountToMerchant from '@/views/merchant/components/changeAmountToMerchant.vue';


export default {
  components: { updateSafe, updateFreezeAmount,ChangeAmountToMerchant },
  name: "merchantAccountList",
  data(vm) {
    return {
      tableConfig: {
        loading: false,
        searchQuery: {},
        data: [],
        columns: [
          {
            value: 'userId',
            label: '登录ID'
          },
          {
            value: 'userName',
            label: '登录账户'
          },
          {
            value: 'balance',
            label: '余额(元)',
            slot: 'balance',
            align: 'center'
          },
          {
            value: 'jindanBalance',
            slot: 'jindanBalance',
            label: '接单余额(元)',
            align: 'center'
          },
          {
            value: 'baseDeposit',
            label: '押金(元)',
            align: 'center'
          },
          {
            value: 'freezeAmount',
            slot: 'freezeAmount',
            label: '佣金(元)',
            align: 'center'
          },
          {
            value: 'minAmount',
            label: '最低接单金额',
            align: 'center'
          },
          {
            value: 'workStatus',
            slot: 'workStatus',
            label: '开工状态',
            align: 'center'
          },
          {
            value: 'orderPermission',
            slot: 'orderPermission',
            label: '接单状态',
            align: 'center'
          },
          {
            value: 'operation',
            label: '操作',
            slot: 'operation',
            width: 180
          }
        ],
        hasPagination: false,
        pagination: {
          pageNum: 1,
          pageSize: 10,
          total: 0
        }
      },
      isVisible: false,
      submitLoading: false,
      form: {
        userId: '',
        userName: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rule: {
        oldPassword: [
          { required: true, message: '请输入旧登录密码', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/, message: '密码必须包含大小写字母和数字，且长度不少于6位', trigger: ['change', 'blur'] }
        ],
        confirmPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.getList()
  },
  methods: {
    getList() {
      this.tableConfig.loading = true
      getMerchantDetail({}).then(res => {
        this.tableConfig.data = [res.data] || []
      }).finally(() => {
        this.tableConfig.loading = false
      })
    },
      // 把自己的余额给其他码商
    handleUpdateAmountToMerchant(row, type) {
        this.$refs.changeAmountToMerchantRef.openDialog(row)
    },
    // 修改状态
    changeStatus(e) {
      this.tableConfig.loading = true
      stopWrokByMerchant({
        workStatus: e
      }).then(() => {
        this.$message.success('操作成功');
        this.getList();
      }).finally(() => {
        this.tableConfig.loading = false
      });
    },
    // 修改密码
    updatePws(row) {
      this.form = this.$lodash.cloneDeep(row)
      this.isVisible = true
    },
    // 提交  修改密码
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        // 两次密码是否一致
        if (this.form.newPassword !== this.form.confirmPassword) {
          this.$message.error('两次密码不一致');
          return
        }
        this.submitLoading = true
        updateUserPwd(this.user.oldPassword, this.user.newPassword).then(() => {
          this.$message.success('修改成功');
          this.closeDialog();
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },
    closeDialog() {
      this.isVisible = false
      this.$refs.form.resetFields()
    },
    // 佣金转余额
    handleUpdateAmount(row) {
      this.$refs.updateFreezeAmountRef.openDialog(row)
    },
    // 安全码
    handleSafe() {
      this.$refs.updateSafe.openDialog(true)
    }
  }
}
</script>
