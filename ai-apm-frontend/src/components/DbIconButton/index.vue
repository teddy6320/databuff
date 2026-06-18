<template>
  <div :class='["db-icon-button", getClass]' :title="describe">
    <span :class='["db-icon", getIconClass]'></span>
  </div>
</template>

<script lang='ts'>
import { Vue, Component, Prop } from 'vue-property-decorator';

@Component
export default class DbIconButton extends Vue {
  @Prop() private icon!: string;
  @Prop() private describe!: string;
  @Prop({ default: 'default' }) private size!: 'large'|'default'|'small'
  @Prop({ default: true }) private border?: boolean;

  get getClass () {
    return [`db-icon-button-${this.size}`, this.border ? '' : 'no-border'].join(' ')
  }

  get getIconClass () {
    const icon = this.icon.indexOf('db-icon-') === -1 ? `db-icon-${this.icon}` : this.icon;
    return [`db-icon-size-${this.size}`, icon].join(' ')
  }

  private clickHandle () {
    this.$emit('click')
  }
}
</script>
<style lang='scss' scoped>
.db-icon-button {
  --color-border-normal: #DFE0E2;
  --color-text-active: #2962FF;
  border-radius: 4px;
  display: inline-flex;
  align-items: stretch;
  border: 1px solid transparent;
  padding: 7px;
  cursor: pointer;
  &:not(.no-border) {
    border-color: var(--color-border-normal);
    &:active {
      border-color: var(--color-text-active);
    }
  }

  &-large {
    padding: 9px;
  }
  &-small {
    padding: 5px;
  }
  &-mini {
    padding: 3px;
  }


  &:active {
    .db-icon {
      color: var(--color-text-active);
    }
  }

  .db-icon {
    font-size: 16px;
    line-height: 1;
    
    &.db-icon-size-large {
      font-size: 20px;
    }
    &.db-icon-size-small {
      font-size: 12px;
    }
    &.db-icon-size-mini {
      font-size: 12px;
    }
  }
}
</style>