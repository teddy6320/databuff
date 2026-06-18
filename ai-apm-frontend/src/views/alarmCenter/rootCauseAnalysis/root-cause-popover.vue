<template>
  <el-popover
    v-model="popoverVisible"
    @show="showPopoverHandle"
    @hide="hidePopoverHandle"
    :disabled="disabled"
    placement="bottom-end"
    width="400">
    <div class="mb-10">{{ $t('modules.views.alarmCenter.rootCauseAnalysis.s_b35ccd73') }}</div>
    <el-form
      ref="timeDateForm" :model="timeDateForm" :rules="timeDateRules"
      label-position="top" inline size="mini"
      class="time-date-form">
      <el-form-item label="" prop="fromTime" class="time-form-item">
        <el-date-picker v-model="timeDateForm.fromTime" size="mini" type="datetime"
          @change="validateHandle"
          :picker-options="pickerOptions"
          format="yyyy-MM-dd HH:mm"
          prefix-icon="el-icon-date"
          class="time-choose-item"></el-date-picker>
      </el-form-item>
      <el-form-item :label="$t('modules.views.alarmCenter.rootCauseAnalysis.s_981cbe31')"></el-form-item>
      <el-form-item label="" prop="toTime" class="time-form-item">
        <el-date-picker v-model="timeDateForm.toTime" size="mini" type="datetime"
          @change="validateHandle"
          :picker-options="pickerOptions"
          format="yyyy-MM-dd HH:mm"
          prefix-icon="el-icon-date"
          class="time-choose-item"></el-date-picker>
      </el-form-item>
    </el-form>

    <div class="tr">
      <el-button size="mini" type="text" @click="popoverVisible = false">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button type="primary" size="mini" @click="jumpRCAHandle">{{ $t('modules.views.alarmCenter.rootCauseAnalysis.s_a7e17ae8') }}</el-button>
    </div>
    
    <template slot="reference">
      <el-tag
        v-if="actionType === 'tag'"
        :type="disabled ? 'info' : ''"
        size="small"
        :class="['mr-10 cp', { 'disable-tag-btn': disabled }]"
      >{{ $t('modules.api.alarm.ts.s_2a9e65b6') }}<i class="el-icon el-icon-arrow-right ml-5"></i></el-tag>

      <el-button
        v-else
        :disabled="disabled"
        size="small"
        type="primary"
        :class="{ text: actionType === 'text' }"
        class="root-cause-action-btn">
        <i class="db-icon db-icon-rootcause font-12"></i>
        {{ $t('modules.views.alarmCenter.rootCauseAnalysis.s_2a9e65b6') }}
        </el-button>
    </template>
  </el-popover>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui';
import { setDateBySeconds } from '@/utils/timeFormat';

@Component
export default class RootCausePopover extends Vue {
  @Prop({ default: false }) private disabled!: boolean;
  @Prop({ default: '' }) private start!: Date | string;
  @Prop({ default: '' }) private end!: Date | string;
  @Prop({ default: 'btn' }) private actionType!: 'btn' | 'text' | 'tag'; // 按钮样式

  public $refs!: {
    timeDateForm: Form,
  }

  private popoverVisible = false; // 是否显示根因分析popover

  // 分析范围
  private disabledFrom = new Date()
  private timeDateForm: any = {
    fromTime: '',
    toTime: '',
  }
  // 校验时间
  get timeDateRules () {
    const maxDuration = 2 * 60 * 60 * 1000 + 59 * 1000 // 2个小时
    return {
      fromTime: [
        { trigger: 'blur', validator: (rule: any, value: any, cb: any) => {
          if (!value) {
            cb(new Error(i18n.t('modules.views.alarmCenter.rootCauseAnalysis.s_90fae33b') as string))
          } else if (this.timeDateForm.toTime && value.getTime() > this.timeDateForm.toTime.getTime()) {
            cb(new Error(i18n.t('modules.views.alarmCenter.rootCauseAnalysis.s_9863e93b') as string))
          } else if (this.timeDateForm.toTime && this.timeDateForm.toTime.getTime() - value.getTime() > maxDuration) {
            cb(new Error(i18n.t('modules.views.alarmCenter.rootCauseAnalysis.s_e0ee3393') as string))
          } else {
            cb()
          }
        } },
      ],
      toTime: [
        { trigger: 'blur', validator: (rule: any, value: any, cb: any) => {
          if (!value) {
            cb(new Error(i18n.t('modules.views.alarmCenter.rootCauseAnalysis.s_69b76a1d') as string))
          } else if (this.timeDateForm.fromTime && value.getTime() < this.timeDateForm.fromTime.getTime()) {
            cb(new Error(i18n.t('modules.views.alarmCenter.rootCauseAnalysis.s_9402e822') as string))
          } else if (this.timeDateForm.fromTime && value.getTime() - this.timeDateForm.fromTime.getTime() > maxDuration) {
            cb(new Error(i18n.t('modules.views.alarmCenter.rootCauseAnalysis.s_e0ee3393') as string))
          } else {
            cb()
          }
        } },
      ],
    }
  }
  get pickerOptions () {
    const _start = +new Date(this.start)
    const _end = +new Date(this.end)
    return {
      disabledDate (time: Date) {
        return time.getTime() > _end || time.getTime() < _start
      }
    }
  }

