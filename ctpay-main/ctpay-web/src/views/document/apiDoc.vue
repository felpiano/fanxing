<!--代收文档-->
<template>
  <div class="app-container">
    <el-tabs class="api-doc" v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="签名规则" name="1">
        <div class="api-page api-page_bg">
          <p>备注：</p>
          <p class="api-page-nm">
            <b style="color: red;">除sign其余参数都要参与签名</b>
          </p>
          <p>第一步：根据充值文档里面的参数，首字母大小写进行排序</p>
          <p class="api-page-nm">
            amount=200&body=21&channel=taobao_pc¤cy=RMB&mchid=100001&notify_url=http://127.0.0.1/notify.php&out_trade_no=1908241507119718&return_url=http://127.0.0.1/return.php&time_stamp=20190824030711
          </p>
          <p>第二步：排序结束后，增加&key=商户秘钥</p>
          <p class="api-page-nm">
            amount=200&body=21&channel=taobao_pc&mchid=100001&notify_url=http://127.0.0.1/notify.php&out_trade_no=1908241507119718&return_url=http://127.0.0.1/return.php&time_stamp=20190824030711&key=xxxx
          </p>
          <p>第三步：md5加密</p>
          <p class="api-page-nm">
            进行md5,签名字符串全部转为小写
          </p>
          <p>PHP加密示例代码</p>
          <div class="api-page-nm">
            <div class="hljs">
              <span class="hljs-meta text-1em">&lt;?php</span>
              <span class="hljs-function text-2em"><span class="hljs-keyword">function</span> <span class="hljs-title">generateSign</span>(<span class="hljs-params"><span class="hljs-variable">$data</span>, <span class="hljs-variable">$key</span></span>) {</span>
              <span class="hljs-comment">// 按字母顺序排序参数</span>
              <span>ksort(<span class="hljs-variable">$data</span>);</span>

              <span class="hljs-comment">// 拼接参数和商户秘钥</span>
              <span><span class="hljs-variable">$signData</span> = <span class="hljs-string">''</span>;</span>
              <span><span class="hljs-keyword">foreach</span> (<span class="hljs-variable">$data</span> <span class="hljs-keyword">as</span> <span class="hljs-variable">$k</span> =&gt; <span class="hljs-variable">$value</span>) {</span>
              <span class="text-4em"><span class="hljs-variable">$signData</span> .= <span class="hljs-variable">$k</span> . <span class="hljs-string">'='</span> . <span class="hljs-variable">$value</span> . <span class="hljs-string">'&amp;'</span>;</span>
              <span>}</span>
              <span><span class="hljs-variable">$signData</span> .= <span class="hljs-string">'key='</span> . <span class="hljs-variable">$key</span>;</span>

              <span class="hljs-comment">// 使用MD5进行加密</span>
              <span><span class="hljs-keyword">return</span> md5(<span class="hljs-variable">$signData</span>);</span>
              <span class="text-2em">}</span>

              <span class="text-2em"><span class="hljs-variable">$data</span> = [</span>
              <span><span class="hljs-string">'amount'</span> =&gt; <span class="hljs-string">'200.00'</span>,</span>
              <span><span class="hljs-string">'body'</span> =&gt; <span class="hljs-string">'123'</span>,</span>
              <span><span class="hljs-string">'channel'</span> =&gt; taobao_pc,</span>
              <span><span class="hljs-string">'currency'</span> =&gt; <span class="hljs-string">'RMB'</span>,</span>
              <span><span class="hljs-string">'mchid'</span> =&gt; <span class="hljs-number">100001</span>,</span>
              <span><span class="hljs-string">'notify_url'</span> =&gt; <span class="hljs-string">'http://127.0.0.1/return.php'</span>,</span>
              <span><span class="hljs-string">'out_trade_no'</span> =&gt; <span class="hljs-number">2305242358355318</span>,</span>
              <span><span class="hljs-string">'return_url'</span> =&gt; <span class="hljs-string">'http://127.0.0.1/return.php'</span>,</span>
              <span><span class="hljs-string">'time_stamp'</span> =&gt; date(<span class="hljs-string">'Ymdhis'</span>),</span>
              <span class="text-2em">];</span>
              <span class="text-2em"><span class="hljs-variable">$key</span> = <span class="hljs-variable">$Md5key</span>; <span class="hljs-comment">// 商户秘钥</span></span>

              <span class="text-2em"><span class="hljs-variable">$sign</span> = generateSign(<span class="hljs-variable">$data</span>, <span class="hljs-variable">$key</span>);</span>

              <span class="hljs-meta text-1em">?&gt;</span>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="充值api" name="2">
        <div class="api-page">
          <div class="api-page_bg">
            <p>网关地址：http://www.xxxx.com/api/order/create</p>
            <br/>
            <p>注意要点：POST  <b style="color: red;font-size: 20px">form-data</b> 方式请求</p>
          </div>
          <el-tabs v-model="apiActiveName" type="card">
            <el-tab-pane label="参数说明" name="1">
              <km-table :table="tableConfig"></km-table>
            </el-tab-pane>
            <el-tab-pane label="返回示例" name="2">
              <p class="api-page-nm fhsl">
                <span class="text-2em">{</span>
                  <span class="text-4em">"code": 0,</span>
                  <span class="text-4em">"msg": "操作成功",</span>
                  <span class="text-4em">"data": {</span>
                    <span>"request_url": "http://xxxxxxxx/payThird.jsp?orderId=easypay2019071831607197005045326668",</span>
                    <span>"money": "100",//订单金额</span>
                    <span>"pay_price": "98.97",//付款金额</span>
                    <span>"account_name": "",//账户名称</span>
                    <span>"bank_name": "中国银行",//银行名称</span>
                    <span>"bank_zhihang": "中国银行",//支行名称</span>
                    <span>"account_number": "111111111111",//银行卡号</span>
                  <span class="text-4em">}</span>
                <span class="text-2em">}</span>
              </p>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>
      <el-tab-pane label="异步通知" name="3">
        <div class="api-page">
          <div class="api-page_bg">
            <p>该通知以POST  <b style="color: red;font-size: 20px">form-data</b> 方式请求，编码：UTF8</p>
            <br/>
            <p>SUCCESS 返回成功表示通知成功</p>
            <br/>
            <p>备注 总共通知6次 直到返回 SUCCESS 不会在发起通知，超过6次不会在发起通知</p>
          </div>
          <el-tabs v-model="apiActiveName2" type="card">
            <el-tab-pane label="参数说明" name="1">
              <km-table :table="tableConfig2"></km-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>
      <el-tab-pane label="订单查询" name="4">
        <div class="api-page">
          <div class="api-page_bg">
            <p>网关地址：http://www.xxxx.com/api/order/query</p>
            <br/>
            <p>注意要点：POST  <b style="color: red;font-size: 20px">form-data</b> 方式请求，编码：UTF8</p>
          </div>
          <el-tabs v-model="apiActiveName3" type="card">
            <el-tab-pane label="参数说明" name="1">
              <km-table :table="tableConfig3"></km-table>
            </el-tab-pane>
            <el-tab-pane label="返回内容示例" name="2">
              <p class="api-page-nm fhsl">
                <span class="text-2em">{</span>
                  <span class="text-4em">"code": 0,</span>
                  <span class="text-4em">"msg": "操作成功",</span>
                  <span class="text-4em">"data": {</span>
                    <span>"trade_no": "平台订单号",</span>
                    <span>"out_trade_no": "商户订单号",</span>
                    <span>"account_name": "",//账户名称</span>
                    <span>"amount": "订单金额",</span>
                    <span>"status": "订单状态【CLOSE：订单关闭，等待支付：WAIT，SUCCESS：订单已成功】",</span>
                  <span class="text-4em">}</span>
                <span class="text-2em">}</span>
              </p>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>
      <!--<el-tab-pane label="余额查询" name="5">
        <div class="api-page">
          <div class="api-page_bg">
            <p>网关地址：http://www.xxxx.com/api/pay/balance</p>
            <br/>
            <p>注意要点：POST  <b style="color: red;font-size: 20px">form-data</b> 方式请求，编码：UTF8</p>
          </div>
          <el-tabs v-model="apiActiveName4" type="card">
            <el-tab-pane label="参数说明" name="1">
              <km-table :table="tableConfig3"></km-table>
            </el-tab-pane>
            <el-tab-pane label="返回内容示例" name="2">
              <p class="api-page-nm fhsl">
                <span class="text-2em">{</span>
                  <span class="text-4em">"code": 0,</span>
                  <span class="text-4em">"msg": "操作成功",</span>
                  <span class="text-4em">"data": {</span>
                    <span> "balance":"100.000"</span>
                  <span class="text-4em">}</span>
                <span class="text-2em">}</span>
              </p>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>-->
      <el-tab-pane label="充值方式" name="6">
        <el-descriptions title="" :column="1" border>
          <el-descriptions-item>
            <template slot="label"><b>渠道名称</b></template>
            <b>渠道值</b>
          </el-descriptions-item>
          <!-- <el-descriptions-item label="支付宝扫码">alipaycode</el-descriptions-item> -->
          <el-descriptions-item label="支付宝扫码小额">alipayCodeSmall</el-descriptions-item>
          <el-descriptions-item label="支付宝扫码大额">alipayCodeBig</el-descriptions-item>
          <el-descriptions-item label="银联扫码">unionPay</el-descriptions-item>
          <el-descriptions-item label="聚合码">AggregateCode</el-descriptions-item>
          <el-descriptions-item label="微信扫码">wechatCode</el-descriptions-item>
          <el-descriptions-item label="支付宝UID">alipayUid</el-descriptions-item>
          <el-descriptions-item label="BC高内卡卡">bankCard</el-descriptions-item>
          <el-descriptions-item label="外汇卡卡">bankCardBig</el-descriptions-item>
          <el-descriptions-item label="专供外汇卡卡">nightBankCardSmall</el-descriptions-item>
          <el-descriptions-item label="公户卡卡大额">nightBankCardBig</el-descriptions-item>
          <el-descriptions-item label="支付宝固额">alipayFixedLimit</el-descriptions-item>
          <el-descriptions-item label="一码通">onebuy</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
