<template>
  <div class="hexmap-wrapper">
    <div class="hexmap-charts" ref="hexmapChart"></div>
    <div class="tips" v-if="tooltip && tooltip.id"  ref="hexmapTooltip" :style="`top:${tooltip.top};right:${tooltip.right};bottom:${tooltip.bottom};left:${tooltip.left}`">
      <div class="label-box">
        <span class="label">{{ $t('modules.views.infrastructure.host.s_643dce6f') }}</span>
        <span class="value">{{ hostmapData[tooltip.id]['df-hostname'] }}</span>
      </div>
      <!-- <div class="label-box">
        <span class="label">{{ $t('modules.views.infrastructure.host.s_1bc46281') }}</span>
        <span class="value">{{ hostmapData[tooltip.id].apps.join('、') }}</span>
      </div> -->
    </div>
    <div class="empty-show describe" v-if='!loading && !hostIds.length'>{{ $t('modules.components.charts.s_21efd88b') }}</div>
  </div>
</template>
<script lang="ts">
  import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
  import { Getter, State } from 'vuex-class';
  import * as d3 from 'd3';
  import { hexbin, HexbinBin } from 'd3-hexbin';
  type Point = [number, number];

  @Component
  export default class HexmapChart extends Vue {
    @Getter('themeVariables') private themeVars!: any;
    @State('themeChanged') private themeChanged!: boolean;

    @Prop({ default: () => [] }) private hostIds!: any[]
    @Prop({ default: () => ({}) }) private hostmapData!: any
    @Prop({ default: () => [] }) private thresholds!: any[]
    @Prop({ default: false }) private isGroup!: boolean
    @Prop({ default: '' }) private thresholdType!: string
    @Prop({ default: false }) private loading!: boolean

    public $refs!: {
      hexmapChart: HTMLDivElement
      hexmapTooltip: HTMLDivElement
    }

    private svg: any = null;
    private graph: any = null;
    private tooltip: any = null;

    private hexRadius = 50
    private margin = {
      top: this.hexRadius,
      right: this.hexRadius * Math.sqrt(3) / 2,
      bottom: this.hexRadius,
      left: this.hexRadius * Math.sqrt(3) / 2,
    }

    @Watch('hostIds', { deep: true })
    private onSearchParamsChange (val: any) {
      this.updateChart()
    }

    @Watch('thresholds', { deep: true })
    private onThresholdsChange (val: any) {
      this.updateChart()
    }

    @Watch('themeChanged')
    private onThemeChanged(value: boolean) {
      if (value) {
        this.updateChart()
      }
    }

    private mounted() {
      window.addEventListener('resize', this.resize);
      this.initChart()
    }

    private beforeDestroy() {
      window.removeEventListener('resize', this.resize);
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
      this.svg!.selectChildren().remove()
      if (!this.hostIds.length) {
        return
      }
      this.draw()
      const zoom = d3
        .zoom()
        .scaleExtent([0.3, 10])
        .on('zoom', (event: any) => {
          this.graph.attr(
            'transform',
            `translate(${event.transform.x},${event.transform.y})scale(${event.transform.k})`,
          );
        });
      this.svg.call(zoom);
      this.svg.call(zoom.transform, d3.zoomIdentity); // 重置缩放

      this.svg!.selectAll('.hexagon').call((g: any) => {
        this.hover(g);
      })
    }

    // 画图
    private draw () {
      const { left, top } = this.margin
      const hexbinFun = hexbin().radius(this.hexRadius!);
      const $chartWrap = this.svg.append('g').attr('class', 'hexagon-chart-wrap')
      const svgWidth = +this.svg.attr('width')
      const svgHeight = +this.svg.attr('height')

      const drawMap = ($parent: any, points: any[], list: any[], pos: any) => {
        const $chartItem = $parent.append('g')
          .attr('class', 'hexagon-chart-item')
          .attr('transform', 'translate(' + pos.left + ',' + pos.top + ')');

        $chartItem
          .selectAll('.hexagon')
          .data(hexbinFun(points))
          .enter()
          .append('path')
          .attr('class', 'hexagon')
          .attr('d', (d: any) => 'M' + d.x + ',' + d.y + hexbinFun.hexagon())
          .attr('stroke', this.themeVars.bgColor)
          .attr('stroke-width', `${this.hexRadius / 10}px`)
          .attr('fill', (d: any, i: number) => list[i].color)
          .attr('data-id', (d: any, i: number) => list[i].id);
        return $chartItem;
      }

      if (!this.isGroup) { // 不分组
        const { points, list, width, height } = this.formatData([...this.hostIds])
        drawMap($chartWrap, points, list, {
          left: (svgWidth - width) / 2 + left,
          top: (svgHeight - height) / 2 + top,
        })
      } else { // 分组
        const columnNum = 3 // 列数，每行显示个数
        const mapGap = 150 // 图表之间的间隔
        const titHeight = 46 // 标题部分高度
        const mapDataList: any[] = this.hostIds.map((t: any) => this.formatData([...t.data]))
        let colWidths: any[] = []
        const rowHeights: number[] = []
        for (let i = 0; i < this.hostIds.length; i++) {
          const idx = i % columnNum
          colWidths[idx] = colWidths[idx] || []
          colWidths[idx].push(mapDataList[i].width)
          if (idx === 0) {
            const hArr: number[] = []
            for (let j = 0; j < columnNum; j++) {
              hArr.push(mapDataList[i + j] ? mapDataList[i + j].height + titHeight : 0)
            }
            rowHeights.push(Math.max(...hArr))
          }
        }
        colWidths = colWidths.map(t => Math.max(...t))

        const groupWidth = colWidths.length ? colWidths.reduce((a: number, b: number) => a + b, 0) + mapGap * (colWidths.length - 1) : 0;
        const groupHeight = rowHeights.length ? rowHeights.reduce((a: number, b: number) => a + b, 0) + mapGap * (rowHeights.length - 1) : 0;

        const $chartGroup = $chartWrap.append('g').attr('class', 'hexagon-chart-group')
          .attr('transform', 'translate(' + ((svgWidth - groupWidth) / 2 + left) + ',' + ((svgHeight - groupHeight) / 2 + top) + ')');

        this.hostIds.forEach((t: any, i: number) => {
          const { points, list, width, height } = mapDataList[i]
          const rowIdx = Math.floor(i / columnNum) // 第几行
          const colIdx = i % columnNum // 第几列
          const prevBottom: number = (mapDataList[i - columnNum] || {}).bottom || 0 // 同列上一个的bottom
          const prevRight: number = i % columnNum === 0 ? 0 : mapDataList[i - 1].right // 同行前一个的right
          mapDataList[i].bottom = prevBottom + rowHeights[rowIdx] + mapGap
          mapDataList[i].right = prevRight + colWidths[colIdx] + mapGap
          const $chartItem = drawMap($chartGroup, points, list, {
            left: prevRight,
            top: prevBottom,
          })
          $chartItem.append('text')
            .attr('x', width / 2 - left)
            .attr('y', -30 - top)
            .attr('font-size', 18)
            .attr('fill', this.themeVars.colorTextRegular)
            .attr('text-anchor', 'middle')
            .text(t.tagGroups)
        })
      }

      this.graph = $chartWrap
    }

    // 鼠标事件
    private hover ($hexagons: any) {
      const _this = this

      $hexagons.on('mousemove', move)
        .on('mouseenter', enter)
        .on('mouseleave', leave)
        .on('click', click)

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
        _this.$emit('on-click', _this.hostmapData[_this.tooltip.id]);
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

    public resize() {
      const $hexmapChart = this.$refs.hexmapChart
      this.svg.attr('width', $hexmapChart.clientWidth);
      this.svg.attr('height', $hexmapChart.clientHeight);
    }

    // 数据处理
    private formatData (ids: string[]) {
      // 获取格子的颜色
      const getHexColor = (num: number) => {
        const thresholds = [...this.thresholds].reverse()
        const thresholdItem = thresholds.find(t => (t.value || 0) <= num)
        return thresholdItem ? thresholdItem.color : this.thresholds[0].color
      }
      const leng = ids.length
      const columns = leng !== 1 ? Math.ceil(Math.sqrt(leng * 1.5)) || 1 : 1;
      const rows = Math.ceil(leng / columns);
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
          const id = ids[index];
          const value = this.hostmapData[id].threshold || 0;
          list.push({
            id, value, color: getHexColor(value),
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
    flex: 1;
    overflow: hidden;
    position: relative;
    border: 1px solid var(--border-color-lighter);
    border-radius: 4px;

    .hexmap-charts {
      height: 100%;
    }

    :deep(.hexagon:hover) {
      filter: brightness(130%);
      cursor: pointer;
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
      font-size: 16px;
      background-color: var(--bg-color);
      border-radius: 4px;
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
