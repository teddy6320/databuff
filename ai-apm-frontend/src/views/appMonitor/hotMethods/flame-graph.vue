<template>
  <div class="flame-graph-wrapper">
    <header v-show="!showEmpty" class="tools">
      <el-button
        @click="toggleReverseHandle"
        icon="el-icon-sort"
        size="mini"
        class="tools-btn sort-btn"></el-button>

      <el-button
        @click="toggleScrollHandle('top')"
        size="mini"
        class="tools-btn top-btn">
        <span class="el-icon-download btn-icon"></span>
      </el-button>

      <el-button
        @click="toggleScrollHandle('bottom')"
        size="mini"
        class="tools-btn bottom-btn">
        <span class="el-icon-download btn-icon"></span>
      </el-button>

      <el-input
        v-model="query"
        @change="searchHandle"
        clearable
        size="mini"
        maxlength="100"
        prefix-icon="db-icon-search"
        :placeholder="$t('modules.views.alarmCenter.alarmDetail.s_e5f71fc3')"
        class="tools-input"
      />

      <div v-show="info.match" class="match">
        Matched: {{ info.match }}
      </div>
    </header>

    <div ref="flameGraphWrap" class="flame-graph-cont">
      <canvas ref="flameGraph"></canvas>
      <div
        v-show="info.show"
        ref="highlight"
        :style="info.style"
        class="highlight">{{ info.titleKey ? $t(info.titleKey) : info.title }}</div>
    </div>

    <p class="text">{{ info.text }}</p>

    <div class="empty-show" v-if="showEmpty && !loading">{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { State } from 'vuex-class';
import { v4 as uuidv4 } from 'uuid';
import { orderBy } from 'lodash';
import { NumberFilter, PercentFilter } from '@/utils/filters/number'
import { debounce } from '@/utils/common'

const ItemHeight = 16;

let flameGraphSource: any[][] = [];

// level: number, left: number, width: number, type: number, title: string, detail?: any
type PropSourceItem = [number, number, number, number, string, any?];

@Component
export default class FlameGraph extends Vue {
  @State('theme') private theme!: 'dark' | 'light';
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [] }) private source!: PropSourceItem[];
  @Prop({ default: false }) private loading!: boolean;

  public $refs!: {
    flameGraphWrap: HTMLDivElement
    flameGraph: HTMLCanvasElement
    highlight: HTMLDivElement
  }

  @Watch('source', { deep: true })
  private onSourceChanged (newVal: PropSourceItem[]) {
    const { source, maxLevel } = this.formatData(this.source);
    flameGraphSource = source;
    this.maxLevel = maxLevel;
    if (this.flameGraph) {
      this.updateGraph();
    } else {
      this.initGraph();
    }
    this.toggleScrollHandle();
  }
  @Watch('themeChanged')
  private onThemeChanged(value: boolean) {
    if (value) {
      if (this.flameGraph) {
        this.updateGraph();
      } else {
        this.initGraph();
      }
      this.toggleScrollHandle();
    }
  }

  get showEmpty () {
    return !this.source.length || (this.source.length === 1 && !this.source[2]);
  }

  private resizeHandler: any = null;

  private flameGraph: any = null;
  private maxLevel = 0;

  private root: any = null; // 根
  private rootLevel = 0; // 根的level
  private px = 1; // canvas宽和根width的缩放系数
  private reverse = false; // 是否翻转

  private query: string = ''; // 搜索关键字

  private info = {
    show: false,
    title: '',
    text: '',
    match: '',
    style: {},
  };

  private mounted() {
    this.query = decodeURIComponent((this.$route.query.hotMethod as string || ''));

    this.resizeHandler = debounce(() => {
      if (this.flameGraph) {
        this.updateGraph();
      }
    }, 100)
    window.addEventListener('resize', this.resizeHandler)
  }

  private beforeDestroy() {
    flameGraphSource = []
    if (!this.flameGraph) {
      return
    }
    window.removeEventListener('resize', this.resizeHandler)
    this.flameGraph = null
  }

  private initGraph () {
    this.updateGraph();

    const findFrame = (frames: any[], x: number, px: number) => {
      let left = 0;
      let right = frames.length - 1;

      while (left <= right) {
        const mid = Math.floor((left + right) / 2);
        const f = frames[mid];

        if (f.left > x) {
          right = mid - 1;
        } else if (f.left + f.width <= x) {
          left = mid + 1;
        } else {
          return f;
        }
      }

      if (frames[left] && (frames[left].left - x) * px < 0.5) {
        return frames[left];
      }
      if (frames[right] && (x - (frames[right].left + frames[right].width)) * px < 0.5) {
        return frames[right];
      }
      return null;
    }

    // 绑定事件
    const canvas = this.$refs.flameGraph;
    canvas.onmousemove = () => {
      const canvasHeight = ItemHeight * this.maxLevel;
      const event = window.event as MouseEvent;
      const h = Math.floor((this.reverse ? event.offsetY : (canvasHeight - event.offsetY)) / ItemHeight);
      if (h >= 0 && h < flameGraphSource.length) {
        const f = findFrame(flameGraphSource[h], event.offsetX / this.px + this.root.left, this.px);
        if (f) {
          if (f.id !== this.root.id) {
            (window.getSelection() as any).removeAllRanges();
          }
          const details = NumberFilter(f.detail.samples, false, ` sample${f.detail.samples <= 1 ? '' : 's'}`) +
              ', ' + PercentFilter(f.width / flameGraphSource[0][0].width);
          canvas.title = f.title + '\n(' + details + ')';
          canvas.style.cursor = 'pointer';
          canvas.onclick = () => {
            if (f.id !== this.root.id) {
              this.renderHandle(f, h);
              (canvas as any).onmousemove();
            }
          };
          this.info = {
            ...this.info,
            show: true,
            title: f.title,
            text: 'Function: ' + canvas.title,
            style: {
              width: (Math.min(f.width, this.root.width) * this.px) + 'px',
              left: (Math.max(f.left - this.root.left, 0) * this.px + canvas.offsetLeft) + 'px',
              top: ((this.reverse ? h * ItemHeight : canvasHeight - (h + 1) * ItemHeight) + canvas.offsetTop) + 'px',
            },
          }
          return;
        }
      }
      (canvas as any).onmouseout();
    }

    canvas.onmouseout = () => {
      canvas.title = '';
      canvas.style.cursor = '';
      canvas.onclick = null;
      this.info.show = false;
      this.info.text = '';
    }

    canvas.ondblclick = () => {
      (window.getSelection() as any).selectAllChildren(this.$refs.highlight);
    }
  }

  private updateGraph () {
    if (this.loading) {
      return;
    }

    const canvasHeight = ItemHeight * this.maxLevel;
    const canvasWidth = this.$refs.flameGraphWrap.clientWidth;

    const canvas = this.$refs.flameGraph;
    canvas.style.height = canvasHeight + 'px';
    canvas.style.width = canvasWidth + 'px';

    this.flameGraph = this.flameGraph || canvas;
    // this.flameGraph = this.flameGraph || (('OffscreenCanvas' in window) ? canvas.transferControlToOffscreen() : canvas);
    const c = this.flameGraph.getContext('2d') as CanvasRenderingContext2D;
    let scale = devicePixelRatio || 1;
    // 数据量过大时，高像素密度屏幕会渲染不出来，缩小渲染精度
    if (scale > 1 && this.source.length >= 10000 && this.source.length < 50000) {
      scale = Math.min(1.99, scale)
    } else if (scale > 1 && this.source.length >= 50000) {
      scale = 1;
    }
    this.flameGraph.width = canvasWidth * scale;
    this.flameGraph.height = canvasHeight * scale;
    c.scale(scale, scale);
    c.font = '12px Verdana, sans-serif';

    if (this.showEmpty) {
      this.clearGraph();
      return;
    }

    const matched = this.renderHandle();
    if (this.query) {
      this.info.match = this.query ? PercentFilter(matched / this.root.width) : '';
    }
  }

  // 清空画布
  private clearGraph () {
    const canvas = this.flameGraph;
    const c = canvas.getContext('2d') as CanvasRenderingContext2D;
    const { width, height } = canvas;
    const { a: scaleX, d: scaleY } = c.getTransform(); // 水平和垂直缩放
    c.fillStyle = this.theme === 'light' ? '#ffffff' : '#1D1D1D';
    // 在scale不为1时，直接使用width、height清空画布可能会出现残留问题，根据scale值缩放后解决
    c.fillRect(0, 0, (width / scaleX) + 20, (height / scaleY) + 20);
  }

  private renderHandle (newRoot?: any, newLevel?: number) {
    const canvas = this.flameGraph;
    const c = canvas.getContext('2d') as CanvasRenderingContext2D;

    const canvasHeight = ItemHeight * this.maxLevel;
    const canvasWidth = this.$refs.flameGraphWrap.clientWidth;

    if (this.root) {
      this.clearGraph();
    }

    this.root = newRoot || flameGraphSource[0][0];
    this.rootLevel = newLevel || 0;
    this.px = canvasWidth / this.root.width;

    const x0 = this.root.left;
    const x1 = x0 + this.root.width;
    const marked: number[] = [];

    const mark = (f: any) => {
      return marked[f.left] >= f.width || (marked[f.left] = f.width);
    }

    const totalMarked = () => {
      let total = 0;
      let left = 0;
      Object.keys(marked).sort((a, b) => +a - +b).forEach((x) => {
        if (+x >= left) {
          total += marked[+x];
          left = +x + marked[+x];
        }
      });
      return total;
    }

    const drawFrame = (f: any, y: number, alpha: boolean) => {
      if (f.left < x1 && f.left + f.width > x0) {
        c.fillStyle = this.query && f.title.includes(this.query) && mark(f) ? '#ee00ee' : f.color;
        c.fillRect((f.left - x0) * this.px, y, f.width * this.px, 15);

        if (f.width * this.px >= 21) {
          const chars = Math.floor(f.width * this.px / 7);
          const title = f.title.length <= chars ? f.title : f.title.substring(0, chars - 2) + '..';
          c.fillStyle = '#262626';
          c.fillText(title, Math.max(f.left - x0, 0) * this.px + 3, y + 12, f.width * this.px - 6);
        }

        if (alpha) {
          c.fillStyle = 'rgba(255, 255, 255, 0.4)';
          c.fillRect((f.left - x0) * this.px, y, f.width * this.px, 15);
        }
      }
    }

    for (let h = 0; h < flameGraphSource.length; h++) {
      const y = this.reverse ? h * ItemHeight : canvasHeight - (h + 1) * ItemHeight;
      const frames = flameGraphSource[h];
      for (let i = 0; i < frames.length; i++) {
        drawFrame(frames[i], y, h < this.rootLevel);
      }
    }

    return totalMarked();
  }

  private formatData (data: PropSourceItem[]) {
    let maxLevel = 0;
    const source: any[][] = [];
    const palette = [
      [0x50e150, 30, 30, 30],
      [0x50e150, 30, 30, 30],
      [0x50e150, 30, 30, 30],
      [0xe15a5a, 30, 40, 40],
      [0xc8c83c, 30, 30, 10],
      [0xe17d00, 30, 30, 0],
      [0x50e150, 30, 30, 30],
    ];
    const getColor = (p: number[]) => {
      const v = Math.random();
      const r = Math.floor(p[1] * v);
      const g = Math.floor(p[2] * v);
      const b = Math.floor(p[3] * v);
      const colorValue = p[0] + (r * 256 * 256) + (g * 256) + b;
      return '#' + colorValue.toString(16).padStart(6, '0');
    }

    // 按level、left从小到大排序
    orderBy(data, ['0', '1'], ['asc', 'asc']).forEach((item: any) => {
      maxLevel = Math.max(item[0] + 1, maxLevel);
      source[item[0]] = [
        ...(source[item[0]] || []),
        {
          id: uuidv4(),
          left: item[1],
          width: item[2],
          color: getColor(palette[item[3]]),
          title: item[4],
          detail: item[5] || {},
        }
      ];
    });
    return { source, maxLevel };
  }

  // 翻转
  private toggleReverseHandle () {
    this.reverse = !this.reverse;
    this.toggleScrollHandle();
    const matched = this.renderHandle();
    if (this.query) {
      this.info.match = this.query ? PercentFilter(matched / this.root.width) : '';
    }
  }

  // 滚动到顶部/底部
  private toggleScrollHandle (type?: 'top'|'bottom') {
    const $wrap = this.$refs.flameGraphWrap;
    if (type === 'bottom' || (!type && !this.reverse)) {
      $wrap.scrollTop = $wrap.scrollHeight;
    } else {
      $wrap.scrollTop = 0;
    }
  }

  // 搜索
  private searchHandle () {
    const matched = this.renderHandle(this.root, this.rootLevel);
    this.info.match = this.query ? PercentFilter(matched / this.root.width) : '';

    const _query = { ...this.$route.query };
    delete _query.hotMethod;
    if (this.query) {
      _query.hotMethod = encodeURIComponent(this.query);
    }
    this.$router.replace({ query: { ..._query } })
  }
}
</script>

