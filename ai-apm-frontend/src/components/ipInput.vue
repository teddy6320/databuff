<template>
  <div class="ip-input-wrapper">
    <span v-for="(segment, index) in segments" :key="index">
      <input
        :class="['ip-input', disabled ? 'ip-input-disabled' : '']"
        :value="segment"
        :placeholder="placeholder"
        :disabled="disabled"
        maxlength="3"
        @keydown="onKeydownHandle($event, index)"
        @input="onInputHandle($event, index)"
        @paste="onPasteHandle($event, index)"
      />
      <span v-if="index !== segments.length - 1" class="ip-input-dot">.</span>
    </span>
  </div>
</template>

<script lang="ts">
  import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
  import Emitter from '@/mixins/emitter';

  @Component({
    mixins: [Emitter],
  })
  export default class IpInput extends Vue {
    @Prop({ default: '' }) private value!: string;
    @Prop({ default: '' }) private placeholder!: string;
    @Prop({ default: false }) private disabled!: boolean;
    @Prop() private input!: () => any;
    @Prop() private onChange!: () => any;

    private segments: string[] = ['', '', '', ''];

    get segmentsStr(): string {
      return this.segments.join('.');
    }

    @Watch('value')
    private onIpChange(value: string, oldValue: string) {
      this.syncIp(value);
    }

    @Watch('segmentsStr')
    private onSegmentsStrChange(value: string, oldValue: string) {
      if (value === '...') {
        value = '';
      }
      // v-model
      this.$emit('input', value);
      // 符合 iview 自定义事件
      this.$emit('on-change', value);
      // 自定义 iview 表单组件 必写该行
      (this as any).dispatch('FormItem', 'on-form-change', this.value);
    }

    private mounted() {
      this.syncIp(this.value);
    }

    private onKeydownHandle(event: any, index: number) {
      const keyCode = event.keyCode || event.which;
      const value = event.target.value;
      if (keyCode === 8 || keyCode === 37) {
        // 8 Backspace  37 向左箭头
        if ((value.length === 0 || this.getRange(event.target).end === 0) && index > 0) {
          this.moveCaretEnd(this.$el.getElementsByTagName('input')[index - 1]);
          event.preventDefault();
        }
      } else if (keyCode === 39) {
        // 39 向右箭头
        if (this.getRange(event.target).end === value.length && index < 3) {
          this.moveCaretStart(this.$el.getElementsByTagName('input')[index + 1]);
        }
      } else if (keyCode === 190) {
        // 190 英文 .
        if (value.length && index < 3) {
          this.moveCaretEnd(this.$el.getElementsByTagName('input')[index + 1]);
        }
        event.preventDefault();
      }
    }

    private onInputHandle(event: any, index: number) {
      const segment = event.target.value;
      event.target.value = this.segments[index];
      if (isNaN(+segment)) {
        return;
      } else if (segment === '') {
        this.segments.splice(index, 1, '');
      } else if (+segment < 0 || +segment > 255) {
        this.segments.splice(index, 1, '255');
      } else {
        this.segments.splice(index, 1, `${+segment}`);
      }

      if (segment.length === 3 && index < 3) {
        this.moveCaretEnd(this.$el.getElementsByTagName('input')[index + 1]);
      }
    }

    private onPasteHandle(event: any, index: number) {
      const pasteText: string = event.clipboardData.getData('text/plain');
      const segments = pasteText.split('.');
      segments.forEach((segment, i) => {
        if (index + i < 4 && !isNaN(+segment)) {
          if (segment === '') {
            this.segments.splice(index + i, 1, '');
          } else if (+segment < 0 || +segment > 255) {
            this.segments.splice(index + i, 1, '255');
          } else {
            this.segments.splice(index + i, 1, `${+segment}`);
          }
        }
      });
      event.preventDefault();
    }

    private syncIp(ip: string) {
      if (ip && ip.indexOf('.') !== -1) {
        ip.split('.').map((segment, index) => {
          if (index < 4 && !isNaN(+segment)) {
            if (segment === '') {
              this.segments.splice(index, 1, '');
            } else if (+segment < 0 || +segment > 255) {
              this.segments.splice(index, 1, '255');
            } else {
              this.segments.splice(index, 1, `${+segment}`);
            }
          }
        });
      } else {
        this.segments = ['', '', '', ''];
      }
    }

    private getRange(el: any) {
      const ret: any = {};
      if (el.setSelectionRange) {
        ret.begin = el.selectionStart;
        ret.end = el.selectionEnd;
        ret.result = el.value.substring(ret.begin, ret.end);
      }
      return ret;
    }

    private moveCaretStart(el: any) {
      el.focus();
      if (el.setSelectionRange) {
        setTimeout(() => {
          el.setSelectionRange(0, 0);
        }, 0);
      }
    }

    private moveCaretEnd(el: any) {
      el.focus();
      const len = el.value.length;
      if (el.setSelectionRange) {
        el.setSelectionRange(len, len);
      }
    }
  }
</script>

<style lang="scss" scoped>
  .ip-input-wrapper {
    width: 100%;
    display: inline-block;
    vertical-align: middle;
    line-height: normal;
  }
  .ip-input {
    width: 19.75%;
    height: 32px;
    padding: 4px 7px;
    border: 1px solid var(--border-color-base);
    border-radius: 4px;
    &:focus {
      outline: 0;
      border-color: var(--color-primary);
    }
    &.ip-input-disabled {
      background-color: var(--background-color-base);
      border-color: var(--border-color-light);
      cursor: not-allowed;
    }
  }
  .ip-input-dot {
    display: inline-block;
    vertical-align: top;
    width: 7%;
    height: 32px;
    line-height: 32px;
    text-align: center;
  }
</style>
