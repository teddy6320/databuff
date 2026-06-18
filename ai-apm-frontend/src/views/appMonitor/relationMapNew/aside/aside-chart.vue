<template>
  <div :class='["bus-map", active ? "active" : ""]' :style="{ left: active ? '0' : contBBox.left, width: contBBox.width }" ref='mapCont'
    v-loading='loading'>
    <div @mousemove="wrapperMouseHandle" @mouseout="wrapperMouseOutHandle" ref='wrapperDom' class="bus-map-wrapper">
      <div class="chart-cont" id="asideChartCont"></div>

      <div class="action-group">
        
        <el-tooltip placement="top" effect="light" :content="$t('modules.views.appMonitor.relationMapNew.s_b4b74a17')">
          <span class="action-btn" v-show='hideAside' @click="toggleAsideHandle(true)">
            <i class="el-icon-caret-left"></i>
          </span>
        </el-tooltip>
      </div>

      <!-- 背景色 -->
      <div v-for='value,key in levelsMap' :key='`bg_${key}`'
        :class='["chart-bg-group ", {
          "chart-bg-group-count-3": true,
          "is-even": key % 2 === 0,
          "active": +key === (typeIndex + 1),
        }]' :style='{ "--i": key }'></div>
    </div>

    <div @mousedown.prevent.stop="onMouseDown" class="split-action">
      <span class="split-action-icon"></span>
      <span class="split-action-icon"></span>
      <span class="split-action-icon"></span>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Watch, Prop } from 'vue-property-decorator'
import { toAsyncWait } from '@/utils/common'
import G6, { Graph, Item } from '@antv/g6';
import i18n from '@/i18n';
import { getDbIcon } from '@/assets/fonts/db-find-icon';
import { TopoChartTheme, ThemeType } from './theme'
import TopologyApi from '@/api/topology'
import { v4 as uuidv4 } from 'uuid'
import deepClone from 'lodash/cloneDeep';
import { getNameByWith } from './utils'
import cloneDeep from 'lodash/cloneDeep';
// import debounce from 'lodash/debounce';
import throttling from 'lodash/throttle';

let relationChartGraph: Graph | null = null;

let scrollTargetX = 0;
let scrollCurrentX = 0;
let scrollAnimating = false;

@Component
export default class BusMap extends Vue {
  @Prop({}) private row!: any;
  @Prop({}) private activeType!: APM.RelationMap["relationType"];
  @Prop({}) private typeIndex!: number;
  @Prop({ default: 'light' }) private theme!: 'light' | 'dark';
  @Prop({}) private serviceIdTypeMap!: any;
  @Prop({}) private times!: { fromTime: string, toTime: string };

  @Watch('row', { deep: true })
  private async onCurrentChange (newVal: any) {
    if (newVal && newVal?.id !== this.current?.id) {
      this.current = cloneDeep(newVal);
      await this.fetchVerticalTree()
      this.initChart()
    }
  }

  @Watch('times', { deep: true })
  private async watchGlobalTime() {
    if (!this.current) {
      return;
    }
    this.clearMapCache()
    if (relationChartGraph) {
      // relationChartGraph.clear()
    }
    // this.singleModel = false
    await this.fetchVerticalTree()
    this.initChart()
  }

  @Watch('active')
  private async watchActive() {
    if (!this.active) {
      this.treeSourceMap = {}
      if (relationChartGraph) {
        relationChartGraph.clear()
      }
      this.$emit('hide', this.active);
    }
  }
  
  public $refs!: {
    mapCont: HTMLElement
    wrapperDom: HTMLElement
  }
  
  private active = false
  private loading = false
  private tooltipTimer: any = null;

  private singleModel = false

  private domBBox = {
    width: 0,
    height: 0,
    activeLevel: 0,
  }
  private contBBox = {
    width: '40%',
    left: '-40%',
    startX: 0,
    startWidth: 0,
  }

  private touch = {
    mouseDown: false,
    dragging: false,
    startClientX: 0,
  }

  private levelsMap: any = {
    1: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
    2: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
    3: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
    // 4: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
    // 5: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
  }


