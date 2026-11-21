import { $t } from '/@/locales';

export interface ListItem {
  id: string;
  avatar: string;
  title: string;
  titleDelete?: boolean;
  datetime: string;
  type: string;
  read?: boolean;
  description: string;
  clickClose?: boolean;
  extra?: string;
  color?: string;
  priority?: string;
}

export enum PriorityTypes {
  L = 'L',
  M = 'M',
  H = 'H',
}

export interface TabItem {
  key: string;
  name: string;
  list: ListItem[];
  unreadlist?: ListItem[];
  count: number;
}

export const tabListData: TabItem[] = [
  {
    key: '1',
    name: $t('appUi.k15d236a4'),
    list: [],
    count: 0,
  },
  {
    key: '2',
    name: $t('appUi.k6017b3b9'),
    list: [],
    count: 0,
  },
];
