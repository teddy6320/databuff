<template>
  <div v-loading='loading' class="cont ovh">
    <div class="wrapper ovy-auto p-10 nosel pb-16 pt-16">

      <!-- 染色规则 -->
      <div class="set-item pl-8 pr-8">
        <div class="set-title font-14 fw-500">{{ $t('modules.views.appMonitor.relationMapNew.s_e774a69b') }}</div>
        <div class="set-cont pt-15">
          <el-select v-model='config.colorType' size="small" disabled class="w-100p">
            <el-option value='alarm' :label="$t('modules.views.appMonitor.relationMapNew.s_e65b894d')"></el-option>
          </el-select>
          <!-- <div class="mt-15 pl-15">
            <div class="mb-12">
              <span class="db-icon db-icon-alarm mr-4 red"></span>
              <span>{{ $t('modules.views.appMonitor.relationMapNew.s_b07ce298') }}</span>
            </div>
            <div class="mb-12">
              <span class="db-icon db-icon-alarm mr-4 yellow"></span>
              <span>{{ $t('modules.views.appMonitor.relationMapNew.s_78f2659c') }}</span>
            </div>
            <div class="mb-12">
              <span class="db-icon db-icon-alarm mr-4 grey"></span>
              <span>{{ $t('modules.views.appMonitor.relationMapNew.s_6b808555') }}</span>
            </div>
          </div> -->
        </div>
      </div>

      <el-divider class="margin-small"></el-divider>

      <!-- 平铺与合并 -->
      <div v-show="!isNpm" class="set-item">
        <div class="set-title font-14 fw-500 pl-8 pr-8">{{ $t('modules.views.appMonitor.relationMapNew.s_14aeaa72') }}</div>
        <div class="set-cont pt-8">
          <el-popover v-for='item in concatOptions.filter(i => i.group.includes("concat"))' :key="item.value" trigger="click" placement="right">
            <div>
              <el-radio-group :value="item.model.concat" @input="changeConcatHandle($event, item)" class="flex-v" :key='`${item.value}-radio-group`' :disabled='loading'>
                <el-radio v-for='child in item.children' :key="`${child.value}-radio-group-item`" :label='child.value' class="m-5">
                  {{ child.labelKey ? $t(child.labelKey) : child.label }}
                </el-radio>
              </el-radio-group>
            </div>
            <div slot="reference">
              <div class="concat-item flex-h-jc p-8 cphl br-4">
                <span>{{ item.labelKey ? $t(item.labelKey) : item.label }}</span>
                <span class="el-icon el-icon-arrow-right"></span>
              </div>
            </div>
          </el-popover>
        </div>
      </div>

      <el-divider v-if="!isNpm" class="margin-small"></el-divider>

      <!-- 显示与隐藏 -->
      <div v-show="!isNpm" class="set-item">
        <div class="set-title font-14 fw-500 pl-8 pr-8">{{ $t('modules.views.appMonitor.relationMapNew.s_82583e9e') }}</div>
        <div class="set-cont pt-8">
          <div v-for='item in concatOptions.filter(i => i.group.includes("visible"))' :key="item.value" class="toggle-item flex-h-jc p-8 br-4">
            <span>{{ item.labelKey ? $t(item.labelKey) : item.label }}</span>
            <el-switch :value='item.model.show' @change="changeVisibleHandle($event, item)" :disabled='loading'></el-switch>
          </div>
        </div>
      </div>

      <el-divider v-if="!isNpm" class="margin-small"></el-divider>


      <!-- 节点数量 -->
      <div class="set-item">
        <div class="set-title font-14 fw-500 pl-8 pr-8">
          <span class="mr-8">{{ $t('modules.views.appMonitor.relationMapNew.s_84e32888') }}</span>
          <el-tooltip :content="$t('modules.views.appMonitor.relationMapNew.s_e5a0174e')" effect="light" placement="top">
            <i class="el-icon-warning-outline describe"></i>
          </el-tooltip>
        </div>
        <div class="set-cont pt-8 pl-8 pr-8">
          <el-select v-model="manualConfig.maxNode" @change="changeMaxNodeHandle" size="small" :disabled='loading'>
            <el-option v-for='item in [500, 1000, 1500, 2000]' :key='`node-count-${item}`' :value='item' :label='item'></el-option>
          </el-select>
        </div>
      </div>

      <el-divider class="margin-small"></el-divider>

      <!-- 连线粗细 -->
      <div class="set-item">
        <div class="set-title font-14 fw-500 pl-8 pr-8">{{ $t('modules.views.appMonitor.relationMapNew.s_2a9d49e6') }}</div>
        <div class="set-cont pt-8 pl-8 pr-8">
          <el-popover key="line-style" trigger="hover" placement="right">
            <div>
              <el-radio-group :value="config.lineType" @input="changeLineTypeHandle" class="flex-v" key='`line-style-radio-group`' :disabled='loading'>
                <el-radio v-for='item in lineTypeOptions' :key="`${item.value}-line-style-item`" :label='item.value' class="m-5">
                  {{ item.labelKey ? $t(item.labelKey) : item.label }}
                </el-radio>
              </el-radio-group>
            </div>
            <div slot="reference" class="flex-h-jc">
              <span>{{ config.lineType }}px</span>
              <span class="dib line-style-preview" :style='`--line-style:${config.lineType}`'></span>
              <span class="el-icon el-icon-arrow-right"></span>
            </div>
          </el-popover>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { init } from 'echarts/core';
