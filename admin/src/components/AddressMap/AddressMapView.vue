<template>
  <div class="address-map-view-wrap">
    <!-- 상단 주소 요약 및 외부 지도 연동 바 -->
    <div class="address-map-toolbar">
      <div class="address-map-toolbar-left">
        <EnvironmentOutlined class="pin-icon" />
        <span v-if="fullAddressText" class="address-label" :title="fullAddressText">
          <a-tag v-if="postCode" color="blue" style="margin-right: 6px;">{{ postCode }}</a-tag>
          {{ fullAddressText }}
        </span>
        <span v-else class="address-empty">
          주소를 검색하거나 지도 위 위치를 클릭하여 지정하세요.
        </span>
      </div>
      <div class="address-map-toolbar-right">
        <a-space :size="4">
          <a-tooltip title="마커 위치로 지도 중심 이동">
            <a-button size="small" :disabled="!hasCoords" @click="recenterMap">
              <template #icon><AimOutlined /></template>
            </a-button>
          </a-tooltip>
          <a-tooltip title="전체 주소 복사">
            <a-button size="small" :disabled="!address" @click="copyFullAddress">
              <template #icon><CopyOutlined /></template>
            </a-button>
          </a-tooltip>
          <a-button
            v-if="address"
            size="small"
            @click="openExternalMap('kakao')"
          >
            카카오맵
          </a-button>
          <a-button
            v-if="address"
            size="small"
            @click="openExternalMap('naver')"
          >
            네이버지도
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 지도 캔버스 영역 -->
    <div class="address-map-canvas-box" :style="{ height: `${height}px` }">
      <div ref="mapContainerRef" class="address-map-canvas"></div>

      <div v-if="geocoding" class="address-map-overlay">
        <a-spin tip="위치 좌표를 조회하고 있습니다..." />
      </div>

      <div v-if="interactive" class="address-map-hint">
        지도 위 원하는 위치를 클릭하거나 마커를 드래그하면 해당 지점의 도로명 주소를 자동 조회합니다.
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import { EnvironmentOutlined, AimOutlined, CopyOutlined } from '@ant-design/icons-vue';
import {
  ensureLeafletLoaded,
  searchAddressGeocode,
  reverseGeocode,
  getKakaoMapUrl,
  getNaverMapUrl,
} from '/@/utils/addressMap';

const props = withDefaults(
  defineProps<{
    address?: string;
    addressDetail?: string;
    postCode?: string;
    placeName?: string;
    height?: number;
    interactive?: boolean;
    initialLat?: number;
    initialLon?: number;
  }>(),
  {
    address: '',
    addressDetail: '',
    postCode: '',
    placeName: '',
    height: 240,
    interactive: false,
    initialLat: undefined,
    initialLon: undefined,
  },
);

const emit = defineEmits<{
  (
    e: 'pick',
    payload: {
      postCode: string;
      address: string;
      buildingName?: string;
      lat: number;
      lon: number;
    },
  ): void;
  (e: 'coords-change', payload: { lat: number; lon: number }): void;
}>();

const mapContainerRef = ref<HTMLElement | null>(null);
const geocoding = ref(false);
const currentLat = ref<number>(37.5665); // 기본 중심: 서울 시청 인근
const currentLon = ref<number>(126.978);
const hasCoords = ref(false);

let mapInstance: any = null;
let markerInstance: any = null;
let resizeObserver: ResizeObserver | null = null;
let lastGeocodedAddress = '';

const fullAddressText = computed(() => {
  const base = (props.address || '').trim();
  const detail = (props.addressDetail || '').trim();
  if (!base) return '';
  return detail ? `${base} ${detail}` : base;
});

function buildPopupHtml(): string {
  const title = props.placeName ? `<div style="font-weight:700;color:#1d39c4;margin-bottom:2px;">${escapeHtml(props.placeName)}</div>` : '';
  const zip = props.postCode ? `<span style="color:#1677ff;font-weight:600;">[${escapeHtml(props.postCode)}]</span> ` : '';
  const addr = escapeHtml(fullAddressText.value || '선택된 위치');
  return `<div style="font-size:12px;line-height:1.45;min-width:180px;">${title}<div>${zip}${addr}</div></div>`;
}

function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function createPinIcon(L: any) {
  const svgHtml = `
    <div style="position:relative;width:30px;height:40px;display:flex;align-items:center;justify-content:center;">
      <svg viewBox="0 0 24 36" width="30" height="40" style="filter:drop-shadow(0 3px 6px rgba(0,0,0,0.32));">
        <path d="M12 0C5.37 0 0 5.37 0 12c0 8.4 12 24 12 24s12-15.6 12-24C24 5.37 18.63 0 12 0z" fill="#1677ff"/>
        <circle cx="12" cy="12" r="5.5" fill="#ffffff"/>
      </svg>
    </div>
  `;
  return L.divIcon({
    className: 'sync-custom-map-pin',
    html: svgHtml,
    iconSize: [30, 40],
    iconAnchor: [15, 38],
    popupAnchor: [0, -34],
  });
}

