<template>
  <div class="detail-overview">
    <el-input
      v-model="queryText"
      @change="handleQuerySearch"
      prefix-icon="db-icon-search dib query-input-icon"
      clearable size="small"
      maxlength="100"
      :placeholder="$t('modules.views.infrastructure.clusterDetail.s_4ad69a0f')"
      class="query-input mb-10"
    />

    <div class="overview-content">
      <div class="type-list-wrap">
        <div class="type-list">
          <template v-for="item in typeList">
            <div :key="item.value"
              @click="activeTypeChangeHandle(item.value)"
              :class="{ 'open-item': item.open, 'active': activeType === item.value }"
              class="type-item mtb-4">
              <span class="type-icon" :class="item.icon"></span>
              <div class="type-name">{{ item.labelKey ? $t(item.labelKey) : item.label }}</div>
              <span v-if="item.subList" @click.stop="item.open = !item.open" class="type-icon-arrow el-icon-arrow-down"></span>
            </div>
            <div v-if="item.subList" :key="`${item.value}-sub`" class="sub-type-list">
              <div v-for="sub in item.subList" :key="sub.value"
                @click="activeTypeChangeHandle(item.value, sub.value)"
                :class="{ 'active': activeType === sub.value }"
                class="type-sub-item mtb-2">{{ sub.labelKey ? $t(sub.labelKey) : sub.label }}</div>
            </div>
          </template>
        </div>
      </div>

      <div class="overview-table">
        <component
          ref="tableList"
          :is="listType"
          :queryParams="getQueryParams"
          :listTitle="typeLabel"
        />
      </div>
    </div>

  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import Namespace from './table/namespace.vue'
import Node from './table/node.vue'
import Workload from './table/workload.vue'
import Pod from './table/pod.vue'
import Service from './table/service.vue'

@Component({
  components: {
    Namespace,
    Node,
    Workload,
    Pod,
    Service,
  }
})
export default class DetailOverview extends Vue {
  @Prop({ default: () => ({}) }) private queryParams!: any;

  public $refs!: {
    tableList: Namespace | Node | Workload | Pod | Service | any
  }

  private queryText: string = ''

  private activeType = 'namespace';

  get listType () {
    const item: any = this.typeList.find(t => t.value === this.activeType || !!t.subList?.find(sub => sub.value === this.activeType))
    return item?.value || ''
  }
  get typeLabel () {
    let label: string = ''
    this.typeList.forEach(t => {
      if (t.value === this.activeType) {
        label = t.label
      } else if (Array.isArray(t.subList)) {
        t.subList.forEach(sub => {
          if (sub.value === this.activeType) {
            label = sub.label
          }
        })
      }
    })
    return label;
  }

  get getQueryParams () {
    const params = {
      ...this.queryParams,
      queryText: this.queryText,
    }
    if (this.listType === 'workload' && this.listType !== this.activeType) {
      params.resourceType = this.activeType
    }
    return params;
  }

  private typeList = [
    { label: 'Namespace', value: 'namespace', icon: 'db-icon-namespace' },
    { label: 'Node', value: 'node', icon: 'db-icon-node' },
    { label: 'Workloads', value: 'workload', icon: 'db-icon-workload', open: true, subList: [
      { label: 'ReplicaSets', value: '42' },
      { label: 'Deployments', value: '43' },
      { label: 'DaemonSets', value: '49' },
      { label: 'StatefulSets', value: '50' },
    ]},
    { label: 'Pod', value: 'pod', icon: 'db-icon-pod2' },
    { label: 'Services', value: 'service', icon: 'db-icon-service' },
  ]

  public getData () {
    this.$nextTick(() => {
      if (this.$refs.tableList) {
        this.$refs.tableList.clear && this.$refs.tableList.clear()
        this.$refs.tableList.refresh && this.$refs.tableList.refresh()
      }
    })
  }

  private handleQuerySearch () {
    this.getData()
  }

  private activeTypeChangeHandle (type: string, subType?: string) {
    this.activeType = subType || type;
    this.getData()
  }
}
</script>

<style lang="scss" scoped>
.detail-overview {
  height: 100%;
  :deep(.query-input-icon) {
    width: 20px;
    color: var(--color-text-primary);
  }

  .overview-content {
    display: flex;
    align-items: flex-start;
    height: calc(100% - 42px);
  }

  .type-list-wrap {
    flex: none;
    width: 153px;
    padding: 5px 9px;
    background: #FFFFFF;
    border: 1px solid #EEEFF1;
    border-radius: 4px;
  }
}

.type-list {
  height: 100%;
  border-right: none;
  .mtb-4 {
    margin-top: 4px;
    margin-bottom: 4px;
  }
  .mtb-2 {
    margin-top: 2px;
    margin-bottom: 2px;
  }
  .sub-type-list {
    margin-top: -4px;
    height: 0;
    transition: height 0.3s;
    overflow: hidden;
  }
  .open-item {
    & + .sub-type-list {
      height: 128px;
    }
    .type-icon-arrow {
      transform: translate(0, -50%) rotateZ(180deg);
    }
  }
  .type-item,
  .type-sub-item {
    display: flex;
    align-items: center;
    padding: 6px 20px 6px 30px !important;
    height: 30px;
    min-width: auto;
    border-radius: 4px;
    transition: all 0.3s;
    line-height: 1;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    font-family: PingFang SC;
    font-size: 13px;
    color: var(--color-text-primary);
    cursor: pointer;
    position: relative;
    &:hover {
      background-color: #F7F7F7;
    }
    &.active {
      background-color: #F7F7F7;
      color: #2962FF;
    }
  }
  .type-sub-item {
    color: #626467;
    font-size: 12px;
    &:last-child {
      margin-bottom: 0;
    }
  }
  .type-name {
    width: 100%;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    transition: width 0.3s;
  }

  .type-icon {
    font-size: 14px;
    transform: translate(0, -50%);
    position: absolute;
    top: 50%;
    left: 10px;
  }
  .type-icon-arrow {
    padding: 6px;
    font-size: 12px;
    transform: translate(0, -50%);
    color: #777A7E;
    transition: all 0.3s;
    position: absolute;
    top: 50%;
    right: 4px;
  }
}

.overview-table {
  margin-left: 20px;
  flex: 1;
  width: calc(100% - 173px);
  height: 100%;
  overflow: hidden;
}
</style>
