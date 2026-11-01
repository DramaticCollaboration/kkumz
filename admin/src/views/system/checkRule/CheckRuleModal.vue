<template>
  <BasicModal v-bind="$attrs" @register="registerModal" @ok="handleSubmit" :title="title" :width="1200" destroyOnClose>
    <BasicForm @register="registerForm" />

    <a-tabs v-model:activeKey="activeKey" animated>
      <a-tab-pane tab="지역 규칙" key="1" :forceRender="true">
        <JVxeTable ref="vTable1" toolbar rowNumber dragSort rowSelection :maxHeight="580" :dataSource="dataSource1" :columns="columns1">
          <template #toolbarAfter>
            <a-alert type="info" showIcon message="입력한 자릿수에 따라 순서대로 로컬 룰이 검사됩니다." style="margin-bottom: 8px" />
          </template>
        </JVxeTable>
      </a-tab-pane>
      <a-tab-pane tab="전역 규칙" key="2" :forceRender="true">
        <JVxeTable
          ref="vTable2"
          toolbar
          rowNumber
          dragSort
          rowSelection
          :maxHeight="580"
          :dataSource="dataSource2"
          :addSetActive="false"
          :columns="columns2"
        >
          <template #toolbarAfter>
            <a-alert type="info" showIcon message="전역 규칙은 사용자가 입력한 모든 문자를 확인할 수 있으며 로컬 규칙보다 우선순위가 높습니다." style="margin-bottom: 8px" />
          </template>
        </JVxeTable>
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { computed, ref, unref } from 'vue';
  import { formSchema } from './check.rule.data';
  import { saveCheckRule, updateCheckRule } from './check.rule.api';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/sync/JVxeTable/types';
  import { pick } from 'lodash-es';

  //제목 설정
  const title = computed(() => (!unref(isUpdate) ? '등록' : '편집'));
  // 방출 선언
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Form 구성
  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const activeKey = ref('1');
  let arr1: any[] = [];
  let dataSource1 = ref(arr1);
  let arr2: any[] = [];
  let dataSource2 = ref(arr2);

  //Form 할당
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Form 재설정
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    activeKey.value = '1';
    dataSource1.value = [];
    dataSource2.value = [];
    if (unref(isUpdate)) {
      //Form 할당
      await setFieldsValue({
        ...data.record,
      });

      let ruleJson = data.record.ruleJson;
      if (ruleJson) {
        let ruleList = JSON.parse(ruleJson);
        // 전역 규칙 및 로컬 규칙 필터링
        let global: any[] = [],
          design: any[] = [],
          priority = '1';
        ruleList.forEach((rule) => {
          if (rule.digits === '*') {
            global.push(Object.assign(rule, { priority }));
          } else {
            priority = '0';
            design.push(rule);
          }
        });
        dataSource1.value = design;
        dataSource2.value = global;
      }
    }
  });

  const vTable1 = ref<JVxeTableInstance>();
  const vTable2 = ref<JVxeTableInstance>();

  // Form 확인 Form 데이터 반환
  function validateMyTable(tableRef, key) {
    return new Promise((resolve, reject) => {
      tableRef.value!.validateTable().then((errMap) => {
        if (errMap) {
          activeKey.value = key;
          reject();
        } else {
          const values = tableRef.value!.getTableData();
          resolve(values);
        }
      });
    });
  }

  //Form 제출 이벤트
  async function handleSubmit() {
    let mainData;
    let globalValues = [];
    let designValues = [];
    validate()
      .then((formValue) => {
        mainData = formValue;
        return validateMyTable(vTable1, '1');
      })
      .then((tableData1: []) => {
        if (tableData1 && tableData1.length > 0) {
          designValues = tableData1;
        }
        return validateMyTable(vTable2, '2');
      })
      .then((tableData2: []) => {
        if (tableData2 && tableData2.length > 0) {
          globalValues = tableData2;
        }
        // 두 하위 테이블의 데이터 통합
        let firstGlobal: any[] = [],
          afterGlobal: any[] = [];
        for (let i = 0; i < globalValues.length; i++) {
          let v: any = globalValues[i];
          v.digits = '*';
          if (v.priority === '1') {
            firstGlobal.push(v);
          } else {
            afterGlobal.push(v);
          }
        }
        let concatValues = firstGlobal.concat(designValues).concat(afterGlobal);
        let subValues = concatValues.map((i) => pick(i, 'digits', 'pattern', 'message'));
        // 백그라운드로 전달하기 위한 formData 생성
        let ruleJson = JSON.stringify(subValues);
        let formData = Object.assign({}, mainData, { ruleJson });
        saveOrUpdateFormData(formData);
      })
      .catch(() => {
        setModalProps({ confirmLoading: false });
        console.error('확인에 실패했습니다!');
      });
  }

  // Form 제출 요청
  async function saveOrUpdateFormData(formData) {
    try {
      console.log('Form 제출 데이터', formData);
      setModalProps({ confirmLoading: true });
      if (isUpdate.value) {
        await updateCheckRule(formData);
      } else {
        await saveCheckRule(formData);
      }
      //팝업 창 닫기
      closeModal();
      //목록 새로 고침
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * 확인
   * @param cellValue
   * @param callback
   */
  const validatePatternHandler = ({ cellValue }, callback) => {
    try {
      new RegExp(cellValue);
      callback(true);
    } catch (e) {
      callback(false, '올바른 정규 표현식을 입력하세요.');
    }
  };

  const columns1 = ref<JVxeColumn[]>([
    {
      title: '자릿수',
      key: 'digits',
      type: JVxeTypes.inputNumber,
      minWidth: 180,
      validateRules: [
        { required: true, message: '${title}비워둘 수 없다' },
        { pattern: /^[1-9]\d*$/, message: '0보다 큰 양의 정수를 입력하세요.' },
      ],
    },
    {
      title: '규칙（정규식）',
      key: 'pattern',
      minWidth: 320,
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '규칙은 필수입니다.' }, { handler: validatePatternHandler }],
    },
    {
      title: '프롬프트 텍스트',
      key: 'message',
      minWidth: 180,
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '${title}비워둘 수 없다' }],
    },
  ]);

  const columns2 = ref<JVxeColumn[]>([
    {
      title: '우선순위',
      key: 'priority',
      type: JVxeTypes.select,
      defaultValue: '1',
      options: [
        { title: '우선 실행', value: '1' },
        { title: '마지막 실행', value: '0' },
      ],
      validateRules: [],
    },
    {
      title: '규칙（정규식）',
      key: 'pattern',
      width: '40%',
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '규칙은 필수입니다.' }, { handler: validatePatternHandler }],
    },
    {
      title: '프롬프트 텍스트',
      key: 'message',
      width: '20%',
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '${title}비워둘 수 없다' }],
    },
  ]);
</script>
