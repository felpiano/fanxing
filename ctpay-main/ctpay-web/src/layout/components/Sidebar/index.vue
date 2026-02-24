<template>
    <div :class="{'has-logo':showLogo}" :style="{ backgroundColor: settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground }">
        <logo v-if="showLogo" :collapse="isCollapse" />
        <el-scrollbar :class="settings.sideTheme" wrap-class="scrollbar-wrapper">
            <el-menu
                :default-active="activeMenu"
                :collapse="isCollapse"
                :background-color="settings.sideTheme === 'theme-dark' ? variables.menuBackground : variables.menuLightBackground"
                :text-color="settings.sideTheme === 'theme-dark' ? variables.menuColor : variables.menuLightColor"
                :unique-opened="true"
                :active-text-color="'#fff'"
                :collapse-transition="false"
                mode="vertical"
            >
                <sidebar-item
                    v-for="(route, index) in sidebarRouters"
                    :key="route.path  + index"
                    :item="route"
                    :base-path="route.path"
                />
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script>
import { mapGetters, mapState } from "vuex";
import Logo from "./Logo";
import SidebarItem from "./SidebarItem";
import variables from "@/assets/styles/variables.scss";

export default {
    components: { SidebarItem, Logo },
    computed: {
        ...mapState(["settings"]),
        ...mapGetters(["sidebarRouters", "sidebar"]),
        activeMenu() {
            const route = this.$route;
            const { meta, path } = route;
            // if set path, the sidebar will highlight the path you set
            if (meta.activeMenu) {
                return meta.activeMenu;
            }
            return path;
        },
        showLogo() {
            return this.$store.state.settings.sidebarLogo;
        },
        variables() {
            return variables;
        },
        isCollapse() {
            return !this.sidebar.opened;
        }
    }
};
</script>

<style lang="scss">
// .router-link-exact-active.router-link-active {
//     position: relative;
//     padding: 2px 0;
//     &:before {
//         content: '';
//         display: block;
//         position: absolute;
//         z-index: 9;
//         top: 0;
//         left: 0;
//         width: 6px;
//         bottom: 0;
//         border-radius: 8px 30px 30px 0;
//         background-color: #fff;
//         box-shadow: 0 2px 4px 0 #4164d75e;
//     }
// }
#app .sidebar-container .theme-dark .el-submenu .el-menu-item.is-active {
    // background:linear-gradient(270deg, rgba(8, 136, 255, 0.4)  0%, #0888ff 100%) !important;
    // border-radius: 2px 0 0 0;
    position: relative;
    background-color: rgba(8, 136, 255, 0.2) !important;
    &:before {
        content: '';
        display: block;
        position: absolute;
        z-index: 9;
        top: 0;
        left: 0;
        bottom: 0;
        width: 6px;
        height: 100%;
        border-radius: 0 40px 40px 0;
        background: #0b81ff;
        border-right: 1px solid #5997d9;
    }

}
</style>
