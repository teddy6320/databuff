<template>
  <div class="update-step-4">
    <div v-if='!hasFailed' class="update-step-success">
      <div class="mb-10">{{ $t('modules.views.configStatus.agent.s_4f4aef69') }}</div>
      <div class="font-13">{{ $t('modules.views.configStatus.agent.s_e62533f8') }}</div>
    </div>

    <div v-else class="update-step-failed">
      <div class="mb-10">{{ $t('modules.views.configStatus.agent.s_c2992090') }}</div>
      <div class="font-13">{{ $t('modules.views.configStatus.agent.s_6714dc2e') }}</div>
      <div v-for="fail, index in updated.failed" :key='index' class="font-13">
        {{ index + 1 }}. {{ fail.host }} -- {{ fail.progress }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator'

@Component({})
export default class UpdateStep4 extends Vue {
  @Prop({ default: () => [] }) private updated!: any;

  get hasFailed () {
    return this.updated && this.updated.failed && this.updated.failed.length
  }

  private mounted () {
    if (!this.hasFailed) {
      this.$emit('on-finish')
    }
  }
}
</script>
