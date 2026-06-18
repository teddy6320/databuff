import G6, { IEdge } from '@antv/g6';
import { getDbIcon } from '@/assets/fonts/db-find-icon';
import { v4 as uuidv4 } from 'uuid';
import deepClone from 'lodash/cloneDeep';

export const CustomShapeName = {
  apmCombo: {
    name: 'apm-combo',
    keyShape: 'combo-key-shape',
    typeIcon: 'combo-type-icon',
    bgShape: 'combo-bg-shape',
    title: 'combo-title',
    collapseMarker: 'combo-marker-shape',
  },
  apmNode: {
    name: 'apm-node',
    keyShape: 'node-key-shape',
    typeIcon: 'node-type-icon',
    bgShape: 'node-bg',
    title: 'node-title',
  },
  apmEdge: {
    name: 'apm-edge',
    keyShape: 'apm-edge-key-shape',
    reqLabel: 'apm-edge-req-label',
    resLabel: 'apm-edge-res-label',
    rateLabel: 'apm-edge-rate-label',
  },
  apmHEdge: {
    name: 'apm-edge-h',
    keyShape: 'apm-edge-key-shape',
    reqLabel: 'apm-edge-req-label',
    resLabel: 'apm-edge-res-label',
    rateLabel: 'apm-edge-rate-label',
  },
  npmEdge: {
    name: 'npm-edge',
    keyShape: 'npm-edge-key-shape',
    vrcvdLabel: 'npm-edge-vrcvd-label',
    vsentLabel: 'npm-edge-vsent-label',
    trcvdLabel: 'npm-edge-trcvd-label',
    tsentLabel: 'npm-edge-tsent-label',
  },
  npmHEdge: {
    name: 'npm-edge-h',
    keyShape: 'npm-edge-key-shape',
    vrcvdLabel: 'npm-edge-vrcvd-label',
    vsentLabel: 'npm-edge-vsent-label',
    trcvdLabel: 'npm-edge-trcvd-label',
    tsentLabel: 'npm-edge-tsent-label',
  },
  tooltip: {
    id: 'apmTooltipId',
    name: 'apm-tooltip',
    keyShape: 'apm-tooltip-key-shape',
    placeholdCircle: 'apm-tooltip-placehold-circle',
    bodyLabel: 'apm-tooltip-body-label',
  }
};

type ChartTheme = 'light' | 'dark';

const NODE_SIZE = 40;

const getCircularLevel = (index: number) => {
  return Math.floor(Math.log10(index + 1)) + 1;
}

// 多行文本
export const multipleFittingString = (str: string, maxWidth: number, fontSize: number, maxLine: number) => {
  let currentWidth = 0;
  let splitIdx = 0;
  const splits: number[] = [];
  const pattern = new RegExp('[\u4E00-\u9FA5]+'); // distinguish the Chinese charactors and letters
  str.split('').forEach((letter, i) => {
    if (currentWidth > maxWidth) {
      splitIdx = i;
      splits.push(splitIdx);
      currentWidth = 0;
    };
    if (pattern.test(letter)) {
      // Chinese charactors
      currentWidth += fontSize;
    } else {
      // get the width of single letter according to the fontSize
      currentWidth += G6.Util.getLetterWidth(letter, fontSize);
    }
    
  });
  if (str && !splits.length) {
    splits.push(str.length);
  }
  let result: string[] = [];
  splits.forEach((idx, i) => {
    if (i === 0) {
      result.push(str.slice(0, idx));
    } else {
      result.push(str.slice(splits[i - 1], idx));
    }
    if (i === splits.length - 1) {
      result.push(str.slice(idx));
    }
  });
  result = result.filter( i => i);
  const rst = result.splice(0, maxLine);
  if (result.length > rst.length) {
    rst[rst.length - 1] = `${rst[rst.length - 1]}...`
  }
  return {
    text: rst.join('\n'),
    line: rst.length
  }
};

// 根据宽度截取字符串
export const getNameByWith = (name: string, width: number, fontsize: number, needLine: boolean = false) => {
  const scale = fontsize / 12
  const _width = width / scale
  const span = document.createElement('span')
  span.style.visibility = 'hidden'
  span.style.whiteSpace = 'nowrap'
  span.style.fontFamily = 'sans-serif'
  span.style.fontSize = '12px'
  document.body.appendChild(span)

  const getText = (str: string, reverse?: boolean, prefix?: string) => {
    let text = ''
    if (reverse) {
      str = str.split('').reverse().join('')
    }
    for (let j = 0; j < str.length; j++) {
      span.innerText = (prefix || '') + text + str[j]
      if (span.offsetWidth <= _width) {
        text += str[j]
      } else {
        break
      }
    }
    if (reverse) {
      text = text.split('').reverse().join('')
    }
    return (prefix || '') + text
  }

  let showName = getText(name)
  let line = 1;
  if (showName.length < name.length) {
    const _name = name.substring(showName.length)
    const _sn = getText(_name)
    if (_name.length < 4) {
      showName = name
    } else {
      if (_sn === _name) {
        showName = `${showName}\n${_sn}`
      } else {
        showName = `${showName}\n${getText(_name, true, '...')}`
      }
      line = 2;
    }
  }
  document.body.removeChild(span)
  return needLine ? {
    line, showName
  } : showName
}

