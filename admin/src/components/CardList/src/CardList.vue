<template>
  <div class="p-2">
    <div class="bg-white mb-2 p-4">
      <BasicForm @register="registerForm" />
    </div>
    {{ sliderProp.width }}
    <div class="bg-white p-2">
      <List :grid="{ gutter: 5, xs: 1, sm: 2, md: 4, lg: 4, xl: 6, xxl: grid }" :data-source="data" :pagination="paginationProp">
        <template #header>
          <div class="flex justify-end space-x-2"
            ><slot name="header"></slot>
            <Tooltip>
              <template #title>
                <div class="w-50">라인당 수량 표시</div><Slider id="slider" v-bind="sliderProp" v-model:value="grid" @change="sliderChange"
              /></template>
              <Button><TableOutlined /></Button>
            </Tooltip>
            <Tooltip @click="fetch">
              <template #title>새로 고치다</template>
              <Button><RedoOutlined /></Button>
            </Tooltip>
          </div>
        </template>
        <template #renderItem="{ item }">
          <ListItem>
            <Card>
              <template #title></template>
              <template #cover>
                <div :class="height">
                  <Image :src="item.imgs[0]" />
                </div>
              </template>
              <template class="ant-card-actions" #actions>
                <!--              <SettingOutlined key="setting" />-->
                <EditOutlined key="edit" />
                <Dropdown
                  :trigger="['hover']"
                  :dropMenuList="[
                    {
                      text: '삭제',
                      event: '1',
                      popConfirm: {
                        title: '삭제 하시겠습니까?',
                        confirm: handleDelete.bind(null, item.id),
                      },
                    },
                  ]"
                  popconfirm
                >
                  <EllipsisOutlined key="ellipsis" />
                </Dropdown>
              </template>

              <CardMeta>
                <template #title>
                  <TypographyText :content="item.name" :ellipsis="{ tooltip: item.address }" />
                </template>
                <template #avatar>
                  <Avatar :src="item.avatar" />
                </template>
                <template #description>{{ item.time }}</template>
              </CardMeta>
            </Card>
          </ListItem>
        </template>
      </List>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { EditOutlined, EllipsisOutlined, RedoOutlined, TableOutlined } from '@ant-design/icons-vue';
  import { List, Card, Image, Typography, Tooltip, Slider, Avatar } from 'ant-design-vue';
  import { Dropdown } from '/@/components/Dropdown';
  import { BasicForm, useForm } from '/@/components/Form';
  import { propTypes } from '/@/utils/propTypes';
  import { Button } from '/@/components/Button';
  import { isFunction } from '/@/utils/is';
  import { useSlider, grid } from './data';
  const ListItem = List.Item;
  const CardMeta = Card.Meta;
  const TypographyText = Typography.Text;
  // 슬라이더 속성 가져오기
  const sliderProp = computed(() => useSlider(4));
  // 구성요소가 매개변수를 수신합니다.
  const props = defineProps({
    // 요청 API 매개변수
    params: propTypes.object.def({}),
    //api
    api: propTypes.func,
  });
  //내부 메소드 노출
  const emit = defineEmits(['getMethod', 'delete']);
  //데이터
  const data = ref([]);
  // 행 수 전환
  // 표지 이미지 적응형 높이
  //pageSize를 수정하고 데이터를 다시 요청하세요.

  const height = computed(() => {
    return `h-${120 - grid.value * 6}`;
  });
  //형태
  const [registerForm, { validate }] = useForm({
    schemas: [{ field: 'type', component: 'Input', label: '유형' }],
    labelWidth: 80,
    baseColProps: { span: 6 },
    actionColOptions: { span: 24 },
    autoSubmitOnEnter: true,
    submitFunc: handleSubmit,
  });
  //Form 제출
  async function handleSubmit() {
    const data = await validate();
    await fetch(data);
  }
  function sliderChange(n) {
    pageSize.value = n * 4;
    fetch();
  }

  // 내부 메서드를 자동으로 요청하고 노출합니다.
  onMounted(() => {
    fetch();
    emit('getMethod', fetch);
  });

  async function fetch(p = {}) {
    const { api, params } = props;
    if (api && isFunction(api)) {
      const res = await api({ ...params, page: page.value, pageSize: pageSize.value, ...p });
      data.value = res.items;
      total.value = res.total;
    }
  }
  //페이지네이션 관련
  const page = ref(1);
  const pageSize = ref(36);
  const total = ref(0);
  const paginationProp = ref({
    showSizeChanger: false,
    showQuickJumper: true,
    pageSize,
    current: page,
    total,
    showTotal: (total) => `총액 ${total} 건`,
    onChange: pageChange,
    onShowSizeChange: pageSizeChange,
  });

  function pageChange(p, pz) {
    page.value = p;
    pageSize.value = pz;
    fetch();
  }
  function pageSizeChange(current, size) {
    pageSize.value = size;
    fetch();
  }

  async function handleDelete(id) {
    emit('delete', id);
  }
</script>