  private presetNodes = [
    // {
    //   id: 'application',
    //   type: 'container-node',
    //   x: 0,
    //   y: 0,
    //   name: i18n.t('modules.views.alarmCenter.alarm.s_5b0520a9') as string, nameKey: 'modules.views.alarmCenter.alarm.s_5b0520a9',
    //   level: 1
    // },
    {
      id: 'service',
      type: 'container-node',
      x: 0,
      y: 0,
      name: i18n.t('modules.views.alarmCenter.alarm.s_47d68cd0') as string, nameKey: 'modules.views.alarmCenter.alarm.s_47d68cd0',
      level: 1
    },
    {
      id: 'process',
      type: 'container-node',
      x: 0,
      y: 0,
      name: i18n.t('modules.views.appMonitor.relationMapNew.s_ed0793a9') as string, nameKey: 'modules.views.appMonitor.relationMapNew.s_ed0793a9',
      level: 2
    },
    {
      id: 'host',
      type: 'container-node',
      x: 0,
      y: 0,
      name: i18n.t('modules.views.alarmCenter.alarm.s_65227369') as string, nameKey: 'modules.views.alarmCenter.alarm.s_65227369',
      level: 3
    },
  ]

  private source: any = {
    nodes: [],
    edges: [],
  }

  private originSource: any = null;
  private originFormatSource: any = null;
  private existNodesMap: any = {
    service: {},
    pod: {},
    process: {},
    host: {},
  }
  private hostNameMap: any = {}

  private treeSourceMap: Record<string, any> = {}


  private current: any = null;

  get isSingleChain () {
    return this.levelsMap['1'].count <= 1 && this.levelsMap['2'].count <= 1 && this.levelsMap['3'].count <= 1
  }

  private created () {
    const localWidths = window.localStorage.getItem('DATABUFF_RM_CUSTOM_WIDTH')
    if (localWidths && String(localWidths).indexOf(',') > -1) {
      const [width = '40%', left = '-40%'] = String(localWidths).split(',')
      this.contBBox.left = left
      this.contBBox.width = width
    }
  }

  private mounted () {
    // 获取asideChartCont的宽高信息
    const { width, height } = document.querySelector('#asideChartCont')!.getBoundingClientRect()
    this.domBBox.width = width
    this.domBBox.height = height
  }

  private beforeDestroy () {
    relationChartGraph?.destroy();
    relationChartGraph = null
    window.clearTimeout(this.tooltipTimer);
    this.tooltipTimer = null;
  }

  public showDetail () {
    this.active = true
  }
  public hideDetail () {
    this.active = false
    if (relationChartGraph) {
      // relationChartGraph.clear()
      this.current = null;
    }
  }


