<template>
  <div class="navbar">
    <div class="flex-center">
      <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
      <template v-if="device!=='mobile'"> 
        <breadcrumb id="breadcrumb-container" class="breadcrumb-container" v-if="!topNav"/>
        <top-nav id="topmenu-container" class="topmenu-container" v-if="topNav"/>
      </template> 
    </div>
    <div class="flex-center">
      <!--码商-->
      <Merchant v-if="identity === 5 && device !== 'mobile'"></Merchant>
      <!--代理-->
      <Agent v-if="identity === 3 && device !== 'mobile'"></Agent>
      <!--管理员-->
      <AdminPage v-if="identity === 1 && device !== 'mobile'"></AdminPage>

      <div class="flex-center right-menu">
        <template v-if="device!=='mobile'">
          <search id="header-search" class="right-menu-item" />

          <screenfull id="screenfull" class="right-menu-item hover-effect" />

          <!--<el-tooltip content="布局大小" effect="dark" placement="bottom">
            <size-select id="size-select" class="right-menu-item hover-effect" />
          </el-tooltip>-->

        </template>

        <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
          <div class="flex-center avatar-wrapper">
            <img v-if="device !== 'mobile'" :src="avatar" class="user-avatar">
            <p style="padding-left: 5px; color: #333">{{ $store.getters.name }}</p>
            <i class="el-icon-caret-bottom" />
          </div>
          <el-dropdown-menu slot="dropdown">
            <router-link to="/user/profile">
              <el-dropdown-item>个人中心</el-dropdown-item>
            </router-link>
            <!--<el-dropdown-item @click.native="setting = true">
              <span>布局设置</span>
            </el-dropdown-item>-->
            <el-dropdown-item divided @click.native="logout">
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import Search from '@/components/HeaderSearch'
import Merchant from './operation/merchant.vue'
import Agent from './operation/agent.vue'
import AdminPage from './operation/admin.vue'

export default {
  components: {
    Breadcrumb,
    TopNav,
    Hamburger,
    Screenfull,
    SizeSelect,
    Search,
    Merchant,
    Agent,
    AdminPage
  },
  data() {
    return {
      identity: 0
    }
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'avatar',
      'device'
    ]),
    setting: {
      get() {
        return this.$store.state.settings.showSettings
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'showSettings',
          value: val
        })
      }
    },
    topNav: {
      get() {
        return this.$store.state.settings.topNav
      }
    }
  },
  created() {
    const showGoogle = this.$store.getters.showGoogle
    if(showGoogle) {
      this.identity = this.$store.getters.identity
    }
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    async logout() {
      this.$confirm('确定退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          localStorage.clear()
          this.$store.dispatch('tagsView/delAllViews')
          this.$router.replace({ path: `/login?xhssf=${this.$store.getters.uuid}` })
        })
      }).catch(() => {});
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .right-menu {
    float: right;
    height: 100%;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 35px;
          height: 35px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 12px;
          font-size: 16px;
        }
      }
    }
  }
}
</style>
