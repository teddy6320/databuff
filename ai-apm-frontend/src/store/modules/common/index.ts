import { Commit, ActionTree, MutationTree } from 'vuex';
import { orderBy } from 'lodash';
import * as types from './index.types';
import * as utils from './utils';
import { toAsyncWait } from '@/utils/common';
import MetricApi from '@/api/metric';

export interface CommonState {
  metricTypeAndList: types.MetricTypeRawItem[] | null;
  metricTypeData: types.CascaderOptionItem[] | null;
  metricInfoMap: types.MetricInfoMap;
  tagLabelMap: types.TagLabelMap | null;
}

const commonState: CommonState = {
  metricTypeAndList: null,
  metricTypeData: null,
  metricInfoMap: {},
  tagLabelMap: null,
}

const unwrapResponseData = (response: any) => {
  if (response && typeof response === 'object' && Object.prototype.hasOwnProperty.call(response, 'data')) {
    return response.data
  }
  return response
}

const actions: ActionTree<CommonState, any> = {
  async GET_METRIC_TYPE_AND_LIST (context: { commit: Commit, state: CommonState }) {
    if (context.state.metricTypeAndList) {
      return
    }
    const { result, error } = await toAsyncWait(MetricApi.getMetricTypesByQuery());
    if (!error) {
      context.commit('SET_METRIC_TYPE_AND_LIST', unwrapResponseData(result));
    }
  },
  async GET_METRIC_TYPES (context: { commit: Commit, state: CommonState }) {
    if (context.state.metricTypeData) {
      return
    }
    const { result, error } = await toAsyncWait(MetricApi.getMetricTypes());
    if (!error) {
      context.commit('SET_METRIC_TYPES', unwrapResponseData(result));
    }
  },
  async GET_METRIC_INFOS (context: { commit: Commit, state: CommonState }, metrics: string[]) {
    return new Promise(async (resolve, reject) => {
      let _metrics = [...new Set(metrics.filter(t => !!t))];
      const mapping: any = {}
      _metrics.forEach((t: string) => {
        if (context.state.metricInfoMap[t]) {
          mapping[t] = context.state.metricInfoMap[t]
        }
      })
      _metrics = _metrics.filter((t: string) => !context.state.metricInfoMap[t])
      if (!_metrics.length) {
        resolve(mapping);
        return;
      }
      const { result, error } = await toAsyncWait(MetricApi.getMetricInfos({ metrics: _metrics }))
      if (!error) {
        const data = unwrapResponseData(result);
        context.commit('SET_METRIC_INFOS', data);
        resolve({ ...mapping, ...data });
      } else {
        reject(error);
      }
    });
  },
  async GET_TAG_LABEL_MAP (context: { commit: Commit, state: CommonState }) {
    if (context.state.tagLabelMap) {
      return
    }
    const { result, error } = await toAsyncWait(MetricApi.getAllTagKey());
    if (!error) {
      context.commit('SET_TAG_LABEL_MAP', unwrapResponseData(result));
    }
  },
}

const mutations: MutationTree<CommonState> = {
  SET_METRIC_TYPE_AND_LIST (state: CommonState, payload: any[]) {
    if (!Array.isArray(payload)) {
      return
    }
    state.metricTypeAndList = payload.map((t: any) => ({
      type1: t.type1,
      type2: t.type2,
      type3: t.type3,
      metricList: t.metricList || [],
    }));
    state.metricTypeData = utils.formatMetricTypeData(payload);
  },
  CLEAR_METRIC_TYPE_AND_LIST (state: CommonState) {
    state.metricTypeAndList = null;
    state.metricTypeData = null;
  },
  SET_METRIC_TYPES (state: CommonState, payload: any[]) {
    if (!Array.isArray(payload)) {
      return
    }
    state.metricTypeData = utils.formatMetricTypeData(payload);
  },
  SET_METRIC_INFOS (state: CommonState, payload: types.MetricInfoMap) {
    state.metricInfoMap = {
      ...state.metricInfoMap,
      ...payload,
    };
  },
  DELETE_METRIC_INFO (state: CommonState, metric: string) {
    delete state.metricInfoMap[metric];
  },
  SET_TAG_LABEL_MAP (state: CommonState, payload: types.TagLabelMap) {
    if (!payload) {
      return
    }
    const tagLabelMap: types.TagLabelMap = {}
    Object.entries(payload).forEach(([key, item]: any) => {
      const name: string = item?.name || key || ''
      tagLabelMap[key] = {
        ...item,
        name: name.split('|').filter(t => t)[0] || '',
        originName: item?.name,
      }
    })
    state.tagLabelMap = tagLabelMap;
  },
}

const getters = {
  metricTypeAndList: (state: CommonState) => state.metricTypeAndList,
  metricTypeData: (state: CommonState) => state.metricTypeData,
  metricInfoMap: (state: CommonState) => state.metricInfoMap,
  tagLabelMap: (state: CommonState) => state.tagLabelMap,
  getMetricTypeDataByType: (state: CommonState) => (types: { type1?: string; type2?: string; type3?: string } = {}) => {
    if (!state.metricTypeData) {
      return null;
    }
    const { type1, type2, type3 } = types;
    return state.metricTypeData.filter(t1 => !type1 || t1.value === type1).map(t1 => {
      const children = t1.children?.filter(t2 => !type2 || t2.value === type2).map(t2 => {
        const grandChildren = t2.children?.filter(t3 => !type3 || t3.value === type3);
        return {
          ...t2,
          children: grandChildren
        };
      });
      return {
        ...t1,
        children
      };
    });
  },
  getMetricsByType: (state: CommonState) => (types: { type1?: string; type2?: string; type3?: string } = {}) => {
    if (!state.metricTypeAndList) {
      return null
    }
    const { type1, type2, type3 } = types;
    const list = state.metricTypeAndList.filter((item) => {
      return (
        (!type1 || item.type1 === type1) &&
        (!type2 || item.type2 === type2) &&
        (!type3 || item.type3 === type3)
      );
    }).map(item => item.metricList).flat();
    return [...new Set(list)].sort();
  },
}

export default {
  namespaced: true,
  state: commonState,
  getters,
  actions,
  mutations,
}
