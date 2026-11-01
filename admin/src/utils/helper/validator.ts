import { dateUtil } from '/@/utils/dateUtil';
import { duplicateCheck } from '/@/views/system/user/user.api';

export const rules = {
  rule(type, required) {
    if (type === 'email') {
      return this.email(required);
    }
    if (type === 'phone') {
      return this.phone(required);
    }
  },
  email(required) {
    return [
      {
        required: required ? required : false,
        validator: async (_rule, value) => {
          if (required == true && !value) {
            return Promise.reject('이메일을 입력해주세요!');
          }
          if (
            value &&
            !new RegExp(
              /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
            ).test(value)
          ) {
            return Promise.reject('올바른 이메일 형식을 입력해주세요!');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ] as ArrayRule;
  },
  phone(required) {
    return [
      {
        required: required,
        validator: async (_, value) => {
          if (required && !value) {
            return Promise.reject('휴대폰번호를 입력해주세요!');
          }
          if (value && !/^010([-\s]?)([0-9]{4})([-\s]?)([0-9]{4})$/.test(value)) {
            return Promise.reject('휴대폰 번호 형식이 잘못되었습니다.');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  startTime(endTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('시작 시간을 선택하세요.');
          }
          if (endTime && value && dateUtil(endTime).isBefore(value)) {
            return Promise.reject('시작 시간은 종료 시간보다 이전이어야 합니다.');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  endTime(startTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('종료 시간을 선택하세요.');
          }
          if (startTime && value && dateUtil(value).isBefore(startTime)) {
            return Promise.reject('종료 시간은 시작 시간보다 이후여야 합니다.');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  confirmPassword(values, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (!value) {
            return Promise.reject('비밀번호는 필수입니다.');
          }
          if (value !== values.password) {
            return Promise.reject('두 번 입력한 비밀번호가 일치하지 않습니다!');
          }
          return Promise.resolve();
        },
      },
    ];
  },
  duplicateCheckRule(tableName, fieldName, model, schema, required?) {
    return [
      {
        validator: (_, value) => {
          if (!value && required) {
            return Promise.reject(`$를 입력하세요.{schema.label}`);
          }
          return new Promise<void>((resolve, reject) => {
            duplicateCheck({
              tableName,
              fieldName,
              fieldVal: value,
              dataId: model.id,
            })
              .then((res) => {
                res.success ? resolve() : reject(res.message || '확인 실패');
              })
              .catch((err) => {
                reject(err.message || '인증 실패');
              });
          });
        },
      },
    ] as ArrayRule;
  },
};

/**
 * 네이티브About 유일한 검증 기능<a-form>사용, Vben Form 확인을 위해 위의 규칙을 사용하는 것이 좋습니다
 * @param tableName 테이블 이름
 * @param fieldName 필드 이름
 * @param fieldVal 필드 값
 * @param dataId 데이터 ID
 */
export async function duplicateValidate(tableName, fieldName, fieldVal, dataId) {
  try {
    let params = {
      tableName,
      fieldName,
      fieldVal,
      dataId: dataId,
    };
    const res = await duplicateCheck(params);
    if (res.success) {
      return Promise.resolve();
    } else {
      return Promise.reject(res.message || '확인 실패');
    }
  } catch (e) {
    return Promise.reject('네트워크 연결 끊김 및 기타 문제로 인해 확인에 실패했습니다.');
  }
}
