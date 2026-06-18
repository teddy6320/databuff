<template>
  <el-dialog
    :visible.sync='showDialog' :title='title' width="800px" modal-append-to-body append-to-body destroy-on-close
    :before-close="cancelHandle">
    <div class="mb-10">
      <div class="lh-26 pb-4">{{ $t('modules.views.configManage.alarm.s_2b82bf9a') }}</div>
      <el-switch v-model='configForm.enable' :active-value="1" :inactive-value="0"></el-switch>
    </div>

    <div class="mb-10">
      <div class="lh-26 pb-4">{{ $t('modules.views.sysManage.group.s_aaa2e45d') }}</div>
      <el-select v-model="configForm.type" :disabled="canToggleType" @change="entityTypeChangeHandle" size="small" :placeholder="$t('modules.views.sysManage.group.s_aaa2e45d')">
        <el-option
          v-for="item in entityTypes"
          :key="item.value"
          :label="item.label"
          :value="item.value">
        </el-option>
      </el-select>
    </div>

    <div class="mb-10">
      <div class="lh-26 pb-4">{{ $t('modules.views.sysManage.group.s_0a984e47') }}</div>
      <config-rule v-if='showDialog' ref='configRule' :showSubConfig="false" :singleModel='true' :symbols='symbolList' :andors="andorList"
        :detailView='true' :conditionData="initRules"
        :fieldData='fieldData' @on-change='ruleChangeHandle' />
    </div>

    <!-- 服务实体独有条件 -->
    <div v-show='configForm.type === "service"' class="mt-10">
      <div class="lh-26 pb-4">{{ $t('modules.views.sysManage.group.s_de20a9a0') }}</div>
      <el-switch v-model='svcRelaHostModel' :active-value="1" :inactive-value="0"></el-switch>
    </div>

    <div slot="footer">
      <el-button size="small" :disabled="postLoading" @click="cancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button size="small" :loading="postLoading" :disabled='postLoading' type="primary" @click="confirmHandle">{{ $t('modules.components.dialog-template.s_38cf16f2') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang='ts'>import i18n from '@/i18n';

import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import ConfigRule from '@/components/matching-criteria/index.vue'
import { FieldData } from '@/components/matching-criteria/index.types'
import GroupApi from '@/api/group'

@Component({
  components: {
    ConfigRule
  }
})
export default class GroupManageModal extends Vue {
  @Prop() private title!: string;
  @Prop() private showModel!: boolean
  @Prop() private curr!: any
  @Prop() private group!: any

  public $refs!: {
    configRule: ConfigRule
  }

  @Watch('showModel')
  private onShowModelChange (newVal: boolean) {
    this.showDialog = newVal
  }

  @Watch('curr')
  private onCurrChange (newVal: any) {
    if (newVal && this.showDialog) {
      const { params = '[]', type = '', enable, groupId, formatted } = newVal || {}
      this.configForm.enable = enable
      this.configForm.type = type
      this.configForm.groupId = groupId
      this.configForm.params = params
      this.configForm.formatted = formatted
      this.entityTypeChangeHandle(type)
      try {
        const _initParams: any[] = JSON.parse(params)
        this.initRules = _initParams.filter(i => i.left !== 'svcRelaHost')
        const hasSvcRelationRule = _initParams.some((i) => i.left === 'svcRelaHost')
        this.svcRelaHostModel = hasSvcRelationRule ? 1 : 0
      } catch {
        //
      }
    }
  }

  get canToggleType () {
    const { data } = this.rulePayload
    const [ first = {} ] = data || []
    if (this.configForm.type && data.length && first.left) {
      return true
    } else {
      return false
    }
  }