export default {
  name: 'apiDoc',
  data() {
    return {
      activeName: '1',
      apiActiveName: '1',
      apiActiveName2: '1',
      apiActiveName3: '1',
      apiActiveName4: '1',
      tableConfig: {
        data: [
          { name: 'mchid', type: 'string', sign: '是', required: '是', desc: '商户id' },
          { name: 'out_trade_no', type: 'string', sign: '是', required: '是', desc: '商户订单id 唯一 不能重复 重复会创建订单失败 由商户控制' },
          { name: 'amount', type: 'double', sign: '是', required: '是', desc: '金额 最大两位小数' },
          { name: 'channel', type: 'string', sign: '是', required: '是', desc: '渠道 见附件 渠道列表' },
          { name: 'notify_url', type: 'string', sign: '是', required: '是', desc: '异步通知地址 必须http:或者https:格式' },
          { name: 'return_url', type: 'string', sign: '是', required: '是', desc: '同步通知地址 必须http:或者https:格式' },
          { name: 'time_stamp', type: 'string', sign: '是', required: '是', desc: '时间如20180824030711' },
          { name: 'body', type: 'string', sign: '是', required: '是', desc: '宝转卡 必须传支付宝用户名 否则无法到账其它请填１２３' },
          { name: 'sign', type: 'string', sign: '是', required: '否', desc: '签名 见签名规则' }
        ],
        columns: [
          { value: 'name', label: '参数' },
          { value: 'type', label: '类型', width: 160 },
          { value: 'sign', label: '参与签名', width: 160  },
          { value: 'required', label: '是否必填', width: 160  },
          { value: 'desc', label: '描述', width: 460 }
        ],
        hasPagination: false
      },
      tableConfig2:{
        data:[
          { name: 'amount', type: 'string', sign: '是', required: '是', desc: '金额' },
          { name: 'body', type: 'string', sign: '是', required: '是', desc: '成功时间 时间戳' },
          { name: 'channel', type: 'string', sign: '是', required: '是', desc: '充值方式' },
          { name: 'trade_no', type: 'string', sign: '是', required: '是', desc: '平台订单号' },
          { name: 'out_trade_no', type: 'string', sign: '是', required: '是', desc: '商户订单号' },
          { name: 'order_status', type: 'string', sign: '是', required: '是', desc: '状态 1 成功' },
          { name: 'sign', type: 'string', sign: '是', required: '否', desc: '签名 见签名规则' }
        ],
        columns: [
          { value: 'name', label: '参数' },
          { value: 'type', label: '类型' },
          { value: 'sign', label: '参与签名' },
          { value: 'required', label: '是否必填' },
          { value: 'desc', label: '描述' }
        ],
      },
      tableConfig3: {
        data:[
          { name: 'out_trade_no', type: 'string', required: '是', desc: '商户订单号' },
          { name: 'mchid', type: 'string', required: '是', desc: '商户号' },
          { name: 'channel', type: 'string', required: '是', desc: '渠道编码' },
          { name: 'sign', type: 'string', required: '是', desc: '签名 见签名规则' }
        ],
        columns: [
          { value: 'name', label: '参数' },
          { value: 'type', label: '类型' },
          { value: 'required', label: '是否必填' },
          { value: 'desc', label: '描述' }
        ],
      },
      tableConfig4: {
        data: [
          { name: 'mchid', type: 'string', required: '是', desc: '商户号' },
          { name: 'sign', type: 'string', required: '是', desc: '签名 见签名规则' }
        ],
        columns: [
          { value: 'name', label: '参数' },
          { value: 'type', label: '类型' },
          { value: 'required', label: '是否必填' },
          { value: 'desc', label: '描述' }
        ],
      }
    }
  },
  methods: {
    handleClick(tab, event) {
      console.log(tab, event);
    }
  }
}
</script>

