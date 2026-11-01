import { registerComponent, registerAsyncComponent, registerASyncComponentReal } from '/@/components/sync/JVxeTable';
import { JVxeTypes } from '/@/components/sync/JVxeTable/types';
import { DictSearchSpanCell, DictSearchInputCell } from './src/components/JVxeSelectDictSearchCell';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
export async function registerJVxeCustom() {
  // ----------------- 참고 사항 -----------------
  //  컴포넌트에 BasicModal이 포함되어 있으면 비동기식 소개를 사용해야 합니다!
  //  그렇지 않으면 i18n이 실패할 것입니다!
  // ----------------- 참고 사항 -----------------

  // 【Popup】（일반 포장방법）
  await registerAsyncComponent(JVxeTypes.popup, import('./src/components/JVxePopupCell.vue'));

  // 【사전 검색 드롭다운】컴포넌트（고급 포장 방법）
  registerComponent(JVxeTypes.selectDictSearch, DictSearchInputCell, DictSearchSpanCell);

  // 【파일 업로드】컴포넌트
  await registerAsyncComponent(JVxeTypes.file, import('./src/components/JVxeFileCell.vue'));
  // 【이미지 업로드】컴포넌트
  await registerAsyncComponent(JVxeTypes.image, import('./src/components/JVxeImageCell.vue'));
  // 【사용자 선택】컴포넌트
  await registerAsyncComponent(JVxeTypes.userSelect, import('./src/components/JVxeUserSelectCell.vue'));
  // 【학과선택】컴포넌트
  await registerAsyncComponent(JVxeTypes.departSelect, import('./src/components/JVxeDepartSelectCell.vue'));
  // 【지방 및 도시 선택】컴포넌트
  // await registerAsyncComponent(JVxeTypes.pca, import('./src/components/JVxePcaCell.vue'));
  registerASyncComponentReal(
    JVxeTypes.pca,
    createAsyncComponent(() => import('./src/components/JVxePcaCell.vue'))
  );
}
