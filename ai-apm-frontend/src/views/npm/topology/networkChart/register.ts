import G6, { IEdge } from '@antv/g6';
import { TopoChartTheme } from './theme'
import { getIcon, getNameByWith } from './utils'

type G6Type = typeof G6;

export const CustomShapeName = {
  node: {
    name: 'network-node',
    typeIcon: 'network-node-icon',
    bgShape: 'network-node-bg',
    title: 'network-node-title',
  },
  edge: {
    name: 'network-edge',
  },
  multiEdge: {
    name: 'network-multi-edge',
  },
}

export const registerTopologyNode = (G6Class: G6Type) => {
  G6Class.registerNode(
    CustomShapeName.node.name,
    {
      drawShape (cfg: any, group) {
        const self = this
        const style = self.getShapeStyle(cfg);
        const _themeColor = TopoChartTheme.light
        const r = cfg?.size / 2;
        const title = group!.addShape(
          'text',
          {
            attrs: {
              text: getNameByWith((cfg!.name) as string || '-', 70, 10),
              x: r + 5,
              y: 0,
              fontSize: 10,
              textBaseline: 'middle',
              fontWeight: 400,
              fill: _themeColor.title,
              cursor: 'pointer',
              opacity: 1,
            },
            draggable: false,
            capture: false,
            name: CustomShapeName.node.title,
          }
        )
        const circleBg = group!.addShape(
          'circle',
          {
            attrs: {
              ...style,
              x: 0,
              y: 0,
              r,
              stroke: _themeColor.circleStroke,
              fill: _themeColor.circleFill,
              cursor: 'pointer',
            },
            draggable: true,
            name: CustomShapeName.node.bgShape
          }
        )
        const typeIcon = group!.addShape(
          'text',
          {
            attrs: {
              textBaseline: 'top',
              x: - (r * 0.85) / 2,
              y: - (r * 0.85) / 2,
              text: getIcon('aside-service'),
              fontFamily: 'db-icon',
              // fontWeight: 600,
              fontSize: r * 0.85,
              fill: _themeColor.typeIcon,
              cursor: 'pointer'
            },
            // draggable: true,
            capture: false,
            name: CustomShapeName.node.typeIcon
          }
        )
        return circleBg;
      },
      // update 方法来处理数据更新
      update (cfg: any, item) {
        const group = item!.getContainer();
        const _themeColor = TopoChartTheme.light;
        const r = cfg?.size / 2;
        // 找到需要更新的图形
        const title = group.find(e => e.get('name') === CustomShapeName.node.title);
        const bgShape = group.find(e => e.get('name') === CustomShapeName.node.bgShape);
        const typeIcon = group.find(e => e.get('name') === CustomShapeName.node.typeIcon);
        if (title) {
          title.attr({ x: r + 5 });
        }
        if (bgShape) {
          bgShape.attr({ r });
        }
        if (typeIcon) {
          typeIcon.attr({
            x: - (r * 0.85) / 2,
            y: - (r * 0.85) / 2,
            fontSize: r * 0.85,
          });
        }
      },
    },
    'circle'
  )
}

export const registerTopologyEdge = (G6Class: G6Type) => {
  G6Class.registerEdge(
    CustomShapeName.edge.name,
    {
      afterDraw (cfg: any, group) {
        const _themeColor = TopoChartTheme.light
        const size = cfg?.size;
        cfg.style = {
          stroke: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
          lineAppendWidth: 3,
          endArrow: {
            path: G6.Arrow.triangle(size * 3 + 1, size * 3 + 1, 0),
            fill: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
            stroke: 'transparent',
            d: size / 2,
          },
          cursor: 'pointer',
        }
      },
      setState (name, value, item) {
        if (!item || !item!.getContainer()) {
          return
        }
        const _themeColor = TopoChartTheme.light
        const group = item!.getContainer();
        const cfg = item!.getModel();
        const keyShape = group!.get('children')[0];
        if (name === 'active' && value) {
          keyShape.attr({
            stroke: cfg.isMax ? _themeColor.edgeStrokeMaxActive : _themeColor.edgeStrokeActive,
            endArrow: {
              ...keyShape.attrs.endArrow,
              fill: cfg.isMax ? _themeColor.edgeStrokeMaxActive : _themeColor.edgeStrokeActive,
            },
          });
        } else {
          keyShape.attr({
            stroke: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
            endArrow: {
              ...keyShape.attrs.endArrow,
              fill: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
            },
          });
        }
      },
    },
    'line',
  )
}