async function initMap() {
  if (!mapContainerRef.value) return;
  try {
    const L = await ensureLeafletLoaded();
    if (!mapContainerRef.value || mapInstance) return;

    mapInstance = L.map(mapContainerRef.value, {
      center: [currentLat.value, currentLon.value],
      zoom: 16,
      zoomControl: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap',
    }).addTo(mapInstance);

    if (props.interactive) {
      mapInstance.on('click', async (evt: any) => {
        const lat = evt?.latlng?.lat;
        const lon = evt?.latlng?.lng;
        if (typeof lat === 'number' && typeof lon === 'number') {
          await handleReverseGeocode(lat, lon);
        }
      });
    }

    resizeObserver = new ResizeObserver(() => {
      if (mapInstance) {
        mapInstance.invalidateSize();
      }
    });
    resizeObserver.observe(mapContainerRef.value);

    if (props.initialLat && props.initialLon) {
      updateMarker(props.initialLat, props.initialLon, true);
    } else if (props.address) {
      await locateByAddress(props.address);
    }
  } catch (err: any) {
    message.warning(err?.message || '지도를 초기화하지 못했습니다.');
  }
}

function updateMarker(lat: number, lon: number, pan = true) {
  if (!mapInstance || !window.L) return;
  const L = window.L;
  currentLat.value = lat;
  currentLon.value = lon;
  hasCoords.value = true;
  emit('coords-change', { lat, lon });

  if (!markerInstance) {
    markerInstance = L.marker([lat, lon], {
      icon: createPinIcon(L),
      draggable: props.interactive,
    }).addTo(mapInstance);

    if (props.interactive) {
      markerInstance.on('dragend', async (e: any) => {
        const pos = e.target.getLatLng();
        if (pos) {
          await handleReverseGeocode(pos.lat, pos.lng);
        }
      });
    }
  } else {
    markerInstance.setLatLng([lat, lon]);
  }

  markerInstance.bindPopup(buildPopupHtml()).openPopup();
  if (pan) {
    mapInstance.setView([lat, lon], 16, { animate: true });
  }
}

async function handleReverseGeocode(lat: number, lon: number) {
  geocoding.value = true;
  try {
    updateMarker(lat, lon, true);
    const res = await reverseGeocode(lat, lon);
    if (res && res.roadAddress) {
      lastGeocodedAddress = res.roadAddress;
      emit('pick', {
        postCode: res.postCode || props.postCode || '',
        address: res.roadAddress,
        buildingName: res.buildingName,
        lat,
        lon,
      });
    }
  } finally {
    geocoding.value = false;
  }
}

async function locateByAddress(addr: string) {
  const trimmed = (addr || '').trim();
  if (!trimmed) return;
  if (trimmed === lastGeocodedAddress && hasCoords.value) {
    if (markerInstance) {
      markerInstance.bindPopup(buildPopupHtml());
    }
    return;
  }

  geocoding.value = true;
  try {
    await ensureLeafletLoaded();
    if (!mapInstance) {
      await initMap();
    }
    const results = await searchAddressGeocode(trimmed, 1);
    if (results.length > 0) {
      lastGeocodedAddress = trimmed;
      updateMarker(results[0].lat, results[0].lon, true);
    }
  } finally {
    geocoding.value = false;
  }
}

function setCoordinates(lat: number, lon: number, addrLabel?: string) {
  if (addrLabel) {
    lastGeocodedAddress = addrLabel;
  }
  nextTick(() => {
    if (mapInstance) {
      mapInstance.invalidateSize();
      updateMarker(lat, lon, true);
    }
  });
}

function recenterMap() {
  if (mapInstance && hasCoords.value) {
    mapInstance.invalidateSize();
    mapInstance.setView([currentLat.value, currentLon.value], 16, { animate: true });
    if (markerInstance) {
      markerInstance.openPopup();
    }
  }
}

async function copyFullAddress() {
  if (!fullAddressText.value) return;
  const textToCopy = props.postCode ? `[${props.postCode}] ${fullAddressText.value}` : fullAddressText.value;
  try {
    await navigator.clipboard.writeText(textToCopy);
    message.success('주소가 클립보드에 복사되었습니다.');
  } catch {
    message.info(`주소: ${textToCopy}`);
  }
}

function openExternalMap(provider: 'kakao' | 'naver') {
  if (!props.address) return;
  const url = provider === 'kakao' ? getKakaoMapUrl(props.address) : getNaverMapUrl(props.address);
  window.open(url, '_blank', 'noopener,noreferrer');
}

watch(
  () => props.address,
  (newAddr) => {
    if (newAddr && newAddr.trim()) {
      locateByAddress(newAddr);
    }
  },
);

watch(
  () => [props.addressDetail, props.postCode, props.placeName],
  () => {
    if (markerInstance && hasCoords.value) {
      markerInstance.bindPopup(buildPopupHtml());
    }
  },
);

onMounted(() => {
  initMap();
});

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
  if (mapInstance) {
    mapInstance.remove();
    mapInstance = null;
  }
});

defineExpose({
  locateByAddress,
  setCoordinates,
  recenterMap,
});
</script>

<style scoped>
.address-map-view-wrap {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
  background: #ffffff;
}

.address-map-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #f0f0f0;
  font-size: 12px;
}

.address-map-toolbar-left {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

.pin-icon {
  color: #1677ff;
  font-size: 14px;
  flex-shrink: 0;
}

.address-label {
  font-weight: 500;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.address-empty {
  color: #8c8c8c;
}

.address-map-toolbar-right {
  flex-shrink: 0;
}

.address-map-canvas-box {
  position: relative;
  width: 100%;
  background: #f1f5f9;
}

.address-map-canvas {
  width: 100%;
  height: 100%;
  z-index: 1;
}

.address-map-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
}

.address-map-hint {
  position: absolute;
  left: 10px;
  bottom: 8px;
  z-index: 5;
  background: rgba(15, 23, 42, 0.75);
  color: #ffffff;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
  pointer-events: none;
}
</style>