  private initChart () {
    if (!relationChartGraph) {
      // 获取dom宽高
      const themeColor = TopoChartTheme[this.theme];
      const { width, height } = document.querySelector('#asideChartCont')!.getBoundingClientRect();
      // const percentY = height / 4
      G6.registerNode(
        'relation-node',
        {
          drawShape(cfg, group) {
            const style = cfg!.style || {};
            const _theme = cfg?.theme || 'light';
            const _themeColor = TopoChartTheme[_theme as ThemeType];
            const { error } = cfg || {};
            const isError = !!error;

            group!.addShape('text', {
              attrs: {
                text: getNameByWith((cfg!.name as string), 57, 9),
                x: 0,
                y: 37,
                fill: _themeColor.title,
                fontSize: 9,
                textAlign: 'center',
                textBaseline: 'middle',
              },
              capture: false,
              name: 'text-shape',
            });
            const circleBg = group!.addShape('circle', {
              attrs: {
                ...style,
                x: 0,
                y: 0,
                r: 16,
                stroke: isError ? _themeColor.circleStroke.error : _themeColor.circleStroke.normal,
                fill: isError ? _themeColor.circleFill.error : _themeColor.circleFill.normal,
                // lineWidth: 2,
              },
              name: 'circle-shape',
            });
            const typeIcon = group!.addShape(
              'text', 
              {
                attrs: {
                  textBaseline: 'top',
                  x: -7,
                  y: -7,
                  text: getDbIcon(cfg!.dataType as string),
                  fontFamily: 'db-icon',
                  // fontWeight: 600,
                  fontSize: 14,
                  fill: isError ? _themeColor.typeIcon.error : _themeColor.typeIcon.normal,
                  // fill: '#fff',
                  cursor: 'pointer'
                },
                name: 'type-icon'
              }
            );
            const circleMain: any = group!.addShape(
              'circle',
              {
                attrs: {
                  ...style,
                  x: 0,
                  y: 0,
                  r: 20,
                  stroke: isError ? _themeColor.keyShapeStroke.error : _themeColor.keyShapeStroke.normal,
                  fill: 'transparent',
                  cursor: 'pointer',
                  lineWidth: 1,
                },
                draggable: false,
                name: 'circle-key-shape'
              }
            )
            return circleMain;
          },
          update: undefined,
        },
        'circle',
      );
      G6.registerNode(
        'relation-tooltip',
        {
          draw (cfg, group) {
            const style = this.getShapeStyle(cfg);
            const { label = '-' } = cfg
            const _theme = (cfg!.theme || 'light') as 'light' | 'dark';
            const _themeColor = TopoChartTheme[_theme];

            const main: any = group.addShape(
              'rect',
              {
                attrs: {
                  ...style,
                  width: 110,
                  height: 40,
                  fill: '#5985FF',
                  stroke: '#5985FF',
                  x: style.x,
                  y: style.y - 20,
                  radius: [0, 4, 4, 0],
                },
                capture: false,
                draggable: false,
                name: 'relation-tooltip-key-shape'
              }
            );
            const subRect: any = group.addShape(
              'rect',
              {
                attrs: {
                  ...style,
                  width: 16,
                  height: 40,
                  fill: '#4677FF',
                  stroke: '#4677FF',
                  x: style.x + 96,
                  y: style.y - 20,
                  radius: [0, 4, 4, 0],
                  cursor: 'pointer',
                },
                // capture: false,
                draggable: false,
                name: 'relation-tooltip-sub-rect'
              }
            );
            const linkIcon = group.addShape(
              'text', 
              {
                attrs: {
                  textBaseline: 'middle',
                  x: style.x + 99,
                  y: style.y,
                  text: getDbIcon('link'),
                  fontFamily: 'db-icon',
                  // fontWeight: 600,
                  fontSize: 8,
                  lineHeight: 40,
                  fill: '#fff',
                  cursor: 'pointer',
                },
                draggable: false,
                name: 'relation-tooltip-link'
              }
            );
            const placeholdCircle = group.addShape(
              'circle',
              {
                attrs: {
                  ...style,
                  fill: _themeColor.bgColor,
                  stroke: 'transparent',
                  x: style.x,
                  y: style.y,
                  r: 20,
                  radius: 4,
                },
                capture: false,
                draggable: false,
                name: 'relation-tooltip-placehold-circle'
              }
            );
            // @ts-ignore
            const { showName, line } = getNameByWith((String(label)), 65, 10, true)
            const text = group.addShape(
              'text',
              {
                attrs: {
                  text: showName,
                  fontSize: 10,
                  fill: '#fff',
                  textBaseline: 'top',
                  x: style.x + 25,
                  y: style.y - (line === 2 ? 12 : 6),
                },
                capture: false,
                draggable: false,
                name: 'relation-tooltip-body-label',
              },
            )
            return main
          },
          update: undefined
        },
        'rect'
      );
      relationChartGraph = new G6.Graph({
        container: 'asideChartCont',
        width,
        height,
        defaultNode: {
          type: 'relation-node',
        },
        defaultEdge: {
          type: 'cubic-vertical',
          style: {
            lineWidth: 1,
            stroke: themeColor.edgeRelation.normal,
          },
        },
        nodeStateStyles: {
          nodeSelected: {
            stroke: 'transparent',
            fill: 'transparent',
            ['circle-shape']: {
              stroke: '#3D71FF',
              // fill: 'transparent',
            },
            ['type-icon']: {
              fill: '#fff'
            },
            ['circle-key-shape']: {
              stroke: '#3D71FF',
              lineWidth: 1,
            }
          },
          nodeErrorActive: {
            ['circle-shape']: {
              fill: '#E12828',
            },
          },
          nodeNormalActive: {
            ['circle-shape']: {
              fill: '#3D71FF',
            },
          },
        },
        animate: true,
        animateCfg: {
          duration: 200
        },
        modes: {
          default: [
            // {
            //   type: 'click-select',
            //   selectedState: ''
            // }
          ],
        },
      });
      
      relationChartGraph.on('canvas:click', () => {
        //
      });
      relationChartGraph.on('afterrender', () => {
        if (!this.current) {
          return;
        }
        let currentNode = relationChartGraph && this.current && relationChartGraph.findById(this.current.id);
        const baseType = this.current?.data?.originType;
        if (baseType === 'host') {
          const hostName = this.current?.hostName;
          const host = this.hostNameMap[hostName];
          if (host) {
            currentNode = relationChartGraph && this.current && relationChartGraph.findById(host.id);
          }
        }
        if (currentNode && relationChartGraph) {
          const hasTooltipNode = relationChartGraph?.findById('relationTooltipId')
          if (!hasTooltipNode) {
            this.addTooltipNode();
          }
          this.updateTooltipPos(currentNode);
          this.toggleTooltip(true, {...currentNode.getModel()});
        }
      });

      relationChartGraph.on('node:click', async (e: any) => {
        const { item, silenceEmit, currentTarget } = e
        const { _cfg } = item
        const { id, type, error, baseType } = _cfg.model;
        item.setState( error ? 'nodeErrorActive' : 'nodeNormalActive', true);
        item.setState('nodeSelected', true);
        if (type === 'relation-tooltip') {
          this.toggleTooltip(false);
          this.viewDetailByType(_cfg.model);
          const nodes = relationChartGraph?.findAllByState('node', 'nodeNormalActive') || [];
          const nodes2 = relationChartGraph?.findAllByState('node', 'nodeErrorActive') || [];
          nodes.concat(nodes2).forEach((n) => {
            n.clearStates(['nodeNormalActive', 'nodeErrorActive']);
          });
          return;
        }

        if (type === 'relation-node') {
          if (!silenceEmit) {
            await this.viewSingleFlow(item);
          }
          const hasTooltipNode = relationChartGraph?.findById('relationTooltipId')
          if (!hasTooltipNode) {
            this.addTooltipNode();
          }
          const _item = currentTarget.findById(item.get('id'));
          _item.toFront();
          this.$emit('map-node-click', {
            ..._cfg.model
          })
        }
      });

      const _this = this;

      const smoothGraphScroll = () => {
        scrollAnimating = true;
        const deltaX = scrollTargetX - scrollCurrentX;
        const stepX = deltaX * 0.2;
        if (Math.abs(deltaX) < 0.5) {
          scrollAnimating = false;
          scrollCurrentX = scrollTargetX;
          return;
        }
        scrollCurrentX += stepX;
        const levelInfo = _this.levelsMap[_this.domBBox.activeLevel];
        _this.scrollChartXHandle(stepX, levelInfo, String(_this.domBBox.activeLevel));

        requestAnimationFrame(smoothGraphScroll);
      }

      const onGraphWheel = (e: any) => {
        const baseStep = 40;
        const speed = Math.abs(e.deltaY) / 100;
        const move = baseStep * speed * (e.deltaY > 0 ? 1 : -1);
        scrollTargetX += move;
        if (!scrollAnimating) {
          smoothGraphScroll();
        }
      }

      relationChartGraph.on('wheel', onGraphWheel);

      this.formatNodesPositionV2();
      const data = JSON.parse(JSON.stringify(this.source))

      relationChartGraph.data(data);
      relationChartGraph.render();

    } else {
      // this.source.nodes = this.source.nodes.concat(this.presetNodes)
      this.formatNodesPositionV2()
      const data = JSON.parse(JSON.stringify(this.source));

      relationChartGraph.data(data);
      relationChartGraph.render();

    }
    if (this.current?.id) {
      let currentNode = relationChartGraph && this.current && relationChartGraph.findById(this.current.id);
      const baseType = this.activeType;
      if (baseType === 'host') {
        const hostName = this.current?.data?.hostName;
        const host = this.hostNameMap[hostName];
        if (host) {
          currentNode = relationChartGraph && this.current && relationChartGraph.findById(host.id);
        }
      }
      if (currentNode && relationChartGraph) {
        const item = currentNode;
        const { _cfg } = item as any;
        const { id, type, error } = _cfg.model
        item.setState( error ? 'nodeErrorActive' : 'nodeNormalActive', true);
        item.setState('nodeSelected', true);
        item.toFront();
      }
    }
  }

