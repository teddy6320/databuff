<template>
  <div class="hexmap-wrapper">
    <div class="hexmap-charts" ref="hexmapChart"></div>
    <div class="tips" v-if="tooltip && tooltip.id"  ref="hexmapTooltip" :style="`top:${tooltip.top};right:${tooltip.right};bottom:${tooltip.bottom};left:${tooltip.left}`">
      <div class="label-box">
        <span class="label">{{ $t('modules.views.infrastructure.clusterDetail.s_f16a482a') }}</span>
        <span class="value">{{ tooltip.nameKey ? $t(tooltip.nameKey) : tooltip.name }}</span>
      </div>
    </div>
    <div class="empty-show describe" v-if='!loading && !nodeList.length'>{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { Getter, State } from 'vuex-class';
import * as d3 from 'd3';
import { hexbin } from 'd3-hexbin';
type Point = [number, number];

@Component
export default class HexmapChart extends Vue {
  @Getter('themeVariables') private themeVars!: any;
  @State('themeChanged') private themeChanged!: boolean;

  @Prop({ default: () => [] }) private source!: any[]
  @Prop({ default: () => [] }) private total!: any[] // 有nodeTotal时会忽略 source 字段
  @Prop({ default: false }) private loading!: boolean

  public $refs!: {
    hexmapChart: HTMLDivElement
    hexmapTooltip: HTMLDivElement
  }

  private svg: any = null;
  private graph: any = null;
  private tooltip: any = null;

  private hexRadius = 11; // 12.6
  private margin = {
    top: this.hexRadius,
    right: this.hexRadius * Math.sqrt(3) / 2,
    bottom: this.hexRadius,
    left: this.hexRadius * Math.sqrt(3) / 2,
  }

  private thresholds: any[] = [
    { color: '#08BE7E', value: 0 },
    { color: '#FADE2A', value: 10 },
    { color: '#F2495C', value: 20 },
  ]

  get nodeList () {
    if (Array.isArray(this.source) && this.source.length) {
      return this.source
    } else if (typeof this.total === 'number' && this.total) {
      return Array.from(Array(this.total), (v, k) => ({}))
    }
    return []
  }

  @Watch('nodeList', { deep: true })
  private onNodeListChange (val: any[]) {
    this.updateChart()
  }
  @Watch('themeChanged')
  private onThemeChanged(value: boolean) {
    if (value) {
      this.updateChart()
    }
  }

  private mounted() {
    this.initChart()
    if (this.nodeList) {
      this.updateChart()
    }
  }

  private beforeDestroy() {
    this.svg?.remove();
    this.graph = null;
    this.svg = null;
  }

  private initChart () {
    const $hexmapChart = this.$refs.hexmapChart
    this.svg = d3
      .select($hexmapChart)
      .append('svg')
      .attr('class', 'hexmap-svg')
      .attr('width', $hexmapChart.clientWidth)
      .attr('height', $hexmapChart.clientHeight);
  }

  private updateChart () {
    if (this.svg) {
      this.svg!.selectChildren().remove()
    }
    if (!this.nodeList.length || !this.svg) {
      return
    }
    this.draw()
    // const zoom = d3
    //   .zoom()
    //   .scaleExtent([0.2, 10])
    //   .on('zoom', (event: any) => {
    //     this.graph.attr(
    //       'transform',
    //       `translate(${event.transform.x},${event.transform.y}) scale(${event.transform.k})`,
    //     );
    //   });
    // this.svg.call(zoom);
    // this.svg.call(zoom.transform, d3.zoomIdentity); // 重置缩放

    // this.svg!.selectAll('.hexagon').call((g: any) => {
    //   this.hover(g);
    // })
  }

  // 画图
  private draw () {
    const hexbinFun = hexbin().radius(this.hexRadius!);
    const svgWidth = +this.svg.attr('width')
    const svgHeight = +this.svg.attr('height')
    const { points, list, width, height } = this.formatData([...this.nodeList])
    // const scale = Math.min(svgWidth / width, svgHeight / height, 1);
    const left = (svgWidth - width) / 2 + this.margin.left;
    const top = (svgHeight - height) / 2 + this.margin.top;

    const $chartWrap = this.svg.append('g').attr('class', 'hexagon-chart-wrap')
    const $chartItem = $chartWrap.append('g')
      .attr('class', 'hexagon-chart-item')
      .attr('transform', `translate(${left},${top})`); // scale(${scale})
    $chartItem
      .selectAll('.hexagon')
      .data(hexbinFun(points))
      .enter()
      .append('path')
      .attr('class', 'hexagon')
      .attr('d', (d: any) => 'M' + d.x + ',' + d.y + hexbinFun.hexagon())
      .attr('stroke', this.themeVars.bgColor)
      .attr('stroke-width', `${this.hexRadius / 5}px`)
      .attr('fill', (d: any, i: number) => list[i].color)
      .attr('data-id', (d: any, i: number) => list[i].id);

    this.graph = $chartWrap
  }

  // 鼠标事件
  private hover ($hexagons: any) {
    const _this = this

    // $hexagons.on('mousemove', move)
    //   .on('mouseenter', enter)
    //   .on('mouseleave', leave)
    //   .on('click', click)

    function move (event: MouseEvent) {
      const { offsetX, offsetY } = event
      _this.tooltip = {
        id: _this.tooltip.id,
        ...getPos([offsetX, offsetY]),
      }
    }
    function enter (event: MouseEvent) {
      const { target = {}, offsetX, offsetY } = event
      const dataset = (target as any).dataset
      _this.tooltip = {
        id: (dataset || {}).id || '',
        ...getPos([offsetX, offsetY]),
      }
    }
    function leave () {
      _this.tooltip = null
    }
    function click (event: PointerEvent) {
      _this.$emit('on-click', _this.tooltip.id);
    }

    function getPos (point: number[]) {
      const offset = 10
      const svgWidth = +_this.svg.attr('width')
      const svgHeight = +_this.svg.attr('height')
      const $tooltip = _this.$refs.hexmapTooltip
      const width = $tooltip ? $tooltip.clientWidth : 300
      const height = $tooltip ? $tooltip.clientHeight : 150
      const pos: any = {
        top: `${point[1] + offset}px`,
        left: `${point[0] + offset}px`,
        right: 'auto',
        bottom: 'auto',
      }
      if (point[1] + offset + height > svgHeight) {
        pos.bottom = `${svgHeight - point[1] + offset}px`
        pos.top = 'auto'
      }
      if (point[0] + offset + width > svgWidth) {
        pos.right = `${svgWidth - point[0] + offset}px`
        pos.left = 'auto'
      }
      return pos
    }
  }

  // 数据处理
  private formatData (nodeList: any[]) {
    // 获取格子的颜色
    const getHexColor = (num: number) => {
      const thresholds = [...this.thresholds].reverse()
      const thresholdItem = thresholds.find(t => (t.value || 0) <= num)
      return thresholdItem ? thresholdItem.color : this.thresholds[0].color
    }
    const leng = nodeList.length
    let columns = leng !== 1 ? Math.ceil(Math.sqrt(leng * 2.5)) || 1 : 1;
    let rows = Math.ceil(leng / columns);
    if (leng <= 20) {
      rows = leng >= 7 ? 2 : 1;
      columns = Math.ceil(leng / rows);
    }
    const points: any[] = [];
    const list: any[] = []
    if (!rows) {
      return { points, list, width: 0, height: 0 }
    }
    for (let i = 0; i < rows; i++) {
      for (let j = 0; j < columns; j++) {
        let x: number = this.hexRadius! * j * Math.sqrt(3);
        if (i % 2 === 1) {
          x += (this.hexRadius! * Math.sqrt(3)) / 2;
        }
        const y: number = this.hexRadius! * i * 1.5;
        points.push([x, y]);
        const index = i * columns + j;
        const node = nodeList[index];
        list.push({
          id: node.id,
          name: node.name,
          value: node.value || 0,
          color: getHexColor(node.value || 0),
        });
        if (index + 1 >= leng) {
          break;
        }
      }
    }
    const width = this.hexRadius * (columns + (leng / columns >= 2 ? 0.5 : 0)) * Math.sqrt(3)
    const height = this.hexRadius * (rows * 1.5 + 0.5)
    return { points, list, width, height }
  }
}
</script>

<style lang="scss" scoped>
.hexmap-wrapper {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;

  .hexmap-charts {
    width: 100%;
    height: 100%;
  }

  :deep(.hexagon:hover) {
    filter: brightness(130%);
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
    z-index: 9;
  }

  .tips {
    max-width: 400px;
    padding: 10px 14px;
    border-radius: 4px;
    background-color: var(--tooltip-bg-color);
    box-shadow:1px 1px 4px 0 var(--tooltip-shadow-color);
    position: absolute;
    top: -10000px;
    left: 0px;

    .label-box {
      margin-bottom: 6px;
      font-size: 12px;
      line-height: 18px;
      color: var(--color-text-regular);
      overflow: hidden;
      &:last-child {
        margin-bottom: 0;
      }
      .label {
        float: left;
      }
      .value {
        display: block;
        word-break: break-all;
        overflow: hidden;
      }
    }
  }
}
</style>