<style lang="scss" scoped>
.flame-graph-wrapper {
  padding: 16px;
  background: var(--bg-color);
  position: relative;

  .flame-graph-cont {
    width: 100%;
    height: calc(100% - 60px);
    overflow-x: hidden;
    overflow-y: auto;
    position: relative;
    font: 12px Verdana, sans-serif;
    canvas {
      display: block;
      width: 100%;
      height: 100%;
    }
  }

  .tools {
    margin: 0 0 8px;
    height: 28px;
    display: flex;
    align-items: center;
    .tools-btn {
      width: 32px;
      height: 28px;
      padding: 0;
      font-size: 16px;
    }
    .top-btn .btn-icon {
      transform: rotate(180deg);
    }
    .tools-input {
      margin-left: 10px;
      width: 200px;
    }
  }

  .highlight {
    position: absolute;
    overflow: hidden;
    white-space: nowrap;
    pointer-events: none;
    background-color: #ffffe0;
    outline: 1px solid #ffc000;
    height: 15px;
    padding: 0 3px;
    color: #262626;
  }

  .match {
    margin-left: 10px;
    overflow: hidden;
    white-space: nowrap;
    line-height: 22px;
    color: var(--color-text-primary);
  }

  .text {
    margin: 6px 0 0;
    height: 18px;
    line-height: 18px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }

  .empty-show{
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 14px;
    color: var(--color-text-regular);
    background-color: var(--bg-color);
    border-radius: 5px;
    z-index: 9;
  }
}
</style>
