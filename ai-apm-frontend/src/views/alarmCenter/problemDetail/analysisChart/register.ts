import i18n from '@/i18n';
import G6 from '@antv/g6';
import { getDbIcon } from '@/assets/fonts/db-find-icon';

type G6Type = typeof G6;

export type ChartTheme = 'light' | 'dark';

export const colors = {
  dark: {
    border: '#272727', // 节点边框色
    borderNormalActive: '#3D71FF', // 节点选中边框色
    borderDangerActive: '#E42828', // 根节点选中边框色
    background: '#1D1D1D', // 节点背景色
    text: '#EBEBED', // 节点文本
    danger: '#E42828', // 节点文本警告色
    empty: '#08BE7E', // 节点无异常文本色
    headerBg: '#272727', // 头部背景色
    headerText: '#FFFFFF', // 头部文本
    headerIcon: '#FFFFFF', // 头部图标
    line: '#45474A', // 节点连线
  },
  light: {
    border: '#EEEFF1',
    borderNormalActive: '#2962FF',
    borderDangerActive: '#E12828',
    background: '#FFFFFF',
    text: '#121317',
    danger: '#E12828',
    empty: '#08BE7E',
    headerBg: '#EEEFF1',
    headerText: '#121317',
    headerIcon: '#121317',
    line: '#B5B7BB',
  },
}

/**
 * format the string 根据最大长度计算可展示的文字
 * @param {string} str The origin string
 * @param {number} maxWidth max width
 * @param {number} fontSize font size
 * @param {boolean} maxLine line number
 * @return {string} the processed result
 */
const multipleFittingString = (str: string, maxWidth: number, fontSize: number, maxLine: number) => {
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
  result = result.filter(i => i);
  const rst = result.splice(0, maxLine);
  if (result.length > 0) {
    const n = rst[rst.length - 1].length
    rst[rst.length - 1] = `${rst[rst.length - 1].substring(0, n - 2)}...`
  }
  return {
    text: rst.join('\n'),
    line: rst.length
  }
};

export const CustomShapeName = {
  name: 'root-cause-node',
  mainBox: 'node-main-box',
  headerBox: 'node-header-box',
  headerIcon: 'node-header-icon',
  headerText: 'node-header-text',
  headerAlarmIcon: 'node-header-alarm-icon',
  headerAlarmBox: 'node-header-alarm-box',
  headerAlarmText: 'node-header-alarm-text',
  contTextBox: 'node-cont-text-box',
  contText: 'node-cont-text',
  contEmptyText: 'node-cont-empty-text',
  rootCause: 'root-cause',
  edge: 'root-cause-edge',
  edgeLine: 'edge-line',
}

// 节点size
const nodeWidth = 180;
const nodeHeight = 96;
const CustomShapeSize = {
  width: nodeWidth,
  height: nodeHeight,
  headerWidth: nodeWidth - 1,
  headerHeight: 27.5,
  headerTextWidth: nodeWidth - 48,
  contTextTop: 50,
  contTextLineHeight: 20,
}
export const NodeSize = [nodeWidth, nodeHeight];

