document.addEventListener('contextmenu', e => e.preventDefault());
document.onkeydown = function (e) {
  if (e.keyCode === 123 || (e.ctrlKey && e.shiftKey && e.keyCode === 73)) {
    return false;
  }
}
new Vue({
  el: '#app',
  data() {
    return {
      loading: true,
      isVisible: false,
      payer: '',
      toastText: '',
      toastShow: false,
      finalURL: '',
      orderInfo: {
        tradeNo: '',
        fixedAmount: 0,
        orderStatus: -1,
        alipayUID: '',
        klUid: '',
        nickName: ''
      },
      countDown: {
        m: 0,
        s: 0
      }
    }
  },
  created() {
    const query = this.getUrlParam(window.location.href)
    if (JSON.stringify(query) !== '{}') {
      this.getOrderInfo(query)
    } else {
      this.loading = false
    }
  },
  methods: {
    getUrlParam (href) {
      const query = {};
      const search = href.substring(href.lastIndexOf('?') + 1);
      const arr = search.split('&');
      for (let i = 0; i < arr.length; i++) {
        const key = arr[i].split('=')[0];
        query[key] = arr[i].split('=')[1]
      }
      return query
    },
    getOrderInfo(query) {
      if (!query.orderNo) {
        this.loading = false
        return
      }
      axios.get('https://cfxpay.jszdf.xyz/api/order/getOrderData', {
        params: {
          tradeNo: query.orderNo
        }
      }).then(res => {
        if (res.status === 200) {
          if (res.data.code !== 200) {
            this.setToast(res.data.msg)
            this.loading = false
            return
          }
          const orderData = res.data.data || {}
          if (JSON.stringify(orderData) === '{}') {
            this.setToast('获取订单信息失败')
            this.loading = false
            return
          }

          this.loading = false
          if (orderData.orderStatus !== 0 ) {
            this.orderInfo.orderStatus = orderData.orderStatus
            return
          }
          this.isVisible = orderData.memberNameFlag === 0

          this.countDownFun(orderData)
          const merchantQrcode = orderData.merchantQrcode || {}
          this.orderInfo = {
            tradeNo: orderData.tradeNo,
            fixedAmount: orderData.fixedAmount,
            orderStatus: orderData.orderStatus,
            alipayUID: merchantQrcode.uid || '',
            klUid: orderData.uid,
            nickName: merchantQrcode.nickName
          }
          this.generateLink(this.orderInfo)
        }
      })
    },
    generateLink(orderInfo) {
      const userId = orderInfo.alipayUID
      const money = orderInfo.fixedAmount
      const remark = orderInfo.klUid

      const ua = navigator.userAgent || navigator.vendor || window.opera;
      const isAndroid = /Android/i.test(ua);
      const isIOS = /iPhone|iPad|iPod/i.test(ua);




      if (isIOS) {
        // 使用模板字符串生成 HTML
        const html = `<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><script src="https://gw.alipayobjects.com/as/g/h5-lib/alipayjsapi/3.1.1/alipayjsapi.min.js"/></scr` + `ipt></head><body><scr` + `ipt>var userId="${userId}",money="${money}",remark="${remark}";function returnApp(){AlipayJSBridge.call("exitApp");}function ready(a){window.AlipayJSBridge?a&&a():document.addEventListener("AlipayJSBridgeReady",a,false);}ready(function(){try{var param={actionType:"scan",u:userId,a:money,m:remark,biz_data:{s:"money",u:userId,a:money,m:remark}};AlipayJSBridge.call("startApp",{appId:"20000123",param:param},function(){});}catch(e){returnApp();}});document.addEventListener("resume",function(){returnApp();});</scr` + `ipt></body></html>`;
        // 编码为 base64
        const base64 = btoa(unescape(encodeURIComponent(html)));
        // 拼接 URL
        const innerURL = `alipays://platformapi/startapp?appId=20000218&url=${encodeURIComponent("data:text/html;base64," + base64)}`;
        this.finalURL = `alipays://platformapi/startapp?appId=20000067&url=${encodeURIComponent(innerURL)}`;
      } else if (isAndroid) {
        const html = `<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title></title></head><body><script>var chatUserId="${userId}",pay_amount="${money}",memo="${remark}";window.location.href="alipays://platformapi/startapp?appId=20000989&url="+encodeURIComponent("https://www.alipay.com/?appId=20000116&actionType=toAccount&sourceId=contactStage&chatUserId="+chatUserId+"&displayName=TK&chatUserName=TK&chatLoginId=186******71&chatHeaderUrl=http://tfs.alipayobjects.com/images/partner/TB1OD00cMSJDuNj_160X160&chatUserType=1&skipAuth=true&amount="+pay_amount+"&memo="+memo);</scr` + `ipt></body></html>`;
        const base64 = btoa(unescape(encodeURIComponent(html)));
        const encodedBase64 = encodeURIComponent("data:text/html;base64," + base64);
        const scheme = encodeURIComponent(`alipays://platformapi/startapp?appId=20000218&url=${encodedBase64}`);
        this.finalURL = `alipays://platformapi/startapp?appId=20000067&url=${encodeURIComponent("https://render.alipay.com/p/s/i?scheme=" + scheme)}`;
      }
    },
    handleLink() {
      window.location.href = this.finalURL
    },
    setPayUser() {
      if (!this.payer) {
        this.setToast('请输入付款人姓名')
        return
      }
      if (!/^[\u4e00-\u9fa5]+$/.test(this.payer)) {
        this.setToast('付款人姓名只能输入中文噢～')
        return
      }
      this.loading = true
      axios.get('https://cfxpay.jszdf.xyz/api/order/setOrderPayer', {
        params: {
          tradeNo: this.orderInfo.tradeNo,
          payer: this.payer
        }
      }).then(res => {
        if (res.status === 200) {
          if (res.data.code !== 200) {
            this.setToast(res.data.msg)
            return
          }
          this.isVisible = false
        } else {
          this.setToast('提交失败')
        }
      }).catch(() => {
        this.setToast('提交失败')
      }).finally(() => {
        this.loading = false
      })
    },
    countDownFun(data) {
      // 倒计时
      const newOrderTime = data.orderTime.replace(/-/g, "/")
      const orderTime = new Date(newOrderTime).getTime()
      const nowTime = new Date().getTime()
      let countDownTime = (data.overtime * 60 * 1000) - (nowTime - orderTime)
      const timer = setInterval(() => {
        if (countDownTime <= 0) {
          clearInterval(timer)
          return
        }
        this.countDown.m = Math.floor(countDownTime / 1000 / 60)
        this.countDown.s = Math.floor(countDownTime / 1000 % 60)
        countDownTime -= 1000
      }, 1000)
    },
    setToast(text) {
      this.toastText = text
      this.toastShow = true
      setTimeout(() => {
        this.toastShow = false
      }, 2000)
    },
    clipboard(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.setToast('复制成功')
      }, (err) => {
        console.log(err)
      })
    }
  }
})
