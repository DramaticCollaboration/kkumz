<template>
  <BasicModal
    v-model:open="visible"
    title="주소 및 지도 위치 검색"
    width="960px"
    ok-text="선택 주소 적용"
    cancel-text="취소"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="p-3">
      <a-row :gutter="16">
        <!-- 좌측: 도로명 우편번호 검색 & 장소 키워드 검색 탭 -->
        <a-col :span="12">
          <div class="search-mode-header">
            <a-radio-group v-model:value="activeMode" button-style="solid" size="middle" style="width: 100%; display: flex;">
              <a-radio-button value="postcode" style="flex: 1; text-align: center;">
                도로명 · 우편번호 검색
              </a-radio-button>
              <a-radio-button value="keyword" style="flex: 1; text-align: center;">
                장소 · 키워드 지도 검색
              </a-radio-button>
            </a-radio-group>
          </div>

          <!-- 모드 1: 다음(카카오) 우편번호 서비스 임베드 -->
          <div v-show="activeMode === 'postcode'" class="postcode-embed-box">
            <div v-if="postcodeLoading" class="postcode-loading">
              <a-spin tip="우편번호 검색창을 불러오는 중입니다..." />
            </div>
            <div v-else-if="postcodeError" class="postcode-error">
              <div style="margin-bottom: 8px; color: #cf1322;">{{ postcodeError }}</div>
              <a-space>
                <a-button size="small" type="primary" @click="mountDaumPostcode">다시 시도</a-button>
                <a-button size="small" @click="activeMode = 'keyword'">지도 키워드 검색으로 전환</a-button>
              </a-space>
            </div>
            <div ref="postcodeContainerRef" class="postcode-embed-container"></div>
          </div>

          <!-- 모드 2: 장소명/주소 키워드 지오코딩 검색 -->
          <div v-show="activeMode === 'keyword'" class="keyword-search-box">
            <a-input-search
              v-model:value="keywordQuery"
              placeholder="건물명, 기관명, 도로명 또는 지번 입력 (예: 한빛은행 본점, 도움6로 42)"
              enter-button="지도 검색"
              :loading="keywordSearching"
              allow-clear
              @search="handleKeywordSearch"
            />

            <div class="keyword-results-list">
              <div v-if="keywordSearching" class="keyword-empty">
                <a-spin tip="장소 및 주소 좌표를 검색하고 있습니다..." />
              </div>
              <div v-else-if="keywordResults.length === 0" class="keyword-empty">
                <CompassOutlined style="font-size: 28px; color: #bfbfbf; margin-bottom: 8px;" />
                <div>건물명, 도로명, 행정동으로 검색하거나</div>
                <div>우측 지도를 직접 클릭하여 위치를 선택하세요.</div>
              </div>
              <div
                v-for="item in keywordResults"
                :key="item.placeId"
                class="keyword-result-item"
                :class="{ active: selectedAddress === item.roadAddress }"
                @click="handleSelectGeocodeItem(item)"
              >
                <div class="keyword-result-title">
                  <EnvironmentOutlined style="color: #1677ff; margin-right: 4px;" />
                  <span>{{ item.roadAddress }}</span>
                  <a-tag v-if="item.postCode" color="blue" style="margin-left: 6px;">{{ item.postCode }}</a-tag>
                </div>
                <div class="keyword-result-sub">{{ item.displayName }}</div>
              </div>
            </div>
          </div>
        </a-col>

        <!-- 우측: 실시간 인터랙티브 지도 확인 및 선택 주소 편집 -->
        <a-col :span="12">
          <div class="map-section-title">
            <span>실시간 지도 위치 확인 및 핀 지정</span>
            <span class="map-section-sub">지도 클릭 또는 마커 드래그 가능</span>
          </div>

          <AddressMapView
            ref="mapViewRef"
            :address="selectedAddress"
            :address-detail="selectedAddressDetail"
            :post-code="selectedPostCode"
            :place-name="placeName"
            :height="265"
            :interactive="true"
            @pick="handleMapPick"
          />

          <!-- 선택된 주소 확인 카드 -->
          <div class="selected-address-card">
            <div class="selected-card-header">
              <span class="selected-card-title">선택된 주소 정보</span>
              <a-tag v-if="selectedAddress" color="green">위치 선택됨</a-tag>
              <a-tag v-else color="default">미선택</a-tag>
            </div>

            <a-row :gutter="[8, 8]" align="middle">
              <a-col :span="6">
                <div class="field-caption">우편번호</div>
                <a-input v-model:value="selectedPostCode" placeholder="5자리" maxlength="6" />
              </a-col>
              <a-col :span="18">
                <div class="field-caption">기본 주소</div>
                <a-input v-model:value="selectedAddress" placeholder="좌측 검색 또는 지도 클릭 시 자동 입력됩니다" />
              </a-col>
              <a-col :span="24">
                <div class="field-caption">상세 주소</div>
                <a-input
                  ref="detailInputRef"
                  v-model:value="selectedAddressDetail"
                  placeholder="동, 층, 호수 및 부서 위치 입력"
                  @pressEnter="handleConfirm"
                />
              </a-col>
            </a-row>
          </div>
        </a-col>
      </a-row>
    </div>
  </BasicModal>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import { EnvironmentOutlined, CompassOutlined } from '@ant-design/icons-vue';
