<template>
  <div class="manage-wrap" :class="{ 'no-inner-menu': !showInnerMenu }">
    <div v-if="showInnerMenu" class="manage-left">
      <db-menu parentPath="/config/manage" />
    </div>

    <div class="manage-right">
      <router-view />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import { namespace } from 'vuex-class';
import DbMenu from '@/components/db-menu/index.vue';
import { FullPropMenu } from '@/router/route.types';

const UserModel = namespace('User');

@Component({
  components: {
    DbMenu,
  },
})
export default class Manage extends Vue {
  @UserModel.State private menusTree!: FullPropMenu[];

  // 完整版「配置管理」挂在「配置」下，需要页内二级菜单；开源版子菜单已在侧栏展示
  get showInnerMenu() {
    return this.menusTree.some(menu => menu.path === '/config');
  }
}
</script>

<style lang="scss" scoped>
.manage-wrap {
  flex: 1;
  padding: 16px;
  position: relative;
  display: flex;
  overflow: auto;

  .manage-left {
    flex: none;
    min-height: 400px;
    width: 140px;
    padding: 11px 0 11px 12px;
    background-color: var(--bg-color);
    border-radius: 4px 0 0 4px;
    border-right: 1px solid var(--border-color-lighter);
    overflow: hidden;

    :deep(.mtb-4) {
      margin-top: 2px;
      margin-bottom: 2px;
    }
    :deep(.db-menu-link),
    :deep(.el-submenu__title) {
      height: 30px;
      padding-left: 10px !important;
    }
    :deep(.db-menu-link) {
      padding-right: 10px !important;
    }
    :deep(.el-submenu__title + .el-menu > .el-menu-item:first-child) {
      margin-top: 0;
    }
    :deep(.db-menu > .el-menu-item .db-menu-link),
    :deep(.el-submenu__title) {
      color: var(--color-text-primary);
    }
    :deep(.db-menu-item.is-active .db-menu-link) {
      color: var(--color-primary);
    }
    :deep(.el-submenu.is-active>.el-submenu__title) {
      font-weight: normal;
    }
    :deep(.el-submenu__title .el-submenu__icon-arrow) {
      right: 8px;
    }
  }

  .manage-right {
    flex: none;
    width: calc(100% - 140px);
    min-height: 400px;
    padding: 20px;
    background-color: var(--bg-color);
    border-radius: 0 4px 4px 0;
  }

  &.no-inner-menu .manage-right {
    width: 100%;
    border-radius: 4px;
  }
}
</style>
