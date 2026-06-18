<template>
  <div class='single-flow-detail'>
    <db-tabnav :tabnavs='tabnavs' v-model='activeName' class="single-flow-detail-tabs"></db-tabnav>
    <div class="single-flow-detail-wrapper">
      <el-collapse v-for='node, idx in chainNodes' :key='node.key'
        :value='(idx === 0 || idx === chainNodes.length - 1) ? "1" : "0"' :class="['service-flow-item', hasChoosed ? 'has-active' : '']">
        <el-collapse-item name="1">
          <!-- collapse header -->
          <template slot="title">
            <span class="db-icon mr-4" v-if='node._serviceType'>{{node._serviceType | DbIconFilter}}</span>
            <el-tooltip :content="node.name" effect="light" placement="top" :enterable='false'>
              <span class="service-name ell">
                <span @click.stop="viewServiceDetailHandle(node._service, node._serviceId)" class="cphu">{{ node.nameKey ? $t(node.nameKey) : node.name }}</span>
              </span>
            </el-tooltip>
          </template>

          <!-- collapse content - 调用链 -->
          <div class="flow-detail-panel" v-show='activeName === "chain"'>
            <p class="flex-h-jc flow-detail-panel-item">
              {{ $t('modules.views.appMonitor.cache.s_96a0c062') }}
              <span class="flow-detail-item-value">{{ node.viewData.response | NsFilter }}</span>
            </p>
            <p class="flex-h-jc flow-detail-panel-item">
              {{ $t('modules.views.appMonitor.cache.s_8bc42b53') }}
              <span class="flow-detail-item-value">{{ node.viewData.reqCnt | NumberFilter }}</span>
            </p>
            <p class="flex-h-jc flow-detail-panel-item">
              {{ $t('modules.views.appMonitor.serviceFlow.s_696acc9b') }}
              <span class="flow-detail-item-value">{{ node.viewData.failCnt | NumberFilter }}</span>
            </p>
            <p class="flex-h-jc flow-detail-panel-item">
              {{ $t('modules.views.appMonitor.serviceFlow.s_b9fa3ce9') }}
              <span class="flow-detail-item-value">{{ node.viewData.outCnt | NumberFilter }}</span>
            </p>
          </div>
        </el-collapse-item>

        <div v-if='idx < chainNodes.length - 1' class="flow-arrow-line"></div>
      </el-collapse>
    </div>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';

@Component
export default class SingleFlow extends Vue {
  @Prop() private currChain!: any;
  @Prop() private hasChoosed!: boolean;

  @Watch('globalTimeV2', { deep: true })
  private watchGlobalTime() {
    this.chainNodes = [];
  }

  @Watch('currChain')
  private onChainChange (newVal: any) {
    if (newVal) {
      this.chainNodes = newVal.map((t: any, index: number) => {
        const serviceInfo = t.serviceInfo || {}
        const viewData = t.viewData || {}
        return {
          ...t,
          serviceInfo,
          viewData,
          _service: serviceInfo.service,
          _serviceId: serviceInfo.serviceId,
          _serviceType: serviceInfo.serviceType,
          _serviceInstances: serviceInfo?.serviceInstances || [],
        }
      });
    } else {
      this.chainNodes = [];
    }
  }

  // tabs导航
  private tabnavs = [
    { label: i18n.t('modules.utils.static.s_d3a1be9f') as string, labelKey: 'modules.utils.static.s_d3a1be9f', value: 'chain', icon: 'icon-tree-split' },
  ];
  private activeName = 'chain';
  private chainNodes: any = [];

  // 查看服务详情
  private viewServiceDetailHandle (sn: string, sid: string) {
    this.$router.push({
      path: '/appMonitor/serviceDetail',
      query: {
        sn: encodeURIComponent(sn),
        sid: encodeURIComponent(sid),
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.single-flow-detail {
  height: 100%;

  .single-flow-detail-tabs {
    width: calc(100% - 40px);
    margin: 20px 20px 0;
    :deep(.tabs-nav-item) {
      margin: 0;
    }
  }

  .single-flow-detail-wrapper {
    height: calc( 100% - 46px);
    padding: 20px;
    box-sizing: border-box;
    // padding-top: 16px;
    overflow-y: auto;
  }

  .service-flow-item {
    margin-bottom: 36px;
    border: 2px solid var(--border-color-lighter);
    box-shadow: 0px 2px 10px 0px rgba(119, 122, 126, 0.12);
    border-radius: 6px;
    position: relative;

    &.has-active:last-child {
      margin-bottom: 0;
      border-color: var(--color-primary);
      :deep(.el-collapse-item__header) {
        background-color: var(--color-primary);
        color: #fff;
        .cphu,
        .el-collapse-item__arrow {
          color: #fff;
        }
      }
    }

    :deep(.el-collapse-item__wrap) {
      border-bottom: none;
      background: none;
    }
    :deep(.el-collapse-item__content) {
      padding: 16px 12px 12px;
      font-size: 12px;
      line-height: 14px;
    }
    :deep(.el-collapse-item__arrow) {
      margin: 0;
      color: var(--color-text-secondary);
      font-weight: bold;
      font-size: 12px;
    }
    :deep(.el-collapse-item__header) {
      background: var(--bg-color03);
      height: 32px;
      padding: 5px 12px;
      line-height: 22px;
      font-size: 13px;
      font-weight: normal;
      border: none;
      border-radius: 3px 3px 0 0;
    }
  }
  .service-name {
    flex: 1;
    overflow: hidden;
    margin-right: 8px;
  }
  .flow-detail-panel-item {
    margin: 0;
    color: var(--color-text-secondary);

    & + .flow-detail-panel-item {
      margin-top: 10px;
    }

    .flow-detail-item-value {
      color: var(--color-text-primary);
    }
  }
  .flow-arrow-line {
    width: 2px;
    height: 36px;
    background: var(--color-text-secondary);
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
    position: absolute;
    left: 50%;
    bottom: 0px;
    transform: translate(-50%, 100%) scaleX(0.5);
    z-index: 10;
    pointer-events: none;
    &::before {
      content: '';
      margin-top: -3px;
      width: 12px;
      height: 6px;
      border-radius: 50%;
      background: var(--color-text-secondary);
    }
    &::after {
      content: '';
      margin-bottom: -1px;
      display: inline-block;
      vertical-align: middle;
      border-top: 8px solid var(--color-text-secondary);
      border-left: 10px solid transparent;
      border-right: 10px solid transparent;
    }
  }
}

.db-icon {
  font-size: 14px;
}
</style>