export const registerNode = (g6Cls: G6Type) => {
  g6Cls.registerNode(CustomShapeName.name, {
    drawShape (cfg: any, group) {
      const theme = (cfg.theme || 'dark') as ChartTheme;
      const themeColor = colors[theme];
      const shape = group!.addShape('rect', {
        attrs: {
          width: CustomShapeSize.width,
          height: CustomShapeSize.height,
          stroke: themeColor.border,
          fill: themeColor.background,
          radius: 6,
          cursor: 'pointer',
        },
        name: CustomShapeName.mainBox,
        draggable: true,
      });

      // header
      group!.addShape('rect', {
        attrs: {
          x: 0.5,
          y: 0.5,
          width: CustomShapeSize.headerWidth,
          height: CustomShapeSize.headerHeight,
          fill: themeColor.headerBg,
          radius: [5.5, 5.5, 0, 0],
          cursor: 'pointer',
        },
        name: CustomShapeName.headerBox,
        draggable: true,
      });

      // header icon
      group!.addShape('text', {
        attrs: {
          text: getDbIcon(cfg!.serviceIcon || 'default'),
          x: 11,
          y: CustomShapeSize.headerHeight / 2,
          fontSize: 16,
          fontFamily: 'db-icon',
          textBaseline: 'middle',
          fill: themeColor.headerIcon,
          cursor: 'pointer',
        },
        name: CustomShapeName.headerIcon,
      });

      // header text
      const headerTextWidth = CustomShapeSize.headerTextWidth - (cfg.alarmCount > 0 ? 32 : 0);
      group!.addShape('text', {
        attrs: {
          text: multipleFittingString(cfg!.serviceName || '-', headerTextWidth, 13, 1).text,
          x: 33,
          y: CustomShapeSize.headerHeight / 2,
          width: CustomShapeSize.headerTextWidth,
          fontSize: 13,
          lineHeight: 14,
          textBaseline: 'middle',
          fill: themeColor.headerText,
          cursor: 'pointer',
        },
        name: CustomShapeName.headerText,
      });

      if (cfg.alarmCount > 0) {
        // header alarm icon
        group!.addShape('text', {
          attrs: {
            text: getDbIcon('lamp'),
            x: CustomShapeSize.headerWidth - 37,
            y: CustomShapeSize.headerHeight / 2,
            fontSize: 14,
            fontWeight: 300,
            fontFamily: 'db-icon',
            textBaseline: 'middle',
            fill: themeColor.danger,
            cursor: 'pointer',
          },
          name: CustomShapeName.headerAlarmIcon,
        });
        const rectSize = cfg.alarmCount > 99 ? 3 : cfg.alarmCount > 9 ? 2 : 1;
        // header alarm count text
        group!.addShape('text', {
          attrs: {
            text: rectSize === 3 ? '99+' : cfg.alarmCount,
            x: CustomShapeSize.headerWidth - 14 + (rectSize - 1),
            y: CustomShapeSize.headerHeight / 2 + 1,
            width: 14 + (rectSize - 1) * 4,
            fontSize: 12,
            lineHeight: 14,
            textAlign: 'center',
            textBaseline: 'middle',
            fill: themeColor.danger,
            cursor: 'pointer',
          },
          name: CustomShapeName.headerAlarmText,
        });
      }

      // cont text
      if (cfg.rootCases.length) {
        if (!cfg!.isRoot) {
          group!.addShape('text', {
            attrs: {
              text: i18n.t('modules.views.alarmCenter.problemDetail.s_ded04c07') as string, textKey: 'modules.views.alarmCenter.problemDetail.s_ded04c07',
              x: 33,
              y: CustomShapeSize.contTextTop,
              fontSize: 12,
              lineHeight: CustomShapeSize.contTextLineHeight,
              textBaseline: 'middle',
              fill: themeColor.text,
              cursor: 'pointer',
            },
            name: CustomShapeName.contText,
          });
        } else {
          group!.addShape('rect', {
            attrs: {
              x: 33,
              y: CustomShapeSize.contTextTop - CustomShapeSize.contTextLineHeight / 2,
              width: 60,
              height: CustomShapeSize.contTextLineHeight,
              fill: themeColor.danger,
              radius: 2,
              cursor: 'pointer',
            },
            name: CustomShapeName.contTextBox,
            draggable: true,
          });
          group!.addShape('text', {
            attrs: {
              text: i18n.t('modules.views.alarmCenter.problemDetail.s_3505dbc7') as string, textKey: 'modules.views.alarmCenter.problemDetail.s_3505dbc7',
              x: 39,
              y: CustomShapeSize.contTextTop,
              fontSize: 12,
              lineHeight: CustomShapeSize.contTextLineHeight,
              textBaseline: 'middle',
              fill: '#FFFFFF',
              cursor: 'pointer',
            },
            name: CustomShapeName.contText,
          });
        }

        // root cause
        group!.addShape('text', {
          attrs: {
            text: multipleFittingString(cfg!.rootCaseText || '-', CustomShapeSize.headerTextWidth, 12, 1).text,
            x: 33,
            y: CustomShapeSize.contTextTop + 2 + CustomShapeSize.contTextLineHeight,
            width: CustomShapeSize.headerTextWidth,
            fontSize: 12,
            lineHeight: CustomShapeSize.contTextLineHeight,
            textBaseline: 'middle',
            fill: cfg!.isRoot ? themeColor.danger : themeColor.text,
            cursor: 'pointer',
          },
          name: CustomShapeName.rootCause,
        });
      } else {
        group!.addShape('text', {
          attrs: {
            text: i18n.t('modules.views.alarmCenter.problemDetail.s_6c5443ce') as string, textKey: 'modules.views.alarmCenter.problemDetail.s_6c5443ce',
            x: 33,
            y: CustomShapeSize.contTextTop + 11,
            fontSize: 12,
            lineHeight: CustomShapeSize.contTextLineHeight,
            textBaseline: 'middle',
            fill: themeColor.empty,
            cursor: 'pointer',
          },
          name: CustomShapeName.contEmptyText,
        });
      }

      return shape;
    },
    update (cfg: any, node) {
      const theme = (cfg.theme || 'dark') as ChartTheme;
      const themeColor = colors[theme];
      const group = node.getContainer(); // 获取容器
      const mainBox = group.find((i: any) => i.get('name') === CustomShapeName.mainBox);
      const headerBox = group.find((i: any) => i.get('name') === CustomShapeName.headerBox);
      const headerIcon = group.find((i: any) => i.get('name') === CustomShapeName.headerIcon);
      const headerText = group.find((i: any) => i.get('name') === CustomShapeName.headerText);
      const headerAlarmIcon = group.find((i: any) => i.get('name') === CustomShapeName.headerAlarmIcon);
      const headerAlarmBox = group.find((i: any) => i.get('name') === CustomShapeName.headerAlarmBox);
      const contText = group.find((i: any) => i.get('name') === CustomShapeName.contText);
      const contEmptyText = group.find((i: any) => i.get('name') === CustomShapeName.contEmptyText);
      const rootCause = group.find((i: any) => i.get('name') === CustomShapeName.rootCause);

      // 设置主题色
      mainBox.attr({ stroke: themeColor.border, fill: themeColor.background, });
      headerBox.attr({ fill: themeColor.headerBg, });
      headerIcon.attr({ fill: themeColor.headerIcon, });
      headerText.attr({ fill: themeColor.headerText, });
      headerAlarmIcon && headerAlarmIcon.attr({ fill: themeColor.headerIcon, });
      headerAlarmBox && headerAlarmBox.attr({ fill: themeColor.danger, });
      contText && contText.attr({ fill: themeColor.text, });
      contEmptyText && contEmptyText.attr({ fill: themeColor.empty, });
      rootCause && rootCause.attr({ fill: cfg.isRoot ? themeColor.danger : themeColor.text, });
    },
    getAnchorPoints: () => [[0.5, 0], [0.5, 1]],
    setState (name, value, item) {
      if (!item) {
        return
      }
      const group = item.getContainer();
      group.attr('opacity', name === 'inactive' && value ? 0.3 : 1);

      const nodeModel: any = item?.getModel();
      const theme = (nodeModel.theme || 'dark') as ChartTheme;
      const themeColor = colors[theme];
      const mainBox = group.find((i: any) => i.get('name') === CustomShapeName.mainBox);
      if (!item.hasState('nodeChoosed')) {
        mainBox.attr({ stroke: themeColor.border, });
      } else if (nodeModel.isRoot && nodeModel.rootCases.length) {
        mainBox.attr({ stroke: themeColor.borderDangerActive, });
      } else {
        mainBox.attr({ stroke: themeColor.borderNormalActive, });
      }
    },
  }, 'single-node')
}

export const registerEdge = (g6Cls: G6Type) => {
  g6Cls.registerEdge(CustomShapeName.edge, {
    afterDraw (cfg: any, group) {
      const theme = (cfg.theme || 'dark') as ChartTheme;
      const themeColor = colors[theme];
      cfg.style = {
        stroke: themeColor.line,
        lineWidth: 1,
        endArrow: {
          path: G6.Arrow.triangle(8, 8, 0),
          fill: themeColor.line,
        },
      }
      // 更新edge的label样式
      const lineText = group!.find((i: any) => i.get('type') === 'text');
      lineText && lineText.attr({
        fill: themeColor.text,
        fontSize: 11,
        stroke: themeColor.background,
        lineWidth: 3,
      });
    },
    setState (name, value, item) {
      if (!item) {
        return
      }
      const group = item.getContainer();
      group.attr('opacity', name === 'inactive' && value ? 0.3 : 1);
    },
  }, 'cubic-vertical')
}
