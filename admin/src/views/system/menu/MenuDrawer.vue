<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" showFooter :width="adaptiveWidth" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" class="menuForm" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, useAttrs } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema, ComponentTypes } from './menu.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { list, saveOrUpdateMenu } from './menu.api';
  import { useDrawerAdaptiveWidth } from '/@/hooks/sync/useAdaptiveWidth';
  import { useI18n } from "/@/hooks/web/useI18n";
  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const { adaptiveWidth } = useDrawerAdaptiveWidth();
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const menuType = ref(0);
  const isButton = (type) => type === 2;
  const [registerForm, { setProps, resetFields, setFieldsValue, updateSchema, validate, clearValidate }] = useForm({
    labelCol: {
      md: { span: 4 },
      sm: { span: 6 },
    },
    wrapperCol: {
      md: { span: 20 },
      sm: { span: 18 },
    },
    labelWidth: 145, schemas: formSchema,
    showActionButtonGroup: false,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    setDrawerProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    menuType.value = data?.record?.menuType;

    //드롭다운 트리 정보 가져오기
    const treeData = await list();
    updateSchema([
      {
        field: 'parentId',
        componentProps: { treeData: translateMenu(treeData, 'name') },
      },
      {
        field: 'name',
        label: isButton(unref(menuType)) ? '버튼/권한' : '메뉴명',
      },
      {
        field: 'url',
        required: !isButton(unref(menuType)),
        componentProps: {
          onChange: (e) => onUrlChange(e.target.value),
        },
      },
    ]);

    // 추가 또는 편집 여부에 관계없이 Form 값을 설정할 수 있습니다
    if (typeof data.record === 'object') {
      let values = { ...data.record };
      setFieldsValue(values);
      onUrlChange(values.url);
    }
    //버튼형의 경우 편집 시 주소 확인을 해제하세요.
    if (menuType.value == 2) {
      clearValidate();
    }
    //Form 비활성화
    setProps({ disabled: !attrs.showFooter });
  });
  //팝업 창 제목 가져오기
  const getTitle = computed(() => (!unref(isUpdate) ? '메뉴 추가' : '편집 메뉴'));
  //이벤트 제출
  async function handleSubmit() {
    try {
      const values = await validate();
      // iframe 호환
      if (ComponentTypes.IFrame === values.component) {
        values.component = values.frameSrc;
      }
      setDrawerProps({ confirmLoading: true });
      //Form 제출
      await saveOrUpdateMenu(values, unref(isUpdate));
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }

  /** URL이 변경되면 컴포넌트 이름 자리 표시자를 동적으로 설정합니다. */
  function onUrlChange(url) {
    let placeholder = '';
    let httpUrl = url;
    if (url != null && url != '') {
      if (url.startsWith('/')) {
        url = url.substring(1);
      }
      url = url.replaceAll('/', '-');
      // 특수 마크
      url = url.replaceAll(':', '@');
      placeholder = `${url}`;
    } else {
      placeholder = '구성요소 이름을 입력하세요.';
    }
    updateSchema([{ field: 'componentName', componentProps: { placeholder } }]);
    if (httpUrl != null && httpUrl != '') {
      if (httpUrl.startsWith('http://') || httpUrl.startsWith('https://')) {
        setFieldsValue({ component: httpUrl });
      }
    }
  }

  /**
  * 2024-03-06
  * liaozhiyang
  * 메뉴 이름 번역
  */
  function translateMenu(data, key) {
    if (data?.length) {
      const { t } = useI18n();
      data.forEach((item) => {
        if (item[key]) {
          if (item[key].includes("t('") && t) {
            item[key] = new Function('t', `return ${item[key]}`)(t);
          }
        }
        if (item.children?.length) {
          translateMenu(item.children, key);
        }
      });
    }
    return data;
  }
</script>
