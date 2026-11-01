import type { App } from 'vue';
import { Icon } from './Icon';
import AIcon from '/@/components/sync/AIcon.vue';
//Tinymce 리치 텍스트
import Editor from '/@/components/Tinymce/src/Editor.vue';

import { Button, JUploadButton } from './Button';
import { GridLayout, GridItem } from 'grid-layout-plus'

// 요청 시 antd 구성요소 등록
import {
  // Need
  Button as AntButton,
  Select,
  Alert,
  Checkbox,
  DatePicker,
  TimePicker,
  Calendar,
  Radio,
  Switch,
  Card,
  List,
  Tabs,
  Descriptions,
  Tree,
  Table,
  Divider,
  Modal,
  Drawer,
  TreeSelect,
  Dropdown,
  Tag,
  Tooltip,
  Badge,
  Popover,
  Upload,
  Transfer,
  Steps,
  PageHeader,
  Result,
  Empty,
  Avatar,
  Menu,
  Breadcrumb,
  Form,
  Input,
  Row,
  Col,
  Spin,
  Space,
  Layout,
  Collapse,
  Slider,
  InputNumber,
  Carousel,
  Popconfirm,
  Skeleton,
  Cascader,
  Rate,
  Progress,
  Statistic,
  Typography,
  Timeline,
  Image,
} from 'ant-design-vue';
const compList = [AntButton.Group, Icon, AIcon, JUploadButton];

export function registerGlobComp(app: App) {
  compList.forEach((comp: any) => {
    const componentName = comp.name || comp.displayName;
    if (componentName) {
      app.component(componentName, comp);
    }
  });

  // grid-layout-plus 컴포넌트 명시적 등록 (SFC script setup 컴포넌트는 name 속성이 없어 직접 명시 등록 필요)
  app.component('GridLayout', GridLayout);
  app.component('GridItem', GridItem);

  //대시보드는 Tinymce를 사용하므로 사전에 로드해야 합니다.（요청 시 로드할 수 있는 방법 없음）
  app.component(Editor.name, Editor);
  // app.component(
  //   'Tinymce',
  //   createAsyncComponent(() => import('./Tinymce/src/Editor.vue'), {
  //     loading: true,
  //   })
  // );
  app
    .use(Select)
    .use(Alert)
    .use(Button)
    .use(Breadcrumb)
    .use(Checkbox)
    .use(DatePicker)
    .use(TimePicker)
    .use(Calendar)
    .use(Radio)
    .use(Switch)
    .use(Card)
    .use(List)
    .use(Descriptions)
    .use(Tree)
    .use(TreeSelect)
    .use(Table)
    .use(Divider)
    .use(Modal)
    .use(Drawer)
    .use(Dropdown)
    .use(Tag)
    .use(Tooltip)
    .use(Badge)
    .use(Popover)
    .use(Upload)
    .use(Transfer)
    .use(Steps)
    .use(PageHeader)
    .use(Result)
    .use(Empty)
    .use(Avatar)
    .use(Menu)
    .use(Tabs)
    .use(Form)
    .use(Input)
    .use(Row)
    .use(Col)
    .use(Spin)
    .use(Space)
    .use(Layout)
    .use(Collapse)
    .use(Slider)
    .use(InputNumber)
    .use(Carousel)
    .use(Popconfirm)
    .use(Skeleton)
    .use(Cascader)
    .use(Rate)
    .use(Progress)
    .use(Statistic)
    .use(Typography)
    .use(Timeline)
    .use(Image);
  console.log('---초기화---, Antd, 대시보드, 프로세스 디자이너, 온라인, 프로세스 및 기타 컴포넌트를 전역적으로 등록합니다.--------------');
}
