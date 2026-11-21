import { $t } from '/@/locales';
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
            return Promise.reject($t('appUi.kcfc19259'));
          }
          if (
            value &&
            !new RegExp(
              /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
            ).test(value)
          ) {
            return Promise.reject($t('appUi.ka420271b'));
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
            return Promise.reject($t('appUi.k9a028585'));
          }
          if (value && !/^010([-\s]?)([0-9]{4})([-\s]?)([0-9]{4})$/.test(value)) {
            return Promise.reject($t('appUi.ked5ae196'));
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
            return Promise.reject($t('appUi.k42be3c29'));
          }
          if (endTime && value && dateUtil(endTime).isBefore(value)) {
            return Promise.reject($t('appUi.kd96e9496'));
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
            return Promise.reject($t('appUi.kf0681d54'));
          }
          if (startTime && value && dateUtil(value).isBefore(startTime)) {
            return Promise.reject($t('appUi.k808744cf'));
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
            return Promise.reject($t('appUi.kb0f50ff1'));
          }
          if (value !== values.password) {
            return Promise.reject($t('appUi.k9764b4dd'));
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
            return Promise.reject($t('appUi.k0b6662d8'));
          }
          return new Promise<void>((resolve, reject) => {
            duplicateCheck({
              tableName,
              fieldName,
              fieldVal: value,
              dataId: model.id,
            })
              .then((res) => {
                res.success ? resolve() : reject(res.message || $t('appUi.kae6c001b'));
              })
              .catch((err) => {
                reject(err.message || $t('appUi.k7790958f'));
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
      return Promise.reject(res.message || $t('appUi.kae6c001b'));
    }
  } catch (e) {
    return Promise.reject($t('appUi.kc3c389a5'));
  }
}