import i18n from '@/i18n';
import { cloneDeep } from 'lodash';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import type { CustomConfig } from './graph/utils';

@Component
export default class TopoSetting extends Vue {
  @Prop({}) private relationType!: APM.RelationMap['relationType'];
  @Prop({}) private config!: CustomConfig;

  private concatOptions = [
    {
      value: 'db',
      label: i18n.t('modules.views.appMonitor.relationMapNew.s_68051bf4') as string, labelKey: 'modules.utils.filters.s_68051bf4',
      children: [
        { value: 'default', label: i18n.t('modules.views.appMonitor.relationMapNew.s_236d309c') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_236d309c' },
        { value: 'type', label: i18n.t('modules.views.appMonitor.relationMapNew.s_d3f2dd9c') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_d3f2dd9c' },
      ],
      group: ['concat', 'visible'],
      model: {
        show: true,
        concat: 'default'
      }
    },
    {
      value: 'mq',
      label: i18n.t('modules.views.appMonitor.relationMapNew.s_8bedb7aa') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_8bedb7aa',
      children: [
        { value: 'default', label: i18n.t('modules.views.appMonitor.relationMapNew.s_236d309c') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_236d309c' },
        { value: 'type', label: i18n.t('modules.views.appMonitor.relationMapNew.s_d3f2dd9c') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_d3f2dd9c' },
      ],
      group: ['concat', 'visible'],
      model: {
        show: true,
        concat: 'type'
      }
    },
    {
      value: 'cache',
      label: i18n.t('modules.views.appMonitor.relationMapNew.s_e80c310e') as string, labelKey: 'modules.utils.filters.s_e80c310e',
      group: ['concat', 'visible'],
      children: [
        { value: 'default', label: i18n.t('modules.views.appMonitor.relationMapNew.s_236d309c') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_236d309c' },
        { value: 'type', label: i18n.t('modules.views.appMonitor.relationMapNew.s_d3f2dd9c') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_d3f2dd9c' },
      ],
      model: {
        show: true,
        concat: 'default'
      }
    },
    // {
    //   value: 'remote',
    //   label: i18n.t('modules.views.appMonitor.external.s_47921e9e') as string, labelKey: 'modules.views.appMonitor.external.s_47921e9e',
    //   group: ['concat', 'visible'],
    //   children: [
    //     { value: 'default', label: i18n.t('modules.views.appMonitor.relationMapNew.s_236d309c') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_236d309c' },
    //     { value: 'ipaddress', label: i18n.t('modules.views.appMonitor.relationMapNew.s_79207877') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_79207877' },
    //     { value: 'protocol', label: i18n.t('modules.views.appMonitor.relationMapNew.s_49be0b48') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_49be0b48' }
    //   ],
    //   model: {
    //     show: true,
    //     concat: 'protocol'
    //   }
    // },
    {
      value: 'nodata',
      label: i18n.t('modules.views.appMonitor.relationMapNew.s_e0247406') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_e0247406',
      children: [],
      group: ['visible'],
      model: {
        show: true,
        concat: 'default'
      }
    },
  ];

  private lineTypeOptions = [
    { value: '0.5', label: '0.5px' },
    { value: '1', label: '1px' },
    { value: '2', label: '2px' },
    { value: '3', label: '3px' },
  ]

  private manualConfig = {
    colorType: 'alarm',
    maxNode: 500,
    lineType: '1'
  }

  private loading: boolean = false;

  get isNpm () {
    return this.relationType === 'process' || this.relationType === 'host';
  }

  @Watch('relationType', { immediate: true })
  private async onRelationTypeChange () {
    this.initConfig();
  }

  private async created () {
    this.initConfig();
  }

  private initConfig () {
    const { lineType, maxNode, visibleModel = {}, concatModel } = this.config;
    this.manualConfig.lineType = lineType ?? '1';
    switch (this.relationType) {
      case 'application':
        this.manualConfig.maxNode = maxNode?.application ?? 500;
        break;
      case 'service':
        this.manualConfig.maxNode = maxNode?.service ?? 500;
        for (const key in visibleModel) {
          if (Object.prototype.hasOwnProperty.call(visibleModel, key)) {
            const item = this.concatOptions.find(i => i.value === key);
            if (item) {
              item.model.show = visibleModel[key as keyof typeof visibleModel];
            }
          }
        }
        for (const key in concatModel) {
          if (Object.prototype.hasOwnProperty.call(concatModel, key)) {
            const item = this.concatOptions.find(i => i.value === key);
            if (item) {
              item.model.concat = concatModel[key as keyof typeof concatModel];
            }
          }
        }
        break;
      case 'host':
        this.manualConfig.maxNode = maxNode?.host ?? 500;
        break;
      case 'process':
        this.manualConfig.maxNode = maxNode?.process ?? 500;
        break;
    }
  }

  private async changeConcatHandle (val: string, item: any) {
    this.$emit('change', { action: 'concatModel', type: item.value, value: val });
    this.waitChangeOver();
    item.model.concat = val;
  }

  private async changeVisibleHandle (val: string, item: any) {
    this.$emit('change', { action: 'visibleModel', type: item.value, value: val });
    this.waitChangeOver();
    item.model.show = val;
  }

  private async changeMaxNodeHandle (val: number) {
    this.$emit('change', { action: 'maxNode', type: this.relationType, value: val });
    this.waitChangeOver();
    this.manualConfig.maxNode = val;
  }

  private async changeLineTypeHandle (val: string) {
    this.$emit('change', { action: 'lineType', type: 'lineType', value: val });
    this.waitChangeOver();
    // this.manualConfig.lineType = val;
  }

  private async waitChangeOver () {
    this.loading = true;
    const result = await new Promise((resolve) => {
      setTimeout(() => {
        resolve(true);
      }, 500);
    });
    this.loading = false;
    return result;
  }
}
</script>

<style lang="scss" scoped>
.cont{
  position: relative;

  .wrapper {
    max-height: 400px;
    padding-right: 0;
    position: relative;
  }

  .concat-item {
    transition: background-color .3s, color .3s;
    
    &:hover {
      background-color: #EAEFFF;
    }
  }

  .set-cont {
    position: relative;
  }
}
.margin-small {
  margin-top: 16px;
  margin-bottom: 16px;
}
.line-style-preview {
  display: inline-block;
  width: 100px;
  height: 1px;
  transform: scaleY(var(--line-style));
  background-color: #ccc;
}
</style>