  get serviceName () {
    const sid: string = decodeURIComponent(String(this.$route.query.sid))
    const basicServiceMap = this.$store.getters['Service/basicServiceMap']
    return basicServiceMap[sid] ? basicServiceMap[sid].name || '' : ''
  }

  // 重新触发一次表单校验，防止不同步
  private validateHandle () {
    this.$refs.timeDateForm.validate((valid) => {
      const { fromTime, toTime } = this.timeDateForm
      // 如果超过全局时间范围，自动适应最大最小时间
      if (toTime && toTime.getTime() > new Date(this.end).getTime()) {
        this.timeDateForm.toTime = new Date(this.end)
      } else {
        this.timeDateForm.toTime = setDateBySeconds(toTime, 0)
      }
      if (fromTime && fromTime.getTime() < new Date(this.start).getTime()) {
        this.timeDateForm.fromTime = new Date(this.start)
      } else {
        this.timeDateForm.fromTime = setDateBySeconds(fromTime, 0)
      }
    })
  }

  private showPopoverHandle () {
    const _start = +new Date(this.start)
    const _end = +new Date(this.end)
    const fifteen = 15 * 60 * 1000
    if (_end - fifteen > _start) {
      this.timeDateForm.fromTime = setDateBySeconds(_end - fifteen, 0)
    } else {
      this.timeDateForm.fromTime = new Date(_start)
    }
    this.timeDateForm.toTime = new Date(_end)
  }

  private hidePopoverHandle () {
    this.$refs.timeDateForm.resetFields()
  }

  // 跳转至根因分析
  private jumpRCAHandle () {
    const { fromTime, toTime } = this.timeDateForm
    // 如果超过全局时间范围，自动适应最大最小时间
    if (toTime && toTime.getTime() > new Date(this.end).getTime()) {
      this.timeDateForm.toTime = new Date(this.end)
    } else {
      this.timeDateForm.toTime = setDateBySeconds(toTime, 0)
    }
    if (fromTime && fromTime.getTime() < new Date(this.start).getTime()) {
      this.timeDateForm.fromTime = new Date(this.start)
    } else {
      this.timeDateForm.fromTime = setDateBySeconds(fromTime, 0)
    }

    this.$refs.timeDateForm.validate((valid) => {
      if (valid) {
        const _query: any = {
          fromTime: this.timeDateForm.fromTime.valueOf(),
          toTime: this.timeDateForm.toTime.valueOf(),
          sns: encodeURIComponent(this.serviceName),
        }
        this.$router.push({
          path: '/alarmCenter/rootCauseAnalysis',
          query: _query,
        });
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.time-date-form {
  line-height: 1.5;

  .time-form-item:last-child {
    margin-right: 0;
  }

  .time-choose-item {
    display: block;
    width: 170px;
  }

  :deep(.el-form-item__label) {
    padding: 0;
  }
}

.root-cause-action-btn {
  &.text {
    color: var(--color-text-link);
    font-size: inherit;
    font-weight: normal;
    border: none;
    padding: 0;
    &:hover {
      color: var(--color-text-link) !important;
    }
    &:deep(.is-disabled) {
      color: var(--color-text-secondary);
      pointer-events: none;
    }
  }
}

.disable-tag-btn {
  cursor: not-allowed;
}
</style>
