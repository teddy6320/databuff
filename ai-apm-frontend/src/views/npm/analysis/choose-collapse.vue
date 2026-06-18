<template>
  <div class="choose-collapse flex-v">
    <div class="choose-collapse-title describe">{{ $t('modules.views.alarmCenter.alarm.s_c0fd0276') }}</div>

    <div class="choose-collapse-tabs">
      <span
        @click="toggleTypeModelHandle('client')"
        :class="{ active: typeModel === 'client' }"
        class="tab-item">{{ $t('modules.views.npm.analysis.s_efc6882b') }}</span>
      <span
        @click="toggleTypeModelHandle('server')"
        :class="{ active: typeModel === 'server' }"
        class="tab-item">{{ $t('modules.views.npm.analysis.s_55abea2d') }}</span>
    </div>

    <div class="choose-collapse-body"
      v-loading='filterLoading'>
      <simplebar style='height: 100%;padding-right: 12px;'>
        <el-collapse v-model="activeNames" class="filter-collapse">
          <el-collapse-item
            v-for='item in filterList'
            :key="item.name"
            :title="item.name"
            :name="item.name"
          >
            <simplebar v-if="item.list.length" style="max-height: 200px;">
              <el-checkbox-group v-model="item.model" @change="changeHandle">
                <template v-for='t in item.list'>
                  <el-checkbox
                    :key='t.value'
                    :label="t.value"
                    class="filter-checkbox"
                  >
                    <span class="label-text" @click.prevent="clickCheckboxTextHandle(t.value, item)">{{ t.labelKey ? $t(t.labelKey) : t.label }}</span>
                  </el-checkbox>
                </template>
              </el-checkbox-group>
            </simplebar>
            <div v-else class="describe" style="margin-left:30px">{{ $t('modules.views.alarmCenter.alarm.s_d81bb206') }}</div>
          </el-collapse-item>
        </el-collapse>
      </simplebar>

      <div v-if="!filterList.length" class="empty">{{ $t('modules.components.charts.s_21efd88b') }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import Simplebar from 'simplebar-vue'
import { toAsyncWait } from '@/utils/common';
import NpmApi from '@/api/npm';

@Component({
  components: {
    Simplebar: Simplebar as any,
  },
})
export default class ChooseCollapse extends Vue {
  @Prop({ default: false }) private isDns!: boolean;
  @Prop({ default: () => ({}) }) private query!: any;
  @Prop({ default: () => ({}) }) private timeParams!: any;

  private typeModel: string = 'server'

  private activeNames: string[] = [];

  private filterData: any = {
    client: [],
    server: [],
  }

  private filterList: any[] = []
  private filterLoading = true

  private toggleTypeModelHandle (type: string) {
    if (this.filterLoading || type === this.typeModel) {
      return
    }
    this.typeModel = type
    if (type === 'client') {
      this.filterData.server = this.filterList
      this.filterList = this.filterData.client
    } else {
      this.filterData.client = this.filterList
      this.filterList = this.filterData.server
    }
    this.changeHandle()
  }

  public init () {
    return new Promise(async (resolve, reject) => {
      await this.getParams()
      const { multisearch } = this.$route.query
      // ⬇⬇⬇根据路由参数还原筛选条件⬇⬇⬇
      if (multisearch) {
        try {
          const _multisearch = decodeURIComponent(multisearch as string);
          _multisearch.split(';').forEach((item) => {
            const [key, values] = item.split('=');
            const filterItem = this.filterList.find(t => t.name === key)
            if (filterItem) {
              const keys = filterItem.list.map((_t: any) => _t.value)
              filterItem.model = values.split(',').filter(_t => _t && keys.indexOf(_t) > -1)
              if (filterItem.model.length && !this.activeNames.find(t => t === key)) {
                this.activeNames.push(key)
              }
            }
          });
        } catch (err) {
          console.log(err);
        }
      }
      // ⬆⬆⬆根据路由参数还原筛选条件⬆⬆⬆

      const _params: any = {}
      this.filterList.filter(t => t.model.length).forEach(t => {
        const { name, model } = t
        _params[name] = model
      })
      resolve({ ..._params })
    })
  }

  private async getParams () {
    const { fromTime, toTime } = this.timeParams
    const { client, server, from, } = this.query
    const params: any = {
      start: Math.floor(+new Date(fromTime)),
      end: Math.floor(+new Date(toTime)),
      by: [client, server].filter(t => t),
      from,
    }
    this.filterLoading = true
    const fetchUrl = !this.isDns ? 'getPerformanceTags' : 'getDnsPerformanceTags'
    const { result, error } = await toAsyncWait(NpmApi[fetchUrl](params))
    this.filterLoading = false
    if (!error) {
      const data = result?.data || {}
      const clientTags = data.client || {}
      const serverTags = data.server || data.dnsServer || {}
      const getFilterList = (tags: any) => {
        const list: any[] = []
        Object.keys(tags).forEach(t => {
          list.push({
            name: t,
            model: [],
            list: (tags[t] || []).map((tag: string) => ({ label: tag, value: tag })),
          })
        })
        return list
      }
      this.filterData.client = getFilterList(clientTags)
      this.filterData.server = getFilterList(serverTags)
      const { filterType } = this.$route.query
      this.typeModel = filterType as string || this.typeModel
      this.filterList = this.filterData[this.typeModel]
      this.activeNames = [this.filterList.map(t => t.name)[0]]
    }
  }

  private changeHandle () {
    // 设置路由中的参数
    // 格式： multisearch=encodeURIComponent(aaa=xx,xxx,xxx;bbb=xxx;ccc=xxx,xxx)
    const _params: any = {}
    this.filterList.filter(t => t.model.length).forEach(t => {
      const { name, model } = t
      _params[name] = model
    })
    const _multisearch = Object.entries(_params).map(([key, values]: any) => {
      if (Array.isArray(values)) {
        return `${key}=${values.join(',')}`
      } else {
        return `${key}=${values}`
      }
    }).join(';')
    const __multisearch = decodeURIComponent(this.$route.query.multisearch as string || '')
    const __filterType = this.$route.query.filterType as string || ''
    if (_multisearch !== __multisearch || this.typeModel !== __filterType) {
      this.$router.replace({
        query: {
          ...this.$route.query,
          filterType: this.typeModel,
          multisearch: encodeURIComponent(_multisearch)
        }
      })
    }
    this.$emit('on-filter-change', { ..._params })
  }

  private clickCheckboxTextHandle (value: string, group: any) {
    const targetGroup = this.filterList.find(item => item.name === group.name)
    const targetList = targetGroup!.list
    const targetModel = targetGroup!.model
    const target = targetModel.find((item: any) => item === value)
    if (target) {
      targetGroup!.model = targetList.filter((item: any) => item.value !== value).map((item: any) => item.value)
    } else {
      targetGroup!.model = [value]
    }
    this.changeHandle()
  }
}
</script>

<style lang="scss" scoped>
.choose-collapse {
  width: 230px;
  min-width: 230px;
  height: 100%;
  padding: 12px 0 12px 12px;
  background-color: var(--bg-color);

  .choose-collapse-title {
    line-height: 28px;
  }

  .choose-collapse-tabs {
    margin-bottom: 6px;
    padding-right: 12px;
    display: flex;
    width: 100%;

    .tab-item {
      width: 50%;
      height: 28px;
      box-sizing: border-box;
      border: 1px solid var(--border-color-base);
      color: var(--color-text-regular);
      font-size: 12px;
      text-align: center;
      line-height: 26px;
      cursor: pointer;
      transition: all 0.3s;
      &:first-child {
        border-radius: 2px 0 0 2px;
        border-right: 0;
      }
      &:last-child {
        border-left: 0;
        border-radius: 0 2px 2px 0;
      }
      &:hover {
        color: var(--color-primary);
      }
      &.active {
        background-color: #5273E0;
        border-color: #5273E0;
        box-shadow: -1px 0 0 0 #5273E0;
        color: #fff;
        cursor: default;
      }
    }
  }

  .choose-collapse-body {
    flex: 1;
    overflow: hidden;
    position: relative;

    .empty {
      padding-right: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      position: absolute;
      top: 0;
      left: 0;
    }
  }

  .filter-collapse {
    border: none;

    :deep(.el-collapse-item) {
      padding: 2px 0;
      &:not(:last-child) {
        border-bottom: 1px solid var(--border-color-base);
      }
    }
    :deep(.el-collapse-item__header) {
      display: block;
      padding-left: 20px;
      padding-right: 8px;
      height: 28px;
      line-height: 28px;
      background-color: transparent;
      border: none;
      border-radius: 3px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      position: relative;
      font-size: 12px;
      font-weight: normal;
      .el-collapse-item__arrow {
        position: absolute;
        top: 7px;
        left: 0;
      }
      &:hover {
        background-color: var(--bg-color03);
      }
    }
    :deep(.el-collapse-item__wrap) {
      background-color: transparent;
      border: none;
      .el-collapse-item__content {
        padding-bottom: 0;
      }
    }
    :deep(.el-checkbox-group) {
      width: 100%;
    }

    .filter-input {
      :deep(.el-input__inner) {
        border: none;
      }
    }

    .filter-checkbox {
      margin: 2px 0;
      padding: 3px 0;
      min-height: 24px;
      border-radius: 3px;
      display: flex;
      align-items: center;
      line-height: 20px;
      color: var(--color-text-regular);
      font-weight: normal;
      &:hover {
        background-color: var(--bg-color03);
      }
      :deep(.el-checkbox__inner) {
        display: block;
      }
      :deep(.el-checkbox__label) {
        font-size: 12px;
      }
      :deep(.el-checkbox__input.is-checked+.el-checkbox__label) {
        color: inherit;
      }
    }
  }
}
</style>