  public clearMapCache () {
    this.treeSourceMap = {}
  }

  private formatNodesPositionV2 () {
    // 最多4层, 根据domBBox计算每层的位置
    const { width, height } = this.domBBox
    const { nodes, edges } = this.source

    const dagreLayout = new G6.Layout.dagre({})
    dagreLayout.init({
      nodes,
      edges,
    })
    dagreLayout.execute()

    const percentY = height / 3
    // const percentY = height / 4
    const xRep = 60
    const levelsMap: any = {
      1: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
      2: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
      3: { count: 0, nodes: [], positionX: [], exceed: false, fullWidth: 0, leftStep: 0, rightStep: 0, currentStep: 0 },
    }
    nodes.forEach((node: any) => {
      const { level, type } = node
      if (type !== 'relation-node') {
        return
      }
      if (!levelsMap[level]) {
        levelsMap[level].count = 1
      } else {
        levelsMap[level].count += 1
      }
      levelsMap[level].nodes.push(node)
    })

    Object.keys(levelsMap).forEach((key: string) => {
      const { count} = levelsMap[key]
      const totalWidth = (count - 1) * xRep
      const startX = (width - totalWidth) / 2;

      levelsMap[key].nodes.sort((a: any, b: any) => a.x - b.x)
      levelsMap[key].positionX = Array.from({ length: count }).map((_, index) => startX + index * xRep)
      levelsMap[key].nodes.forEach((node: any, index: number) => {
        node.x = levelsMap[key].positionX[index]
      })
      // xRep的宽度 * count + 左右两边节点大小的一半（25px * 2 = 50px）
      const realMaxWidth = (count - 1) * xRep + 50
      const isExceed = realMaxWidth > width
      levelsMap[key].exceed = isExceed
      levelsMap[key].fullWidth = realMaxWidth
      // 设置每层左右
      if (isExceed) {
        const nodeXs = levelsMap[key].positionX;
        const halfRealWidth = realMaxWidth / 2;
        const firstXNode = nodeXs.sort((a: number, b: number) => a - b)[0];
        const lastXNode = nodeXs.sort((a: number, b: number) => b - a)[0];
        levelsMap[key].leftStep = firstXNode - halfRealWidth;
        levelsMap[key].rightStep = lastXNode + halfRealWidth;
      }
    })
    nodes.forEach((node: any) => {
      const { level, type } = node
      if (type === 'relation-node') {
        // node.x = centerX
        node.theme = this.theme;
        node.y = percentY * ( level - 1) + ( percentY / 2 - 12 );
      }
    })
    // 特殊处理第3层和第4层有穿透情况
    // if (levelsMap['3'].count === 1 && levelsMap['4'].count === 1) {
    //   // 查找有无第3层和第4层的连线
    //   const edge = edges.find((e: any) => {
    //     const { source, target } = e
    //     return source === levelsMap['3'].nodes[0].id && target === levelsMap['4'].nodes[0].id
    //   })
    //   if (!edge) {
    //     // 待定是否需要这么处理
    //     // levelsMap['3'].nodes[0].x = levelsMap['3'].nodes[0].x - 60
    //     // levelsMap['4'].nodes[0].x = levelsMap['4'].nodes[0].x + 60
    //   }
    // }
    this.levelsMap = levelsMap
    // console.log(this.levelsMap)
  }

