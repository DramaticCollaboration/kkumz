// Single Sign-On 핵심 클래스
import { getToken } from '/@/utils/auth';
import { getUrlParam } from '/@/utils';
import { useGlobSetting } from '/@/hooks/setting';
import { validateCasLogin } from '/@/api/sys/user';
import { useUserStore } from '/@/store/modules/user';
const globSetting = useGlobSetting();
const openSso = globSetting.openSso;
export function useSso() {
  let locationUrl = document.location.protocol +"//" + window.location.host + '/';

  /**
   * 싱글 사인온(SSO)
   */
  async function ssoLogin() {
    if (openSso == 'true') {
      let token = getToken();
      let ticket = getUrlParam('ticket');
      if (!token) {
        if (ticket) {
          await validateCasLogin({
            ticket: ticket,
            service: locationUrl,
          }).then((res) => {
            const userStore = useUserStore();
            userStore.setToken(res.token);
            return userStore.afterLoginAction(true, {});
          });
        } else {
          window.location.href = globSetting.casBaseUrl + '/login?service=' + encodeURIComponent(locationUrl);
        }
      }
    }
  }

  /**
   * 로그아웃
   */
  async function ssoLoginOut() {
    window.location.href = globSetting.casBaseUrl + '/logout?service=' + encodeURIComponent(locationUrl);
  }
  return { ssoLogin, ssoLoginOut };
}
