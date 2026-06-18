<template>
  <div class="layout-main-cont">
    <div :class='["layout-main-wrapper", scrollY ? "scroll-y-auto" : "", subClass]'>
      <slot></slot>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop } from 'vue-property-decorator';

@Component
export default class ContWrapper extends Vue {
  @Prop({ default: true }) private scrollY!: boolean;
  @Prop() private subClass!: string;
  
}
</script>
<style lang='scss' scoped>
.layout-main-cont {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;

  .layout-main-wrapper {
    height: 100%;
    padding: 16px;
    position: relative;

    &.scroll-y-auto {
      overflow-y: auto;
    }
  }
}
</style>