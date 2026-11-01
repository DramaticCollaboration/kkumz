import type { App } from 'vue';
import { registerJVxeTable } from '/@/components/sync/JVxeTable';
import { registerJVxeCustom } from '/@/components/JVxeCustom';

// 글로벌 데이 JS 등록
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

export async function registerThirdComp(app: App) {
  //---------------------------------------------------------------------
  // JVxeTable 컴포넌트 등록
  registerJVxeTable(app);
  // JVxeTable 사용자 정의 컴포넌트 등록
  await registerJVxeCustom();
  //---------------------------------------------------------------------
  // 글로벌 채팅 이모티콘 패키지 등록
  app.component(
    'Picker',
    createAsyncComponent(() => {
      return new Promise((resolve, rejected) => {
        import('emoji-mart-vue-fast/src')
          .then((res) => {
            const { Picker } = res;
            resolve(Picker);
          })
          .catch((err) => {
            rejected(err);
          });
      });
    })
  );
  //---------------------------------------------------------------------
  // 글로벌 데이 JS 등록
  dayjs.locale('ko_KR');
  dayjs.extend(relativeTime);
  dayjs.extend(customParseFormat);
  app.config.globalProperties.$dayjs = dayjs
  app.provide('$dayjs', dayjs)
  //---------------------------------------------------------------------
}
