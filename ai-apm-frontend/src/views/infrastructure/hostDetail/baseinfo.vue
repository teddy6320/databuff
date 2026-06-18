<template>
  <div class="detail-baseinfo">
    <div class="font-14 line-height-22 fw-500 mb-16">{{ $t('modules.views.metrics.list.s_1f7be0a9') }}</div>

    <div class="tag-list">
      <span v-for="(tag, index) in systemTags" :key="index" class="tag-item ell">{{ tag }}</span>
      <span v-for="(tag, index) in customTags" :key="index" class="tag-item ell">
        {{ tag }}
        <i @click.stop="deleteTagHandle(index)"
          :class="{ disabled: tagLoading }"
          class="tag-close-icon db-icon-close"></i>
      </span>

      <el-button
        type="primary"
        :disabled="tagLoading"
        @click="showAddLabelHandle"
        size="small">
        <i class="db-icon-add"></i>{{ $t('modules.views.infrastructure.hostDetail.s_14d34236') }}
      </el-button>
    </div>

    <div class="font-14 line-height-22 fw-500 mb-16 mt-20">{{ $t('modules.views.infrastructure.clusterDetail.s_c5ea2ca1') }}</div>

    <el-collapse v-model="activeNames" class="db-collapse">
      <el-collapse-item name="baseinfo">
        <template slot="title">
          <i class="db-icon-baseinfo-o mr-6"></i>{{ $t('modules.views.alarmCenter.eventDetail.s_9e5ffa06') }}
        </template>
        <div class="flex-h wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.host.s_7714bc11') }}</div>{{ detail.oneAgent || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.host.s_30d23ef4') }}</div>{{ detail.hostOs || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_b2f511d7') }}</div>{{ (detail.platform || {}).machine || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_a8d9eef0') }}</div>{{ (detail.platform || {}).kernel_release || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_1857e702') }}</div>{{ detail.osVersion || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.configManage.entity.s_2dc9105c') }}</div>{{ hostManageIp || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_4aa89eaa') }}</div>{{ cpu.cpu_logical_processors || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_36471b13') }}</div>{{ cpu.cpu_cores || '-' }}</div>
        <div v-if="getEnableStatus" class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.alarmCenter.alarm.s_f9d4e244') }}</div>{{ domainNames || '-' }}</div>
      </el-collapse-item>

      <el-collapse-item name="cpu">
        <template slot="title">
          <i class="db-icon-cpu-o mr-6"></i>{{ $t('modules.views.configInstall.dataAccess.s_e492af4c') }}
        </template>
        <div class="flex-h wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_ec61d5a3') }}</div>{{ cpu.vendor_id || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_322ff093') }}</div>{{ cpu.model_name || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_b4daf968') }}</div>{{ cpu.cpu_cores || '-' }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_b70a5564') }}</div>{{ cpu.ghz || '-' }}</div>
      </el-collapse-item>

      <el-collapse-item name="memory">
        <template slot="title">
          <i class="db-icon-memory-o mr-6"></i>{{ $t('modules.views.appMonitor.hotMethods.s_9932551c') }}
        </template>
        <div class="flex-h wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_1584be1c') }}</div>{{ memory.memoryTotal | BytesFilter }}</div>
        <div class="flex-h mt-16 wba lh-18"><div class="label">{{ $t('modules.views.infrastructure.hostDetail.s_a519e251') }}</div>{{ memory.memorySwapTotal | BytesFilter }}</div>
      </el-collapse-item>

      <el-collapse-item name="networkCard">
        <template slot="title">
          <i class="db-icon-networkcard-o mr-6"></i>{{ $t('modules.views.infrastructure.hostDetail.s_fa5a706d') }}
        </template>
        <el-table
          :data="interfaces"
          size="mini" max-height="300"
          highlight-current-row
          tooltip-effect="light"
          class="item-table">
          <el-table-column :label="$t('modules.views.alarmCenter.eventDetail.s_34cab80c')" prop="name" min-width="100" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.hostDetail.s_8eff61d4')" prop="ipv4" min-width="120" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.hostDetail.s_55fc51a7')" prop="ipv6" min-width="100" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.hostDetail.s_7e81dfb5')" prop="macaddress" min-width="80" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.hostDetail.s_b7f920e3')" width="110">
            <template slot="header">
              {{ $t('modules.views.infrastructure.hostDetail.s_b7f920e3') }}
              <el-tooltip placement="top" effect="light">
                <i class="db-icon-info describe font-13"></i>
                <template slot="content">
                  {{ $t('modules.views.infrastructure.hostDetail.s_b685d587') }}
                  <div class="mt-5">{{ $t('modules.views.infrastructure.hostDetail.s_3419fbd0') }}</div>
                </template>
              </el-tooltip>
            </template>
            <template slot-scope="{ row }">
              <el-switch
                v-model="row.isManageIp"
                :disabled="row.loading || !row.ipv4 || row.ipv4 === '-'"
                @change="toggleManageIpHandle(row)">
              </el-switch>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>

      <el-collapse-item name="disk">
        <template slot="title">
          <i class="db-icon-disk-o mr-6"></i>{{ $t('modules.views.infrastructure.hostDetail.s_4f5537dd') }}
        </template>
        <el-table
          :data="filesystem"
          size="mini" max-height="300"
          highlight-current-row
          tooltip-effect="light"
          class="item-table">
          <el-table-column :label="$t('modules.views.alarmCenter.alarm.s_63cf5e77')" prop="name" min-width="150" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.hostDetail.s_15d67ed4')" prop="size" min-width="100" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.hostDetail.s_1c61dfb8')" prop="mounted_on" min-width="150" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.clusterDetail.s_ce2ed8c2')" prop="used" min-width="100" show-overflow-tooltip />
          <el-table-column :label="$t('modules.views.infrastructure.clusterDetail.s_41d8b224')" prop="usage" min-width="100" show-overflow-tooltip>
            <template slot-scope="{ row }">{{ row.usage | PercentFilter(row.used !== '0') }}</template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <!-- 添加标签弹窗 -->
    <tag-dialog
      ref="tagDialog"
      :saveTagApi="saveTagApi"
      :params="{ ids: [detail.id] }"
      @on-saved="tagSaveHandle" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { copy } from '@/utils/common';
