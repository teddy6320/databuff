<template>
  <div class="search-group">
    <div class="query-item">
      <span class="query-label">{{ $t('modules.views.npm.analysis.s_adccf28b') }}</span>
      <el-select
        v-model="queryParams.client"
        @change="changeHandle"
        size="small"
        :placeholder="$t('modules.views.npm.analysis.s_11f6ae1b')"
        class="query-select">
        <el-option v-for="item in clientList" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </div>

    <div class="query-item">
      <span class="query-label">{{ $t('modules.views.npm.analysis.s_fb659d21') }}</span>
      <el-select
        v-model="queryParams.server"
        @change="changeHandle"
        :disabled="isDns"
        size="small"
        :placeholder="$t('modules.views.npm.analysis.s_4bfabe65')"
        class="query-select">
        <el-option v-for="item in serverList" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </div>

    <!-- <el-button size="small" type="primary" class="query-button">{{ $t('modules.views.npm.analysis.s_50ce429d') }}</el-button> -->

    <tag-input
      ref="tagInput"
      :isDns="isDns"
      @on-change="tagChangeHandle"
      class="query-tag-input" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import TagInput from './tag-input.vue';

@Component({
  components: {
    TagInput,
  }
})
export default class SearchGroup extends Vue {
  @Prop({ default: false }) private isDns!: boolean;

  public $refs!: {
    tagInput: TagInput
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

  private queryParams: any = {
    client: 'srcHostname',
    server: 'hostname',
    from: [],
  }

  private created () {
    if (this.isDns) {
      this.queryParams.server = 'ip'
    }
  }

  public init () {
    return new Promise(async (resolve, reject) => {
      // 搜索参数回显
      const client = this.$route.query.client as string || ''
      const server = this.$route.query.server as string || ''
      if (client) {
        this.queryParams.client = client
      }
      if (server) {
        this.queryParams.server = server
      }
      const from: string[] = JSON.parse(this.$route.query.from as string || '[]')
      const conn = this.$route.query.conn as string || ''
      this.queryParams.from = this.$refs.tagInput.init(from, conn);
      resolve({ ...this.queryParams })
    })
  }

  private changeHandle () {
    const from = this.queryParams.from.map((t: any) => `${t.left}:${t.right}`)
    const query = {
      ...this.$route.query,
      ...this.queryParams,
    }
    Object.entries(query).forEach(([key, value]) => {
      if (value === '') {
        delete query[key]
      }
    })
    delete query.from
    delete query.conn
    if (from.length) {
      query.from = JSON.stringify(from)
      if (from.length > 1) {
        query.conn = this.queryParams.from[1].connector
      }
    }
    this.$router.replace({ query })
    this.$emit('on-change', { ...this.queryParams })
  }

  private tagChangeHandle (from: any[]) {
    this.queryParams.from = from
    this.changeHandle()
  }
}
</script>

<style lang="scss" scoped>
.search-group {
  display: flex;
  flex-wrap: wrap;
  position: relative;

  .query-item {
    box-sizing: content-box;
    display: flex;
    align-items: center;
    &:not(:first-child) {
      padding-left: 20px;
    }
    .query-label {
      white-space: nowrap;
      text-align: right;
      font-size: 14px;
      color: var(--color-text-primary);
    }
    .query-input,
    .query-select {
      width: 240px;
      :deep(.el-input__inner){
        color: var(--color-text-regular);
      }
    }
  }

  .query-tag-input {
    width: 100%;
    margin-top: 16px;
  }

  .query-button {
    position: absolute;
    top: 0;
    right: 0;
  }
}
</style>
