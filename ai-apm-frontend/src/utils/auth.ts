/**
 * 授权拦截器
 */
import { getToken } from '@/utils/jsCookie'
import SystemApi from '@/api/system'
import { toAsyncWait } from '@/utils/common';
import store from '@/store';

/** 0 = 无需 License 激活检查（开源部署） */
const ACTIVATED_STATUS = 0;

export default class AuthBuilder {
  private async __getAccountStatus () {
    const { result = {} } = await toAsyncWait(SystemApi.isOuttime());
    const { data = ACTIVATED_STATUS } = result;
    store.commit('UPDATE_AUTH_FINAL_STATUS', data)
    return data
  }

  public async getStatus () {
    if (!getToken()) {
      store.commit('UPDATE_AUTH_FINAL_STATUS', ACTIVATED_STATUS)
      return ACTIVATED_STATUS
    }
    return await this.__getAccountStatus()
  }
}
