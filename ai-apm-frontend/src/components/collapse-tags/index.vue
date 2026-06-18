<template>
  <div class="tags-item-cont" :style="{ minWidth: minWidth+'px' }">
    <el-tag v-for='(tag, index) in calcTags'
      :key='index' size="mini" effect="plain"
      @click.stop.prevent="handleMetricTagClick(tag)"
      :class='["tags-item", { "tags-item-silent": isSilent, "tags-item-hover": isClick }]'
    >
      <span class="tags-item-label" :title="tag">{{ tag }}</span>
      <i v-if='isCopy' @click.stop="copyTagHandle(tag)"
        class="db-icon-copy copy-btn"></i>
    </el-tag>

    <template v-if='!isSilent'>
      <el-tag size='small' type='primary' effect='plain' v-if='needEllipse && !toggled'
        @click="hideMoreHandle"
        class="tags-item action-tag cp"
      >
        <i class="el-icon el-icon-caret-top"></i>
        {{ $t('modules.components.collapse-tags.s_e082621c') }}
      </el-tag>
      <el-tag size='small' type='primary' effect='plain' v-if='needEllipse && toggled'
        @click="showMoreHandle"
        class="tags-item action-tag cp"
      >
        <i class="el-icon el-icon-caret-bottom"></i>
        {{ $t('modules.components.collapse-tags.s_e2edde5a') }}
      </el-tag>
    </template>
    <template v-if='isSilent && hideTags.length'>
      <span class="ellipsis-dot ellipsis-dot-offset">...</span>
    </template>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator'
import { getTextWidth } from '@/utils/common'
import { copy } from '@/utils/common'

@Component({})
export default class CollapseTags extends Vue {
  @Prop({ default: () => [] }) private tags!: string[];
  @Prop({ default: false }) private silent!: boolean; // 是否静态，如果是静态状态，超出展示"..."
  @Prop({ default: false }) private copyable!: boolean; // 单个标签是否可复制内容
  @Prop({ default: false }) private clickable!: boolean; // 单个标签可点击
  @Prop({ default: 20 }) private parentPadding !: number; // 父级容器的padding，左右总和
  @Prop({ default: 120 }) private minWidth !: number; // 
  @Prop({ default: 1, type: Number }) private maxLine !: number;

  private showTags: string[] = [];
  private hideTags: string[] = [];
  private maxShowLength = 0;
  private needEllipse = false;
  private toggled = true;

  get calcTags () {
    return this.tags.slice(0, this.maxShowLength)
  }

  get isSilent () {
    return this.silent !== false
  }
  get isCopy () {
    return this.copyable !== true
  }
  get isClick () {
    return this.clickable !== false
  }

  private created () {
    // console.log(tagWidth)
  }

  private mounted () {
    this.$nextTick(() => {
      let parentWidth = 350;
      const paretnDom = this.$el.parentElement
      if (paretnDom && paretnDom.getBoundingClientRect) {
        const { width } = paretnDom.getBoundingClientRect()
        parentWidth = width
      }
      // padding/border/margin，左右总和
      const tagPadding = 16;
      const tagBorderWidth = 2;
      const tagMargin = 8;
      // 展开/折叠按钮的宽度，包括padding/margin/border
      const actionBtnWidth = 62;

      const tagsWidthArr = this.tags.map((tag) => {
        const tagTextWidth = getTextWidth(`${tag}`, { size: 12 })
        return tagTextWidth + tagPadding + tagBorderWidth + tagMargin
      });

      // 最大可展示标签数量
      let maxShowLength = 0;
      // 最大可展示标签宽度
      const availWidth = parentWidth - this.parentPadding - actionBtnWidth
      // 需要根据最多显示几行来计算
      // 标签会换行
      let calcLine = 0;

      // 全部标签的宽度
      const allTagsWidth = tagsWidthArr.reduce((prev, curr) => prev + curr, 0);

      tagsWidthArr.reduce((prev, curr, index) => {
        if (calcLine < this.maxLine) {
          maxShowLength = index
          if (prev + curr <= availWidth) {
            // 未超过宽度
            return prev + curr
          } else {
            // 超过宽度，需要换行，以prev的宽度作为第一个
            calcLine += 1
            return curr
          }
        } else {
          return prev + curr
        }
      }, 0);

      if (maxShowLength === 0 && this.tags.length) {
        maxShowLength = 1
      }

      if (allTagsWidth > parentWidth - this.parentPadding) {
        // 需要折叠
        this.needEllipse = true
        this.maxShowLength = maxShowLength
        this.showTags = this.tags.slice(0, maxShowLength)
        this.hideTags = this.tags.slice(maxShowLength)
      } else {
        // 全部展示
        this.needEllipse = false
        this.maxShowLength = this.tags.length
        this.showTags = this.tags
        this.hideTags = []
      }
      // console.log(this.calcTags)
    })
  }
  private showMoreHandle () {
    this.toggled = false;
    this.maxShowLength = this.tags.length
  }
  private hideMoreHandle () {
    this.toggled = true;
    this.maxShowLength = this.showTags.length
  }
  private handleMetricTagClick(tag: string) {
    if (this.isClick) {
      this.$emit('on-click', tag)
    }
  }

  private copyTagHandle (tag: string) {
    copy(tag);
  }
}
</script>
<style lang='scss' scoped>
.tags-item-cont {
  margin: -4px 0 0 -4px;
  white-space: normal;
  line-height: 0;

  .tags-item{
    margin: 4px 0 0 4px;
    max-width: calc( 100% - 70px );
    padding: 0 8px;
    height: 22px;
    line-height: 22px;
    background: var(--bg-color03);
    border: none;
    border-radius: 4px;
    color: var(--color-text-primary);
    vertical-align: middle;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    position: relative;

    &.tags-item-silent {
      max-width: calc( 100% - 16px );
    }

    &.action-tag {
      border-color: transparent;
      background-color: transparent;
      color: var(--color-text-link);
    }

    &.tags-item-hover:hover {
      cursor: pointer;
    }

    &:hover .copy-btn {
      display: block;
    }
  }

  .copy-btn {
    display: none;
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    padding: 3px 3px 3px 5px;
    font-size: 12px;
    background-color: var(--bg-color03);
    color: var(--color-text-link);
    line-height: 14px;
    cursor: pointer;
  }

  .ellipsis-dot {
    margin-left: 5px;

    &.ellipsis-dot-offset {
      position: relative;
      top: 0;
    }
  }
}
</style>