  // 手动触发节点翻页
  private scrollChartXHandle (step: number, val: any, level: string) {
    if (!this.levelsMap?.[level]) {
      return

    }
    const { nodes, positionX, exceed, fullWidth } = this.levelsMap[level];
    if (!exceed) {
      return
    }
    const _data = relationChartGraph?.get('data')
    const { nodes: _nodes } = _data;
    const nodesAtLevel = _nodes.filter((_node: any) => _node.level === Number(level) && _node.type === 'relation-node');
    const nodeXs = nodesAtLevel.map((n: any) => n.x);
    const firstXNode = nodeXs.sort((a: number, b: number) => a - b)[0];
    const lastXNode = nodeXs.sort((a: number, b: number) => b - a)[0];

    // 超出最大滚动范围后跳出
    if (firstXNode + step < this.levelsMap[level].leftStep || lastXNode + step > this.levelsMap[level].rightStep) {
      return
    }
   
    nodesAtLevel.forEach((_node: any) => {
      _node.x += step
    })
    this.levelsMap[level].currentStep += step
    
    relationChartGraph?.changeData()
    // relationChartGraph.positionsAnimate()

  }

  private async fetchVerticalTree () {
    if (!this.current) {
      return
    }
    if (this.treeSourceMap?.[this.current.id]) {
      this.source = deepClone(this.treeSourceMap[this.current.id])
      return 
    }
    this.loading = true
    const params: any = {
      fromTime: this.times.fromTime,
      toTime: this.times.toTime,
    }
    switch (this.current.baseType) {
      case 'service':
        params.serviceId = this.current.id
        break;
      case 'pod':
          params.namespace = this.current.namespace
          params.clusterId = this.current._clusterId
          params.k8sPodName = this.current.name
          break;
      case 'process':
          params.processId = this.current.id
          params.processName = this.current.name
        break;
      case 'host':
        params.hostName = this.current.hostName
        break;
    }
    const { error, result } = await toAsyncWait(TopologyApi.getVerticalTree(params))
    if (!error) {
      const { data = {} } = result || {}
      this.originSource = deepClone(data);
      const nodes: any[] = [];
      const edges: any[] = [];
      this.formatNodes(data, nodes)
      this.formatEdges(data, edges)
      this.originFormatSource = deepClone(data)
      const ids = [...new Set(nodes.map(n => n.id))];
      const _nodes = ids.map(id => {
        return nodes.find(n => n.id === id)
      })
      this.source = {
        nodes: _nodes.filter(n => n.id), edges
      }
      if (this.current) {
        this.treeSourceMap[this.current.id] = deepClone(this.source)
      }
    }
    this.loading = false
  }

  private formatNodes (data: any, nodes: any[]) {
    // const { businesses = [], services = [], containers = [], processes = [], hosts = [] } = data || {};
    const { services = [], processes = [], hosts = [] } = data || {};
    processes.forEach((i: any) => {
      i.hostName = i.hostName || i.hostname
    })
    this.formatNodeByLevel(services, 'service', 1, nodes)
    this.formatNodeByLevel(processes.filter((i: any) => i.type === 'pod'), 'pod', 2, nodes)
    this.formatNodeByLevel(processes.filter((i: any) => i.type === 'process'), 'process', 2, nodes)
    this.formatNodeByLevel(hosts, 'host', 3, nodes)
  }

