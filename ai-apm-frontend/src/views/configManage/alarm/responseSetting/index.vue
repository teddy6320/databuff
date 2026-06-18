<template>
  <div class="setting-wrapper">
    <div class="setting-content" v-loading="detailLoading">
      <el-form ref="settingForm" :model="settingForm" :rules="settingRules" size="small" label-position="left" label-width="80px" class="setting-form">
        <el-collapse class="db-setting-collapse" v-model='collapseData.value'>
          <el-collapse-item :title="$t('modules.views.infrastructure.hostDetail.s_9e5ffa06')" name='1'>
            <el-form-item :label="$t('modules.views.configManage.alarm.s_53cf4106')" prop="policyName">
              <el-input
                v-model="settingForm.policyName"
                :placeholder="$t('modules.views.configManage.alarm.s_687e9183')"
                :minlength="2"
                :maxlength="40"
                class="w550"
              ></el-input>
            </el-form-item>
            <el-form-item :label="$t('modules.views.configManage.alarm.s_2b82bf9a')" prop="enabled">
              <el-switch v-model="settingForm.enabled"></el-switch>
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item :title="$t('modules.views.configManage.alarm.s_97bdde57')" name='2'>
            <el-form-item :label="$t('modules.views.configManage.alarm.s_67a30e47')" prop="filterType">
              <el-select
                v-model="settingForm.filterType"
                filterable :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
                class="w550">
                <el-option :label="$t('modules.views.configManage.alarm.s_c8864c6e')" value="all"></el-option>
                <el-option :label="$t('modules.views.configManage.alarm.s_efd1b65f')" value="filter"></el-option>
              </el-select>

              <matching-criteria
                v-if="settingForm.filterType === 'filter' && !detailLoading"
                ref="filterCriteria"
                :conditionData="settingForm.filterConditions"
                :fieldData="fieldData"
                :addMultipleText="$t('modules.views.configManage.alarm.s_c9ee9e43')"
                :maxLevel="10"
                :showView="true"
                @on-change="conditionChangeHandle($event, 'filterConditions')"
                class="mt-10"
              />
            </el-form-item>

            <div class="df">
              <div class="w80 lh-32">{{ $t('modules.views.configManage.alarm.s_7004e95a') }}</div>
              <div class="pt-5">
                <el-checkbox v-model="settingForm.firstNotify" class="lh-22 mb-5">{{ $t('modules.views.configManage.alarm.s_eeae27b6') }}</el-checkbox><br>

                <el-checkbox v-model="settingForm.sameNotify" class="lh-22 mb-5">{{ $t('modules.views.configManage.alarm.s_5ac09f1f') }}</el-checkbox><br>
                <el-form-item
                  v-if="settingForm.sameNotify"
                  prop="interval" label="" :show-message="false"
                  class="form-inline-item pl-20 pb-5">
                  <div class="flex-h">
                    {{ $t('modules.views.configManage.alarm.s_406bec76') }} <el-input-number
                      v-model="settingForm.interval"
                      :controls="false" :min="1" :max="999" :precision="0"
                      class="w80 ml-5 mr-5"
                    ></el-input-number> {{ $t('modules.views.configManage.alarm.s_495021a9') }}
                  </div>
                </el-form-item>

                <div class="flex-h">
                  <el-checkbox v-model="settingForm.restoreNotify" class="lh-22">{{ $t('modules.views.configManage.alarm.s_493a91a9') }}</el-checkbox>
                  <el-tooltip class="ml-10" content="" placement="right" effect="light">
                    <i class="db-icon-info describe"></i>
                    <div slot="content">
                      <div>{{ $t('modules.views.configManage.alarm.s_93aef95a') }}</div>
                      <div class="mt-5">{{ $t('modules.views.configManage.alarm.s_b6224292') }}</div>
                    </div>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </el-collapse-item>

          <el-collapse-item :title="$t('modules.views.alarmCenter.alarmDetail.s_d15d9c61')" name='3'>
            <el-form-item :label="$t('modules.views.configManage.alarm.s_67a30e47')" prop="actionType">
              <el-select
                v-model="settingForm.actionType"
                filterable :placeholder="$t('modules.views.metrics.list.s_708c9d6d')"
                disabled
                class="w550">
                <el-option :label="$t('modules.views.configManage.alarm.s_fd6d4c58')" :value="1"></el-option>
                <!-- <el-option :label="$t('modules.views.configManage.alarm.s_c0c998cd')" :value="2"></el-option> -->
              </el-select>
            </el-form-item>

            <el-form-item :label="$t('modules.views.configManage.alarm.s_9fd00f0a')">
              <notice-config
                ref="noticeConfig"
                :initConfig="settingForm.respActions"
                @notice-enable="enable => hasNoticeEnable = enable"
                class="mt-5" />
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>

      <div class="mt-20">
        <el-button :disabled="postLoading" size="small" @click="cancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
        <el-button v-if="_hasAlarmManageAuth || !isEdit" :loading="postLoading" type="primary" size="small" @click="saveHandle">{{ $t('modules.views.configManage.alarm.s_769d88e4') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui'
import deepClone from 'lodash/cloneDeep';
import MatchingCriteria from '@/components/matching-criteria/index.vue'
import NoticeConfig from './notice-receiver-config.vue'
import { toAsyncWait } from '@/utils/common'
import AlarmApi from '@/api/alarm';
import MetricApi from '@/api/metric';
import { buildAlarmListLocation } from '../alarm-routes';

@Component({
  components: {
    MatchingCriteria,
    NoticeConfig,
  }
})
export default class ResponseSetting extends Vue {
  public $refs!: {
    settingForm: Form
    filterCriteria: MatchingCriteria
    noticeConfig: NoticeConfig
  }

  get isEdit () {
    return this.detail && this.detail.id
  }
  get isCopy () {
    return this.detail && !this.detail.id
  }

  get _hasAlarmManageAuth () {
    return this.hasAlarmManageAuth(this.detail || {})
  }

  private routerTimer: any = null;

  private detail: any = null;
  private detailLoading = false;
  private postLoading = false;
  private hasNoticeEnable: boolean = true // 是否有通知权限

  private settingForm: any = {
    policyName: '', // 策略名称
    enabled: true, // 启停状态
    filterType: 'all', // 筛选事件类型
    filterConditions: [], // 筛选条件
    firstNotify: true, // 新告警产生时触发
    sameNotify: false, // 告警持续时，重复触发
    restoreNotify: false, // 告警恢复时触发
    interval: '', // 重复触发间隔
    actionType: 1, // 响应动作类型
    respActions: [], // 通知方式
  }
  get settingRules(): any {
    const validateInputText = (rule: any, value: string, callback: any) => {
      if (!value.trim()) {
        callback(new Error(rule.message || i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string));
      } else {
        callback();
      }
    };
    return {
      policyName: [
        { required: true, validator: validateInputText, message: i18n.t('modules.views.configManage.alarm.s_bb922a78') as string, messageKey: 'modules.views.configManage.alarm.s_bb922a78', trigger: 'blur' },
        { required: true, validator: validateInputText, message: i18n.t('modules.views.configManage.alarm.s_bb922a78') as string, messageKey: 'modules.views.configManage.alarm.s_bb922a78', trigger: 'change' },
        { min: 2, max: 40, message: i18n.t('modules.views.configManage.alarm.s_b6d4b40c') as string, messageKey: 'modules.views.configManage.alarm.s_b6d4b40c', trigger: 'blur' },
        { min: 2, max: 40, message: i18n.t('modules.views.configManage.alarm.s_b6d4b40c') as string, messageKey: 'modules.views.configManage.alarm.s_b6d4b40c', trigger: 'change' },
      ],
      interval: { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f', type: 'number' },
    }
  }

  private collapseData: any = { // 折叠面板展开控制
    value: ['1', '2', '3'],
    1: ['policyName'],
    2: [],
    3: [],
  }

  private async created() {
    const { id, mode } = this.$route.query
    // 设置面包屑
    this.$nextTick(() => {
      this.$store.commit('UPDATE_BREADCRUMB', [{
        name: i18n.t('modules.views.configManage.alarm.s_7f3cf73d', { value0: id && mode !== 'c' ? i18n.t('modules.views.configManage.alarm.s_95b351c8') as string : i18n.t('modules.views.configManage.alarm.s_26bb8418') as string }) as string,
        path: this.$route.path,
      }]);
    });

    await this.$store.dispatch('Common/GET_TAG_LABEL_MAP');

    if (id) {
      this.detailLoading = true;
      const { result, error } = await toAsyncWait(AlarmApi.getResponseDetail({ id: (id as string) }))
      this.detailLoading = false;
      if (error || !result.data) {
        this.$message.error(i18n.t('modules.views.configManage.alarm.s_d3e1114c') as string);
        this.routerTimer = setTimeout(() => {
          this.closeHandle(true)
        }, 2500)
        return;
      }
      if (mode === 'c') {
        delete result.data.id
      }
      this.detail = result.data
      this.initForm()
    }
  }

  private beforeDestroy() {
    // 清空面包屑
    this.$store.commit('CLEAR_BREADCRUMB');

    if (this.routerTimer) {
      window.clearTimeout(this.routerTimer);
      this.routerTimer = null;
    }
  }

  private initForm () {
    for (const key in this.settingForm) {
      if (this.detail.hasOwnProperty(key)) {
        if (key === 'filterConditions') {
          const filterData = this.detail.filterConditions || []
          this.settingForm.filterType = filterData.length ? 'filter' : 'all'
          this.settingForm.filterConditions = filterData
        } else if (!['firstNotify', 'restoreNotify', 'sameNotify', 'interval'].includes(key)) {
          this.settingForm[key] = this.detail[key]
        }
      }
    }

    const respConditions: any[] = (this.detail.respConditions || []).filter((t: any) => t.way)
    const sameNotify = respConditions.find(t => t.way === 'sameNotify')
    this.settingForm.firstNotify = !!respConditions.find(t => t.way === 'firstNotify')
    this.settingForm.restoreNotify = !!respConditions.find(t => t.way === 'restoreNotify')
    this.settingForm.sameNotify = !!sameNotify
    this.settingForm.interval = (sameNotify || {}).interval || ''
  }

  private saveHandle () {
    if (!this.settingForm.policyName) {
      // 滚动至上方
      document.querySelector('.setting-wrapper')!.scrollTop = 0
    }

    this.$refs.settingForm.validate(async (valid: boolean, fields: any) => {
      if (valid) {
        const filterValid = this.$refs.filterCriteria ? this.$refs.filterCriteria.validate() : true
        if (!filterValid) {
          this.collapseData.value = [...new Set([...this.collapseData.value, '2'])]
          this.$message.error(i18n.t('modules.views.configManage.alarm.s_7e03cb7f') as string)
          document.querySelector('.setting-wrapper')!.scrollTop = 0
          return
        }
        const params: any = deepClone(this.settingForm)
        if (params.filterType !== 'filter') {
          params.filterConditions = []
        }
        params.respConditions = []
        if (params.firstNotify) {
          params.respConditions.push({ way: 'firstNotify' })
        }
        if (params.sameNotify) {
          params.respConditions.push({ way: 'sameNotify', interval: params.interval })
        }
        if (params.restoreNotify) {
          params.respConditions.push({ way: 'restoreNotify' })
        }
        delete params.filterType
        delete params.firstNotify
        delete params.sameNotify
        delete params.restoreNotify
        delete params.interval
        const noticeConfig = (this.$refs.noticeConfig as NoticeConfig).getData()
        if (noticeConfig.errorMsg) {
          this.collapseData.value = [...new Set([...this.collapseData.value, '3'])]
          this.$message.warning(noticeConfig.errorMsg)
          return
        } else {
          params.respActions = noticeConfig.data
        }
        if (this.isEdit) {
          params.id = this.detail.id
        }
        this.postLoading = true;
        const { result, error } = await toAsyncWait(AlarmApi.saveResponse(params))
        this.postLoading = false;
        if (!error) {
          this.$message.success(this.isEdit ? i18n.t('modules.views.configInstall.dataAccess.s_55aa6366') as string : i18n.t('modules.views.configManage.alarm.s_3fdaeadf') as string);
          this.closeHandle()
        } else {
          this.$message.error(error.message || i18n.t('modules.views.cockpit.tab.s_6de920b4') as string);
        }
      } else {
        const fieldList = Object.keys(fields);
        const collapseValue = [...this.collapseData.value];
        for (const key in this.collapseData) {
          const collapseItem = this.collapseData[key];
          if (key !== 'value' && fieldList.some((t) => collapseItem.includes(t))) {
            collapseValue.push(key);
          }
        }
        this.collapseData.value = [...new Set(collapseValue)];
      }
    })
  }

  private cancelHandle () {
    if (!this._hasAlarmManageAuth) {
      this.closeHandle()
      return
    }
    this.$confirm(i18n.t('modules.views.configInstall.dataAccess.s_6e1ca07e') as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(() => this.closeHandle())
      .catch(() => null)
  }

  private closeHandle(isReplace?: boolean) {
    this.$router[!isReplace ? 'push' : 'replace'](buildAlarmListLocation('rule', this.$route.query))
  }

  get allFieldData () {
    const fieldData: any = {
      description: { value: 'description', label: i18n.t('modules.views.alarmCenter.alarm.s_606a249f') as string, labelKey: 'modules.views.alarmCenter.alarm.s_606a249f', enabled: true, },
      level: { value: 'level', label: i18n.t('modules.views.alarmCenter.alarm.s_ed7094f4') as string, labelKey: 'modules.views.alarmCenter.alarm.s_ed7094f4', enabled: true,
        options: [
          { label: i18n.t('modules.views.alarmCenter.alarm.s_fc7e3846') as string, labelKey: 'modules.utils.filters.s_fc7e3846', value: '3' },
          { label: i18n.t('modules.views.alarmCenter.alarm.s_bde77082') as string, labelKey: 'modules.utils.filters.s_bde77082', value: '2' },
          { label: i18n.t('modules.views.alarmCenter.alarm.s_01ceb3ed') as string, labelKey: 'modules.utils.filters.s_01ceb3ed', value: '1' },
        ],
      },
      ruleName: { value: 'ruleName', label: i18n.t('modules.views.configInstall.plugin.s_b4c5a9d9') as string, labelKey: 'modules.views.alarmCenter.alarm.s_b4c5a9d9', enabled: true, },
      classification: { value: 'classification', label: i18n.t('modules.views.appMonitor.serviceDetail.s_986329a3') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_986329a3', enabled: true,
        options: [
          { label: i18n.t('modules.views.configManage.alarm.s_150b3f91') as string, labelKey: 'modules.utils.filters.s_150b3f91', value: 'singleMetric' },
        ],
      },
    }
    const tagLabelMap = this.$store.getters['Common/tagLabelMap']
    Object.entries(tagLabelMap || {}).filter(([key]: any) => !fieldData[key]).forEach(([key, item]: any) => {
      const options = Object.entries(item.tagValue || {}).map(t => ({
        label: t[1],
        value: t[0],
      }))
      fieldData[key] = { value: key, label: item.name, enabled: !!item.enabled, options }
    })
    return fieldData;
  }
  get fieldData () {
    const data: any = {}
    const fields = this.getDetailFields((this.detail || {}).filterConditions || [])
    const fieldData = Object.entries(this.allFieldData).filter(([key, value]: any) => value.enabled || fields.includes(key))
    fieldData.forEach((item: any) => {
      const value = { ...item[1] }
      delete value.enabled
      data[item[0]] = value
    })
    return data
  }
  private conditionChangeHandle (conditions: any, field: string) {
    this.settingForm[field] = conditions.data
  }

  public getDetailFields (data: any[]) {
    const fields: string[] = []
    data.forEach((item) => {
      if (!Array.isArray(item.left)) {
        fields.push(item.left)
      } else if (item.left.length) {
        const subFields = this.getDetailFields(item.left)
        fields.push(...subFields)
      }
    })
    return Array.from(new Set(fields))
  }
}
</script>

<style lang="scss" scoped>
.setting-wrapper {
  flex: 1;
  padding: 16px;
  height: 100%;
  font-size: 13px;
  color: var(--color-text-primary);
  overflow: auto;

  .setting-content {
    display: flex;
    flex-direction: column;
    min-height: 100%;
    padding: 20px;
    border-radius: 4px;
    background-color: var(--bg-color);
  }
}

.setting-form {
  :deep(.el-input.is-disabled .el-input__inner) {
    color: inherit;
  }
  :deep(.el-input-number .el-input__inner) {
    text-align: left;
  }

  .w550 {
    width: 550px;
  }

  .w80 {
    width: 80px;
  }


  .form-inline-item {
    margin-bottom: 0;
    line-height: 1;
    :deep(.el-form-item__content) {
      margin-left: 0 !important;
      line-height: 1;
      display: flex;
      align-items: center;
    }
    :deep(.el-form-item__error) {
      display: none;
    }
  }
}
</style>