export const getIcon = getDbIcon;

export const formatSourceTheme = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light') => {
  const { nodes, edges } = source;
  const nodeLength = nodes.length;
  const nodeType = nodeLength > 500 ? 'circle' : CustomShapeName.apmNode.name;

  nodes.forEach((n, i) => {
    n.theme = theme;
    n.nodeIndex = i;
    // n.name = getNameByWith(n.name, 60, 10)
    if (n.children && Array.isArray(n.children)) {
      n.children.forEach((c: any) => {
        c.theme = theme;
        // c.name = getNameByWith(c.name, 60, 10)
      })
    }
    n.type = nodeType;
    if (nodeType === 'circle') {
      const isError = n?.custom && (n.custom as any).error ? true : false;
      n.label = getNameByWith(n.name, 60, 7);
      n.labelCfg = {
        style: {
          fill: theme === 'light' ? '#45474A' : '#EBEBED',
          fontSize: 9,
          opacity: 1,
          text: getNameByWith(n.name, 60, 7)
        },
        position: 'top',
      }
      n.style = {
        fill: theme === 'light' ? (isError ? '#E42828' : '#F7F8FA') : (isError ? '#E42828' : '#262626'),
        stroke: theme === 'light' ? '#d9d9d9' : '#ACACAC'
      }
    }
  })

  edges.forEach((e) => {
    e.theme = theme;
  });

  return source;
}


export const formatApplicationSource = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light') => {
  const { nodes, edges } = source;
  
  formatSourceTheme(source, theme);
  nodes.forEach((n, index) => {
    n.id = `app${n.id}`
    n.baseType = 'application';
    // n.type = CustomShapeName.apmNode.name;
    n.degree = getCircularLevel(index);
    // n.custom = {
    //   error: !!n.alarmCount,
    // }
    
  });

  edges.forEach((e) => {
    e.baseType = 'application';
  });

  return source;
}

export const formatBusinessSource = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light') => {
  const { nodes, edges } = source;
  const _nodes: any[] = [];
  const _combos: any[] = [];
  formatSourceTheme(source, theme);
  nodes.forEach((n) => {
    const group: any[] = [];
    const _bsItem = {
      id: n.id,
      label: n.name,
      name: n.name,
      typeIcon: 'bs',
      custom: {
        hasChild: n.children && n.children.length,
        error: !!n.alarmCount,
      },
      theme: n.theme,
      baseType: 'business',
      type: CustomShapeName.apmCombo.name,
      extraInfo: {
        ...n.extraInfo
      }
    }
    if (n.children && n.children.length) {
      const comboItem: any = {
        id: uuidv4(),
        padding: 5,
        collapsed: false,
        label: '',
        theme: n.theme,
      }
      
      // _combos.push(comboItem)
      const __item = {
        ..._bsItem,
        comboId: comboItem.id,
        typeIcon: 'bs',
        type: CustomShapeName.apmNode.name,
      }
      _nodes.push(__item);
      group.push(__item);

      n.children.forEach((c: any) => {
        const __subItem = {
          id: c.id,
          label: c.name,
          name: c.name,
          pname: n.name,
          pid: n.id,
          comboId: comboItem.id,
          theme: n.theme,
          typeIcon: 'subbs',
          custom: {
            error: !!c.alarmCount,
          },
          baseType: 'business',
          type: CustomShapeName.apmNode.name,
          extraInfo: {
            ...c.extraInfo
          }
        }
        _nodes.push(__subItem);
        group.push(__subItem);
      });
      const pipes = getBsCustomConcentricPipes(group)
      // comboItem.fixSize = (pipes[pipes.length - 1].radius + NODE_SIZE) * 2
      _combos.push(comboItem)

    } else {
      _nodes.push({
        ..._bsItem,
        typeIcon: 'bs',
        type: CustomShapeName.apmNode.name,
      });
    }
  });

  edges.forEach((e) => {
    e.baseType = 'business';
    e.theme = theme
    e.type = CustomShapeName.apmEdge.name;
  });

  return {
    nodes: _nodes,
    edges,
    combos: _combos
  };
}

export const formatServiceSource = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light') => {
  const { nodes, edges } = source;

  nodes.forEach((n) => {
    n.baseType = 'service';
    n.label = n.name
    // n.originType = n?.originType
    n.extraInfo = {
      ...n.extraInfo
    }
  });

  edges.forEach((e) => {
    e.baseType = 'service';
  });
  formatSourceTheme(source, theme);

  return source;
}

export const formatContainerSource = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light') => {
  const { nodes, edges } = source;

  formatSourceTheme(source, theme);

  nodes.forEach((n, index) => {
    n.baseType = 'container';
    n.degree = getCircularLevel(index);
  });

  edges.forEach((e) => {
    e.baseType = 'container';
    e.theme = theme
    e.type = CustomShapeName.npmEdge.name;
  });

  return source;
}
export const formatProcessSource = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light') => {
  const { nodes, edges } = source;

  formatSourceTheme(source, theme);
  nodes.forEach((n, index) => {
    if (n.originType === 'pod') {
      n.baseType = 'pod';
    } else {
      n.baseType = 'process';
    }
    n.degree = getCircularLevel(index);
  });

  edges.forEach((e) => {
    e.baseType = 'process';
    e.theme = theme
    e.type = CustomShapeName.npmEdge.name;
  });
  return source;
}

