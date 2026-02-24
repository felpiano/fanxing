<template>
  <div class="page-login">
    <canvas class="cavs" id="login-canvas" width="1575" height="1337"></canvas>
    <div class="loginmain">
      <div class="login-title">
          <span>繁星三方支付系统登录</span>
      </div>

     <div class="login-box">
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            auto-complete="off"
            @change="getCode"
            placeholder="请输入用户名">
            <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            placeholder="请输入密码"
            @keyup.enter.native="handleLogin">
            <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <el-input
            v-model="loginForm.code"
            auto-complete="off"
            placeholder="请输入谷歌验证码"
            maxlength="6"
            @keyup.enter.native="handleLogin">
            <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
          </el-input>
        </el-form-item>
        <el-form-item prop="validCode">
          <div class="flex-between">
            <el-input
              v-model="loginForm.validCode"
              auto-complete="off"
              placeholder="请输入验证码"
              maxlength="6"
              @keyup.enter.native="handleLogin">
              <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
            </el-input>
            <el-button v-if="loginForm.username && loginForm.username === 'qiezi'" type="success" :loading="isAgain" class="min-button" @click="getCode">重新发送</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button class="login-btn"
            :loading="loading"
            size="medium"
            type="primary"
            style="width:100%;"
            @click.native.prevent="handleLogin">
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
        </el-form-item>
      </el-form>
      </div>
    </div>
    <!--  底部  -->
    <div class="login-footer">
      <span>Copyright © 2018-2024 All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getValidCode } from "@/api/login";

export default {
  name: "Login",
  data() {
    return {
      codeUrl: "",
      isAgain: false,
      loginForm: {
        username: "", // admin
        password: "", // admin123
        code: "",
        uuid: "",
        validCode: "",
        address: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        // code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      redirect: undefined,
      captchaEnabled: false
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.loginForm.address = route.query ? route.query.xhssf : ''
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  mounted() {
    window.addEventListener("keyup", (e) => {
      if (e.keyCode === 13) {
        this.handleLogin();
      }
    });
  },
  methods: {
    getCode() {
      if (this.loginForm.username === 'qiezi') {
        this.isAgain = true
      }
      getValidCode({userName: this.loginForm.username}).then(res => {
        this.loginForm.uuid = res.uuid;
        if (this.isAgain) {
          this.$message.success('验证码已发送')
        }
        this.isAgain = false
      });
    },
    handleLogin() {
      this.$refs.loginFormRef.validate(valid => {
        if (valid) {
          this.loading = true;
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).finally(() => {
            this.loading = false;
          }).catch(() => {

          });
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.cavs{
  z-index:1;
  position: fixed;
  width: 96%;
  height: 96%;
  left: 3%;
  top: 3%;
}

.page-login{
  width: 100%;
  height: 100%;
  background: url("~@/assets/images/bg1.jpg") no-repeat;
  background-size: 100% 100%;
  position: relative;
}

.loginmain{
  background: rgba(0, 0, 0, 0.5);
  width: 540px;
  height: 450px;
  position: absolute;
  top: -20%;
  left: 0;
  right: 0;
  bottom: 0;
  margin: auto;
  padding: 30px 40px;
  box-shadow: -15px 15px 15px rgba(6, 17, 47, 0.7);
  z-index: 99;
}
.login-title{
  color: #f7f7fe;
  height: 60px;
  font-size: 28px;
  text-align: center;
  margin-bottom: 20px;
}
.login-box{
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .login-form {
    width: 100%;
    max-width: 320px;
    margin: 0 auto;
    ::v-deep .el-input__inner {
      border-radius: 8px;
      background-color: rgba(255, 255, 255, 0.01);
      color: #f3f3f3;
      border: none;
      &::placeholder {
        color: #828282;
      }
    }
  }
}
.login-btn{
  width: 80%;
  margin: 0 auto;
  position: relative;
  background: transparent;
  border: 2px solid #3b86d6;
  color: #3b86d6;
  text-transform: uppercase;
  transition-property: background,color;
  -webkit-transition-duration: .2s;
  transition-duration: .2s;
  &:hover{
    color: white !important;
    background: #3b86d6;
    cursor: pointer;
    -webkit-transition-property: background,color;
    transition-property: background,color;
    -webkit-transition-duration: .2s;
    transition-duration: .2s;
  }
}

.login-footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: #fff
}
</style>
