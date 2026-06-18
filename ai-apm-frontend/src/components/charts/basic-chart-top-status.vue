<template>
  <div class="top-status flex-h" :style='getPositionStyle' ref='container'>
    <template v-for='item, idx in calcSource' >
      <span
        v-if='item.status'
        :key='idx'
        class="h-100p dbl top-status-item"
        :class="{
          'bg-green': item.status === 'success',
          'bg-red': item.status === 'danger',
          'bg-yellow': item.status === 'warning',
          'bg-blue': item.status === 'primary',
          'cp': true
        }"
        :style="{
          flex: item.width,
          '--top-status-height': areaHeight + 'px'
        }"
        @mouseenter="showTooltip($event, item)"
        @mouseleave="hideTooltip">
      </span>
      <span
        v-else 
        :key='idx'
        class="h-100p dbl top-status-item is-normal"
        :style="{
          flex: item.width,
          '--top-status-height': areaHeight + 'px'
        }">
      </span>
    </template>
    <div class="top-status-tooltip"
      v-show="tooltipVisible"
      :style="tooltipStyle"
      @mouseenter="tooltipMouseEnter"
      @mouseleave="hideTooltip" id="topStatusTooltip" ref='topStatusTooltip'>
      <slot name='ts' v-bind='{ row: currentRow }'></slot>
    </div>
  </div>
</template>

<script lang="ts">
import dayjs from 'dayjs';
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';

export interface TsPosition {
  height: string | number;
  top: string | number;
  left: string | number;
  right: string | number;
}

const mergeTimeRanges = (ranges: any[]) => {
  if (!ranges.length) {
    return [];
  };

  // 按照 start 时间升序排序，如果 start 相同则按 end 时间升序排序
  ranges.sort((a, b) => (a.start === b.start ? a.end - b.end : a.start - b.start));

  const merged: any[] = [];
  let current = ranges[0];

  for (let i = 1; i < ranges.length; i++) {
    const next = ranges[i];

    // 检查是否有交叉、重叠，或者包含关系
    if (current.end >= next.start) {
      // 合并时间范围
      current = {
        ...current,
        start: current.start,
        end: Math.max(current.end, next.end),
        info: [...current.info, ...next.info]
      };
    } else {
      // 如果没有交叉或重叠，将当前时间范围加入结果
      merged.push(current);
      current = next;
    }
  }

  // 将最后一个时间范围加入结果
  merged.push(current);
  // console.log([...merged].map((i) => [ dayjs(i.start).format('HH:mm'), dayjs(i.end).format('HH:mm') ]));
  return merged;
}

@Component({})
export default class TopStatus extends Vue {
  @Prop({ default: () => ([]) }) public source!: any;
  @Prop({ default: 0 }) public areaHeight!: string;
  @Prop({ default: () => ([]) }) public xAxis!: any;
  @Prop({ default: () => ({
    height: '10px',
    top: '0px',
    left: '0px',
    right: '0px',
  }) }) public position!: TsPosition;

  public $refs!: {
    container: HTMLDivElement,
    topStatusTooltip: HTMLDivElement,
  }

  public tooltipVisible = false;
  public tooltipStyle: any = {
    top: '0px',
    left: '0px',
    transform: ''
  };
  public currentRow: any = null;
  public timer: any = null;
  public mergedDurations: Array<{ start: number, end: number }> = [];
  public placeHolders: Array<{ start: number, end: number }> = [];

  private beforeDestroy () {
    window.clearTimeout(this.timer);
    this.timer = null;
  }

  get getPositionStyle () {
    const { height = '10px', top = '0px', left = '0px', right = '0px' } = this.position;
    return {
      height: String(height).indexOf('%') !== -1 ? height : (String(height).replace('px', '') + 'px'),
      top: String(top).indexOf('%') !== -1 ? top : (String(top).replace('px', '') + 'px'),
      left: String(left).indexOf('%') !== -1 ? left : (String(left).replace('px', '') + 'px'),
      right: String(right).indexOf('%') !== -1 ? right : (String(right).replace('px', '') + 'px'),
    };
  }