import { orderBy } from 'lodash';
import { BytesFilter } from '@/utils/filters/number';
import { toAsyncWait } from '@/utils/common';
import InfraApi from '@/api/infrastructure';
import TagDialog from '../host/tag-dialog.vue';

@Component({
  components: {
    TagDialog,
  }
})
export default class DetailBaseinfo extends Vue {
  @Prop({ default: () => ({}) }) private detail!: any;

  public $refs!: {
    tagDialog: TagDialog
  }

  private activeNames: string[] = ['baseinfo', 'cpu', 'disk', 'memory', 'networkCard'];

  get getEnableStatus () {
    return this.$store.getters['User/getGroupEnabled']
  }

  get hostManageIp () {
    const { manageIp, hostIp } = this.detail;
    return (manageIp || hostIp || '-') + (manageIp && manageIp !== hostIp ? ` (${hostIp || '-'})` : '')
  }

  get domainNames () {
    const domainManager: any[] = this.detail?.domainManager || [];
    return domainManager.map(t => t.name).join(', ')
  }

  get cpu () {
    const cpu = this.detail.cpu || {}
    return {
      ...cpu,
      ghz: cpu.mhz ? `${(cpu.mhz / 1000).toFixed(1)}0 GHz` : '',
    }
  }

  get memory () {
    const memory = this.detail.memory || {}
    const { total, swap_total } = memory
    return {
      ...memory,
      memoryTotal: total ? parseInt(total, 10) * (total.includes('kB') ? 1024 : 1) : '',
      memorySwapTotal: swap_total ? parseInt(swap_total, 10) * (total.includes('kB') ? 1024 : 1) : '',
    }
  }

  private interfaces: any[] = []
  @Watch('detail.network.interfaces', { immediate: true, deep: true })
  private onDetailInterfacesChange () {
    const interfaces = this.detail?.network?.interfaces || []
    this.interfaces = interfaces.map((t: any) => ({
      name: t.name || '-',
      ipv4: t.ipv4 || '-',
      ipv6: t.ipv6 || '-',
      macaddress: t.macaddress || '-',
      isManageIp: (t.ipv4 && t.ipv4 !== '-') && t.ipv4 === this.detail.manageIp,
      loading: false,
    }))
  }

  get filesystem () {
    const filesystem = (this.detail?.filesystem || []).filter((t: any) => !!t).map((t: any) => {
      const used = !t.used || t.used === '-' ? '0' : t.used
      return {
        ...t,
        size: t.size || BytesFilter(+t.kb_size * 1024),
        used,
        usage: typeof t.usedPercent === 'number' ? t.usedPercent / 100 : '-',
        kb_size: !isNaN(+t.kb_size) ? +t.kb_size : 0,
        usedPercent: t.usedPercent,
      }
    })
    const list = filesystem.filter((t: any) => typeof t.usedPercent === 'number')
    return [
      ...orderBy(list, ['usedPercent'], ['desc']),
      ...filesystem.filter((t: any) => typeof t.usedPercent !== 'number'),
    ]
  }