  private formatNodeByLevel (nodes: any[], type: string, level: number, cont: any[]) {
    const systems = ['windows', 'linux', 'mac']

    nodes.forEach((n) => {
      n.dataType = type
      n.originType = n.type || '-'
      n.type = 'relation-node'
      n.x = 0
      n.y = 0
      n.level = level;
      n.error = n.errType > 0 || n.alarmCount > 0;
      if (type === 'service') {
        n.dataType = this.serviceIdTypeMap && this.serviceIdTypeMap[n.id] || 'default'
        n.baseType = 'service'
      }
      if (type === 'pod') {
        n.id = n.spuid || (n.hostName + n.pname)
        n.name = n.pname || '-'
        n.baseType = 'pod'
      }
      if (type === 'process') {
        n.id = n.spuid || (n.hostName + n.pname)
        n.name = n.pname || '-'
        n.baseType = 'process'
      }
      if (type === 'host') {
        n.id = n.hostName || uuidv4()
        n.name = n.name || n.hostName || '-'
        this.hostNameMap[n.hostName] = n
        const operatingSystem = String(n.os).toLocaleLowerCase()
        n.os = systems.find(t => operatingSystem.includes(t)) || n.os
        n.dataType = n.os || 'host'
        n.baseType = 'host'
      }
      this.existNodesMap[type][n.id] = n.id
      cont.push(n)
    })
  }

  private formatEdges (data: any, edges: any[]) {
    const { serviceToServices = {}, serviceToProcesses = {}, processToHosts = {} } = data || {}
    this.formatServiceToServiceEdge(serviceToServices, edges);
    this.formatServiceToProcessEdge(serviceToProcesses, edges);
    this.formatPodToHostEdge(processToHosts, edges);
    this.formatProcessToHostEdge(processToHosts, edges);
  }

  private formatServiceToServiceEdge (obj: any, edges: any[]) {
    Object.entries(obj || {}).forEach((item: any[]) => {
      const [serviceId, targetItems] = item
      const targets = Array.isArray(targetItems) ? targetItems : []
      targets.forEach((targetId: string) => {
        if (this.existNodesMap.service[serviceId] && this.existNodesMap.service[targetId]) {
          edges.push({
            source: serviceId,
            target: targetId,
          })
        }
      })
    })
  }

  private formatServiceToProcessEdge (obj: any, edges: any[]) {
    Object.entries(obj).forEach((item: any[]) => {
      const [serviceId, targetItems] = item
      targetItems.forEach((sub: any) => {
        if (sub && sub.type === 'pod' && this.existNodesMap.pod[sub.spuid] && this.existNodesMap.service[serviceId]) {
          edges.push({
            source: serviceId,
            target: sub.spuid || (sub.hostName + sub.pname)
          })
        } else if (sub &&  sub.type === 'process' && this.existNodesMap.process[sub.spuid] && this.existNodesMap.service[serviceId]) {
          edges.push({
            source: serviceId,
            target: sub.spuid || (sub.hostName + sub.pname)
          })
        }
      })
    })
  }

  private formatProcessToHostEdge (obj: any, edges: any[]) {
    Object.entries(obj).forEach((item: any[]) => {
      const [pgId, targetItems] = item
      targetItems.forEach((sub: any) => {
        if (sub && sub.hostName && this.hostNameMap[sub.hostName] && this.existNodesMap.process[pgId]) {
          edges.push({
            source: pgId,
            target: this.hostNameMap[sub.hostName].id
          })
        }
      })
    })
  }

  private formatPodToHostEdge (obj: any, edges: any[]) {
    Object.entries(obj).forEach((item: any[]) => {
      const [pgId, targetItems] = item
      targetItems.forEach((sub: any) => {
        if (sub && sub.hostName && this.hostNameMap[sub.hostName] && this.existNodesMap.pod[pgId]) {
          edges.push({
            source: pgId,
            target: this.hostNameMap[sub.hostName].id
          })
        }
      })
    })
  }

  private async viewSingleFlow (item: any) {
    const { _cfg } = item
    const { id, type, level } = _cfg.model
    this.current = { ..._cfg.model };
    await this.fetchVerticalTree()
    this.initChart()
  }

  private hideAside = true;

  public toggleAsideHandle (status?: boolean) {
    if (typeof status === 'boolean') {
      this.hideAside = status
    } else {
      this.hideAside = !this.hideAside
    }
    this.active = !this.hideAside
    if (!this.active) {
      //
    }
  }

