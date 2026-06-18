<template>
  <div class="search-group">
    <div class="query-item">
      <span class="query-label">{{ $t('modules.views.npm.topology.s_5a2433e0') }}</span>
      <el-select
        v-model="queryParams.dimension"
        @change="changeHandle"
        size="small"
        :placeholder="$t('modules.views.npm.topology.s_92e3bf3e')"
        class="query-select">
        <el-option v-for="item in dimensionList" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </div>

    <div class="query-item">
      <span class="query-label">{{ $t('modules.views.npm.topology.s_2655847d') }}</span>
      <el-select
        v-model="queryParams.metric"
        @change="changeHandle"
        size="small"
        :placeholder="$t('modules.views.npm.topology.s_df8b8c94')"
        class="query-select">
        <el-option-group
          v-for="group in metricList"
          :key="group.label"
          :label="group.label">
          <el-option v-for="item in group.options" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-option-group>
      </el-select>
    </div>

    <!-- <el-button size="small" type="primary" class="query-button">{{ $t('modules.views.npm.analysis.s_50ce429d') }}</el-button> -->

    <tag-input
      ref="tagInput"
      @on-change="tagChangeHandle"
      class="query-tag-input" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import TagInput from '../analysis/tag-input.vue';

@Component({
  components: {
    TagInput,
  }
})
export default class SearchGroup extends Vue {
  @Prop({ default: () => [] }) private metricList!: any[];

  public $refs!: {
    tagInput: TagInput
  }

  private dimensionList = [
    { label: 'cname', value: 'cname' },
    { label: 'hostname', value: 'hostname' },
    { label: 'ip', value: 'ip' },
    { label: 'podName', value: 'podName' },
    // { label: 'port', value: 'port' },
    { label: 'service', value: 'service' },
  ];

  private queryParams: any = {
    dimension: 'hostname',
    metric: 'npm.volume_sent',
    from: [],
  }

  public init () {
    return new Promise(async (resolve, reject) => {
      // 搜索参数回显
      const dimension = this.$route.query.dimension as string || ''
      const metric = this.$route.query.metric as string || ''
      if (dimension) {
        this.queryParams.dimension = dimension
      }
      if (metric) {
        this.queryParams.metric = metric
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