export const formatHostSource = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light') => {
  const { nodes, edges } = source;

  formatSourceTheme(source, theme);
  nodes.forEach((n, index) => {
    n.baseType = 'host';
    n.degree = getCircularLevel(index);
  });

  edges.forEach((e) => {
    e.baseType = 'host';
    e.theme = theme
    e.type = CustomShapeName.npmEdge.name;
  });

  return source;
}

export const formatSourceByBaseType = (source: { nodes: any[], edges: any[] }, theme: ChartTheme = 'light', baseType: any) => {
  switch (baseType) {
    case 'application':
      return formatApplicationSource(source, theme);
    case 'business':
      return formatBusinessSource(source, theme);
    case 'service':
      return formatServiceSource(source, theme);
    case 'container':
      return formatContainerSource(source, theme);
    case 'process':
      return formatProcessSource(source, theme);
    case 'host':
      return formatHostSource(source, theme);
    default:
      return formatSourceTheme(source, theme);
  }
}

export const getBsCustomConcentricPipes = (nodes: any[]) => {
  const getMaxLevel = (total: number) => {
    if (total <= 1) {
      return 1
    }
    const limit = (total - 1) / 4;
    let i = 1;
    while (i * (i + 1) < limit) {
      i++;
    }
    return i + 1;
  }
  const size = NODE_SIZE;
  const nodeStep = 8; // 每个环的节点数以8递增
  const nodeTotal = (nodes || []).length;
  const pipes: any[] = [];
  
  let maxLevel = getMaxLevel(nodeTotal); // 最大层级

  // 最后一层只有一个节点是时，需要特殊处理，把点加到上一层里 -- 【增加】如果只有两层的情况下，不处理
  const secondLastCount = (maxLevel - 2) / 2 * (maxLevel - 1) * nodeStep + 1; // 倒数第二层的节点数
  const lastOnlyOneNode = nodeTotal - secondLastCount === 1; // 最后一层是否只有一个节点
  if (lastOnlyOneNode && maxLevel > 2) {
    maxLevel = maxLevel - 1;
  }
  // console.log(nodes.map((i) => i.name));
  for (let i = 0; i < maxLevel; i++) {
    const min = (i - 1) / 2 * i * nodeStep;
    const max = i / 2 * (i + 1) * nodeStep;
    const pipeItem: any = {
      radius: (size + 15) * i,
      nodes: nodes.filter((n, idx) => idx > min && idx <= max),
    }
    if (i === 0) {
      pipeItem.nodes = nodes[0]
    } else if (i === maxLevel - 1) { // 最后一层
      pipeItem.nodes = nodes.filter((n, idx) => idx > min)
    }
    pipes.push(pipeItem);
  }
  return pipes
}
export const calcComboPosition = (combos: any[]) => {
  combos.forEach((c) => {
    c.size = c.fixSize;
  });
  const maxSize = combos.map(i => i.fixSize)
  const forceLayout = new G6.Layout.force({
    preventOverlap: true,
  });
  forceLayout.init({
    nodes: combos,
    edges: [],
  })
  forceLayout.execute();
  // console.log(combos)
}
export const getServiceConcatConcentricPipesByInitRadius = (baseRadius: number, nodeSize: number, nodes: any[]) => {
  
  // const nodeStep = 8; // 每个环的节点数以8递增
  // 每环的节点数，需根据半径和节点大小计算
  const cloneNodes = deepClone(nodes);
  const nodeDepth: any[][] = [];
  let depth = 0;
  const nodeTotal = (nodes || []).length;
  const nodeDepthMap: any = {};

  while (cloneNodes.length > 0) {
    const calcRadius = baseRadius + depth * nodeSize * 2;
    const circleLen = calcRadius * Math.PI * 2;
    const nodeLen = Math.floor(circleLen / (nodeSize * 2)); // nodeSize 直径 + 间隙
    const depthNodes = cloneNodes.splice(0, nodeLen);
    depthNodes.forEach((n) => {
      n.outDepth = depth;
      nodeDepthMap[n.id] = depth;
    })
    nodeDepth.push(depthNodes); // 利用splice方法变更cloneNodes长度，while中判断length为0跳出
    depth += 1;
  }

  const pipes = nodeDepth.map((nd, index) => ({
    type: 'circular',
    center: [0, 0], // 环的中心
    radius: baseRadius + index * nodeSize * 2, // 环的半径
    startAngle: 0,
    endAngle: Math.PI * 2,
    nodesFilter: (node: any) => node.outDepth === index,
  }));

  nodes.forEach((n) => {
    if (Object.prototype.hasOwnProperty.call(nodeDepthMap, n.id)) {
      n.outDepth = nodeDepthMap[n.id]
    }
  })

  return pipes
}