import { BasicModal } from '/@/components/Modal';
import AddressMapView from './AddressMapView.vue';
import {
  ensureDaumPostcodeLoaded,
  parseDaumPostcodeData,
  searchAddressGeocode,
  type GeocodeResultItem,
} from '/@/utils/addressMap';

const emit = defineEmits<{
  (
    e: 'select',
    payload: {
      postCode: string;
      address: string;
      addressDetail: string;
    },
  ): void;
}>();

const visible = ref(false);
const activeMode = ref<'postcode' | 'keyword'>('postcode');
const placeName = ref('');

const selectedPostCode = ref('');
const selectedAddress = ref('');
const selectedAddressDetail = ref('');

const postcodeContainerRef = ref<HTMLElement | null>(null);
const postcodeLoading = ref(false);
const postcodeError = ref('');

const keywordQuery = ref('');
const keywordSearching = ref(false);
const keywordResults = ref<GeocodeResultItem[]>([]);

const mapViewRef = ref<InstanceType<typeof AddressMapView> | null>(null);
const detailInputRef = ref<any>(null);

async function open(initial?: {
  postCode?: string;
  address?: string;
  addressDetail?: string;
  placeName?: string;
  defaultMode?: 'postcode' | 'keyword';
}) {
  visible.value = true;
  selectedPostCode.value = initial?.postCode || '';
  selectedAddress.value = initial?.address || '';
  selectedAddressDetail.value = initial?.addressDetail || '';
  placeName.value = initial?.placeName || '';
  keywordQuery.value = initial?.address || initial?.placeName || '';
  activeMode.value = initial?.defaultMode || 'postcode';

  await nextTick();
  await mountDaumPostcode();

  if (selectedAddress.value) {
    mapViewRef.value?.locateByAddress(selectedAddress.value);
  }
}

async function mountDaumPostcode() {
  if (!postcodeContainerRef.value) return;
  postcodeLoading.value = true;
  postcodeError.value = '';
  try {
    await ensureDaumPostcodeLoaded();
    if (!window.daum?.Postcode || !postcodeContainerRef.value) {
      throw new Error('우편번호 검색 모듈을 초기화할 수 없습니다.');
    }
    postcodeContainerRef.value.innerHTML = '';
    const postcode = new window.daum.Postcode({
      width: '100%',
      height: '100%',
      animation: false,
      oncomplete: (data) => {
        const parsed = parseDaumPostcodeData(data);
        selectedPostCode.value = parsed.postCode;
        selectedAddress.value = parsed.address;
        if (!selectedAddressDetail.value && parsed.buildingHint) {
          selectedAddressDetail.value = parsed.buildingHint;
        }
        mapViewRef.value?.locateByAddress(parsed.address);
        // 우편번호 재검색이 가능하도록 다시 마운트 유지 및 상세 주소 포커스
        nextTick(() => {
          mountDaumPostcode();
          detailInputRef.value?.focus?.();
        });
      },
    });
    postcode.embed(postcodeContainerRef.value, {
      q: selectedAddress.value ? undefined : placeName.value || undefined,
      autoClose: false,
    });
  } catch (err: any) {
    postcodeError.value = err?.message || '우편번호 검색 서비스를 불러오지 못했습니다.';
    activeMode.value = 'keyword';
  } finally {
    postcodeLoading.value = false;
  }
}