  private addTooltipNode () {
    const model = {
      id: 'relationTooltipId',
      x: 0,
      y: 0,
      type: 'relation-tooltip',
      theme: this.theme,
      nodeInfo: {
        baseType: '',
        targetId: '',
        targetName: '',
        hostName: '',
        originType: '',
      }
    }
    relationChartGraph?.addItem('node', model);
    this.toggleTooltip(false);
  }

  private toggleTooltip (status: boolean, nodeModel?: any) {
    const tooltipItem = relationChartGraph?.findById('relationTooltipId');
    if (tooltipItem) {
      if (status) {
        if (nodeModel) {
          tooltipItem.update({
            ...tooltipItem.getModel(),
            nodeInfo: {
              baseType: nodeModel?.baseType,
              targetId: nodeModel?.id,
              targetName: nodeModel?.name,
              hostName: nodeModel?.hostName || nodeModel?.hostname,
              originType: nodeModel?.originType
            }
          });
        }        
        tooltipItem.show();
        tooltipItem.toBack();
      } else {
        tooltipItem.hide();
      }
    }
  }

  private updateTooltipPos (node?: Item) {
    if (node) {
      const { x, y, name, originType } = node.getModel();
      const tooltipItem = relationChartGraph?.findById('relationTooltipId');
      if (!tooltipItem) {
        this.addTooltipNode();
      }
      if (tooltipItem && typeof x === 'number' && typeof y === 'number') {
        relationChartGraph?.updateItem(tooltipItem, {
          x: x + 50,
          y: y + 15,
          label: String(name),
        });
      }
    }
  }

  private viewDetailByType (model: any) {
    const { baseType, targetId, targetName, hostName, originType } = model?.nodeInfo || {};
    const query: any = {};
    let path = '';
    switch (baseType) {
      case 'service':
        path = '/appMonitor/serviceDetail';
        query.sid = targetId;
        query.sn = encodeURIComponent(targetName);
        break;
      case 'process':
        path = '/infrastructure/processDetail';
        query.processName = encodeURIComponent(targetName);
        query.hostName = encodeURIComponent(hostName);
        break;
      case 'pod':
        path = '/infrastructure/pod';
        query.tags = `podName:${targetName}`;
        break;
      case 'host':
        path = '/infrastructure/host';
        query.hostName = encodeURIComponent(targetName);
        break;
    };
    if (path) {
      this.$router.push({
        path, query
      })
    }
  }


  private onMouseDown (event: any) {
    const wrapperRect = this.$refs.wrapperDom.getBoundingClientRect();
    this.contBBox.startX = wrapperRect.left;
    this.contBBox.startWidth = wrapperRect.width;
    // 记录鼠标起点
    if ('ontouchstart' in window && event.touches) {
      this.touch.startClientX = event.touches[0].clientX;
    } else {
      this.touch.startClientX = event.clientX;
    }
    this.bindEvents();
    this.touch.mouseDown = true;
  }
  private onMouseUp (event: any) {
    event.preventDefault()
    this.touch.mouseDown = false
    // Keep dragging flag until click event is finished (click happens immediately after mouseup)
    // in order to prevent emitting `splitter-click` event if splitter was dragged.
    setTimeout(() => {
      this.touch.dragging = false
      this.unbindEvents()
    }, 100)
  }

  private onMouseMove (event: any) {
    if (this.touch.mouseDown) {
      // Prevent scrolling while touch dragging (only works with an active event, eg. passive: false).
      event.preventDefault();
      this.touch.dragging = true;
      let clientX;
      if ('ontouchstart' in window && event.touches) {
        clientX = event.touches[0].clientX;
      } else {
        clientX = event.clientX;
      }
      // 增量计算宽度
      const deltaX = clientX - this.touch.startClientX;
      const startWidth = this.contBBox.startWidth || this.$refs.wrapperDom.offsetWidth;
      const newWidth = startWidth + deltaX;
      const parentDiv = this.$refs.mapCont?.parentElement || document.body;
      const parentWidth = parentDiv.getBoundingClientRect().width;
      const minWidth = parentWidth * 0.1;
      const maxWidth = parentWidth * 0.6;
      if (newWidth < minWidth || newWidth > maxWidth) {
        return;
      }
      this.contBBox.width = `${newWidth}px`;
      this.contBBox.left = `-${newWidth}px`;
      window.localStorage.setItem('DATABUFF_RM_CUSTOM_WIDTH', `${this.contBBox.width},${this.contBBox.left}`);
    }
  }

