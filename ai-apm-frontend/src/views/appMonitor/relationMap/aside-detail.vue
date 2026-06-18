<template>
  <div class="node-detail-cont">
    <el-tooltip v-show='!!current' effect="light" :content="showDetail ? $t('modules.components.text-expand.s_def9e98b') : $t('modules.components.text-expand.s_e2edde5a')" placement="bottom">
      <div
        @click="toggleExpandHandle"
        :class='["toggle-btn", showDetail ? "is-expanded" : ""]'>
        <span :class="showDetail ? 'db-icon-unfold' : 'db-icon-fold'" class="toggle-icon" />
      </div>
    </el-tooltip>

    <apm-detail v-if='showApm' :current="current" ref="asideDetail" />
    <npm-detail v-if='showNpm' :current="current" ref="asideDetail" />
  </div>
</template>
<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator'
import ApmDetail from './aside-detail-apm.vue'
import NpmDetail from './aside-detail-npm.vue'

@Component({
  components: {
    ApmDetail, NpmDetail
  },
})
export default class AsideDetail extends Vue {
  @Prop() private current!: any;

  public $refs!: {
    asideDetail: ApmDetail | NpmDetail;
  }

  @Watch('current', { immediate: true })
  private onCurrentChange (newVal: any, oldVal: any) {
    if (newVal?.id) {
      this.isLock = false
      this.showDetail = true
    }
    if (!newVal) {
      this.showDetail = false;
    }
  }

  public showDetail = false;
  private isLock = false;

  get showApm () {
    const { baseType } = this.current || {};
    return (baseType === 'business' || baseType === 'service') && this.showDetail && !this.isLock
  }
  get showNpm () {
    const { baseType } = this.current || {};
    return (baseType === 'host' || baseType === 'process' || baseType === 'pod') && this.showDetail && !this.isLock
  }

  private toggleExpandHandle () {
    this.showDetail = !this.showDetail;
    this.isLock = !this.showDetail;
  }

  public getData () {
    if (this.$refs.asideDetail) {
      this.$refs.asideDetail.fetchAllData();
    }
  }
}
</script>
<style lang='scss' scoped>
.node-detail-cont {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 12;
  pointer-events: auto;

  .toggle-btn {
    width: 26px;
    height: 24px;
    padding-right: 2px;
    border: 1px solid var(--border-color-base);
    border-right: none;
    border-radius: 2px 0 0 2px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #777A7E;
    transition: all .3s ease;
    cursor: pointer;
    font-size: 16px;
    position: absolute;
    top: 7px;
    right: 0;
    z-index: 9;

    &.is-expanded {
      border-color: transparent;
    }

    .toggle-icon {
      width: 10px;
      height: 10px;
      font-size: 10px;
    }
  }
}
</style>