async function handleKeywordSearch() {
  const q = keywordQuery.value.trim();
  if (!q) {
    message.warning('검색할 주소나 건물명을 입력해 주세요.');
    return;
  }
  keywordSearching.value = true;
  try {
    const list = await searchAddressGeocode(q, 8);
    keywordResults.value = list;
    if (list.length > 0) {
      handleSelectGeocodeItem(list[0]);
    } else {
      message.info('일치하는 장소 좌표를 찾지 못했습니다. 도로명 또는 행정동 단위로 검색해 보세요.');
    }
  } catch {
    message.error('지도 검색 중 오류가 발생했습니다.');
  } finally {
    keywordSearching.value = false;
  }
}

function handleSelectGeocodeItem(item: GeocodeResultItem) {
  if (item.postCode) {
    selectedPostCode.value = item.postCode;
  }
  selectedAddress.value = item.roadAddress;
  if (!selectedAddressDetail.value && item.buildingName) {
    selectedAddressDetail.value = item.buildingName;
  }
  mapViewRef.value?.setCoordinates(item.lat, item.lon, item.roadAddress);
}

function handleMapPick(payload: {
  postCode: string;
  address: string;
  buildingName?: string;
  lat: number;
  lon: number;
}) {
  if (payload.postCode) {
    selectedPostCode.value = payload.postCode;
  }
  selectedAddress.value = payload.address;
  if (!selectedAddressDetail.value && payload.buildingName) {
    selectedAddressDetail.value = payload.buildingName;
  }
}

function handleConfirm() {
  if (!selectedAddress.value.trim()) {
    message.warning('적용할 기본 주소를 먼저 검색하거나 지도에서 선택해 주세요.');
    return;
  }
  emit('select', {
    postCode: selectedPostCode.value.trim(),
    address: selectedAddress.value.trim(),
    addressDetail: selectedAddressDetail.value.trim(),
  });
  visible.value = false;
}

function handleCancel() {
  visible.value = false;
}

defineExpose({
  open,
});
</script>

<style scoped>
.search-mode-header {
  margin-bottom: 10px;
}

.postcode-embed-box {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  height: 430px;
  overflow: hidden;
  position: relative;
  background: #fafafa;
}

.postcode-embed-container {
  width: 100%;
  height: 100%;
}

.postcode-loading,
.postcode-error {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.keyword-search-box {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 12px;
  height: 430px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.keyword-results-list {
  margin-top: 10px;
  flex: 1;
  overflow-y: auto;
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}

.keyword-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  font-size: 12px;
  text-align: center;
  line-height: 1.6;
}

.keyword-result-item {
  padding: 9px 10px;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.keyword-result-item:hover {
  border-color: #91caff;
  background: #f0f7ff;
}

.keyword-result-item.active {
  border-color: #1677ff;
  background: #e6f4ff;
}

.keyword-result-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.keyword-result-sub {
  font-size: 11px;
  color: #6b7280;
  margin-top: 3px;
}

.map-section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.map-section-sub {
  font-size: 11px;
  font-weight: 400;
  color: #6b7280;
}

.selected-address-card {
  margin-top: 10px;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #f8fafc;
}

.selected-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.selected-card-title {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

.field-caption {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 2px;
}
</style>
