<template>
  <div class="db-menu-wrapper">
    <simplebar class="db-menu-simplebar">
      <el-menu
        :default-active="activeMenu"
        :default-openeds="activeMenuGroup"
        :unique-opened="true"
        :collapse="collapse"
        :collapse-transition="false"
        :class="['db-menu', { 'db-menu-light': isLightTheme }]">
        <template v-for="(menu, index) in menuTreesWithoutStatic">
          <el-menu-item
            v-if="!menu.children"
            :key="index"
            :index="`${menu.id}`"
            class="db-menu-item mtb-4">
            <el-tooltip
              :content="menuLabel(menu)"
              placement="right"
              :open-delay="300">
              <router-link
                :to="{ path: menu.path, query: getRouteTimeQuery(menu.time) }"
                class="db-menu-link">
                <span v-if="menu.icon" :class="menu.icon" class="db-menu-icon"></span>
                <div class="db-menu-name">{{ menuLabel(menu) }}</div>
              </router-link>
            </el-tooltip>
          </el-menu-item>

          <el-submenu
            v-else
            :key="index"
            :index="`${menu.id}`"
            popper-class="db-menu-popover"
            class="mtb-4">
            <template slot="title">
              <el-tooltip
                :content="menuLabel(menu)"
                placement="right"
                :open-delay="300"
                :disabled="collapse">
                <div class="db-menu-title-content">
                  <span v-if="menu.icon" :class="menu.icon" class="db-menu-icon"></span>
                  <div class="db-menu-name">{{ menuLabel(menu) }}</div>
                </div>
              </el-tooltip>
            </template>

            <el-menu-item
              v-for="(sub, i) in menu.children"
              :key="i"
              :index="`${sub.id}`"
              class="db-menu-item">
              <router-link
                :to="{ path: sub.path, query: getRouteTimeQuery(sub.time) }"
                class="db-menu-link"
                :class="{ 'has-sub-icon': !!sub.icon }">
                <el-tooltip
                  :content="menuLabel(sub)"
                  placement="right"
                  :open-delay="300">
                  <div class="db-menu-title-content">
                    <span v-if="sub.icon" :class="sub.icon" class="db-menu-icon db-menu-sub-icon"></span>
                    <div class="db-menu-name">{{ menuLabel(sub) }}</div>
                  </div>
                </el-tooltip>
              </router-link>
            </el-menu-item>
          </el-submenu>
        </template>
      </el-menu>
    </simplebar>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Getter, State, namespace } from 'vuex-class';
import { FullPropMenu } from '@/router/route.types';
import { TimeRangeMsOptions } from '@/router/time-new';
import Simplebar from 'simplebar-vue';
import { BreadcrumbItem } from '@/store/index';

const UserModel = namespace('User');
const PathSourceQuery = { __ps: 'm' } // 跳转路径来源，m表示左侧菜单

// 使用isMenu过滤掉非菜单
const filterMenu = (menus: FullPropMenu[]): FullPropMenu[] => {
  const list: FullPropMenu[] = [];
  menus.filter((item) => item.isMenu).forEach((item) => {
    const t = { ...item }
    if (t.level && t.level >= 2) {
      t.icon = ''
    }
    if (t.children && t.children.length) {
      t.children = filterMenu(t.children)
    }
    if (t.children && !t.children.length) {
      delete t.children
    }
    list.push({ ...t })
  });
  return list
}

// 根据path查找children
const findChildrenByPath = (menus: FullPropMenu[], path: string): FullPropMenu[] | null => {
  for (const item of menus) {
    if (item.path === path) {
      return item.children || null;
    }
    if (item.children) {
      const children = findChildrenByPath(item.children, path);
      if (children) {
        return children;
      }
    }
  }
  return null;
}

@Component({
  components: {
    Simplebar: Simplebar as any,
  }
})
export default class DbMenu extends Vue {
  @UserModel.State private menusTree!: FullPropMenu[];
  @UserModel.State private currMenu!: FullPropMenu | null;
  @State('theme') private theme!: 'dark' | 'light'; // 全局主题
  @State('locale') private locale!: string;
  @State('breadcrumbList') private globalBreadcrumbList!: BreadcrumbItem[];
  @Getter('globalTime') private globalTimeFunc!: any;

