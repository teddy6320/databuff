<template>
  <div class="cont ovy-auto">
    <div class="wrapper">
      <db-tabnav v-model='activeName' :tabnavs='navs'></db-tabnav>

      <div class="main pt-16 pb-16">
        <Config :typeName="getTypeName" :type="activeName" />
      </div>
    </div>
  </div>
</template>

<script lang="ts">import i18n from '@/i18n';

import { Vue, Component, Prop, Watch } from 'vue-property-decorator'
import Config from './config.vue'

@Component({
  components: {
    Config,
  }
})
export default class HealthComp extends Vue {

  private activeName = 'HOST';
  private navs = [
    { value: 'HOST', label: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, labelKey: 'modules.views.alarmCenter.alarm.s_65227369',  },
    { value: 'SERVICE', label: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, labelKey: 'modules.views.alarmCenter.alarm.s_47d68cd0',  },
    { value: 'DB', label: i18n.t('modules.views.appMonitor.relationMapNew.s_68051bf4') as string, labelKey: 'modules.utils.filters.s_68051bf4',  },
    { value: 'MQ', label: i18n.t('modules.views.appMonitor.relationMapNew.s_8bedb7aa') as string, labelKey: 'modules.views.appMonitor.relationMapNew.s_8bedb7aa',  },
    { value: 'CACHE', label: i18n.t('modules.views.appMonitor.relationMapNew.s_e80c310e') as string, labelKey: 'modules.utils.filters.s_e80c310e',  },
    { value: 'REMOTECALL', label: i18n.t('modules.views.appMonitor.external.s_47921e9e') as string, labelKey: 'modules.views.appMonitor.external.s_47921e9e',  },
    { value: 'ENDPOINTS', label: i18n.t('modules.views.sysManage.health.s_54ea89b4') as string, labelKey: 'modules.views.observe.scene.s_54ea89b4',  },
    // { value: 'web', label: i18n.t('modules.views.appMonitor.serviceCall.s_ef367e82') as string, labelKey: 'modules.utils.filters.s_ef367e82',  },
    // { value: 'ios', label: i18n.t('modules.views.sysManage.health.s_f67a0ce2') as string, labelKey: 'modules.views.sysManage.health.s_f67a0ce2',  },
  ];

  get getTypeName() {
    const current = this.navs.find(item => item.value === this.activeName);
    return current ? current.label : '';
  }

  // 复制
  private async created () {
    // 指标数据
    if (this.$route.query.active) {
      const active = String(this.$route.query.active).toUpperCase();
      this.activeName = this.navs.some(item => item.value === active) ? active : 'HOST';
    }
    this.$store.dispatch('Common/GET_METRIC_TYPE_AND_LIST');
  }
}
</script>

<style lang="scss" scoped>
.cont{
  height: 100%;
}
.wrapper {
  height: 100%;
}
</style>