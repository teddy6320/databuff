<template>
  <div class="service-cont flex-v">
    <!-- 服务流拓扑图 -->
    <service-flow v-if='noExpireLimit' />
    <div v-else class="describe expire-limit-info">
      {{ $t('modules.views.alarmCenter.rootCause.s_4f216394') }}
    </div>
  </div>
</template>
<script lang="ts">
  import { Vue, Component } from 'vue-property-decorator';
  import ServiceFlow from './components/service-flow.vue';

  @Component({
    components: {
      ServiceFlow,
    },
  })
  export default class ServiceFlowManage extends Vue {

    get noExpireLimit () {
      const expireLimit = this.$store.getters['User/isExpireLimit']
      const authFinalStatus = this.$store.state.finalStatus
      return authFinalStatus === 0 || (authFinalStatus === 2 && !expireLimit)
    }

  }
</script>
<style lang="scss" scoped>
.service-cont {
  flex: 1;
  padding: 16px;
  height: 100%;
  overflow: hidden;
  position: relative;
}
</style>
