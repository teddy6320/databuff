<template>
  <div class="not-found-cont">
    <div class="not-found-wrapper" :style="{  backgroundImage: `url(${notFound})` }">
      <p class="not-found-info">{{ $t('modules.views.404.s_0acfadfc') }}</p>
      <el-button
        v-if="!isNested"
        @click="$router.replace('/')"
        type="primary"
        class="back-home-btn">{{ $t('modules.views.404.s_5a136705') }}</el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { Component } from 'vue-property-decorator';
import Cont from '@/views/layout/index.vue';
import NotFound from '@/assets/img/404/404-bg.png';
@Component
export default class NotFoundPage extends Cont {
  private isNested = window.parent !== window.self // 页面是否被嵌套

  private notFound = NotFound;
}
</script>

<style lang="scss" scoped>
.not-found-cont {
  width: 100vw;
  height: 100vh;
  min-width: 980px;
  min-height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;

  .not-found-wrapper {
    width: 600px;
    height: 400px;
    background-repeat: no-repeat;
    background-position: center;
    background-size: contain;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
  }

  .not-found-info {
    margin-left: 30px;
    margin-top: 150px;
    color: #fff;
  }

  .back-home-btn {
    margin-top: 20px;
    margin-left: 30px;
  }
}

:root[data-theme=light] {
  .not-found-cont .not-found-info {
    color: var(--color-text-secondary);
  }
}
</style>
