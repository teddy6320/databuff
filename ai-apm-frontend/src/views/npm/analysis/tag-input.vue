<template>
  <div class="tag-input-wrapper">
    <div
      @click="toggleConnectorHandle"
      class="connector">{{ query.connector }}</div>

    <div class="tag-input">
      {{ $t('modules.views.npm.analysis.s_5439441f') }}
      <el-popover
        v-model="clientVisible"
        :visible-arrow="false"
        placement="bottom-start"
        width="200"
        trigger="focus"
        popper-class="npm-tag-input-popover"
        class="query-input-wrap">
        <el-input
          ref="client"
          v-model="query.client"
          @change="changeHandle"
          @blur="changeHandle"
          @keyup.enter.native="enterHandle"
          slot="reference"
          clearable
          size="small"
          maxlength="100"
          prefix-icon="db-icon-search"
          :placeholder="$t('modules.views.alarmCenter.alarmDetail.s_d1f0b009')"
          class="query-input"
        />
        <div class="option-list">
          <div
            v-for="t in clientList"
            :key="t.value"
            @click="tagChangeHandle(t.value, 'client')"
            class="option">
            <i class="prefix-icon el-icon-search"></i> {{ t.labelKey ? $t(t.labelKey) : t.label }}
          </div>
        </div>
      </el-popover>
    </div>

    <div class="tag-input mt-10">
      {{ $t('modules.views.npm.analysis.s_d9aec692') }}
      <el-popover
        v-model="serverVisible"
        :visible-arrow="false"
        placement="bottom-start"
        width="200"
        trigger="focus"
        popper-class="npm-tag-input-popover"
        class="query-input-wrap">
        <el-input
          ref="server"
          v-model="query.server"
          @change="changeHandle"
          @blur="changeHandle"
          @keyup.enter.native="enterHandle"
          slot="reference"
          clearable
          size="small"
          maxlength="100"
          prefix-icon="db-icon-search"
          :placeholder="$t('modules.views.alarmCenter.alarmDetail.s_d1f0b009')"
          class="query-input"
        />
        <div class="option-list">
          <div
            v-for="t in serverList"
            :key="t.value"
            @click="tagChangeHandle(t.value, 'server')"
            class="option">
            <i class="prefix-icon el-icon-search"></i> {{ t.labelKey ? $t(t.labelKey) : t.label }}
          </div>
        </div>
      </el-popover>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import { Input } from 'element-ui';

@Component
export default class TagInput extends Vue {
  @Prop({ default: false }) private isDns!: boolean;

  public $refs!: {
    client: Input
    server: Input
  }

  private clientList = [
    { label: 'srcCname', value: 'srcCname' },
    { label: 'srcHostname', value: 'srcHostname' },
    { label: 'srcIp', value: 'srcIp' },
    { label: 'srcPodName', value: 'srcPodName' },
    // { label: 'srcPort', value: 'srcPort' },
    { label: 'srcService', value: 'srcService' },
  ];

  get serverList () {
    return !this.isDns ? [
      { label: 'cname', value: 'cname' },
      { label: 'hostname', value: 'hostname' },
      { label: 'ip', value: 'ip' },
      { label: 'podName', value: 'podName' },
      { label: 'ip:port', value: 'ip:port' },
      { label: 'service', value: 'service' },
    ] : [
      { label: 'ip', value: 'ip' },
    ];
  };

  private clientVisible = false
  private serverVisible = false

  private prevQueryStr = '' // 上一次请求的查询条件

  private query: any = {
    client: '',
    server: '',
    connector: 'OR',
  }

  private created() {
    this.prevQueryStr = JSON.stringify(this.query)
  }

  public init (from: string[], conn: string) {
    // 搜索参数回显
    if (from.length >= 2) {
      this.query.client = from[0]
      this.query.server = from[1]
    } else if (from.length === 1) {
      const key = from[0].indexOf('ip:port') === 0 ? 'ip:port' : from[0].split(':')[0]
      const isServer = !!this.serverList.find(t => t.value === key)
      this.query[isServer ? 'server' : 'client'] = from[0]
    }
    if (conn) {
      this.query.connector = conn
    }
    this.prevQueryStr = JSON.stringify(this.query)
    return this.getFrom()
  }

  private toggleConnectorHandle () {
    this.query.connector = this.query.connector === 'OR' ? 'AND' : 'OR';
    this.changeHandle()
  }

  private tagChangeHandle (tag: any, type: 'client' | 'server' = 'client') {
    this.query[type] = `${tag}:`
    this.$refs[type].focus()
    this.clientVisible = false
    this.serverVisible = false
  }

  private enterHandle () {
    this.clientVisible = false
    this.serverVisible = false
    this.changeHandle()
  }

  private changeHandle () {
    if (JSON.stringify(this.query) !== this.prevQueryStr) {
      this.prevQueryStr = JSON.stringify(this.query)
      this.$emit('on-change', this.getFrom())
    }
  }

  private getFrom () {
    const { client, server, connector } = this.query
    const from = [client, server].filter(t => t).map((tag, index) => {
      if (tag.indexOf('ip:port') === 0) {
        return { left: 'ip:port', operator: 'like', right: tag.replace('ip:port:', ''), connector }
      }
      const [key, ...values] = tag.split(':')
      return { left: key, operator: 'like', right: values.join(':'), connector }
    })
    return [...from]
  }
}
</script>

<style lang="scss" scoped>
.tag-input-wrapper {
  padding-left: 50px;
  position: relative;

  .tag-input {
    display: flex;
    white-space: nowrap;
    align-items: center;
  }

  .query-input-wrap {
    width: 100%;
  }

  .connector {
    width: 40px;
    height: 20px;
    background: var(--bg-color);
    border: 1px solid var(--border-color-base);
    border-radius: 20px;
    transform: translate(0, -50%);
    text-align: center;
    line-height: 18px;
    user-select: none;
    transition: all 0.3s;
    position: absolute;
    left: 0;
    top: 50%;
    cursor: pointer;
    &::before,
    &::after {
      content: '';
      display: block;
      width: 24px;
      height: 12px;
      border: 1px solid var(--border-color-base);
      position: absolute;
      left: 50%;
      pointer-events: none;
    }
    &::before {
      border-width: 1px 0 0 1px;
      top: -13px;
    }
    &::after {
      bottom: -13px;
      border-width: 0 0 1px 1px;
    }
  }
}

.option-list {
  margin: -12px;
  width: calc(100% + 24px);
  // background: var(--bg-color);
  // border: 1px solid var(--border-color-base);
  // border-radius: 0 0 4px 4px;
  // border-top: none;
  // position: absolute;
  // top: 100%;
  // left: 0;
  // z-index: 10;
  // overflow: hidden;
  // &::before {
  //   content: '';
  //   padding: 2px;
  //   background: var(--bg-color);
  //   border: 1px solid var(--border-color-base);
  //   border-width: 0 0 1px 1px;
  //   position: absolute;
  //   top: -5px;
  //   left: -1px;
  // }

  .option {
    padding: 0 20px 0 30px;
    height: 34px;
    line-height: 34px;
    font-size: 14px;
    color: var(--color-text-regular);
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    cursor: pointer;
    position: relative;
    &:hover {
      background-color: var(--bg-color02);
    }
    .prefix-icon {
      width: 25px;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      color: var(--color-text-regular);
      position: absolute;
      top: 0;
      left: 5px;
    }
  }
}
</style>
<style lang="scss">
.npm-tag-input-popover[x-placement^=bottom] {
  margin-top: -1px;
  border-radius: 0 0 4px 4px;
  border-color: var(--border-color-base);
}
</style>
