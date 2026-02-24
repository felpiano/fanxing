<!--
** 渠道配置
-->

<template>
  <el-dialog title="渠道配置" append-to-body :visible.sync="isVisible" width="40%">
    <el-form ref="form" :model="form" :rules="rules" label-width="180px" class="dialog-form-height">
      <el-form-item label="订单超时时间(分钟)" prop="overtime">
        <el-input-number :controls="false" v-model="form.overtime" placeholder="请输入订单超时时间" class="w100"></el-input-number>
      </el-form-item>
      <el-form-item label="最大二维码数量" prop="maxCode">
        <el-input v-model="form.maxCode" placeholder="请输入最大二维码数量"></el-input>
      </el-form-item>
      <el-form-item label="隐藏二维码" prop="hideQrcode">
        <el-radio-group v-model="form.hideQrcode">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="自定义引导语" prop="guidance">
        <el-input type="textarea" autosize v-model="form.guidance" placeholder="请输入自定义引导语"></el-input>
      </el-form-item>
      <el-form-item label="支付页面模板" prop="qrcodeModel">
        <el-select v-model="form.qrcodeModel" class="w100">
          <el-option
            v-for="item in qrcodeModelList"
            :key="item.url"
            :label="item.name"
            :value="item.url" >
          </el-option>
        </el-select>
        <!-- <p class="remark-text"><span class="text-red pointer" @click="checkImage">点击</span> 查看模板图片</p> -->
      </el-form-item>
      <el-form-item label="支付页面是否提交姓名" prop="payurlUser">
        <el-radio-group v-model="form.payurlUser">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否跳转支付宝" prop="initpageForwrod" v-if="form.qrcodeModel && form.qrcodeModel.indexOf('alipay') > -1">
        <el-radio-group v-model="form.initpageForwrod">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="跳转方式" prop="jumpType" v-if="(form.qrcodeModel && form.qrcodeModel.indexOf('alipay') > -1) || form.qrcodeModel === 'wechatCode' ">
        <el-radio-group v-model="form.jumpType">
          <el-radio :label="0">转账</el-radio>
          <el-radio :label="1">扫码</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否需要账号" prop="accountNeed">
        <el-radio-group v-model="form.accountNeed">
          <el-radio :label="0">是</el-radio>
          <el-radio :label="1">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="订单金额浮动" prop="overAmount">
        <el-radio-group v-model="form.overAmount">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="订单浮动金额比例" prop="overAmountRate">
        <el-input v-model="form.overAmountRate" placeholder="请输入订单浮动金额比例"></el-input>
      </el-form-item>
      <el-form-item label="订单浮动金额个数" prop="overAmountValue">
        <el-input v-model="form.overAmountValue" placeholder="请输入订单浮动金额个数"></el-input>
      </el-form-item>
      <!-- <el-form-item label="锁码" prop="lockedCode">
        <el-radio-group v-model="form.lockedCode">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item> -->
      <el-form-item label="是否展示通道" prop="showChannel">
        <el-radio-group v-model="form.showChannel">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <!--<el-form-item label="码商设置收款码金额区间" prop="setAmount">
        <el-radio-group v-model="form.setAmount">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="显示确认收款" prop="showConfirm">
        <el-radio-group v-model="form.showConfirm">
          <el-radio :label="0">开启</el-radio>
          <el-radio :label="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>-->
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="isVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {updateAgentChannel} from '@/api/agent'

export default {
  name: "channelConfig",
  data() {
    return {
      isVisible: false,
      submitLoading: false,
      form: {},
      rules: {
        // maxCode: [
        //   { required: true, message: '请输入最大二维码数量', trigger: 'blur' }
        // ],
        // qrcodeModel: [
        //   { required: true, message: '请选择支付页面模板', trigger: 'blur' }
        // ],
        // hideQrcode: [
        //   { required: true, message: '请选择是否隐藏二维码', trigger: 'blur' }
        // ],
        // overtime: [
        //   { required: true, message: '请输入订单超时时间', trigger: 'blur' }
        // ],
        // payurlUser: [
        //   { required: true, message: '请选择支付页面是否提交姓名', trigger: 'blur' }
        // ],
        // overAmount: [
        //   { required: true, message: '请选择订单金额浮动', trigger: 'blur' }
        // ],
        // lockedCode: [
        //   { required: true, message: '请选择是否锁码', trigger: 'blur' }
        // ],
        // setAmount: [
        //   { required: true, message: '请选择码商设置收款码金额区间', trigger: 'blur' }
        // ]
      },
      qrcodeModelList: [
        { id: 1, name: '模板1(支付宝扫码)', url: 'alipaycode', src: '1.png' },
        { id: 2, name: '模板2(银联扫码)', url: 'unionPay', src: '2.png' },
        { id: 3, name: '模板3(数字人民币)', url: 'CnyNumber', src: '3.png' },
        { id: 4, name: '模板4(聚合码)', url: 'AggregateCode', src: '4.png' },
        { id: 5, name: '模板5(微信扫码)', url: 'wechatCode', src: '5.png' },
        { id: 5, name: '模板6(银行卡)', url: 'bankCard', src: '6.png' },
        { id: 5, name: '模板7(金条)', url: 'goldBar', src: '7.png' },
        { id: 5, name: '模板8(微信内付)', url: 'weChatInPay', src: '8.png' }
      ],
      qrcodeModel: ''
    }
  },
  methods: {
    openDialog(row) {
      this.form = this.$lodash.cloneDeep(row)
      let channelCode = ''
      if (row.channelCode.includes('alipay')) {
        channelCode = 'alipaycode'
      } else if (row.channelCode.includes('bankCard') || row.channelCode.includes('BankCard') || row.channelCode.includes('Card')) {
        channelCode = 'bankCard'
      } else if (row.channelCode.includes('goldBar')) {
        channelCode = 'goldBar'
      } else {
        channelCode = row.channelCode
      }
      this.form.qrcodeModel = !this.form.qrcodeModel ? channelCode : this.form.qrcodeModel
      this.isVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.submitLoading = true
          updateAgentChannel([this.form]).then(res => {
            this.$message.success('保存成功')
            this.$emit('updateList')
            this.isVisible = false
          }).finally(() => {
            this.submitLoading = false
          })
        }
      })
    }
  }
}
</script>
