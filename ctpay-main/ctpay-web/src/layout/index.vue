<template>
  <div :class="classObj" class="app-wrapper" :style="{'--current-color': theme}" v-loading="loading">
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>
    <sidebar v-if="!sidebar.hide" class="sidebar-container"/>
    <div :class="{hasTagsView:needTagsView,sidebarHide:sidebar.hide}" class="main-container">
      <div :class="{'fixed-header':fixedHeader}">
        <navbar/>
        <tags-view v-if="needTagsView"/>
      </div>
      <app-main/>
      <right-panel>
        <settings/>
      </right-panel>
    </div>

    <show-google ref="showGoogle" @success="handleSuccess"/>

    <!--安全码-->
    <update-safe ref="updateSafe" @success="handleUpdateSafe"></update-safe>

    <!--音乐-->
    <div class="audio-display">
      <audio ref="audio" id="audio" src="@/assets/mp3/newOrder.mp3" controls ></audio>
      <el-button size="mini" @click="playAudio"></el-button>
    </div>

    <!--谷歌验证码app下载-->
    <div class="google-play" @click="dialogVisible = true">
      <img class="google-play-img" src="@/assets/images/1.png">
      <p>谷歌验证器下载</p>
    </div>

    <el-dialog
      title="谷歌验证器下载地址"
      :visible.sync="dialogVisible"
      width="30%"
      center>
      <div class="down-url mb-10">
        <p>Android下载地址：</p>
        <a href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2" target="_blank">
          https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2
        </a>
      </div>
      <div class="down-url">
        <p>IOS下载地址：</p>
        <a href="https://apps.apple.com/sg/app/google-authenticator/id388497605?l=zh" target="_blank">
          https://apps.apple.com/sg/app/google-authenticator/id388497605?l=zh
        </a>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import RightPanel from '@/components/RightPanel'
import {AppMain, Navbar, Settings, Sidebar, TagsView} from './components'
import ResizeMixin from './mixin/ResizeHandler'
import {mapState} from 'vuex'
import variables from '@/assets/styles/variables.scss'
import { initWebSocket } from '@/utils/websocket'
import ShowGoogle from '@/views/components/showGoogle.vue'
import updateSafe from '@/views/components/updateSafe.vue';


export default {
  name: 'Layout',
  components: {
    AppMain,
    Navbar,
    RightPanel,
    Settings,
    Sidebar,
    TagsView,
    ShowGoogle,
    updateSafe
  },
  data() {
    return {
      loading: true,
      dialogVisible: false
    }
  },
  mixins: [ResizeMixin],
  computed: {
    ...mapState({
      theme: state => state.settings.theme,
      sideTheme: state => state.settings.sideTheme,
      sidebar: state => state.app.sidebar,
      device: state => state.app.device,
      needTagsView: state => state.settings.tagsView,
      fixedHeader: state => state.settings.fixedHeader
    }),
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    },
    variables() {
      return variables;
    }
  },
  watch: {
    '$store.getters.isPlay':{
      handler(val){
        if(val){
          this.playAudio()
        }
      }
    }
  },
  mounted() {
    const showGoogle = this.$store.getters.showGoogle;
    if (!showGoogle) {
      this.$refs.showGoogle.init();
      if (this.$store.getters.identity === 5) {
        localStorage.setItem('isShowSafe', 'show');
      }
    } else {
      this.loading = false;
      // 码商端连接websocket
      if (this.$store.getters.identity === 5) {
        const isShowSafe = localStorage.getItem('isShowSafe');
        if (isShowSafe === 'show') {
          this.$refs.updateSafe.openDialog(false);
        }
        initWebSocket()
      }
    }
  },
  methods: {
    playAudio() {
      const audio = this.$refs.audio;
      audio.play().then(() => {
        console.log("Playback started successfully");
        setTimeout(() => {
          audio.pause();
          this.$store.commit('SET_ISPLAY', false);
        }, 5600);
      }).catch(error => {
        console.error("Playback failed:", error);
      });
    },
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    },
    handleSuccess() {
      this.loading = false;
      // 设置安全码
      if (this.$store.getters.identity === 5) {
        this.$refs.updateSafe.openDialog(false);
      } else {
        window.location.reload();
      }
    },
    handleUpdateSafe() {
      this.loading = false;
      // 刷新页面
      window.location.reload();
      localStorage.setItem('isShowSafe', 'hide');
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "~@/assets/styles/mixin.scss";
  @import "~@/assets/styles/variables.scss";

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    width: 100%;

    &.mobile.openSidebar {
      position: fixed;
      top: 0;
    }
  }

  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 999;
  }

  .fixed-header {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9;
    width: calc(100% - #{$base-sidebar-width});
    transition: width 0.28s;
  }

  .hideSidebar .fixed-header {
    width: calc(100% - 54px);
  }

  .sidebarHide .fixed-header {
    width: 100%;
  }

  .mobile .fixed-header {
    width: 100%;
  }


  .audio-display {
    position: fixed;
    bottom: -50px
  }

  .google-play {
    position: fixed;
    bottom: 20px;
    right: -110px;
    z-index: 1111;
    background: #fff;
    border-radius: 25px 0 0 25px;
    display: flex;
    align-items: center;
    padding: 0 10px 0 0;
    cursor: pointer;
    box-shadow: 1px 3px 5px 0 rgba(0, 0, 0, 0.3);
    &-img {
      width: 38px;
      height: 38px;
      border-radius: 25px 0 0 25px;
    }
    p {
      font-size: 14px;
    }
    &:hover {
      animation: slideInRight 0.5s forwards;
    }
  }
  // google-play滑出动画
  @-webkit-keyframes slideInRight {
    0% {
      right: -110px;
    }
    100% {
      right: 0;
    }
  }



  .down-url {
    a {
      color: #409EFF;
      text-decoration: line;
    }
    p {
      color: #333;
      font-weight: 700;
    }
  }
</style>
