new Vue({
  el: '#app',
  data: {
    isVisible: false,
    pageLoading: true,
    orderId: null,
    orderInfo: {
      channelName: '',
      channelCode: '',
      amount: 0,
      traceNo: '',
      jumpUrl: 0,
      warnNotes: null,
      shopPayTimeout: 0
    },
    countDown: {
      m: 0,
      s: 0
    },
    timer: null,
    form: {
      cardInfo: '',
      cardNo: '',
      cardPassword: ''
    },
    isExpire: false,
    loading: false,
    isShow: true,
    isNoPage: false,
    isMessage: false,
    toastText: '',
    toastShow: false,
    isTwo: false,
    isTwoModal: false
  },
  mounted() {
    const query = this.getUrlParam(window.location.href)
    if (JSON.stringify(query) === '{}') {
      this.isNoPage = true
      this.pageLoading = false
    } else {
      this.getOrderInfo(query)
    }
  },
  destroyed() {
    clearInterval(this.timer)
    this.timer = null
  },
  methods: {
    getUrlParam(href) {
      let query = {}
      let search = href.substring(href.lastIndexOf('?') + 1)
      let arr = search.split('&')
      for (var i = 0; i < arr.length; i++) {
        let key = arr[i].split('=')[0]
        let value = arr[i].split('=')[1]
        query[key] = value
      }
      return query
    },
    getOrderInfo(query) {
      if (query && query.orderId) {
        axios.get(API_URL.getOrderById, {
          params: {
            id: query.orderId
          }
        }).then(res => {
          if (res.status === 200) {
            if (res.data.code !== 200) {
              this.setToast(res.data.msg)
              return
            }
            const data = res.data.data
            if (data) {
              this.orderId = query.orderId
              this.orderInfo = data
              this.isShow = data.orderStatus === '0'
              if (data.orderStatus !== '0') {
                this.isMessage = true
              } else {
                this.isVisible = true
              }
              this.countDownFun()
            } else {
              this.setToast('获取订单信息失败')
            }
          } else {
            this.setToast('获取订单信息失败')
          }
        }).finally(() => {
          this.pageLoading = false
        })
      }
    },
    countDownFun() {
      // 倒计时
      // 当前时间 - orderTime
      const orderTime = new Date((this.orderInfo.orderTime).replace(/-/g, "/")).getTime()
      const nowTime = new Date().getTime()
      let countDownTime = (Number(this.orderInfo.shopPayTimeout)) * 60 * 1000 - (nowTime - orderTime)
      this.timer = setInterval(() => {
        if (countDownTime <= 0) {
          clearInterval(this.timer)
          this.timer = null
          this.isExpire = true
          this.isShow = false
          return
        }
        this.countDown.m = Math.floor(countDownTime / 1000 / 60)
        this.countDown.s = Math.floor(countDownTime / 1000 % 60)
        countDownTime -= 1000
      }, 1000)
    },
    submitCard() {
      if (this.isExpire) {
        this.setToast('订单已过期，请重新下单')
        return
      }
      if (!this.orderId) {
        this.setToast('订单不存在')
        return
      }
      if (!this.form.cardNo || this.form.cardNo === '') {
        this.setToast('请填写卡号')
        return
      }
      if (!this.form.cardPassword || this.form.cardPassword === '') {
        this.setToast('请填写卡密')
        return
      }
      this.form.cardNo = this.form.cardNo.replace(/\s+/g, '')
      this.form.cardPassword = this.form.cardPassword.replace(/\s+/g, '')
      // 创建实例
      var encryptor = new JSEncrypt()
      encryptor.setPublicKey(API.pkey)
      const dataObj = JSON.stringify({orderId: this.orderId, cardNo: this.form.cardNo,cardPassword: this.form.cardPassword})
      const pass = encryptor.encrypt(dataObj)
      this.loading = true
      axios.post(API_URL.submitShopSubCard, {
        pass: pass,
        orderId: this.orderId,
        cardNo: this.form.cardNo,
        cardPassword: this.form.cardPassword
      }).then(res => {
        if (res.status === 200) {
          if (res.data.code === 200) {
            this.isMessage = true
            this.setToast('提交成功')
            this.loading = false
            this.isShow = false
          } else {
            this.setToast(res.data.msg)
            this.loading = false
          }
        } else {
          this.setToast('提交失败请重试')
          this.loading = false
        }
      }).catch(() => {
        this.loading = false
      })
    },
    changeTextarea(val) {
      const value = val.target.value
      this.form.cardNo = ''
      this.form.cardPassword = ''
      // 如果包含4个以上的中文字符，不做处理
      if (value.match(/[\u4e00-\u9fa5]/g) && value.match(/[\u4e00-\u9fa5]/g).length > 4) {
        this.setToast('无法识别，请手动输入')
        return
      }
      // 如果存在逗号 根据,分割自动识别卡号和卡密
      if (value.includes(',')) {
        const cardInfo = value.split(',')
        if (cardInfo.length === 2) {
          this.form.cardNo = cardInfo[0]
          this.form.cardPassword = cardInfo[1]
        }
      } else if (value.includes(' ')) {
        // 如果存在空格 根据空格分割自动识别卡号和卡密
        const cardInfo = value.split(' ')
        if (cardInfo.length === 2) {
          this.form.cardNo = cardInfo[0]
          this.form.cardPassword = cardInfo[1]
        }
      } else if(value.includes('\n') && (value.includes('卡号') || value.includes('卡密') || value.includes('密码'))) {
        // 如果存在卡号或卡密 根据卡号或卡密分割自动识别卡号和卡密
        const cardInfo = value.split('\n')
        const cardInfo1 = cardInfo[0].split('卡号')
        if (cardInfo1.length === 2) {
          //如果cardInfo1[1]存在：或者:分隔符，去掉分隔符
          if (cardInfo1[1].includes('：')){
            this.form.cardNo = cardInfo1[1].split('：')[1]
          } else if(cardInfo1[1].includes(':')){
            this.form.cardNo = cardInfo1[1].split(':')[1]
          } else{
            this.form.cardNo = cardInfo1[1]
          }
        }
        const cardInfo2 = cardInfo[1].split('卡密')
        if (cardInfo2.length === 2) {
          if (cardInfo2[1].includes('：')){
            this.form.cardPassword = cardInfo2[1].split('：')[1]
          } else if(cardInfo1[1].includes(':')){
            this.form.cardPassword = cardInfo2[1].split(':')[1]
          } else{
            this.form.cardPassword = cardInfo2[1]
          }
        } else {
          const cardInfo3 = cardInfo[1].split('密码')
          if (cardInfo3.length === 2) {
            if (cardInfo3[1].includes('：')){
              this.form.cardPassword = cardInfo3[1].split('：')[1]
            } else if(cardInfo1[1].includes(':')){
              this.form.cardPassword = cardInfo3[1].split(':')[1]
            } else{
              this.form.cardPassword = cardInfo3[1]
            }
          }
        }
      } else {
        if (!(/[\u4e00-\u9fa5]/.test(value))) {
          this.form.cardNo = value
        }
      }
      if (this.form.cardNo) {
        this.form.cardNo = this.form.cardNo.replace(/[-/\\:： ]/g, '')
        this.form.cardNo = this.form.cardNo.replace(/[\u4e00-\u9fa5]/g, '')
      }
      if (this.form.cardPassword) {
        this.form.cardPassword = this.form.cardPassword.replace(/[-/\\：: ]/g, '')
        this.form.cardPassword = this.form.cardPassword.replace(/[\u4e00-\u9fa5]/g, '')
      }
    },
    // 跳转淘宝
    handleTaobao() {
      if (this.orderInfo.channelName.indexOf('金富卡') > -1 || this.orderInfo.channelName.indexOf('玄武卡') > -1) {
        let qstr = "";
        for (let d in this.orderInfo) {
          qstr += encodeURIComponent(d) + '=' + encodeURIComponent(this.orderInfo[d] || '') + '&';
        }
        qstr += 'url=' + encodeURIComponent(window.location.href);
        window.location.href = 'taobao.html?' + qstr;
        return;
      }
      if (this.orderInfo.channelName.indexOf('福惠卡') > -1 || this.orderInfo.channelName.indexOf('鸿福卡') > -1 || this.orderInfo.channelName.indexOf('鑫旺卡') > -1 || this.orderInfo.channelName.indexOf('益享卡') > -1) {
        this.isTwoModal = true
      } else {
        location.href = "tbopen://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fmain.m.taobao.com%2Fsearch%2Findex.html%3Fspm%3Da215s.7406091.topbar.1.560c6770snz1OF%26pageType%3D3%26q%3D"+ encodeURIComponent(encodeURIComponent(this.orderInfo.channelName)) + this.orderInfo.amount;
      }
    },
    handleTaobaoTwo() {
      location.href = "tbopen://m.taobao.com/tbopen/index.html?h5Url=https%3A%2F%2Fmain.m.taobao.com%2Fsearch%2Findex.html%3Fspm%3Da215s.7406091.topbar.1.560c6770snz1OF%26pageType%3D3%26q%3D"+ encodeURIComponent(encodeURIComponent(this.orderInfo.channelName)) + this.orderInfo.amount;
    },
    // 跳转京东
    openJDApp() {
      if (this.orderInfo.jdShop) {
        const shopIds = this.orderInfo.jdShop.split(';');
        if (shopIds.length) {
          const args = {
            category: "jump",
            des: "jshopMain",
            shopId: shopIds[Math.floor(Math.random() * shopIds.length)],
            sourceType: "M_sourceFrom",
            sourceValue: "dp"
          };
          location.href = "openApp.jdMobile://virtual?params=" + encodeURIComponent(JSON.stringify(args))
          return
        }
      }
      // 沃尔玛再次弹框
      if(this.orderInfo.channelCode === "8054" || this.orderInfo.channelCode === "8068") {
        this.isVisible = true
        this.isTwo = true
        return
      }
      // 京东 App 的 URI，带上你希望跳转的 URL
      location.href = 'openapp.jdmobile://virtual?params=' + encodeURIComponent(
        JSON.stringify({
          category: "jump",
          des: "m",
          sourceValue: "babel-act",
          sourceType: "babel",
          url: `https://so.m.jd.com/ware/search.action?keyword=${this.orderInfo.channelName}${this.orderInfo.amount}&searchFrom=search&sf=11&as=1`,
          M_sourceFrom: "h5auto",
          msf_type: "auto"
        })
      )
    },
    closeDialog() {
      this.isVisible = false
      if(this.isTwo) {
        // 京东 App 的 URI，带上你希望跳转的 URL
        let href = 'openapp.jdmobile://virtual?params=' + encodeURIComponent(
          JSON.stringify({
            category: "jump",
            des: "m",
            sourceValue: "babel-act",
            sourceType: "babel",
            url: `https://so.m.jd.com/ware/search.action?keyword=${this.orderInfo.channelName}${this.orderInfo.amount}&searchFrom=search&sf=11&as=1`,
            M_sourceFrom: "h5auto",
            msf_type: "auto"
          })
        );
        this.isTwo = false
        // 跳转到京东 App
        location.href = href;
      }
    },
    setToast(text) {
      this.toastText = text
      this.toastShow = true
      setTimeout(() => {
        this.toastShow = false
      }, 2000)
    }
  }
})