<style lang="scss" scoped>
  ::v-deep {
    .api-doc {
      width: 100%;
      height: 100%
    }
    .el-tabs__content {
      width: 100%;
      height: 92%
    }
    .el-tab-pane {
      width: 100%;
      height: 100%;
      overflow: hidden;
    }

    .el-descriptions {
      width: 500px
    }
  }
  .api-page {
    width: 100%;
    height: 100%;
    overflow: auto;
    &_bg {
      overflow: auto;
      margin-bottom: 10px;
      padding: 15px;
      line-height: 22px;
      border-left: 5px solid #009688;
      border-radius: 0 2px 2px 0;
      background-color: #f2f2f2;
      word-break: break-all;
      font-size: 14px;
      color: #000;
    }
    p {
      line-height: 24px;
    }
    &-nm {
      margin-bottom: 10px;
      padding: 15px;
      border-radius: 0 2px 2px 0;
      border-style: solid;
      border-width: 1px 1px 1px 5px;
      border-color: #e6e6e6;
      background: transparent;
    }

    .hljs {
      background: #23241f;
      color: #f8f8f2;
      font-family: monospace;
      text-indent: -2em;
      text-align: left;
      display: block;
      overflow-x: auto;
      padding: 16px 50px;

      > span {
        display: block;
        text-indent: 3rem;
      }
    }

    hljs-meta {
      color: #75715e;
    }

    .hljs-attr, .hljs-keyword, .hljs-name, .hljs-selector-tag {
      color: #f92672;
    }

    .hljs-code, .hljs-section, .hljs-selector-class, .hljs-title {
      color: #a6e22e;
    }
    .hljs-class .hljs-title, .hljs-params, .hljs-title.class_ {
      color: #f8f8f2;
    }
    .hljs-comment, .hljs-deletion, .hljs-meta {
      color: #75715e;
    }
    .hljs-addition, .hljs-built_in, .hljs-selector-attr, .hljs-selector-id, .hljs-selector-pseudo, .hljs-string, .hljs-template-variable, .hljs-type, .hljs-variable {
      color: #e6db74;
    }
    .hljs-bullet, .hljs-link, .hljs-literal, .hljs-number, .hljs-quote, .hljs-regexp {
      color: #ae81ff;
    }

    .fhsl {
      position: relative;
      margin: 10px 0;
      padding: 15px;
      line-height: 20px;
      border: 1px solid #ddd;
      border-left-width: 6px;
      background-color: #F2F2F2;
      color: #333;
      font-family: Courier New;
      font-size: 12px;
      span {
        display: block;
        text-indent: 5rem;
      }
    }

    span.text-4em {
      text-indent: 4em;
    }
    span.text-2em {
      text-indent: 2em;
    }
    span.text-3em {
      text-indent: 3em;
    }
    span.text-1em {
      text-indent: 1em;
    }
  }
</style>
