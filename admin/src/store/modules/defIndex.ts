import {store} from '/@/store';
import {defineStore} from 'pinia';
import {defHttp} from "@/utils/http/axios";

interface DefIndexState {
  // 홈페이지 URL
  url: string,
  // 홈 페이지 컴포넌트
  component: string
}

export const useDefIndexStore = defineStore({
  id: 'defIndex',
  state: (): DefIndexState => ({
    url: '',
    component: '',
  }),
  getters: {},
  actions: {
    /**
     * 기본 홈페이지 구성 쿼리
     */
    async query() {
      const config = await defIndexApi.query();
      this.url = config.url;
      this.component = config.component;
    },
    /**
     * 기본 홈 페이지 구성 업데이트
     * @param url 홈페이지 url
     * @param 컴포넌트 홈 페이지 컴포넌트
     * @param isRoute는 경로입니다
     */
    async update(url: string, component: string, isRoute: boolean) {
      await defIndexApi.update(url, component, isRoute);
      await this.query()
    },

    check(url: string) {
      return url === this.url;
    }
  }
});

// Need to be used outside the setup
export function useDefIndexStoreWithOut() {
  return useDefIndexStore(store);
}

/**
 * 기본 홈페이지 구성 API
 */
export const defIndexApi = {
  /**
   * 기본 홈페이지 구성 쿼리
   */
  async query() {
    const url = '/api/v1/kkumz/sys/sysRoleIndex/queryDefIndex'
    return await defHttp.get({url});
  },
  /**
   * 기본 홈페이지 구성 업데이트
   * @param url 홈페이지 url
   * @param 컴포넌트 홈 페이지 컴포넌트
   * @param isRoute는 경로입니다
   */
  async update(url: string, component: string, isRoute: boolean) {
    let apiUrl = '/api/v1/kkumz/sys/sysRoleIndex/updateDefIndex'
    apiUrl += '?url=' + url
    apiUrl += '&component=' + component
    apiUrl += '&isRoute=' + isRoute
    return await defHttp.put({url: apiUrl});
  },

}