  @Prop({ default: false }) private collapse!: boolean;
  @Prop({ default: '' }) private menuTheme!: 'dark' | 'light'; // 传入的主题
  @Prop({ default: '' }) private parentPath!: string; // 父级菜单路径，用于过滤菜单的范围

  get isLightTheme () {
    return (this.menuTheme || this.theme) === 'light'
  }

  // 过滤菜单
  get menuTreesWithoutStatic () {
    let menus = this.menusTree
    if (this.parentPath) {
      menus = findChildrenByPath(menus, this.parentPath) || []
    }
    return filterMenu(menus)
  }

  // 最外层菜单的层级
  get topLevel () {
    if (!this.menuTreesWithoutStatic.length) {
      return 1
    }
    return this.menuTreesWithoutStatic[0].level || 1
  }

  private activeMenu = ''; // 当前高亮的菜单id
  private activeMenuGroup: string[] = []; // 当前展开的菜单ids

  private menuLabel(menu: FullPropMenu) {
    void this.locale
    const key = `menu.${menu.id}`
    if (this.$i18n.te(key)) {
      return i18n.t(key) as string
    }
    return menu.name
  }

  @Watch('currMenu', { immediate: true, deep: true })
  private onRoutePathChange(menu: FullPropMenu | null) {
    if (menu) {
      const menuIds = menu.menuIds || []
      const pid = menuIds[this.topLevel - 1]
      const mid = menuIds[this.topLevel] || menuIds[menuIds.length - 1]
      this.activeMenuGroup = typeof pid === 'number' ? [`${pid}`] : []
      this.activeMenu = typeof mid === 'number' ? `${mid}` : ''
    }
  }
  @Watch('globalBreadcrumbList', { immediate: true, deep: true })
  private onGlobalBreadcrumbListChange(list: BreadcrumbItem[]) {
    if (!list.length || this.topLevel !== 1) {
      return
    }
    const activeMenuGroup: any = this.menuTreesWithoutStatic.find(t => t.path === (list[0] || {}).path)
    const activeMenu = (activeMenuGroup?.children || []).find((t: any) => t.path === (list[1] || {}).path)
    if (activeMenuGroup) {
      this.activeMenuGroup = [`${activeMenuGroup.id}`]
      this.activeMenu = activeMenu ? `${activeMenu.id}` : `${activeMenuGroup.id}`
    }
  }

  private getRouteTimeQuery (type: any) {
    // 判断路由是否需要时间范围
    if (!type) {
      return { ...PathSourceQuery }
    }
    const globalTime = this.globalTimeFunc()
    if (globalTime.type === 'custom') {
      return { ...PathSourceQuery, fromTime: +globalTime.fromTime, toTime: +globalTime.toTime }
    } else {
      return { ...PathSourceQuery, durationRange: globalTime.duration }
    }
  }
}
</script>

<style lang="scss" scoped>
.db-menu-wrapper {
  flex: 1;
  width: 100%;
  height: 100%;
  overflow: hidden;

  .db-menu-simplebar {
    height: 100%;
    padding-right: 10px;
  }

  .mtb-4 {
    margin-top: 4px;
    margin-bottom: 4px;
  }

  :deep(.simplebar-track.simplebar-horizontal) {
    display: none;
  }
}