  private bindEvents () {
    document.addEventListener('mousemove', this.onMouseMove, { passive: false })
    document.addEventListener('mouseup', this.onMouseUp)

    // Passive: false to prevent scrolling while touch dragging.
    if ('ontouchstart' in window) {
      document.addEventListener('touchmove', this.onMouseMove, { passive: false })
      document.addEventListener('touchend', this.onMouseUp)
    }
  }

  private unbindEvents () {
    document.removeEventListener('mousemove', this.onMouseMove)
    document.removeEventListener('mouseup', this.onMouseUp)

    if ('ontouchstart' in window) {
      document.removeEventListener('touchmove', this.onMouseMove)
      document.removeEventListener('touchend', this.onMouseUp)
    }
    this.calcPaneSize();
  }

  private calcPaneSize () {
    if (relationChartGraph) {
      const { width, height } = document.querySelector('#asideChartCont')!.getBoundingClientRect();
      this.domBBox.width = width
      this.domBBox.height = height
      relationChartGraph.changeSize(width, height);
      this.initChart()
    }
  }

  private wrapperMouseHandle (e: any) {
    // console.log(e)
    const { offsetX, offsetY, target } = e;
    if (target.classList.contains('chart-action-item') || target.classList.contains('el-icon')) {
      return
    }
    const percentY = this.domBBox.height / 3
    // const percentY = this.domBBox.height / 4
    const level = Math.floor(offsetY / percentY) + 1
    if (this.domBBox.activeLevel !== level) {
      this.domBBox.activeLevel = level
    }
    // console.log(this.domBBox.activeLevel)
  }

  private wrapperMouseOutHandle (e: any) {
    const { relatedTarget } = e
    if (this.$refs.wrapperDom.contains(relatedTarget)) {
      return
    }
    this.domBBox.activeLevel = 0
  }
}
</script>

<style lang='scss' scoped>
.bus-map {
  position: absolute;
  top: 0;
  bottom: 0;
  background-color: var(--bg-color);
  transition: left 0.15s ease-in;
  z-index: 11;
  overflow: hidden;

  &.active {
    left: 0px;
    border-right: 1px solid #E9EAEB;
    box-shadow: 3px 0px 3px -3px #E9EAEB;
  }

  .bus-map-wrapper {
    width: 100%;
    height: 100%;
    position: relative;
    z-index: 0;
    background: #F1F1F1;

    .chart-cont {
      width: 100%;
      height: 100%;
      position: relative;
    }

    .action-group {
      position: absolute;
      right: 5px;
      top: 5px;

      .action-btn {
        margin-left: 6px;
        display: inline-flex;
        width: 24px;
        height: 24px;
        border-radius: 2px;
        font-size: 22px;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: #B5B7BB;
        transition: color .2s ease;

        &:hover {
          color: #2962FF;
        }
      }
    }

    .chart-bg-group {
      position: absolute;
      left: 0;
      right: 0;
      height: 25%;
      top: calc( 25% * ( var(--i) - 1 ) );
      pointer-events: none;
      z-index: -1;
      border-bottom: 1px solid #E9EAEB;
      &.chart-bg-group-count-4 {
        height: 33.33%;
        top: calc( 33.33% * ( var(--i) - 1 ) );
      }
      &.is-even {
        background: #F9F9F9;
      }
      &.active {
        background: #FFFFFF;
      }
      &:last-child {
        border-bottom: none;
      }
    }

    .chart-action-group {
      position: absolute;
      left: 0;
      right: 0;
      height: 25%;
      top: calc( 25% * ( var(--i) - 1 ) );
      pointer-events: none;

      &.chart-action-group-count-4 {
        height: 33.33%;
        top: calc( 33.33% * ( var(--i) - 1 ) );
      }
    }
    .chart-action-item {
      width: 40px;
      height: 100%;
      background-color: rgba(245, 245, 245, 0.8);
      cursor: pointer;
      pointer-events: auto;
      position: absolute;

      &:hover {
        .el-icon {
          color: #5394ef;
        }
      }
      &:active {
        background-color: rgba(238, 244, 254, 0.8);
      }

      .el-icon {
        color: #626467;
        font-size: 24px;
      }
    }
  }
}

.split-action {
  position: absolute;
  right: 4px;
  width: 12px;
  height: 40px;
  top: calc( 50% - 20px );
  padding-right: 4px;
  cursor: ew-resize;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;

  .split-action-icon {
    display: block;
    width: 6px;
    height: 6px;
    position: relative;
    line-height: 1;
    font-size: 0;
    margin-bottom: 3px;

    &::before, &::after {
      content: '';
      position: absolute;
      width: 1px;
      height: 5px;
      background-color: #777A7E;
      border-radius: 1px;
    }
    &::before {
      left: 0;
    }
    &::after {
      right: 0;
    }
  }
}
</style>
