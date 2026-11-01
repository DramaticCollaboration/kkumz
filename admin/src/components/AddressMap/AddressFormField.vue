<template>
  <div class="address-form-field">
    <!-- 1행: 우편번호 + 주소·지도 검색 액션 버튼 그룹 -->
    <div class="address-row-top">
      <a-input
        :value="postCode"
        placeholder="우편번호"
        style="width: 115px;"
        maxlength="6"
        @update:value="(val: string) => emit('update:postCode', val)"
        @click="!postCode && !address ? openSearchModal('postcode') : undefined"
      />
      <a-button type="primary" ghost @click="openSearchModal('postcode')">
        <template #icon><SearchOutlined /></template>
        우편번호 · 주소 검색
      </a-button>
      <a-button @click="openSearchModal('keyword')">
        <template #icon><EnvironmentOutlined /></template>
        지도 검색
      </a-button>
      <a-button
        v-if="address"
        :type="showInlineMap ? 'primary' : 'default'"
        @click="showInlineMap = !showInlineMap"
      >
        <template #icon><CompassOutlined /></template>
        {{ showInlineMap ? '지도 닫기' : '지도 위치 확인' }}
      </a-button>
      <a-button
        v-if="postCode || address || addressDetail"
        type="text"
        danger
        size="small"
        @click="handleClear"
      >
        초기화
      </a-button>
    </div>

    <!-- 2행: 기본 주소 -->
    <div class="address-row-item">
      <a-input
        :value="address"
        placeholder="도로명 또는 지번 기본 주소"
        allow-clear
        @update:value="(val: string) => emit('update:address', val)"
        @click="!address ? openSearchModal('postcode') : undefined"
      >
        <template #prefix>
          <EnvironmentOutlined style="color: #8c8c8c;" />
        </template>
      </a-input>
    </div>

    <!-- 3행: 상세 주소 -->
    <div class="address-row-item">
      <a-input
        ref="detailInputRef"
        :value="addressDetail"
        placeholder="동, 층, 호수, 부서 위치 등 상세 주소 입력"
        allow-clear
        @update:value="(val: string) => emit('update:addressDetail', val)"
      />
    </div>

    <!-- 4행: 인라인 지도 위치 미리보기 (선택 시 또는 토글 시 노출) -->
    <div v-if="showInlineMap && address" class="address-inline-map">
      <AddressMapView
        :address="address"
        :address-detail="addressDetail"
        :post-code="postCode"
        :place-name="placeName"
        :height="200"
        :interactive="true"
        @pick="handleInlineMapPick"
      />
    </div>

    <!-- 통합 주소·지도 검색 모달 -->
    <AddressSearchModal ref="searchModalRef" @select="handleModalSelect" />
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import { SearchOutlined, EnvironmentOutlined, CompassOutlined } from '@ant-design/icons-vue';
import AddressMapView from './AddressMapView.vue';
import AddressSearchModal from './AddressSearchModal.vue';

const props = withDefaults(
  defineProps<{
    postCode?: string;
    address?: string;
    addressDetail?: string;
    placeName?: string;
  }>(),
  {
    postCode: '',
    address: '',
    addressDetail: '',
    placeName: '',
  },
);

const emit = defineEmits<{
  (e: 'update:postCode', value: string): void;
  (e: 'update:address', value: string): void;
  (e: 'update:addressDetail', value: string): void;
  (
    e: 'change',
    payload: {
      postCode: string;
      address: string;
      addressDetail: string;
    },
  ): void;
}>();

const searchModalRef = ref<InstanceType<typeof AddressSearchModal> | null>(null);
const detailInputRef = ref<any>(null);
const showInlineMap = ref(false);

function openSearchModal(mode: 'postcode' | 'keyword' = 'postcode') {
  searchModalRef.value?.open({
    postCode: props.postCode,
    address: props.address,
    addressDetail: props.addressDetail,
    placeName: props.placeName,
    defaultMode: mode,
  });
}

function handleModalSelect(payload: {
  postCode: string;
  address: string;
  addressDetail: string;
}) {
  emit('update:postCode', payload.postCode);
  emit('update:address', payload.address);
  emit('update:addressDetail', payload.addressDetail);
  emit('change', payload);
  showInlineMap.value = true;
  nextTick(() => {
    detailInputRef.value?.focus?.();
  });
}

function handleInlineMapPick(payload: {
  postCode: string;
  address: string;
  buildingName?: string;
}) {
  if (payload.postCode) {
    emit('update:postCode', payload.postCode);
  }
  emit('update:address', payload.address);
  if (!props.addressDetail && payload.buildingName) {
    emit('update:addressDetail', payload.buildingName);
  }
}

function handleClear() {
  emit('update:postCode', '');
  emit('update:address', '');
  emit('update:addressDetail', '');
  showInlineMap.value = false;
}
</script>

<style scoped>
.address-form-field {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-row-top {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.address-row-item {
  width: 100%;
}

.address-inline-map {
  margin-top: 2px;
}
</style>
