export type ThemeType = 'light' | 'dark';

export const TopoChartTheme = {
  dark: {
    title: '#EBEBED',
    bgColor: '#151515',
    typeIcon: {
      normal: '#FFFFFF',
      normalActive: '#FFFFFF',
      error: '#FFFFFF',
      errorActive: '#FFFFFF',
      selected: '#FFFFFF',
    },
    keyShapeStroke: {
      normal: 'transparent',
      normalActive: '#3D71FF',
      error: 'transparent',
      errorActive: '#E12828',
      selected: '#3D71FF',
    },
    circleStroke: {
      normal: '#727375',
      normalActive: '#3D71FF',
      error: '#E12828',
      errorActive: '#3D71FF',
    },
    circleFill: {
      normal: '#39393A',
      normalActive: '#3D71FF',
      error: '#E42828',
      errorActive: '#E42828',
    },
    comboFill: {
      normal: '#1b1b1b'
    },
    comboStroke: {
      normal: '#1e1e1e'
    },
    edgeStroke: {
      normal: '#3E3E40',
      normalActive: '#3D71FF'
    },
    edgeRelation: {
      normal: '#555658',
    },
    tooltipStroke: {
      normal: '#fff',
    },
    tooltipFill: {
      normal: '#5985FF',
    }
  },
  light: {
    title: '#45474A',
    bgColor: '#fff',
    typeIcon: {
      normal: '#121317',
      normalActive: '#FFFFFF',
      error: '#FFFFFF',
      errorActive: '#FFFFFF',
      selected: '#FFFFFF',
    },
    keyShapeStroke: {
      normal: 'transparent',
      normalActive: '#2962FF',
      error: 'transparent',
      errorActive: '#E12828',
      selected: '#2962FF',
    },
    circleStroke: {
      normal: '#B5B7BB',
      normalActive: '#2962FF',
      error: '#E12828',
      errorActive: '#2962FF',
    },
    circleFill: {
      normal: '#F5F6F7',
      normalActive: '#2962FF',
      error: '#E42828',
      errorActive: '#E42828',
    },
    comboFill: {
      normal: '#fafafa',
      normalActive: '#2962FF',
      error: '#E42828',
      errorActive: '#E42828',
    },
    comboStroke: {
      normal: '#CECFD1',
      normalActive: '#2962FF',
      error: '#E12828',
      errorActive: '#2962FF',
    },
    edgeStroke: {
      normal: '#CCCED4',
      normalActive: '#2962FF'
    },
    edgeRelation: {
      normal: '#CECFD1',
    },
    tooltipStroke: {
      normal: '#fff',
    },
    tooltipFill: {
      normal: '#5985FF',
    }
  },
}
