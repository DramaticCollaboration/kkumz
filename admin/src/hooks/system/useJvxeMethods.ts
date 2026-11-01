import { defHttp } from '/@/utils/http/axios';
import { ref, unref } from 'vue';
import { VALIDATE_FAILED, validateFormModelAndTables } from '/@/utils/common/vxeUtils';

export function useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys, validateSubForm?) {
  const formRef = ref();
  /** 특정 탭의 데이터 쿼리 */
  function requestSubTableData(url, params, tab, success) {
    tab.loading = true;
    defHttp
      .get({ url, params }, { isTransformResponse: false })
      .then((res) => {
        let { result } = res;
        if (res.success && result) {
          if (Array.isArray(result)) {
            tab.dataSource = result;
          } else if (Array.isArray(result.records)) {
            tab.dataSource = result.records;
          }
        }
        typeof success === 'function' ? success(res) : '';
      })
      .finally(() => {
        tab.loading = false;
      });
  }

  /* --- 이벤트 처리 --- */

  /** ATab 탭 전환 이벤트 */
  function handleChangeTabs(key) {
    // 흰색 화면을 방지하기 위해 scrollTop 상태를 자동으로 재설정합니다.
    tableRefs[key]?.value?.resetScrollTop(0);
  }

  /** 모든 editableTable 인스턴스 가져오기*/
  function getAllTable() {
    let values = Object.values(tableRefs);
    return Promise.all(values);
  }
  /** 버튼 클릭 이벤트 확인 */
  function handleSubmit() {
    /** 트리거 Form 검증 */
    getAllTable()
      .then((tables) => {
        let values = formRef.value.getFieldsValue();
        return validateFormModelAndTables(formRef.value.validate, values, tables, formRef.value.getProps, false);
      })
      .then((allValues) => {
        /** 모든 하위 테이블을 일대일로 한 번에 확인 */
        return validateSubForm && typeof validateSubForm === 'function' ? validateSubForm(allValues) : validateAllSubOne(allValues);
      })
      .then((allValues) => {
        if (typeof classifyIntoFormData !== 'function') {
          throw throwNotFunction('classifyIntoFormData');
        }
        let formData = classifyIntoFormData(allValues);
        // 요청하기
        return requestAddOrEdit(formData);
      })
      .catch((e) => {
        if (e.error === VALIDATE_FAILED) {
          // Form 확인에 실패한 하위 테이블이 있는 경우 자동으로 해당 테이블이 있는 탭으로 이동합니다.
          if(e.paneKey){
            activeKey.value = e.paneKey
          }else{
            activeKey.value = e.subIndex == null ? (e.index == null ? unref(activeKey) : refKeys.value[e.index]) : Object.keys(tableRefs)[e.subIndex];
          }
        } else {
          console.error(e);
        }
      });
  }
  //모든 하위 Form 확인
  function validateAllSubOne(allValues) {
    return new Promise((resolve) => {
      resolve(allValues);
    });
  }
  /* --- throw --- */

  /** not a function */
  function throwNotFunction(name) {
    return `${name} 정의되지 않았거나 함수가 아닙니다`;
  }

  /** not a array */
  function throwNotArray(name) {
    return `${name} 정의되지 않았거나 배열이 아닙니다`;
  }
  return [handleChangeTabs, handleSubmit, requestSubTableData, formRef];
}

/**
 * 기본 antd-vue Form에 사용되는 여러 Form 및 하위 테이블 확인
 * @param activeKey 하위 테이블 Form/vxe-table이 위치한 탭의 activeKey
 * @param refMap 하위 테이블 형식/vxe-table에 해당하는 참조 개체 맵 구조
 * 예：
 * useValidateAntFormAndTable(activeKey, {
 *   'tableA': tableARef,
 *   'formB': formBRef
 * })
 */
export function useValidateAntFormAndTable(activeKey, refMap) {
  /**
   * 모든 하위 테이블 데이터 가져오기
   */
  async function getSubFormAndTableData() {
    let formData = {};
    let all = Object.keys(refMap);
    let key = '';
    for (let i = 0; i < all.length; i++) {
      key = all[i];
      let instance = refMap[key].value;
      if (instance.isForm) {
        let subFormData = await validateFormAndGetData(instance, key);
        if (subFormData) {
          formData[key + 'List'] = [subFormData];
        }
      } else {
        let arr = await validateTableAndGetData(instance, key);
        if (arr && arr.length > 0) {
          formData[key + 'List'] = arr;
        }
      }
    }
    return formData;
  }

  /**
   * 데이터를 변환하려면 배열이 있는 경우 쉼표로 구분된 형식으로 변환하세요.
   * @param data
   */
  function transformData(data) {
    if (data) {
      Object.keys(data).map((k) => {
        if (data[k] instanceof Array) {
          data[k] = data[k].join(',');
        }
      });
    }
    return data;
  }

  /**
   * 하위 표
   * @param instance
   * @param key
   */
  async function validateTableAndGetData(instance, key) {
    const errors = await instance.validateTable();
    if (!errors) {
      return instance.getTableData();
    } else {
      activeKey.value = key;
      // 흰색 화면을 방지하기 위해 scrollTop 상태를 자동으로 재설정합니다.
      instance.resetScrollTop(0);
      return Promise.reject(1);
    }
  }

  /**
   * 하위 표 Form
   * @param instance
   * @param key
   */
  async function validateFormAndGetData(instance, key) {
    try {
      let data = await instance.getFormData();
      transformData(data);
      return data;
    } catch (e) {
      activeKey.value = key;
      return Promise.reject(e);
    }
  }

  return {
    getSubFormAndTableData,
    transformData,
  };
}