export const registerTopologyMultiEdge = (G6Class: G6Type) => {
  G6Class.registerEdge(
    CustomShapeName.multiEdge.name,
    {
      curveOffset: -10,
      afterDraw (cfg: any, group) {
        const _themeColor = TopoChartTheme.light
        const size = cfg?.size;
        cfg.style = {
          stroke: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
          lineAppendWidth: 3,
          endArrow: {
            path: G6.Arrow.triangle(size * 3 + 1, size * 3 + 1, 0),
            fill: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
            stroke: 'transparent',
            d: size / 2,
          },
          cursor: 'pointer',
        }
      },
      setState (name, value, item) {
        if (!item || !item!.getContainer()) {
          return
        }
        const _themeColor = TopoChartTheme.light
        const group = item!.getContainer();
        const cfg = item!.getModel();
        const keyShape = group!.get('children')[0];
        if (name === 'active' && value) {
          keyShape.attr({
            stroke: cfg.isMax ? _themeColor.edgeStrokeMaxActive : _themeColor.edgeStrokeActive,
            endArrow: {
              ...keyShape.attrs.endArrow,
              fill: cfg.isMax ? _themeColor.edgeStrokeMaxActive : _themeColor.edgeStrokeActive,
            },
          });
        } else {
          keyShape.attr({
            stroke: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
            endArrow: {
              ...keyShape.attrs.endArrow,
              fill: cfg.isMax ? _themeColor.edgeStrokeMax : _themeColor.edgeStroke,
            },
          });
        }
      },
    },
    'quadratic',
  )
}

export const tooltip = () => {
  return new G6.Tooltip({
    className: 'g6-network-tooltip',
    itemTypes: ['node', 'edge'],
    fixToNode: [0.65, 0.65],
    getContent: (e: any) => {
      const outDiv = document.createElement('div');
      const model = e.item.getModel();
      let innerHTML = ''
      if (model.type === CustomShapeName.node.name) {
        innerHTML = `<div><div class="title">${model.name}</div></div>`
        const [metric, ...others] = (model.viewData || {}).metrics || []
        innerHTML += '<div class="content">'
        if (metric) {
          innerHTML += `<div class="metric-item">${metric.name}
            <div class="value"><span>${metric._value[0]}</span> ${metric._value[1] || ''}</div>
          </div>`
        }
        others.forEach((t: any, i: number) => {
          if (i % 3 === 0) {
            innerHTML += '<div class="metrics">'
          }
          innerHTML += `<div class="metric-item">${t.name}
            <div class="value"><span>${t._value[0]}</span> ${t._value[1] || ''}</div>
          </div>`
          if (i % 3 === 2 || i === others.length - 1) {
            innerHTML += '</div>'
          }
        })
        innerHTML += '</div>'
      } else if (model.type === CustomShapeName.edge.name || model.type === CustomShapeName.multiEdge.name) {
        innerHTML = `<div><div class="title">${model.source} to ${model.target}</div></div>`
        const metrics = (model.viewData || {}).metrics || []
        const metric = metrics.slice(-1)[0]
        const others = metrics.slice(0, 2)
        innerHTML += '<div class="content">'
        others.forEach((t: any, i: number) => {
          if (i === 0) {
            innerHTML += '<div class="metrics border-bottom">'
          }
          innerHTML += `<div class="metric-item">${t.name}
            <div class="value"><span>${t._value[0]}</span> ${t._value[1] || ''}</div>
          </div>`
          if (i === others.length - 1) {
            innerHTML += '</div>'
          }
        })
        if (metric) {
          innerHTML += `<div class="metric-item">${metric.name}
            <div class="value"><span>${metric._value[0]}</span> ${metric._value[1] || ''}</div>
          </div>`
        }
        innerHTML += '</div>'
      }
      outDiv.innerHTML = innerHTML;
      return outDiv;
    },
  });
}

export const registerTopoFull = (G6Class: G6Type) => {
  registerTopologyNode(G6Class);
  registerTopologyEdge(G6Class);
  registerTopologyMultiEdge(G6Class);
}
