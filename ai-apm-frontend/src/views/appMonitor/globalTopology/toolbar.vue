<template>
  <div class="search-cont">
    <div class="wrapper flex-h-jc">
      <div>
        <el-select
          v-model="localSearch.nameQuery"
          @change="nameQueryHandle"
          @clear="nameQueryHandle('')"
          key="nameSearch"
          size="small"
          :placeholder="$t('modules.views.appMonitor.globalTopology.s_1e39398e')"
          filterable
          clearable
          class="w-220">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>

        <div class="ml-10 dib">
          <el-radio-group v-model="customConfig.direction" @change="toggleLayoutHandle" size="small" class="radio-group">
            <el-radio-button label="circular" class="radio-btn">
              <span class="db-icon-layout-circular icon-vm"></span>
            </el-radio-button>
            <el-radio-button label="horizontal" class="radio-btn">
              <span class="db-icon-layout-horizontal icon-vm"></span>
            </el-radio-button>
          </el-radio-group>
        </div>

      </div>
    </div>
  </div>
</template>

<script lang="ts">
import cloneDeep from 'lodash/cloneDeep'
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import type { CustomConfig } from '../relationMapNew/graph/utils'

@Component({})
export default class GlobalTopologyToolbar extends Vue {
  @Prop({}) private config!: CustomConfig
  @Prop({}) private searchOptions!: Common.NestLabelValue[]

  private localSearch = {
    nameQuery: '',
  }

  private customConfig: CustomConfig = {
    ...cloneDeep(this.config),
  }

  get options () {
    return this.searchOptions
  }

  @Watch('config', { deep: true })
  private onConfigChange (val: CustomConfig) {
    this.customConfig = cloneDeep(val)
  }

  private nameQueryHandle (val: string) {
    this.$emit('search', {
      nameQuery: val || '',
      serviceIds: val ? [val] : [],
    })
  }

  private toggleLayoutHandle () {
    this.$emit('change', cloneDeep(this.customConfig))
  }

  private mounted () {
    this.$emit('inited', cloneDeep(this.customConfig))
  }
}
</script>

<style lang="scss" scoped>
.search-cont {
  height: 40px;
}
.wrapper {
  height: 100%;
  position: relative;
}
</style>
