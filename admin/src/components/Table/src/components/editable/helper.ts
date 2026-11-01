import { ComponentType } from '../../types/componentType';
import { useI18n } from '/@/hooks/web/useI18n';

const { t } = useI18n();

/**
 * @description: 자리 표시자 생성
 */
export function createPlaceholderMessage(component: ComponentType) {
  if (component.includes('Input')) {
    return t('common.inputText');
  }
  if (component.includes('Picker')) {
    return t('common.chooseText');
  }

  if (/Select|Cascader|Checkbox|Radio|Switch|Picker/.test(component)) {
    return t('common.chooseText');
  }
  return '';
}
