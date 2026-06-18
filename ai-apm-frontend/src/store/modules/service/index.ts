import { Commit, ActionTree, MutationTree, Dispatch } from 'vuex';
import { toAsyncWait } from '@/utils/common';
import ServiceApi from '@/api/service';

interface ServiceBusinessMap {
  [idOrName: string]: {
    business: { name: string, id: number } | null
    subSystem: { name: string, id: number } | null
  }
}

interface SubBusiness extends BusinessTree {
  pid: number
  type: number
}

interface BusinessTree {
  id: number
  name: string
  label: string
  value: number
  hasPermission: boolean
  disabled: boolean
  services: BasicService[]
  subsystems: SubBusiness[]
  children: SubBusiness[]
}

export interface BasicService {
  id: string
  name: string // 服务显示名称
  service: string // 服务展示名称
  service_type: string
  type: string
}
export interface BasicServiceMap {
  [id: string]: BasicService
}
export interface ServiceState {
  basicServiceMap: BasicServiceMap; // 无关管理域的所有服务
  basicGroupServiceMap: BasicServiceMap | null; // 当前管理域的服务
  businessTree: BusinessTree[]
  serviceBusinessMap: ServiceBusinessMap; // 服务与业务的映射关系
}

const serviceState: ServiceState = {
  // 服务基础信息列表
  basicServiceMap: {},
  basicGroupServiceMap: null,
  businessTree: [],
  serviceBusinessMap: {},
}

const actions: ActionTree<ServiceState, any> = {
  async GET_BASIC_SERVICE (context: { commit: Commit, state: ServiceState }) {
    const { result, error } = await toAsyncWait(ServiceApi.getAllServicesIds({ ignoreTime: 1, fromTime: '', toTime: '' }))
    if (!error) {
      const { data = [] } = result || {};
      if (Array.isArray(data)) {
        context.commit('SET_BASIC_SERVICE', { data });
      }
    }
  },
  async GET_BASIC_GROUP_SERVICE (context: { commit: Commit, state: ServiceState }) {
    if (context.state.basicGroupServiceMap) {
      return
    }
    const { result, error } = await toAsyncWait(ServiceApi.getServicesIds({ ignoreTime: 1, fromTime: '', toTime: '' }))
    if (!error) {
      const { data = [] } = result || {};
      if (Array.isArray(data)) {
        context.commit('SET_BASIC_GROUP_SERVICE', { data });
      }
    }
  },
}

const mutations: MutationTree<ServiceState> = {
  SET_BASIC_SERVICE (state: ServiceState, payload: any) {
    const { data = [] } = payload
    if (!Array.isArray(data)) {
      return
    }
    const basicServiceMap: BasicServiceMap = {}
    data.forEach((service: BasicService) => {
      basicServiceMap[service.id] = service
      basicServiceMap[service.service] = service
    })
    state.basicServiceMap = {
      ...basicServiceMap
    }
  },
  SET_BASIC_GROUP_SERVICE (state: ServiceState, payload: any) {
    const { data = [] } = payload
    if (!Array.isArray(data)) {
      return
    }
    const basicGroupServiceMap: BasicServiceMap = {}
    data.forEach((service: BasicService) => {
      basicGroupServiceMap[service.id] = service
    })
    state.basicGroupServiceMap = basicGroupServiceMap
  },
  SET_BUSINESS_TREE (state: ServiceState, payload: any) {
    const { data = [] } = payload
    if (!Array.isArray(data)) {
      return
    }
    const map: ServiceBusinessMap = {};
    data.forEach((business: BusinessTree) => {
      business.label = business.name
      business.value = business.id
      business.disabled = !business.hasPermission
      business.services.forEach((service: BasicService) => {
        map[service.id] = {
          business: { name: business.name, id: business.id },
          subSystem: null,
        }
        map[service.service] = map[service.id]
      })
      if (Array.isArray(business.subsystems) && business.subsystems.length > 0) {
        business.subsystems.forEach((subsystem: SubBusiness) => {
          subsystem.label = subsystem.name
          subsystem.value = subsystem.id
          subsystem.disabled = !subsystem.hasPermission
          subsystem.services.forEach((service: BasicService) => {
            map[service.id] = {
              business: { name: business.name, id: business.id },
              subSystem: { name: subsystem.name, id: subsystem.id },
            }
            map[service.service] = map[service.id]
          })
        });
        business.children = business.subsystems;
      }
    })
    state.serviceBusinessMap = {
      ...map,
    }
    state.businessTree = data
  },
}

// getters
const getters = {
  basicServiceMap: (state: ServiceState) => state.basicServiceMap,
  basicGroupServiceMap: (state: ServiceState) => state.basicGroupServiceMap,
  businessTree: (state: ServiceState) => state.businessTree,
  getServiceBusinessMap: (state: ServiceState) => state.serviceBusinessMap,
}

export default {
  namespaced: true,
  state: serviceState,
  getters,
  actions,
  mutations,
}
