import { $t } from '/@/locales';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: $t('appUi.k078b3a1b'),
    dataIndex: 'titile',
    width: 100,
    align: 'left',
  },
  {
    title: $t('appUi.kb2d237a7'),
    dataIndex: 'msgCategory',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: $t('appUi.k15d236a4'), value: '1', color: 'blue' },
          { label: $t('appUi.k6017b3b9'), value: '2' },
        ],
        true
      );
    },
  },
  {
    title: $t('appUi.k17bd7c06'),
    dataIndex: 'sender',
    width: 80,
  },
  {
    title: $t('appUi.kbdd3b0a6'),
    dataIndex: 'sendTime',
    width: 80,
  },
  {
    title: $t('appUi.ka0914b4d'),
    dataIndex: 'priority',
    width: 80,
    customRender: ({ text }) => {
      const color = text == 'L' ? 'blue' : text == 'M' ? 'yellow' : 'red';
      return render.renderTag(render.renderDict(text, 'priority'), color);
    },
  },
  {
    title: $t('appUi.k0389fb62'),
    dataIndex: 'readFlag',
    width: 80,
    customRender: ({ text }) => {
      return render.renderDictNative(
        text,
        [
          { label: $t('appUi.k587bed98'), value: '0', color: 'red' },
          { label: $t('appUi.kbe4f86a9'), value: '1' },
        ],
        true
      );
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'titile',
    label: $t('appUi.k078b3a1b'),
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'sender',
    label: $t('appUi.k17bd7c06'),
    component: 'Input',
    colProps: { span: 8 },
  },
];
