<template>
  <div class="breadcrumb-cont flex-h">
    <span
      v-if="showBackBtn"
      @click="historyBackHandle"
      class="back-btn db-icon-back font-14 mr-6 default-text tc cp"></span>

    <template v-for="item,idx in breadcrumbList">
      <span
        v-if="idx > 0"
        :key="`separator_${idx}`"
        class="breadcrumb-separator">/</span>
      <router-link
        v-if="item.path"
        :key="idx"
        :to="item"
        :class="{
          'breadcrumb-item-last': idx === breadcrumbList.length - 1,
          'breadcrumb-item-first': idx === 0,
        }"
        class="breadcrumb-item breadcrumb-item-link">{{ item.titleKey ? $t(item.titleKey) : item.title }}</router-link>
      <span
        v-else
        :key="idx"
        :class="{
          'breadcrumb-item-last': idx === breadcrumbList.length - 1,
          'breadcrumb-item-first': idx === 0,
        }"
        class="breadcrumb-item">{{ item.titleKey ? $t(item.titleKey) : item.title }}</span>
    </template>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { State, Action, Getter, namespace } from 'vuex-class';
import { FullPropMenu } from '@/router/route.types';
import type { BreadcrumbItem, BreadcrumbMap } from '@/store/index';
import BreadcrumbData from '@/router/breadcrumb-data';
import { menuDisplayName } from '@/i18n';

const UserModel = namespace('User');

@Component
export default class BreadcrumbList extends Vue {
  @State('breadcrumbMap') private globalBreadcrumbMap!: BreadcrumbMap;
  @State('breadcrumbList') private globalBreadcrumbList!: BreadcrumbItem[];
  @State('locale') private locale!: string;
  @UserModel.State private menus!: FullPropMenu[];
  @UserModel.State private currMenu!: FullPropMenu | null;
  @Getter('globalTime') private globalTimeFunc!: any;

  get showBackBtn () {
    // 从菜单进入
    const isMenuSource = this.$route.query.__ps === 'm'
    // 新窗口打开的页面
    const isNewWindow = this.$route.query.__nw === 't'
    // 隐藏页面
    const isHide = this.currMenu?.module === 'hide'
    return !isMenuSource && !isNewWindow && !isHide;
  }

  get menuPathMap () {
    const data: any = {};
    this.menus.forEach(item => {
      data[item.path] = {
        id: item.id,
        name: item.name,
        path: item.path,
        leaf: item.leaf,
        time: item.time,
        isMenu: item.isMenu,
      };
    });
    return data;
  }

  get breadcrumbList () {
    const { fromTime, toTime, duration: durationRange, type } = this.globalTimeFunc()
    const currPath = this.$route.path
    // globalBreadcrumbList
    let list = this.globalBreadcrumbList.filter(item => this.menuPathMap[item.path]).map(item => ({
      ...this.menuPathMap[item.path],
      ...item,
    }))
    if (!list.length) {
      // BreadcrumbData
      list = [...(BreadcrumbData[currPath] || []), currPath].filter(path => this.menuPathMap[path]).map(path => ({
        ...this.menuPathMap[path],
        ...this.globalBreadcrumbMap[path],
      }))
    }
    // 只显示可下钻或者当前页的面包屑
    list = list.filter(item => item.leaf || item.path === currPath)
    return list.map((item, index) => {
      void this.locale
      const _item: any = { title: menuDisplayName(item.id, item.name), }
      if (item.leaf && index !== list.length - 1) {
        _item.path = item.path
        const query: any = { ...item.query }
        if (item.isMenu) {
          query.__ps = 'm'
        }
        if (item.time) {
          const customTime = type === 'custom' && item.time !== 'latest'
          _item.query = {
            ...(!customTime ? { durationRange } : {
              fromTime: +fromTime,
              toTime: +toTime,
            }),
            ...query,
          }
        } else {
          _item.query = query
        }
      }
      return _item
    });
  }

  private historyBackHandle () {
    // 返回逻辑和浏览器保持一致
    this.$router.go(-1);
  }
}
</script>

<style lang="scss" scoped>
.breadcrumb-cont {
  padding-left: 6px;
  font-size: 13px;
  line-height: 28px;
  white-space: nowrap;
  overflow: hidden;
  color: var(--color-text-primary);
  .breadcrumb-item {
    flex: none;
    max-width: 400px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }
  .breadcrumb-item-first {
    font-size: 16px;
    font-weight: 500;
  }
  .breadcrumb-item-last {
    max-width: none;
    flex: 1;
  }
  .breadcrumb-item-link {
    color: var(--color-text-link);
    cursor: pointer;
  }
  .breadcrumb-separator {
    margin: 0 8px;
    color: var(--border-color-base)
  }
}

.back-btn {
  display: block;
  margin-left: -6px;
  width: 28px;
  height: 32px;
  line-height: 32px;
  transition: all .3s ease;
  &:hover {
    color: var(--color-primary);
  }
}
</style>
