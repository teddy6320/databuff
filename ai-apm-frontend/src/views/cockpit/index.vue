<template>
  <div class="cockpit-cont">
    <div class="wrapper p-16">
      <Fault />
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator'
import Fault from './tab/fault/index.vue';

@Component({
  components: {
    Fault,
  },
})
export default class Cockpit extends Vue {}
</script>

<style lang="scss" scoped>
.cockpit-cont {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.wrapper {
  flex: 1;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
}
</style>