  private tagLoading = false
  private saveTagApi = InfraApi.customHostTag
  get tags () {
    return this.detail['host-tags'] || {}
  }
  get systemTags (): string[] {
    return (this.tags.system || []).filter((t: any) => !!t).sort()
  }
  private customTags: string[] = []
  @Watch('tags', { immediate: true, deep: true })
  private onDetailCustomTagsChange () {
    this.customTags = (this.tags.custom || []).filter((t: any) => !!t)
  }

  // 删除自定义标签
  private async deleteTagHandle (index: number) {
    if (this.tagLoading) {
      return
    }
    const bacTags = [...this.customTags]
    bacTags.splice(index, 1);
    this.tagLoading = true;
    const { result, error } = await toAsyncWait(InfraApi.customHostTag({
      cover: 1, // 覆盖模式
      ids: [this.detail.id],
      tags: [...bacTags],
    }));
    this.tagLoading = false
    if (!error) {
      this.$message.success(i18n.t('modules.views.appMonitor.serviceDetail.s_a6d082e4') as string);
      this.customTags = bacTags;
      this.detail['host-tags'] = {
        ...this.detail['host-tags'],
        custom: [...this.customTags],
      }
    } else {
      this.$message.error(error.message || i18n.t('modules.views.appMonitor.serviceDetail.s_8a3997a3') as string);
    }
  }
  private showAddLabelHandle () {
    this.$refs.tagDialog.showHandle();
  }
  private tagSaveHandle (data: any) {
    this.customTags = [...new Set([...this.customTags, ...data.tags])]
    this.detail['host-tags'] = {
      ...this.detail['host-tags'],
      custom: [...this.customTags],
    }
  }

  // 切换管理IP
  private async toggleManageIpHandle (row: any) {
    const params: any = {
      id: this.detail.id,
      managerIpaddress: row.isManageIp ? row.ipv4 : '',
    }
    this.interfaces.forEach(t => t.loading = true);
    const { result, error } = await toAsyncWait(InfraApi.setHostManagerIp(params));
    this.interfaces.forEach(t => t.loading = false);
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.entity.s_33130f5c') as string);
      this.detail.manageIp = params.managerIpaddress
      this.interfaces.forEach(t => {
        t.isManageIp = (t.ipv4 && t.ipv4 !== '-') && t.ipv4 === this.detail.manageIp
      });
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
      row.isManageIp = !row.isManageIp
    }
  }

  // 跳转到主机详情
  private viewHostDetail () {
    if (!this.detail.hostName) {
      return
    }
    this.$router.push({
      path: '/infrastructure/hostDetail',
      query: { hostName: encodeURIComponent(this.detail.hostName), }
    })
  }
}
</script>

<style lang="scss" scoped>
.detail-baseinfo {
  margin: 0 -4px;
  padding: 0 4px;
  height: 100%;
  overflow: auto;
  font-size: 13%;

  .flex-h {
    align-items: flex-start;
  }
  .label {
    width: 140px;
    flex: none;
  }

  .item-table {
    width: 650px;
    border: 1px solid var(--border-color-lighter);
  }
}

.tag-list {
  font-size: 13px;
  .tag-item {
    box-sizing: border-box;
    margin: 0 10px 10px 0;
    padding: 6px 10px;
    max-width: calc(100% - 10px);
    height: 32px;
    display: inline-block;
    vertical-align: top;
    background: var(--bg-color);
    box-shadow: 0px 2px 8px 0px rgba(139, 142, 147, 0.26);
    border-radius: 4px;
    line-height: 20px;
    position: relative;
    &:has(.tag-close-icon) {
      padding-right: 24px;
    }

    .tag-close-icon {
      height: 16px;
      width: 16px;
      font-size: 12px;
      font-weight: bold;
      line-height: 16px;
      text-align: center;
      transform: translate(0, -50%) scale(0.8);
      transition: background-color .3s ease, color .3s ease;
      opacity: 0.6;
      cursor: pointer;
      position: absolute;
      right: 5px;
      top: 50%;
      &.disabled {
        cursor: not-allowed;
      }
      &:not(.disabled):hover {
        opacity: 1;
      }
    }
  }
}

.db-collapse {
  border: 1px solid var(--border-color-lighter);
  border-radius: 4px;
  overflow: hidden;
  :deep(.el-collapse-item__header) {
    box-sizing: content-box;
    padding: 0 20px 0 41px;
    height: 38px;
    line-height: 38px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    position: relative;
    &.is-active {
      border-color: var(--border-color-lighter);
    }
  }
  :deep(.el-collapse-item__arrow) {
    margin: 0;
    position: absolute;
    font-size: 14px;
    left: 15px;
  }
  :deep(.el-collapse-item__content) {
    padding: 16px 20px 16px 41px;
  }
}
</style>
