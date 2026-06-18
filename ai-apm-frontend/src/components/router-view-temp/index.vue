<template>
  <router-view />
</template>

<script lang="ts">
/**
 * 本组件只用来展示router-view，而不做任何处理，
 * 用于复用同功能的包裹层，
 * 如需特殊处理，请在业务中使用单独的index.vue
 */
import { Vue, Component } from 'vue-property-decorator';

@Component
export default class RouterViewTemp extends Vue {}
</script>
