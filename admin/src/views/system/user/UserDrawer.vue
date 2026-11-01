<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="getTitle"
    :width="adaptiveWidth"
    @ok="handleSubmit"
    :showFooter="showFooter"
    destroyOnClose
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, useAttrs } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './user.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { saveOrUpdateUser, getUserRoles, getUserDepartList, getAllRolesList } from './user.api';
  import { useDrawerAdaptiveWidth } from '/@/hooks/sync/useAdaptiveWidth';

  // 방출 선언
  const emit = defineEmits(['success', 'register']);
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const rowId = ref('');
  const departOptions = ref([]);
  //Form 구성
  const [registerForm, { setProps, resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 115,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  const showFooter = ref(true);
  //Form 할당
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    showFooter.value = data?.showFooter ?? true;
    setDrawerProps({ confirmLoading: false, showFooter: showFooter.value });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      rowId.value = data.record.id;

      try {
        const userRoles = await getUserRoles({ userid: data.record.id });
        if (userRoles && userRoles.length > 0) {
          data.record.selectedroles = userRoles;
        }
      } catch (error) {
        console.error('사용자 역할 조회 실패:', error);
      }

      //부서/직무를 확인하세요
      const userDepart = await getUserDepartList({ userId: data.record.id });
      if (userDepart && userDepart.length > 0) {
        data.record.selecteddeparts = userDepart;
        let selectDepartKeys = Array.from(userDepart, ({ key }) => key);
        data.record.selecteddeparts = selectDepartKeys.join(',');
        departOptions.value = userDepart.map((item) => {
          return { label: item.title, value: item.key };
        });
      }
      //담당부서/업무
      data.record.departIds && !Array.isArray(data.record.departIds) && (data.record.departIds = data.record.departIds.split(','));
      data.record.departIds = (!data.record.departIds || data.record.departIds == '') ? [] : data.record.departIds;
    }
    //역할 사용자 목록 상황 처리(역할 목록 관련)
    data.selectedroles && (await setFieldsValue({ selectedroles: data.selectedroles }));
    //편집 시 비밀번호 숨기기/역할 목록에서 역할 정보 숨기기/내 부서 편집 시 부서 숨기기
    updateSchema([
      {
        field: 'password',
        ifShow: !unref(isUpdate),
      },
      {
        field: 'confirmPassword',
        ifShow: !unref(isUpdate),
      },
      {
        field: 'selectedroles',
        show: !data.isRole,
      },
      {
        field: 'departIds',
        componentProps: { options: departOptions },
      },
      {
        field: 'selecteddeparts',
        show: !data?.departDisabled,
      },
      {
        field: 'selectedroles',
        show: !data?.departDisabled,
        componentProps: {
          api: getAllRolesList,
        },
      },
    ]);
    // 추가 또는 편집 여부에 관계없이 Form 값을 설정
    if (typeof data.record === 'object') {
      setFieldsValue({
        ...data.record,
      });
    }
    // 하단을 숨길 때 전체 Form 비활성화
    setProps({ disabled: !showFooter.value });
  });
  //제목 가져오기
  const getTitle = computed(() => {
    if (!unref(isUpdate)) {
      return '사용자 추가';
    } else {
      return unref(showFooter) ? '사용자 편집' : '사용자 세부정보';
    }
  });
  const { adaptiveWidth } = useDrawerAdaptiveWidth();

  //이벤트 제출
  async function handleSubmit() {
    try {
      let values = await validate();
      setDrawerProps({ confirmLoading: true });
      values.userIdentity === 1 && (values.departIds = '');
      let isUpdateVal = unref(isUpdate);
      //Form 제출
      await saveOrUpdateUser(values, isUpdateVal);
      //팝업 창 닫기
      closeDrawer();
      //목록 새로 고침
      emit('success', { isUpdateVal, values });
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