  private entityTypes = [
    { label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369', value: 'host' },
    { label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0', value: 'service' },
    // { label: 'Kubernetes cluster', value: 'cluster' },
    { label: 'Kubernetes NameSpace', value: 'namespace' },
    // { label: 'Kubernetes WorkLoad', value: 'workload' },
  ]

  private symbolList = [
    { label: i18n.t('modules.components.matching-criteria.s_4c35bf2e') as string, labelKey: 'modules.components.matching-criteria.s_4c35bf2e', value: '=' },
    { label: i18n.t('modules.components.matching-criteria.s_14a8af58') as string, labelKey: 'modules.components.matching-criteria.s_14a8af58', value: '!=' },
    { label: i18n.t('modules.components.matching-criteria.s_e13556bb') as string, labelKey: 'modules.components.matching-criteria.s_e13556bb', value: 'like' },
    { label: i18n.t('modules.components.matching-criteria.s_da0291f4') as string, labelKey: 'modules.components.matching-criteria.s_da0291f4', value: 'notLike'},
    { label: i18n.t('modules.views.configManage.alarm.s_af5c8737') as string, labelKey: 'modules.views.configManage.alarm.s_af5c8737', value: 'startWith'},
    { label: i18n.t('modules.views.configManage.alarm.s_bd20bafe') as string, labelKey: 'modules.views.configManage.alarm.s_bd20bafe', value: 'endWith'},
    { label: i18n.t('modules.components.matching-criteria.s_4af4af44') as string, labelKey: 'modules.components.matching-criteria.s_4af4af44', value: 'in'},
    { label: i18n.t('modules.components.matching-criteria.s_33a76148') as string, labelKey: 'modules.components.matching-criteria.s_33a76148', value: 'notIn'},
  ];

  private andorList = [
    { label: 'AND', value: 'AND' },
  ];

  private hostFieldData: FieldData = {
    host_name: { value: 'host_name', label: i18n.t('modules.views.configManage.alarm.s_3d022a63') as string, labelKey: 'modules.views.configManage.alarm.s_3d022a63', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith', 'in', 'notIn']  },
    ipaddress: { value: 'ipaddress', label: i18n.t('modules.views.configManage.entity.s_2dc9105c') as string, labelKey: 'modules.views.configManage.entity.s_2dc9105c', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith', 'in', 'notIn'] },
    tags: { value: 'tags', label: i18n.t('modules.views.configManage.entity.s_5e17f4d3') as string, labelKey: 'modules.views.configManage.entity.s_5e17f4d3', symbols: ['=', '!='] },
    goos: { value: 'goos', label: i18n.t('modules.views.sysManage.group.s_97bbcf0e') as string, labelKey: 'modules.views.sysManage.group.s_97bbcf0e', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
    os: { value: 'os', label: i18n.t('modules.views.infrastructure.host.s_30d23ef4') as string, labelKey: 'modules.views.appMonitor.traceDetail.s_30d23ef4', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
    user_agent: { value: 'user_agent', label: i18n.t('modules.views.infrastructure.host.s_7714bc11') as string, labelKey: 'modules.views.infrastructure.host.s_7714bc11', symbols: ['=', '!='] },
  }

  private serviceFieldData: FieldData = {
    name: { value: 'name', label: i18n.t('modules.views.alarmCenter.alarm.s_8f3747c0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_8f3747c0', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith', 'in', 'notIn'] },
    service: { value: 'service', label: i18n.t('modules.views.sysManage.group.s_3a06d4a1') as string, labelKey: 'modules.views.appMonitor.serviceDetail.s_3a06d4a1', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith', 'in', 'notIn'] },
    tags: { value: 'tags', label: i18n.t('modules.views.sysManage.group.s_c33ad4b2') as string, labelKey: 'modules.views.sysManage.group.s_c33ad4b2', symbols: ['=', '!='] },
  }

  private clusterFieldData: FieldData = {
    clusterName: { value: 'clusterName', label: i18n.t('modules.views.sysManage.group.s_2797943f') as string, labelKey: 'modules.views.sysManage.group.s_2797943f', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
    clusterId: { value: 'clusterId', label: 'Cluster ID', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
  }

  private namespaceFieldData: FieldData = {
    name: { value: 'name', label: i18n.t('modules.views.sysManage.group.s_291f2f45') as string, labelKey: 'modules.views.sysManage.group.s_291f2f45', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
    clusterName: { value: 'clusterName', label: i18n.t('modules.views.sysManage.group.s_2797943f') as string, labelKey: 'modules.views.sysManage.group.s_2797943f', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
    clusterId: { value: 'clusterId', label: 'Cluster ID', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
    labels: { value: 'labels', label: 'k8s Label', symbols: ['=', '!=', 'like', 'notLike'] },
  }
  
  private workloadFieldData: FieldData = {
    name: { value: 'name', label: i18n.t('modules.views.sysManage.group.s_ccd09c36') as string, labelKey: 'modules.views.sysManage.group.s_ccd09c36', symbols: ['=', '!=', 'like', 'notLike', 'startWith', 'endWith'] },
    type: { value: 'type', label: i18n.t('modules.views.sysManage.group.s_e618db93') as string, labelKey: 'modules.views.sysManage.group.s_e618db93', symbols: ['=', '!='] },
    labels: { value: 'labels', label: 'K8s Label', symbols: ['=', '!=', 'like', 'notLike'] },
  }

  private fieldData = {}

  private showDialog = false
  private postLoading = false

  private configForm = {
    enable: 1,
    type: '',
    params: '',
    groupId: null,
    formatted: '',
  }

  private rulePayload: any = {
    data: [],
    viewCharStrArr: [],
    viewStr: '',
  }

  private svcRelaHostModel = 0

  private initRules: any[] = []

  private entityTypeChangeHandle (type: string) {
    switch (type) {
      case 'host':
        this.fieldData = this.hostFieldData
        break
      case 'service':
        this.fieldData = this.serviceFieldData
        break
      case 'cluster':
        this.fieldData = this.clusterFieldData
        break
      case 'namespace':
        this.fieldData = this.namespaceFieldData
        break
      case 'workload':
        this.fieldData = this.workloadFieldData
        break
      default:
        this.fieldData = {}
        break
    }
  }

  private cancelHandle () {
    this.postLoading = false
    this.$emit('on-close')
    this.resetHandle()
    this.showDialog = false
  }

  private resetHandle () {
    this.configForm = {
      enable: 1,
      type: '',
      params: '',
      groupId: null,
      formatted: '',
    }
    this.rulePayload = {
      data: [],
      viewCharStrArr: [],
      viewStr: '',
    }
    this.initRules = []
    this.svcRelaHostModel = 0
    this.fieldData = {}
  }

  private confirmHandle () {
    const isRuleComplete = this.$refs.configRule.validate()
    if (!isRuleComplete || !this.configForm.type) {
      return
    }
    if (!this.rulePayload.data.length) {
      this.$message.info(i18n.t('modules.views.sysManage.group.s_97109d18') as string)
      return
    }
    this.postLoading = true
    // 格式rulePayload
    const _rules = [...this.rulePayload.data]
    this.configForm.params = JSON.stringify(this.rulePayload.data)
    this.configForm.groupId = this.group.id
    const succMsg = this.curr ? i18n.t('modules.views.configInstall.dataAccess.s_55aa6366') as string : i18n.t('modules.views.sysManage.group.s_a5bfd70d') as string
    const errMsg = this.curr ? i18n.t('modules.views.appMonitor.serviceAnalysis.s_930442e2') as string : i18n.t('modules.views.sysManage.group.s_bac372f6') as string
    if (this.svcRelaHostModel && this.configForm.type === 'service') {
      _rules.push({
        connector: 'AND',
        left: 'svcRelaHost',
        operator: 'match',
        right: 'true',
      })
    }
    const params: any = this.curr ?
      { ...this.configForm, params: JSON.stringify(_rules), id: this.curr.id } :
      { ...this.configForm, params: JSON.stringify(_rules) }


    const fetchApi = this.curr ? GroupApi.updateRule : GroupApi.addRule
    fetchApi({ ...params })
      .then((res: any) => {
        if (res.message.toLowerCase() === 'success' && res.status === 200) {
          this.$message.success(succMsg)
          this.cancelHandle()
        } else {
          this.$message.error(res.message || errMsg)
        }
      })
      .catch(() => {
        this.$message.error(errMsg)
      })
      .finally(() => {
        this.postLoading = false
      })
  }

  private ruleChangeHandle (payload: any) {
    this.rulePayload = payload
    const { viewCharStrArr = [] } = payload
    const typeOption = this.entityTypes.find((i) => i.value === this.configForm.type)
    if (typeOption) {
      const typeStr = typeOption ? typeOption.label : ''
      const conditionStr = viewCharStrArr.map((i: string) => i.substring(3)).join(' and ')
      const formatted = `${typeStr} where ( ${conditionStr} )`
      this.configForm.formatted = formatted
    }
  }
}
</script>