  get calcSource () {
    const { interval } = this.getGlobalTimeV2();
    const placeholder = this.xAxis.map((i: any, idx: number) => ({
      start: new Date(i).valueOf(),
      end: new Date(this.xAxis[idx + 1]).valueOf() || (new Date(this.xAxis[idx]).valueOf() + interval * 1000),
      from: dayjs(new Date(i).valueOf()).format('MM-DD HH:mm:ss'),
      to: dayjs(new Date(this.xAxis[idx + 1]).valueOf() || (new Date(this.xAxis[idx]).valueOf() + interval * 1000)).format('MM-DD HH:mm:ss'),
      status: '',
      width: 1,
      info: null
    }));
    if (Array.isArray(this.source) && !this.source.length) {
      this.placeHolders = placeholder;
      return placeholder;
    }
    if (!Array.isArray(this.source) || !Array.isArray(this.xAxis) || !this.xAxis.length) {
      this.placeHolders = placeholder;
      return placeholder;
    }
    // 合并时间范围
    const mergedDurations = mergeTimeRanges(
      this.source.map((item: any) => ({
        ...item,
        start: item.duration[0],
        end: item.duration[1],
        from: dayjs(item.duration[0]).format('MM-DD HH:mm:ss'),
        to: dayjs(item.duration[1]).format('MM-DD HH:mm:ss'),
        info: [{
          start: item.duration[0],
          end: item.duration[1],
          status: 'danger',
          from: dayjs(item.duration[0]).format('MM-DD HH:mm:ss'),
          to: dayjs(item.duration[1]).format('MM-DD HH:mm:ss'),
          info: { ...item.info },
        }],
        status: 'danger'
      }))
    );
    const maxEnd = new Date(this.xAxis[this.xAxis.length - 1]).valueOf() + interval * 1000;
    this.mergedDurations = mergedDurations.filter(i => i.start < maxEnd);
    // 柱子总数 = total
    // 遍历source,根据项中的duration[start, end],找到xaxis中对应的索引
    // 计算出每个柱子在x轴上的跨度
    // 跨度用splice(start, end)计算
    // 项布局为flex
    // 计算出每个柱子在x轴上的跨度
    mergedDurations.forEach(({ start, end, info }: { start: number; end: number, info: any[] }, index: number) => {
      let startIdx = placeholder.findIndex((i: any) => start >= i.start && start < i.end);
      let endIdx = placeholder.findIndex((i: any) => end <= i.end);

      // 第一个时间刻度小于刻度轴开始位置，手动处理为0
      if (index === 0 && startIdx === -1 && start < placeholder[0].start && end > placeholder[0].start) {
        startIdx = 0
      }
      if ((index === mergedDurations.length - 1) && endIdx === -1 && end > placeholder[placeholder.length - 1].end && start < placeholder[placeholder.length - 1].end) {
        endIdx = placeholder.length - 1
      }
      if (startIdx !== -1 && endIdx !== -1) {
        const startTimes = placeholder[startIdx].start;
        const endTimes = placeholder[endIdx].end;
        const addItem = {
          start: startTimes,
          end: endTimes,
          from: dayjs(startTimes).format('MM-DD HH:mm:ss'),
          to: dayjs(endTimes).format('MM-DD HH:mm:ss'),
          status: 'danger',
          width: 1,
          info: [...info]
        }
        const spliceItems = placeholder.splice(startIdx, endIdx - startIdx + 1, addItem);
        const spliceInfos = spliceItems.filter((i: any) => i.info?.length > 0).map((i: any) => i.info).flat();
        if (spliceInfos.length > 0) {
          addItem.info = [...spliceInfos, ...addItem.info].map((i) => ({
            ...i,
            width: endIdx - startIdx + 1,
          }))
        }
      }
    });
    placeholder.forEach((i: any) => {
      i.width = Math.floor((i.end - i.start) / interval / 1000);
      if (Array.isArray(i.info)) {
        i.info.forEach((sub: any) => {
          sub.width = i.width
        })
      }
    })
    this.placeHolders = placeholder;
    return placeholder;
  }

  public showTooltip(event: any, item: any) {
    if (this.timer) {
      window.clearTimeout(this.timer)
    }
    this.tooltipVisible = true;
    const $target = event.target || event.srcElement;
    const rect = $target.getBoundingClientRect(); // 获取元素的位置信息
    const right = window.innerWidth - rect.right; // 计算屏幕右侧的距离
    const halfWidth = rect.width / 2
    this.tooltipStyle = {
      top: rect.top + rect.height + 'px',
      left: right > 360 ? rect.left + 'px' : `auto`,
      right: right <= 360 ? right + 'px' : 'auto',
      transform: rect.left < 360 ? `translateX(-${rect.left / 2}px)` : right < 360 ? `translateX(${right / 2}px)` : `translateX(-50%)`
    };
    this.currentRow = { ...item };
    this.$emit('on-ts-tooltip-show', { ...item });
  }

  public hideTooltip(event: any) {
    // if (event.relatedTarget && this.$refs.container.contains(event.relatedTarget)) {
    //   return; // 跳出，不执行后续逻辑
    // }

    this.timer = setTimeout(() => {
      this.tooltipVisible = false;
    }, 200)
    // this.currentRow = null;
  }

  public tooltipMouseEnter() {

    // 阻止鼠标移入 tooltip 时触发 hideTooltip
    if (this.timer) {
      window.clearTimeout(this.timer)
    }
    this.tooltipVisible = true;
  }

  public getMergedDurations (): Array<{ start: number, end: number }> {
    return this.mergedDurations
  }
  public getPlaceHolders (): Array<{ start: number, end: number }> {
    return this.placeHolders
  }
}

</script>
<style scoped lang='scss'>
.top-status {
  position: absolute;
  z-index: 2;

  &:hover {
    z-index: 999;
  }

  .dbl {
    display: inline-block;
  }

  .top-status-item {
    position: relative;

    &:not(.is-normal) {
      opacity: 0.7;
      &:hover {
        opacity: 1;
      }
    }

    &.bg-red::before {
      content: '';
      position: absolute;
      width: 100%;
      height: var(--top-status-height, 10px);
      top: 0px;
      pointer-events: none;
      background-color: rgba(225, 40, 40, .1);
    }
  }

  .top-status-tooltip {
    position: fixed;
    // background: white;
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(10px);
    border: 1px solid #fff;
    padding: 5px;
    border-radius: 4px;
    box-shadow: 0px 4px 10px 0px rgba(119, 122, 126, 0.14);
    z-index: 10;
  }
}
</style>