.db-menu {
  height: 100%;
  border-right: none;
  background-color: #2F313B;
  &:not(.el-menu--collapse) {
    width: 100%;
  }

  &.el-menu--collapse {
    width: 36px;
    .db-menu-name {
      width: 0;
    }
    .db-menu-link,
    :deep(.el-submenu__title) {
      padding-right: 0 !important;
    }
  }

  :deep(.el-menu) {
    background-color: #2F313B;
  }

  .db-menu-item {
    padding: 0 !important;
    min-width: auto;
    height: auto;
    line-height: 1;
    background-color: transparent !important;
  }

  :deep(.el-submenu__title + .el-menu > .el-menu-item:first-child) {
    margin-top: 4px;
  }

  .db-menu-link,
  :deep(.el-submenu__title) {
    display: flex;
    align-items: center;
    padding: 6px 20px 6px 34px !important;
    height: 32px;
    border-radius: 4px;
    transition: all 0.3s;
    line-height: 1;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    font-family: PingFang SC;
    font-size: 13px;
    color: #CCCED4;
    // &:focus,
    &:hover {
      background: rgba(255, 255, 255, 0.08);
    }
  }

  .db-menu-title-content {
    display: flex;
    align-items: center;
    width: 100%;
    min-width: 0;
  }

  .db-menu-name {
    width: 100%;
    min-width: 0;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    transition: width 0.3s;
  }

  .db-menu-icon {
    width: 16px !important;
    height: 16px !important;
    visibility: visible !important;
    font-size: 14px;
    text-align: center;
    line-height: 16px;
    transform: translate(0, -50%);
    position: absolute;
    top: 50%;
    left: 10px;
  }

  .db-menu-link.has-sub-icon {
    padding-left: 34px !important;
  }

  .db-menu-sub-icon {
    left: 10px;
  }

  .db-menu-item.is-active .db-menu-link {
    background-color: rgba(255, 255, 255, 0.15);
  }

  // :deep(.el-submenu.is-active>.el-submenu__title .el-submenu__icon-arrow),
  .db-menu-item.is-active .db-menu-link,
  :deep(.el-submenu.is-active>.el-submenu__title) {
    color: #FFFFFF;
  }
  :deep(.el-submenu.is-active>.el-submenu__title) {
    font-weight: 500;
  }

  :deep(.el-submenu__title .el-submenu__icon-arrow) {
    margin-top: -6px;
    right: 10px;
    color: #A3A7B2;
  }

  &.db-menu-light {
    background-color: #FFFFFF;

    :deep(.el-menu) {
      background-color: #FFFFFF;
    }

    .db-menu-link,
    :deep(.el-submenu__title) {
      color: #626467;
      // &:focus,
      &:hover {
        background-color: #F7F7F7;
      }
    }

    .db-menu-item.is-active .db-menu-link {
      background-color: #F7F7F7;
    }

    // :deep(.el-submenu.is-active>.el-submenu__title .el-submenu__icon-arrow),
    .db-menu-item.is-active .db-menu-link,
    :deep(.el-submenu.is-active>.el-submenu__title) {
      color: #121317;
    }
  }
}
</style>

<style lang="scss">
.db-menu-popover.el-menu--vertical {
  .el-menu.el-menu--popup {
    margin: 0 0 0 10px;
    min-width: 180px;
    max-width: 240px;
    background-color: #2F313B;
    box-shadow: 0 0 16px 0 var(--bg-color-base);
  }

  .db-menu-item {
    padding: 0 !important;
    height: auto;
    line-height: 1;
    background-color: transparent !important;
  }

  .db-menu-link {
    display: block;
    padding: 6px 16px !important;
    height: 32px;
    border-radius: 4px;
    transition: background-color 0.3s;
    line-height: 20px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    font-size: 13px;
    color: #CCCED4;
    // &:focus,
    &:hover {
      background: rgba(255, 255, 255, 0.08);
    }
  }

  .db-menu-item.is-active .db-menu-link {
    background-color: rgba(255, 255, 255, 0.15);
  }

  .db-menu-item.is-active .db-menu-link {
    color: #FFFFFF;
  }
}

:root[data-theme=light] {
  .db-menu-popover.el-menu--vertical {
    .el-menu.el-menu--popup {
      background-color: #FFFFFF;
      box-shadow: 2px 0 10px 0 var(--shadow-color02);
    }

    .db-menu-link {
      color: #626467;
      // &:focus,
      &:hover {
        background-color: #F7F7F7;
      }
    }

    .db-menu-item.is-active .db-menu-link {
      background-color: #F7F7F7;
    }

    .db-menu-item.is-active .db-menu-link {
      color: #121317;
    }
  }
}
</style>
