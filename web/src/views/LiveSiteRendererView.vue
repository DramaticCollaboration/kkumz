<template>
  <div
    class="min-h-screen w-full flex flex-col selection:bg-indigo-600 selection:text-white"
    :style="{
      backgroundColor: surfaceColor,
      fontFamily: runtime?.designTokens?.fontFamily || 'Pretendard, -apple-system, BlinkMacSystemFont, sans-serif',
    }"
  >
    <!-- 로딩 상태 -->
    <div v-if="loading" class="min-h-screen flex flex-col items-center justify-center gap-3 bg-white text-slate-400">
      <div
        class="w-9 h-9 rounded-full border-3 border-slate-200 border-t-slate-800 animate-spin"
      ></div>
      <p class="text-sm font-bold text-slate-500">공식 브랜드 스토어를 불러오는 중입니다...</p>
    </div>

    <!-- 실제 렌더링되는 독립 브랜드 사이트 (Full-Bleed 100% Viewport) -->
    <template v-else-if="runtime">
      <!-- 상단 프로모션 어나운스먼트 바 (배지/쿠폰 있을 때) -->
      <div
        v-if="customBadge || primaryCoupon"
        class="w-full py-2 px-4 text-center text-xs font-bold text-white tracking-tight"
        :style="{ backgroundColor: primaryColor }"
      >
        <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-center gap-2">
          <span v-if="customBadge">{{ customBadge }}</span>
          <span v-if="customBadge && primaryCoupon" class="opacity-50">|</span>
          <span v-if="primaryCoupon">
            {{ primaryCoupon.name }} — <strong>{{ Number(primaryCoupon.amount).toLocaleString() }}원 즉시 할인</strong> (코드:
            <code class="px-1.5 py-0.5 rounded bg-white/20 font-mono">{{ primaryCoupon.code }}</code>)
          </span>
        </div>
      </div>

      <!-- 브랜드 사이트 자체 Sticky GNB 헤더 -->
      <header class="sticky top-0 z-40 w-full border-b border-slate-200/80 bg-white/95 backdrop-blur-md">
        <div class="max-w-7xl mx-auto px-6 md:px-10 h-20 flex items-center justify-between gap-4">
          <a href="#" class="flex items-center gap-3 group">
            <div
              v-if="runtime.logoSvg"
              class="site-header-logo flex items-center overflow-hidden"
              v-html="runtime.logoSvg"
            ></div>
            <span v-else class="text-xl font-extrabold tracking-tight text-slate-900">
              {{ brandName }}
            </span>
          </a>

          <nav class="flex items-center gap-2 sm:gap-4 text-sm font-bold text-slate-600 flex-wrap">
            <a
              v-if="products.length"
              href="#section-commerce"
              class="px-3 py-2 rounded-lg hover:bg-slate-100 hover:text-slate-950 transition"
            >
              객실·스토어 ({{ products.length }})
            </a>
            <a
              v-if="pricingMatrix?.rows?.length"
              href="#section-pricing"
              class="px-3 py-2 rounded-lg hover:bg-slate-100 hover:text-slate-950 transition"
            >
              시즌 요금표
            </a>
            <a
              v-if="spaceTour.length || (showPhotoGallery && galleryImages.length)"
              href="#section-gallery"
              class="px-3 py-2 rounded-lg hover:bg-slate-100 hover:text-slate-950 transition"
            >
              공간·갤러리 ({{ galleryImages.length || spaceTour.length }})
            </a>
            <a
              v-if="articles.length"
              href="#section-articles"
              class="px-3 py-2 rounded-lg hover:bg-slate-100 hover:text-slate-950 transition"
            >
              매거진 ({{ articles.length }})
            </a>
            <a
              v-if="faqItems.length || locationGuide"
              href="#section-faq-location"
              class="px-3 py-2 rounded-lg hover:bg-slate-100 hover:text-slate-950 transition"
            >
              FAQ·오시는길
            </a>
            <a
              v-if="cmsForm"
              href="#section-form"
              class="px-3 py-2 rounded-lg hover:bg-slate-100 hover:text-slate-950 transition"
            >
              예약·신청
            </a>
            <a
              href="#section-concierge"
              class="px-4 py-2 rounded-full text-xs sm:text-sm font-extrabold text-white shadow-sm hover:opacity-90 transition"
              :style="{ backgroundColor: primaryColor }"
            >
              AI 브랜드 컨시어지
            </a>
          </nav>
        </div>
      </header>

      <!-- 풀와이드 히어로 배너 섹션 -->
      <section
        class="w-full text-white relative overflow-hidden"
        :style="{
          background: `linear-gradient(135deg, ${primaryColor} 0%, #0f172a 100%)`,
        }"
      >
        <div
          class="max-w-7xl mx-auto px-6 md:px-10 py-16 md:py-24 grid grid-cols-1 lg:grid-cols-12 gap-10 items-center relative z-10"
        >
          <div :class="heroBannerImage ? 'lg:col-span-7' : 'lg:col-span-9'" class="space-y-6">
            <div
              class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/15 text-xs font-extrabold backdrop-blur-sm border border-white/15"
            >
              <span>{{ brandName }}</span>
              <span class="opacity-60">·</span>
              <span>Official Brand Site</span>
            </div>

            <h1 class="text-3xl sm:text-4xl md:text-5xl font-extrabold leading-tight tracking-tight">
              {{ runtime.layoutSchema?.heroTitle || brandName }}
            </h1>

            <p class="text-base md:text-lg text-white/85 leading-relaxed max-w-2xl">
              {{ runtime.layoutSchema?.heroSubtitle || tagline }}
            </p>

            <div class="pt-2 flex flex-wrap gap-3">
              <a
                v-if="products.length"
                href="#section-commerce"
                class="px-7 py-3.5 rounded-xl bg-white text-slate-900 font-extrabold text-sm shadow-lg hover:bg-slate-100 transition"
              >
                {{ primaryCtaText }}
              </a>
              <a
                v-if="cmsForm"
                href="#section-form"
                class="px-7 py-3.5 rounded-xl bg-white/15 hover:bg-white/25 text-white font-extrabold text-sm border border-white/25 transition"
              >
                {{ cmsForm.title || cmsForm.formName || '온라인 예약·신청하기' }}
              </a>
              <a
                v-else-if="articles.length"
                href="#section-articles"
                class="px-7 py-3.5 rounded-xl bg-white/15 hover:bg-white/25 text-white font-extrabold text-sm border border-white/25 transition"
              >
                브랜드 스토리 읽기
              </a>
            </div>
          </div>

          <!-- 우측 대표 배너 비주얼 카드 -->
          <div v-if="heroBannerImage" class="lg:col-span-5">
            <div class="rounded-3xl overflow-hidden border border-white/15 shadow-2xl bg-slate-900/40 backdrop-blur-sm">
              <img
                :src="heroBannerImage"
                :alt="brandName"
                class="w-full h-64 md:h-80 object-cover"
              />
              <div v-if="heroBannerCaption" class="p-4 bg-slate-950/75 text-xs text-slate-200 leading-relaxed">
                <strong class="block text-white font-extrabold mb-0.5">{{ heroBannerCaption.title }}</strong>
                <span class="text-slate-300">{{ heroBannerCaption.sub }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- SDUI METRIC_STRIP: 핵심 신뢰 지표 바 -->
        <div v-if="trustMetrics.length" class="border-t border-white/10 bg-slate-950/40 backdrop-blur-sm">
          <div class="max-w-7xl mx-auto px-6 md:px-10 py-5 grid grid-cols-2 md:grid-cols-4 gap-6">
            <div v-for="(tm, tmIdx) in trustMetrics" :key="tmIdx" class="space-y-0.5">
              <div class="text-xs text-white/65 font-semibold">{{ tm.label }}</div>
              <div class="text-lg md:text-xl font-extrabold text-white">{{ tm.value }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 메인 콘텐츠 영역 -->
      <main class="flex-1 w-full bg-white">
        <div class="max-w-7xl mx-auto px-6 md:px-10 py-14 md:py-20 space-y-20">
          <!-- 1. 브랜드 스토어 상품·객실·패키지 카탈로그 & 즉시 예약/주문 (SDUI CATALOG_GRID) -->
          <section v-if="products.length" id="section-commerce" class="space-y-8 scroll-mt-24">
            <div class="flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-slate-200 pb-5">
              <div>
                <span
                  class="text-xs font-extrabold uppercase tracking-wider"
                  :style="{ color: secondaryColor }"
                >
                  OFFICIAL BRAND SHOWCASE & RESERVATION
                </span>
                <h2 class="text-2xl md:text-3xl font-extrabold text-slate-900 mt-1">
                  시그니처 객실 · 프로그램 · 스토어 컬렉션
                </h2>
              </div>

              <!-- 카테고리 필터 칩 -->
              <div v-if="commerceCategories.length" class="flex flex-wrap gap-2">
                <button
                  type="button"
                  class="px-3.5 py-1.5 rounded-full text-xs font-extrabold border transition"
                  :class="
                    selectedCategoryId === null
                      ? 'bg-slate-900 text-white border-slate-900'
                      : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
                  "
                  @click="selectedCategoryId = null"
                >
                  전체 ({{ products.length }})
                </button>
                <button
                  v-for="cat in commerceCategories"
                  :key="cat.id"
                  type="button"
                  class="px-3.5 py-1.5 rounded-full text-xs font-extrabold border transition"
                  :class="
                    selectedCategoryId === cat.id
                      ? 'bg-slate-900 text-white border-slate-900'
                      : 'bg-slate-50 text-slate-600 border-slate-200 hover:bg-slate-100'
                  "
                  @click="selectedCategoryId = cat.id"
                >
                  {{ cat.name }}
                </button>
              </div>
            </div>

            <!-- 주문 완료 영수증 배너 -->
            <div
              v-if="orderResult"
              class="p-6 rounded-2xl bg-emerald-50 border border-emerald-300 flex flex-col md:flex-row md:items-center justify-between gap-4"
            >
              <div class="space-y-1.5">
                <div class="flex items-center gap-2">
                  <span class="px-2.5 py-0.5 rounded-full bg-emerald-600 text-white text-xs font-extrabold">
                    예약 · 결제 완료
                  </span>
                  <span class="text-sm font-extrabold text-emerald-950">
                    주문번호: {{ orderResult.orderSn }}
                  </span>
                </div>
                <p class="text-xs text-emerald-800">
                  선택 항목: <strong>{{ orderResult.productName }}</strong> ({{ orderResult.quantity }}건) · 결제금액:
                  <strong>{{ Number(orderResult.payAmount).toLocaleString() }}원</strong> · 잔여 수량:
                  <strong>{{ orderResult.remainingStock }}</strong>
                </p>
              </div>
              <button
                class="px-3.5 py-2 rounded-xl bg-white text-emerald-800 text-xs font-bold border border-emerald-200 self-start md:self-center hover:bg-emerald-100/50 transition"
                @click="orderResult = null"
              >
                확인 닫기
              </button>
            </div>

            <!-- 상품·객실 그리드 -->
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              <div
                v-for="prod in filteredProducts"
                :key="prod.id"
                class="group rounded-2xl border border-slate-200 overflow-hidden bg-white hover:shadow-xl hover:-translate-y-0.5 transition-all duration-200 flex flex-col justify-between"
              >
                <div>
                  <div class="h-56 bg-slate-100 overflow-hidden relative">
                    <img
                      :src="prod.pic"
                      :alt="prod.name"
                      class="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                    />
                    <span
                      v-if="prod.note || prod.badge"
                      class="absolute top-3 left-3 px-2.5 py-1 rounded-full text-white text-[11px] font-extrabold shadow-sm"
                      :style="{ backgroundColor: primaryColor }"
                    >
                      {{ prod.note || prod.badge }}
                    </span>
                    <span
                      class="absolute top-3 right-3 px-2.5 py-1 rounded-full bg-slate-900/80 text-white text-[11px] font-bold backdrop-blur-sm"
                    >
                      잔여 {{ prod.stock }}{{ prod.unit || '건' }}
                    </span>
                  </div>
                  <div class="p-5 space-y-2.5">
                    <div class="flex items-center justify-between text-[11px] font-bold text-slate-400">
                      <span>{{ prod.productCategoryName || prod.productSn }}</span>
                      <span v-if="prod.sale">누적 {{ prod.sale }}건 이용</span>
                    </div>
                    <h3 class="text-base font-extrabold text-slate-900 line-clamp-2 leading-snug">
                      {{ prod.name }}
                    </h3>
                    <p class="text-xs text-slate-500 line-clamp-2 leading-relaxed">
                      {{ prod.subTitle || prod.description }}
                    </p>
                    <div v-if="Array.isArray(prod.options) && prod.options.length" class="flex flex-wrap gap-1 pt-1">
                      <span
                        v-for="opt in prod.options.slice(0, 3)"
                        :key="opt"
                        class="px-2 py-0.5 rounded-md bg-slate-100 text-[10px] font-bold text-slate-600"
                      >
                        {{ opt }}
                      </span>
                    </div>
                  </div>
                </div>

                <div class="p-5 pt-0 space-y-3.5">
                  <div class="flex items-baseline justify-between border-t border-slate-100 pt-3.5">
                    <span class="text-xs font-bold text-slate-400">공식 이용·판매가</span>
                    <div class="text-right">
                      <span
                        v-if="prod.originalPrice && prod.originalPrice > displayPrice(prod)"
                        class="text-xs text-slate-400 line-through mr-1.5"
                      >
                        {{ Number(prod.originalPrice).toLocaleString() }}원
                      </span>
                      <span class="text-lg font-extrabold text-slate-900">
                        {{ Number(displayPrice(prod)).toLocaleString() }}원
                      </span>
                      <small v-if="prod.unit" class="text-[11px] text-slate-400 ml-0.5">/ {{ prod.unit }}</small>
                    </div>
                  </div>
                  <button
                    class="w-full py-3 rounded-xl text-white text-xs font-extrabold transition shadow-sm hover:opacity-95"
                    :style="{ backgroundColor: primaryColor }"
                    @click="openOrderModal(prod)"
                  >
                    즉시 예약 · 구매하기
                  </button>
                </div>
              </div>
            </div>
          </section>

          <!-- 1-A. 시즌·요일·인원별 상세 요금표 (SDUI MATRIX_TABLE / SHOP_PRICING_MATRIX) -->
          <section
            v-if="pricingMatrix?.rows?.length"
            id="section-pricing"
            data-testid="sdui-pricing-matrix"
            class="space-y-6 scroll-mt-24"
          >
            <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-200 pb-5">
              <div>
                <span class="text-xs font-extrabold uppercase tracking-wider" :style="{ color: secondaryColor }">
                  SEASON & GUEST PRICING MATRIX
                </span>
                <h2 class="text-2xl md:text-3xl font-extrabold text-slate-900 mt-1">
                  {{ pricingMatrix.title || '시즌 · 요일 · 인원별 상세 요금 안내' }}
                </h2>
              </div>
              <span class="text-xs text-slate-500">주중·주말·성수기 및 기준 인원별 공식 이용 요금표입니다</span>
            </div>

            <div class="overflow-x-auto rounded-3xl border border-slate-200 shadow-sm bg-white">
              <table class="w-full text-left border-collapse text-xs md:text-sm">
                <thead>
                  <tr class="bg-slate-900 text-white">
                    <th
                      v-for="col in pricingMatrix.columns || ['구분', '기준 인원', '비수기 주중', '비수기 주말', '성수기/연휴', '포함 혜택']"
                      :key="col"
                      class="py-4 px-5 font-extrabold whitespace-nowrap"
                    >
                      {{ col }}
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-200">
                  <tr
                    v-for="(row, rIdx) in pricingMatrix.rows"
                    :key="rIdx"
                    class="hover:bg-slate-50/80 transition"
                  >
                    <td class="py-4 px-5 font-extrabold text-slate-900 whitespace-nowrap">{{ row.tier }}</td>
                    <td class="py-4 px-5 text-slate-600 whitespace-nowrap">{{ row.capacity || row.target }}</td>
                    <td class="py-4 px-5 font-bold text-slate-800 whitespace-nowrap">{{ row.offPeakWeekday }}</td>
                    <td class="py-4 px-5 font-bold text-indigo-700 whitespace-nowrap">{{ row.offPeakWeekend }}</td>
                    <td class="py-4 px-5 font-extrabold text-amber-700 whitespace-nowrap">{{ row.peakSeason }}</td>
                    <td class="py-4 px-5 text-slate-600">{{ row.note || row.includes }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <!-- 1-B. 공간 구역별 스페이스 투어 (SDUI SPACE_TOUR / CMS_SPACE_TOUR) -->
          <section
            v-if="spaceTour.length"
            id="section-space-tour"
            data-testid="sdui-space-tour"
            class="space-y-8 scroll-mt-24"
          >
            <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-200 pb-5">
              <div>
                <span class="text-xs font-extrabold uppercase tracking-wider" :style="{ color: secondaryColor }">
                  SIGNATURE SPACE & ZONE ARCHIVE
                </span>
                <h2 class="text-2xl md:text-3xl font-extrabold text-slate-900 mt-1">
                  공간 구역별 스페이스 투어 ({{ spaceTour.length }}개 존)
                </h2>
              </div>
              <span class="text-xs text-slate-500">머무시는 동안 경험하게 될 핵심 공간과 시설을 미리 살펴보세요</span>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div
                v-for="(zone, zIdx) in spaceTour"
                :key="zIdx"
                class="rounded-3xl border border-slate-200 overflow-hidden bg-slate-50 flex flex-col justify-between hover:shadow-lg transition"
              >
                <div>
                  <div v-if="zone.imageUrl" class="h-48 bg-slate-200 overflow-hidden relative">
                    <img :src="zone.imageUrl" :alt="zone.title" class="w-full h-full object-cover" />
                    <span
                      class="absolute top-3 left-3 px-2.5 py-1 rounded-full text-[11px] font-extrabold text-white"
                      :style="{ backgroundColor: primaryColor }"
                    >
                      {{ zone.zone }}
                    </span>
                  </div>
                  <div class="p-5 space-y-2">
                    <span
                      v-if="!zone.imageUrl"
                      class="inline-block px-2.5 py-0.5 rounded-full text-[11px] font-extrabold text-white"
                      :style="{ backgroundColor: primaryColor }"
                    >
                      {{ zone.zone }}
                    </span>
                    <h3 class="text-base font-extrabold text-slate-900">{{ zone.title }}</h3>
                    <p class="text-xs text-slate-600 leading-relaxed">{{ zone.desc || zone.description }}</p>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- 1-C. 현장 포토 갤러리 아카이브 (SDUI MEDIA_GALLERY / CMS_PHOTO_GALLERY) -->
          <section
            v-if="showPhotoGallery && galleryImages.length"
            id="section-gallery"
            class="space-y-8 scroll-mt-24"
          >
            <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-200 pb-5">
              <div>
                <span
                  class="text-xs font-extrabold uppercase tracking-wider"
                  :style="{ color: secondaryColor }"
                >
                  SPACE & PHOTO GALLERY ARCHIVE
                </span>
                <h2 class="text-2xl md:text-3xl font-extrabold text-slate-900 mt-1">
                  현장 포토 갤러리 ({{ galleryImages.length }}장)
                </h2>
              </div>
              <span class="text-xs text-slate-500">사진을 클릭하면 고해상도 원본으로 크게 볼 수 있습니다</span>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              <div
                v-for="(gImg, gIdx) in galleryImages"
                :key="gImg.id || gIdx"
                class="group rounded-3xl overflow-hidden border border-slate-200 bg-slate-50 cursor-pointer hover:shadow-xl transition"
                @click="selectedGalleryImage = gImg"
              >
                <div class="h-64 overflow-hidden bg-slate-200">
                  <img
                    :src="gImg.fileUrl"
                    :alt="gImg.caption || gImg.originalName"
                    class="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                  />
                </div>
                <div class="p-4 flex items-center justify-between gap-2 bg-white">
                  <strong class="text-sm font-extrabold text-slate-900 truncate">
                    {{ gImg.caption || gImg.originalName }}
                  </strong>
                  <span class="text-xs font-bold text-indigo-600 shrink-0">크게 보기 ↗</span>
                </div>
              </div>
            </div>
          </section>

          <!-- 1-D. 단계별 이용 & 추천 코스 가이드 (SDUI TIMELINE_STEPS / CMS_STEP_GUIDE) -->
          <section
            v-if="stepGuide.length"
            id="section-step-guide"
            data-testid="sdui-step-guide"
            class="space-y-8 scroll-mt-24"
          >
            <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-200 pb-5">
              <div>
                <span class="text-xs font-extrabold uppercase tracking-wider text-emerald-600">
                  STEP-BY-STEP EXPERIENCE GUIDE
                </span>
                <h2 class="text-2xl md:text-3xl font-extrabold text-slate-900 mt-1">
                  {{ brandName }} 100% 즐기는 단계별 추천 코스
                </h2>
              </div>
              <span class="text-xs text-slate-500">방문 전부터 이용 완료까지 가장 만족도 높은 추천 동선입니다</span>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              <div
                v-for="(st, sIdx) in stepGuide"
                :key="sIdx"
                class="p-6 rounded-3xl bg-slate-50 border border-slate-200 space-y-3 relative"
              >
                <span
                  class="inline-block px-3 py-1 rounded-full text-xs font-extrabold text-white"
                  :style="{ backgroundColor: primaryColor }"
                >
                  {{ st.step }}
                </span>
                <h3 class="text-base font-extrabold text-slate-900">{{ st.title }}</h3>
                <p class="text-xs text-slate-600 leading-relaxed">{{ st.desc }}</p>
              </div>
            </div>
          </section>

          <!-- 2. 브랜드 스토리 & 에디토리얼 매거진 (SDUI EDITORIAL_FEED) -->
          <section v-if="articles.length" id="section-articles" class="space-y-8 scroll-mt-24">
            <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-200 pb-5">
              <div>
                <span class="text-xs font-extrabold uppercase tracking-wider text-emerald-600">
                  EDITORIAL & BRAND STORY
                </span>
                <h2 class="text-2xl md:text-3xl font-extrabold text-slate-900 mt-1">
                  브랜드 매거진 & 아카이브
                </h2>
              </div>
              <span class="text-xs text-slate-500">{{ brandName }}의 철학과 새로운 소식을 만나보세요</span>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
              <article
                v-for="art in articles"
                :key="art.id"
                class="rounded-3xl bg-slate-50 border border-slate-200/80 overflow-hidden hover:shadow-lg transition flex flex-col"
              >
                <div v-if="art.cover" class="h-56 bg-slate-200 overflow-hidden">
                  <img :src="art.cover" :alt="art.title" class="w-full h-full object-cover" />
                </div>
                <div class="p-6 md:p-7 space-y-3 flex-1 flex flex-col justify-between">
                  <div class="space-y-3">
                    <div class="flex items-center justify-between text-xs text-slate-400">
                      <span class="font-bold" :style="{ color: secondaryColor }">
                        Editor · {{ art.author || brandName }}
                      </span>
                      <span>조회 {{ art.clicks ?? art.viewCount ?? 0 }}</span>
                    </div>
                    <h3 class="text-lg font-extrabold text-slate-900 leading-snug">{{ art.title }}</h3>
                    <p class="text-sm text-slate-600 leading-relaxed">
                      {{ art.description || art.summary }}
                    </p>
                  </div>
                  <div
                    v-if="art.bodyHtml"
                    class="pt-3 text-xs text-slate-700 prose max-w-none border-t border-slate-200/60"
                    v-html="art.bodyHtml"
                  ></div>
                </div>
              </article>
            </div>
          </section>

          <!-- 2-A. 실방문객·구매자 베스트 후기 & 실시간 선호 투표 (SDUI REVIEW_WALL & POLL_WIDGET) -->
          <section
            v-if="reviews.length || pollWidget"
            id="section-reviews"
            data-testid="sdui-reviews-poll"
            class="grid grid-cols-1 lg:grid-cols-12 gap-8 scroll-mt-24"
          >
            <div v-if="reviews.length" :class="pollWidget ? 'lg:col-span-8' : 'lg:col-span-12'" class="space-y-5">
              <div>
                <span class="text-xs font-extrabold uppercase tracking-wider" :style="{ color: secondaryColor }">
                  VERIFIED GUEST & CUSTOMER REVIEWS
                </span>
                <h2 class="text-2xl font-extrabold text-slate-900 mt-1">
                  실방문객 · 구매자 베스트 후기 ({{ reviews.length }}건)
                </h2>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div
                  v-for="(rv, rvIdx) in reviews"
                  :key="rvIdx"
                  class="p-5 rounded-3xl bg-slate-50 border border-slate-200 space-y-2.5 flex flex-col justify-between"
                >
                  <div class="space-y-2">
                    <div class="flex items-center justify-between">
                      <span class="text-amber-500 text-xs font-extrabold">★★★★★ {{ rv.rating || 5 }}.0</span>
                      <span class="px-2 py-0.5 rounded-full bg-white border border-slate-200 text-[10px] font-bold text-slate-600">
                        {{ rv.tag }}
                      </span>
                    </div>
                    <p class="text-xs text-slate-700 leading-relaxed">"{{ rv.comment || rv.content }}"</p>
                  </div>
                  <div class="text-[11px] font-bold text-slate-400 pt-2 border-t border-slate-200/60">
                    작성자: {{ rv.author }}
                  </div>
                </div>
              </div>
            </div>

            <div
              v-if="pollWidget"
              :class="reviews.length ? 'lg:col-span-4' : 'lg:col-span-12'"
              class="p-6 rounded-3xl bg-indigo-950 text-white flex flex-col justify-between space-y-4"
            >
              <div class="space-y-2">
                <span class="px-2.5 py-1 rounded-full bg-indigo-500/30 text-indigo-200 text-[11px] font-extrabold">
                  LIVE VISITOR POLL
                </span>
                <h3 class="text-base font-extrabold leading-snug">{{ pollWidget.question }}</h3>
              </div>
              <div class="space-y-2.5">
                <button
                  v-for="(opt, oIdx) in pollWidget.options || []"
                  :key="oIdx"
                  type="button"
                  class="w-full p-3 rounded-2xl border text-left transition flex items-center justify-between gap-2 text-xs font-bold"
                  :class="
                    votedOptionIndex === oIdx
                      ? 'bg-indigo-600 border-indigo-400 text-white'
                      : 'bg-white/10 border-white/15 text-slate-200 hover:bg-white/15'
                  "
                  @click="votePollOption(oIdx)"
                >
                  <span>{{ opt.label }}</span>
                  <span class="px-2 py-0.5 rounded-full bg-black/30 text-[11px] font-extrabold">
                    {{ Number(opt.votes || 0) + (votedOptionIndex === oIdx ? 1 : 0) }}표
                  </span>
                </button>
              </div>
            </div>
          </section>

          <!-- 2-B. 브랜드 공식 안내자료 & 요금표·첨부문서 다운로드 -->
          <section
            v-if="downloadableDocs.length"
            id="section-docs"
            class="space-y-6 scroll-mt-24"
          >
            <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-2 border-b border-slate-200 pb-5">
              <div>
                <span class="text-xs font-extrabold uppercase tracking-wider text-indigo-600">
                  OFFICIAL BROCHURE & GUIDE DOCUMENTS
                </span>
                <h2 class="text-2xl md:text-3xl font-extrabold text-slate-900 mt-1">
                  공식 안내자료 & 첨부파일 다운로드
                </h2>
              </div>
              <span class="text-xs text-slate-500">요금표, 이용수칙, 브로슈어 등 공식 안내 문서를 열람하거나 내려받으실 수 있습니다</span>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                v-for="(doc, dIdx) in downloadableDocs"
                :key="doc.id || dIdx"
                class="p-5 rounded-2xl bg-slate-50 border border-slate-200 flex items-center justify-between gap-4 hover:border-indigo-300 transition"
              >
                <div class="flex items-center gap-4 min-w-0">
                  <span
                    class="px-3 py-2 rounded-xl text-xs font-extrabold text-white shrink-0"
                    :style="{ backgroundColor: primaryColor }"
                  >
                    {{ (doc.fileExt || 'DOC').toUpperCase() }}
                  </span>
                  <div class="min-w-0">
                    <strong class="block text-sm font-extrabold text-slate-900 truncate">
                      {{ doc.caption || doc.originalName }}
                    </strong>
                    <span class="text-xs text-slate-500">
                      {{ doc.originalName }} · {{ formatDocSize(doc.fileSize) }}
                    </span>
                  </div>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                  <a
                    :href="doc.fileUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="px-3.5 py-2 rounded-xl bg-white border border-slate-300 text-xs font-bold text-slate-700 hover:bg-slate-100 transition"
                  >
                    열람 ↗
                  </a>
                  <a
                    :href="doc.downloadUrl || `${doc.fileUrl}?download=true`"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="px-3.5 py-2 rounded-xl text-xs font-extrabold text-white transition hover:opacity-90"
                    :style="{ backgroundColor: secondaryColor }"
                  >
                    다운로드 ↓
                  </a>
                </div>
              </div>
            </div>
          </section>

          <!-- 2-C. 자주 묻는 질문(FAQ) 아코디언 & 오시는 길·이용수칙 (SDUI FAQ_ACCORDION & LOCATION_COURSE & POLICY_BOX) -->
          <section
            v-if="faqItems.length || locationGuide || policyRules.length"
            id="section-faq-location"
            data-testid="sdui-faq-location"
            class="grid grid-cols-1 lg:grid-cols-12 gap-8 scroll-mt-24"
          >
            <!-- FAQ 아코디언 -->
            <div v-if="faqItems.length" class="lg:col-span-6 space-y-4">
              <div>
                <span class="text-xs font-extrabold uppercase tracking-wider" :style="{ color: secondaryColor }">
                  FREQUENTLY ASKED QUESTIONS
                </span>
                <h2 class="text-2xl font-extrabold text-slate-900 mt-1">
                  자주 묻는 질문 (FAQ)
                </h2>
              </div>
              <div class="space-y-3">
                <div
                  v-for="(item, fIdx) in faqItems"
                  :key="fIdx"
                  class="rounded-2xl border border-slate-200 overflow-hidden bg-slate-50"
                >
                  <button
                    type="button"
                    class="w-full px-5 py-4 text-left flex items-center justify-between gap-3 font-extrabold text-sm text-slate-900 hover:bg-slate-100 transition"
                    @click="openFaqIndex = openFaqIndex === fIdx ? -1 : fIdx"
                  >
                    <span>Q. {{ item.q }}</span>
                    <span class="text-xs text-slate-400">{{ openFaqIndex === fIdx ? '▲' : '▼' }}</span>
                  </button>
                  <div
                    v-if="openFaqIndex === fIdx"
                    class="px-5 pb-4 pt-1 text-xs text-slate-600 leading-relaxed border-t border-slate-200/60 bg-white"
                  >
                    {{ item.a }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 오시는 길 & 주변 추천 코스 & 이용수칙 -->
            <div v-if="locationGuide || policyRules.length" class="lg:col-span-6 space-y-5">
              <div v-if="locationGuide" class="p-6 rounded-3xl bg-slate-50 border border-slate-200 space-y-3">
                <span class="px-2.5 py-1 rounded-full bg-emerald-100 text-emerald-800 text-[11px] font-extrabold">
                  LOCATION & NEARBY COURSE GUIDE
                </span>
                <h3 class="text-lg font-extrabold text-slate-900">오시는 길 및 교통 · 주차 안내</h3>
                <div class="space-y-1.5 text-xs text-slate-700">
                  <div><strong>📍 주소:</strong> {{ locationGuide.address }}</div>
                  <div v-if="locationGuide.parking"><strong>🚗 주차 안내:</strong> {{ locationGuide.parking }}</div>
                  <div v-if="locationGuide.publicTransit"><strong>🚌 대중교통:</strong> {{ locationGuide.publicTransit }}</div>
                </div>
                <div v-if="Array.isArray(locationGuide.nearbySpots) && locationGuide.nearbySpots.length" class="pt-2 space-y-1.5">
                  <strong class="block text-xs font-extrabold text-slate-900">주변 함께 가볼 만한 추천 코스</strong>
                  <div class="grid grid-cols-1 sm:grid-cols-3 gap-2">
                    <div
                      v-for="(spot, spIdx) in locationGuide.nearbySpots"
                      :key="spIdx"
                      class="p-2.5 rounded-xl bg-white border border-slate-200 text-[11px]"
                    >
                      <strong class="block text-slate-900">{{ spot.name }}</strong>
                      <span class="text-indigo-600 font-bold">{{ spot.distance }}</span>
                      <p class="text-slate-500 mt-0.5">{{ spot.desc }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="policyRules.length" class="p-6 rounded-3xl bg-amber-50/70 border border-amber-200 space-y-2.5">
                <strong class="block text-sm font-extrabold text-amber-950">📌 이용수칙 및 예약·환불 규정 안내</strong>
                <ul class="space-y-1.5 text-xs text-amber-900 list-disc pl-4">
                  <li v-for="(rule, rIdx) in policyRules" :key="rIdx">{{ rule }}</li>
                </ul>
              </div>
            </div>
          </section>

          <!-- 3. 업종 맞춤형 동적 예약/신청 폼 (SDUI DYNAMIC_FORM) & AI 브랜드 컨시어지 -->
          <section class="grid grid-cols-1 lg:grid-cols-12 gap-8 pt-2">
            <!-- 온라인 신청 폼 -->
            <div
              v-if="cmsForm"
              id="section-form"
              data-testid="sdui-dynamic-form"
              class="lg:col-span-6 p-7 md:p-9 rounded-3xl bg-slate-50 border border-slate-200 space-y-6 scroll-mt-24"
            >
              <div>
                <span class="px-3 py-1 rounded-full bg-indigo-100 text-indigo-800 text-xs font-extrabold">
                  온라인 예약 · 신청 접수
                </span>
                <h3 class="text-xl md:text-2xl font-extrabold text-slate-900 mt-3">
                  {{ cmsForm.title || cmsForm.formName }}
                </h3>
                <p class="text-xs md:text-sm text-slate-500 mt-1.5 leading-relaxed">
                  {{ cmsForm.description || '신청서를 작성하시면 브랜드 운영팀에게 즉시 전달됩니다.' }}
                </p>
              </div>

              <div
                v-if="formSubmitResult"
                class="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-900 space-y-1"
              >
                <div class="font-extrabold">
                  신청이 정상 접수되었습니다. (접수번호 #{{ formSubmitResult.submissionId }})
                </div>
                <div>
                  신청자: {{ formSubmitResult.applicantName }} · 총 누적 신청 {{ formSubmitResult.totalSubmissions }}건
                </div>
              </div>

              <div class="space-y-4">
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label class="block text-xs font-bold text-slate-700 mb-1.5">예약·신청자 성함 *</label>
                    <input
                      v-model="cmsFormInput.applicantName"
                      type="text"
                      placeholder="성함을 입력하세요"
                      class="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-xs bg-white focus:outline-none focus:border-slate-900"
                    />
                  </div>
                  <div>
                    <label class="block text-xs font-bold text-slate-700 mb-1.5">연락처 *</label>
                    <input
                      v-model="cmsFormInput.phone"
                      type="tel"
                      placeholder="010-0000-0000"
                      class="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-xs bg-white focus:outline-none focus:border-slate-900"
                    />
                  </div>
                </div>

                <!-- DB 동적 필드 렌더링 (date, select, number, checkbox 등) -->
                <div v-if="dynamicCustomFields.length" class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div
                    v-for="field in dynamicCustomFields"
                    :key="field.key"
                    :class="field.type === 'checkbox' || field.type === 'textarea' ? 'sm:col-span-2' : ''"
                  >
                    <label class="block text-xs font-bold text-slate-700 mb-1.5">{{ field.label }}</label>
                    <input
                      v-if="field.type === 'date'"
                      v-model="dynamicFieldValues[field.key]"
                      type="date"
                      class="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-xs bg-white focus:outline-none focus:border-slate-900"
                    />
                    <select
                      v-else-if="field.type === 'select'"
                      v-model="dynamicFieldValues[field.key]"
                      class="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-xs bg-white focus:outline-none focus:border-slate-900"
                    >
                      <option value="">선택해 주세요</option>
                      <option v-for="opt in field.options || []" :key="opt" :value="opt">{{ opt }}</option>
                    </select>
                    <input
                      v-else-if="field.type === 'number'"
                      v-model.number="dynamicFieldValues[field.key]"
                      type="number"
                      :min="field.min || 1"
                      :max="field.max || 50"
                      class="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-xs bg-white focus:outline-none focus:border-slate-900"
                    />
                    <div v-else-if="field.type === 'checkbox'" class="flex flex-wrap gap-2">
                      <label
                        v-for="opt in field.options || []"
                        :key="opt"
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white border border-slate-300 text-xs cursor-pointer"
                      >
                        <input
                          type="checkbox"
                          :value="opt"
                          :checked="Array.isArray(dynamicFieldValues[field.key]) && dynamicFieldValues[field.key].includes(opt)"
                          @change="toggleDynamicCheckbox(field.key, opt)"
                        />
                        <span>{{ opt }}</span>
                      </label>
                    </div>
                  </div>
                </div>
                <div v-else>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">세션 / 객실 / 프로그램 선택</label>
                  <input
                    v-model="cmsFormInput.optionValue"
                    type="text"
                    placeholder="예: 냇가 독채 스테이 / 평상 백숙 예약 / 주말 프로그램"
                    class="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-xs bg-white focus:outline-none focus:border-slate-900"
                  />
                </div>

                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">추가 요청사항</label>
                  <textarea
                    v-model="cmsFormInput.message"
                    rows="3"
                    placeholder="도착 예정 시간, 바비큐/백숙 사전 주문, 기타 문의사항을 남겨주세요"
                    class="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-xs bg-white focus:outline-none focus:border-slate-900"
                  ></textarea>
                </div>
                <button
                  class="w-full py-3.5 rounded-xl text-white text-xs font-extrabold shadow-sm transition hover:opacity-95"
                  :style="{ backgroundColor: primaryColor }"
                  :disabled="submittingForm"
                  @click="handleSubmitCmsForm"
                >
                  {{ submittingForm ? '신청서 접수 중...' : cmsForm.submitText || '온라인 예약·신청서 제출하기' }}
                </button>
              </div>
            </div>

            <!-- AI 브랜드 컨시어지 챗봇 -->
            <div
              id="section-concierge"
              :class="cmsForm ? 'lg:col-span-6' : 'lg:col-span-12'"
              class="p-7 md:p-9 rounded-3xl bg-slate-900 text-white space-y-6 flex flex-col justify-between scroll-mt-24"
            >
              <div class="space-y-3">
                <div class="flex items-center justify-between">
                  <span
                    class="px-3 py-1 rounded-full bg-indigo-500/20 text-indigo-300 text-xs font-extrabold border border-indigo-400/30"
                  >
                    AI 브랜드 컨시어지
                  </span>
                  <span class="text-xs text-slate-400">24시간 실시간 응대</span>
                </div>
                <h3 class="text-xl md:text-2xl font-extrabold">
                  {{ brandName }}에 대해 무엇이든 물어보세요
                </h3>
                <p class="text-xs md:text-sm text-slate-300 leading-relaxed">
                  브랜드 스토리, 판매 중인 시그니처 상품 및 재고, 커뮤니티 프로그램 안내까지 실시간으로 답변해 드립니다.
                </p>

                <div class="flex flex-wrap gap-2 pt-2">
                  <button
                    class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 transition"
                    @click="askPreset('대표 추천 상품과 가격을 알려주세요')"
                  >
                    대표 추천 상품과 가격
                  </button>
                  <button
                    class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 transition"
                    @click="askPreset('브랜드 핵심 가치와 특징이 궁금해요')"
                  >
                    브랜드 핵심 가치
                  </button>
                  <button
                    class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 transition"
                    @click="askPreset('현재 참여 가능한 오프라인 클래스나 모임이 있나요?')"
                  >
                    클래스/모임 신청 안내
                  </button>
                </div>
              </div>

              <div class="space-y-3 pt-4">
                <div
                  v-if="conciergeAnswer"
                  class="p-4 rounded-2xl bg-slate-800/90 border border-slate-700 text-xs md:text-sm text-slate-100 whitespace-pre-line leading-relaxed"
                >
                  {{ conciergeAnswer }}
                </div>

                <div class="flex gap-2">
                  <input
                    v-model="conciergeQuestion"
                    type="text"
                    placeholder="궁금한 내용을 입력하세요..."
                    class="flex-1 px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-xs md:text-sm text-white placeholder-slate-400 focus:outline-none focus:border-indigo-400"
                    @keyup.enter="handleAskConcierge"
                  />
                  <button
                    class="px-5 py-3 rounded-xl text-white text-xs md:text-sm font-extrabold shrink-0 transition hover:opacity-95"
                    :style="{ backgroundColor: secondaryColor }"
                    :disabled="askingConcierge"
                    @click="handleAskConcierge"
                  >
                    {{ askingConcierge ? '답변 중...' : '질문하기' }}
                  </button>
                </div>
              </div>
            </div>
          </section>
        </div>
      </main>

      <!-- 브랜드 사이트 자체 공식 Footer -->
      <footer class="w-full border-t border-slate-200 bg-slate-950 text-slate-400 py-12 px-6 md:px-10">
        <div class="max-w-7xl mx-auto flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div class="space-y-2">
            <div class="text-base font-extrabold text-white">{{ brandName }}</div>
            <p class="text-xs text-slate-400 max-w-xl">{{ tagline }}</p>
          </div>
          <div class="text-xs text-slate-500 space-y-1 md:text-right">
            <div v-if="runtime.customDomain">Official Domain · {{ runtime.customDomain }}</div>
            <div>© {{ new Date().getFullYear() }} {{ brandName }}. All rights reserved.</div>
          </div>
        </div>
      </footer>
    </template>

    <!-- 즉시 구매 주문 모달 -->
    <div
      v-if="selectedProduct"
      class="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-sm flex items-center justify-center p-4"
    >
      <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-extrabold text-slate-900">스토어 즉시 주문</h3>
          <button class="text-slate-400 hover:text-slate-700 font-bold" @click="selectedProduct = null">
            ✕
          </button>
        </div>

        <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 flex items-center gap-3">
          <img :src="selectedProduct.pic" class="w-14 h-14 rounded-xl object-cover" />
          <div>
            <div class="text-xs font-extrabold text-slate-900">{{ selectedProduct.name }}</div>
            <div class="text-xs text-indigo-600 font-bold mt-0.5">
              {{ Number(displayPrice(selectedProduct)).toLocaleString() }}원 · 현재 재고 {{ selectedProduct.stock }}개
            </div>
          </div>
        </div>

        <div class="space-y-3">
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">수량</label>
              <input
                v-model.number="orderInput.quantity"
                type="number"
                min="1"
                max="10"
                class="w-full px-3 py-2 rounded-xl border border-slate-300 text-xs"
              />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">수령인 성함</label>
              <input
                v-model="orderInput.receiverName"
                type="text"
                placeholder="홍길동"
                class="w-full px-3 py-2 rounded-xl border border-slate-300 text-xs"
              />
            </div>
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">연락처</label>
            <input
              v-model="orderInput.receiverPhone"
              type="text"
              placeholder="연락처를 입력하세요 (숫자·하이픈)"
              class="w-full px-3 py-2 rounded-xl border border-slate-300 text-xs"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">배송지 주소</label>
            <input
              v-model="orderInput.receiverAddress"
              type="text"
              placeholder="배송받으실 주소를 입력하세요"
              class="w-full px-3 py-2 rounded-xl border border-slate-300 text-xs"
            />
          </div>
        </div>

        <div class="flex items-center justify-between pt-3 border-t border-slate-100">
          <div class="text-xs text-slate-500">
            총 결제금액:
            <strong class="text-base text-slate-900">
              {{ Number(displayPrice(selectedProduct) * (orderInput.quantity || 1)).toLocaleString() }}원
            </strong>
          </div>
          <div class="flex gap-2">
            <button
              class="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold"
              @click="selectedProduct = null"
            >
              취소
            </button>
            <button
              class="px-4 py-2 rounded-xl text-white text-xs font-extrabold shadow-sm"
              :style="{ backgroundColor: primaryColor }"
              :disabled="placingOrder"
              @click="submitCommerceOrder"
            >
              {{ placingOrder ? '결제 처리 중...' : '결제 및 주문 확정' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 포토 갤러리 고해상도 라이트박스 모달 -->
    <div
      v-if="selectedGalleryImage"
      class="fixed inset-0 z-50 bg-slate-950/85 backdrop-blur-sm flex items-center justify-center p-4"
      @click.self="selectedGalleryImage = null"
    >
      <div class="bg-slate-900 rounded-3xl max-w-4xl w-full overflow-hidden border border-white/15 shadow-2xl">
        <div class="flex items-center justify-between px-6 py-4 border-b border-white/10 text-white">
          <strong class="text-sm font-extrabold">
            {{ selectedGalleryImage.caption || selectedGalleryImage.originalName }}
          </strong>
          <button
            type="button"
            class="px-3 py-1 rounded-lg bg-white/10 hover:bg-white/20 text-xs font-bold"
            @click="selectedGalleryImage = null"
          >
            닫기 ✕
          </button>
        </div>
        <div class="p-4 flex items-center justify-center bg-slate-950">
          <img
            :src="selectedGalleryImage.fileUrl"
            :alt="selectedGalleryImage.caption || selectedGalleryImage.originalName"
            class="max-h-[75vh] w-auto object-contain rounded-xl"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { kkumzApi } from '@/api/kkumzApi';

const route = useRoute();

const currentSlug = computed(() => (route.params.siteSlug as string) || '');
const loading = ref(false);
const runtime = ref<any>(null);
const selectedCategoryId = ref<number | null>(null);

// 정규화된 브랜드/디자인/콘텐츠 Computed 속성
const brandName = computed(() => runtime.value?.brandName || runtime.value?.siteName || '공식 브랜드 스토어');
const tagline = computed(() => runtime.value?.tagline || runtime.value?.brandSlogan || '');
const primaryColor = computed(
  () => runtime.value?.designTokens?.primaryColor || runtime.value?.designTokens?.primary || '#1e1b4b',
);
const secondaryColor = computed(
  () => runtime.value?.designTokens?.secondaryColor || runtime.value?.designTokens?.secondary || '#4f46e5',
);
const surfaceColor = computed(
  () => runtime.value?.designTokens?.surfaceColor || runtime.value?.designTokens?.surface || '#ffffff',
);
const customBadge = computed(
  () => runtime.value?.layoutSchema?.customBadge || runtime.value?.layoutSchema?.announcement || '',
);
const primaryCtaText = computed(
  () => runtime.value?.layoutSchema?.primaryCta || runtime.value?.layoutSchema?.ctaText || '컬렉션 스토어 살펴보기',
);

const products = computed<any[]>(() => {
  if (Array.isArray(runtime.value?.products) && runtime.value.products.length > 0) {
    return runtime.value.products;
  }
  if (Array.isArray(runtime.value?.commerce?.products)) {
    return runtime.value.commerce.products;
  }
  return [];
});

const commerceCategories = computed<any[]>(() => {
  if (Array.isArray(runtime.value?.categories) && runtime.value.categories.length > 0) {
    return runtime.value.categories;
  }
  if (Array.isArray(runtime.value?.commerce?.categories)) {
    return runtime.value.commerce.categories;
  }
  return [];
});

const filteredProducts = computed<any[]>(() => {
  if (selectedCategoryId.value === null) return products.value;
  const selectedCat = commerceCategories.value.find((c: any) => c.id === selectedCategoryId.value);
  const matched = products.value.filter(
    (p) =>
      p.productCategoryId === selectedCategoryId.value ||
      (selectedCat && p.productCategoryName === selectedCat.name),
  );
  return matched.length > 0 ? matched : products.value;
});

const primaryCoupon = computed<any>(() => {
  const list = runtime.value?.coupons || runtime.value?.commerce?.coupons || [];
  return Array.isArray(list) && list.length > 0 ? list[0] : null;
});

const articles = computed<any[]>(() => {
  if (Array.isArray(runtime.value?.articles) && runtime.value.articles.length > 0) {
    return runtime.value.articles;
  }
  if (Array.isArray(runtime.value?.cms?.articles)) {
    return runtime.value.cms.articles;
  }
  return [];
});

const cmsForm = computed<any>(() => {
  if (runtime.value?.layoutSchema?.customCmsForm) {
    return runtime.value.layoutSchema.customCmsForm;
  }
  if (runtime.value?.cmsForm) return runtime.value.cmsForm;
  const forms = runtime.value?.cms?.forms;
  return Array.isArray(forms) && forms.length > 0 ? forms[0] : null;
});

const showPhotoGallery = computed<boolean>(() => {
  return runtime.value?.layoutSchema?.showPhotoGallery !== false;
});

const galleryImages = computed<any[]>(() => {
  const list =
    runtime.value?.galleryImages ||
    runtime.value?.layoutSchema?.galleryImages ||
    [];
  return Array.isArray(list) ? list : [];
});

const downloadableDocs = computed<any[]>(() => {
  const list =
    runtime.value?.downloadableDocs ||
    runtime.value?.layoutSchema?.downloadableDocs ||
    [];
  return Array.isArray(list) ? list : [];
});

const featureBlocks = computed<Record<string, any>>(
  () => runtime.value?.featureBlocks || runtime.value?.layoutSchema?.featureBlocks || {},
);

const pricingMatrix = computed<any>(() => {
  const raw = featureBlocks.value?.pricingMatrix;
  if (!raw) return null;
  if (Array.isArray(raw)) {
    return {
      title: '시즌 · 요일 · 인원별 상세 요금 안내',
      columns: ['구분', '기준 인원', '비수기 주중', '비수기 주말', '성수기/연휴', '포함 혜택'],
      rows: raw,
    };
  }
  return raw;
});
const spaceTour = computed<any[]>(() =>
  Array.isArray(featureBlocks.value?.spaceTour) ? featureBlocks.value.spaceTour : [],
);
const stepGuide = computed<any[]>(() =>
  Array.isArray(featureBlocks.value?.stepGuide) ? featureBlocks.value.stepGuide : [],
);
const faqItems = computed<any[]>(() =>
  Array.isArray(featureBlocks.value?.faqItems) ? featureBlocks.value.faqItems : [],
);
const locationGuide = computed<any>(() => featureBlocks.value?.locationGuide || null);
const policyRules = computed<string[]>(() =>
  Array.isArray(featureBlocks.value?.policyRules) ? featureBlocks.value.policyRules : [],
);
const reviews = computed<any[]>(() =>
  Array.isArray(featureBlocks.value?.reviews) ? featureBlocks.value.reviews : [],
);
const pollWidget = computed<any>(() => featureBlocks.value?.pollWidget || null);
const trustMetrics = computed<any[]>(() =>
  Array.isArray(featureBlocks.value?.trustMetrics) ? featureBlocks.value.trustMetrics : [],
);

const dynamicCustomFields = computed<any[]>(() => {
  const rawFields = cmsForm.value?.fields;
  if (!Array.isArray(rawFields)) return [];
  return rawFields.filter(
    (f: any) =>
      f &&
      f.key &&
      !['applicantName', 'phone', 'message'].includes(String(f.key)),
  );
});

const dynamicFieldValues = ref<Record<string, any>>({});
const openFaqIndex = ref<number>(0);
const votedOptionIndex = ref<number | null>(null);

function toggleDynamicCheckbox(fieldKey: string, opt: string) {
  const cur = Array.isArray(dynamicFieldValues.value[fieldKey])
    ? [...dynamicFieldValues.value[fieldKey]]
    : [];
  const idx = cur.indexOf(opt);
  if (idx >= 0) {
    cur.splice(idx, 1);
  } else {
    cur.push(opt);
  }
  dynamicFieldValues.value[fieldKey] = cur;
}

function votePollOption(idx: number) {
  votedOptionIndex.value = idx;
}

const selectedGalleryImage = ref<any | null>(null);

function formatDocSize(bytes: any) {
  const num = Number(bytes || 0);
  if (!num || num <= 0) return '0 KB';
  if (num < 1024) return `${num} B`;
  if (num < 1024 * 1024) return `${(num / 1024).toFixed(1)} KB`;
  return `${(num / (1024 * 1024)).toFixed(2)} MB`;
}

const heroBannerImage = computed<string>(() => {
  if (runtime.value?.layoutSchema?.heroBannerImage) {
    return String(runtime.value.layoutSchema.heroBannerImage);
  }
  const shopBanners = runtime.value?.commerce?.banners;
  if (Array.isArray(shopBanners) && shopBanners.length > 0 && shopBanners[0].pic) {
    return shopBanners[0].pic;
  }
  const cmsBanners = runtime.value?.cms?.banners;
  if (Array.isArray(cmsBanners) && cmsBanners.length > 0 && cmsBanners[0].imageUrl) {
    return cmsBanners[0].imageUrl;
  }
  return '';
});

const heroBannerCaption = computed<{ title: string; sub: string } | null>(() => {
  const rawCap = runtime.value?.layoutSchema?.heroBannerCaption;
  if (rawCap) {
    if (typeof rawCap === 'object') {
      return {
        title: String(rawCap.title || ''),
        sub: String(rawCap.sub || brandName.value),
      };
    }
    return {
      title: String(rawCap),
      sub: brandName.value,
    };
  }
  const shopBanners = runtime.value?.commerce?.banners;
  if (Array.isArray(shopBanners) && shopBanners.length > 0) {
    return { title: shopBanners[0].name || '', sub: shopBanners[0].note || '' };
  }
  const cmsBanners = runtime.value?.cms?.banners;
  if (Array.isArray(cmsBanners) && cmsBanners.length > 0) {
    return { title: cmsBanners[0].title || '', sub: cmsBanners[0].subTitle || '' };
  }
  return null;
});

function displayPrice(prod: any): number {
  const promo = Number(prod?.promotionPrice || 0);
  if (promo > 0) return promo;
  const price = Number(prod?.price || 0);
  return price > 0 ? price : 120000;
}

// 주문 상태
const selectedProduct = ref<any>(null);
const placingOrder = ref(false);
const orderResult = ref<any>(null);
const orderInput = ref({
  quantity: 1,
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
});

// 온라인 신청 폼 상태
const submittingForm = ref(false);
const formSubmitResult = ref<any>(null);
const cmsFormInput = ref({
  applicantName: '',
  phone: '',
  optionValue: '',
  message: '',
});

// AI 브랜드 컨시어지 상태
const conciergeQuestion = ref('');
const conciergeAnswer = ref('');
const askingConcierge = ref(false);

function normalizeRuntimePayload(raw: any) {
  if (!raw) return null;
  const site = raw.site || raw;
  const rel = raw.release || {};
  return {
    ...site,
    ...raw,
    layoutSchema: raw.layoutSchema || rel.layoutSchema || {},
    designTokens: raw.designTokens || rel.designTokens || {},
  };
}

async function loadRuntime() {
  if (!currentSlug.value) return;
  loading.value = true;
  orderResult.value = null;
  formSubmitResult.value = null;
  conciergeAnswer.value = '';
  selectedCategoryId.value = null;
  try {
    const raw = await kkumzApi.getSiteRuntime(currentSlug.value);
    runtime.value = normalizeRuntimePayload(raw);
    if (runtime.value) {
      document.title = `${brandName.value} — Official Brand Site`;
    }
  } finally {
    loading.value = false;
  }
}

function openOrderModal(prod: any) {
  selectedProduct.value = prod;
  orderInput.value.quantity = 1;
}

async function submitCommerceOrder() {
  if (!selectedProduct.value) return;
  placingOrder.value = true;
  try {
    orderResult.value = await kkumzApi.createCommerceOrder(currentSlug.value, {
      productId: selectedProduct.value.id,
      quantity: orderInput.value.quantity || 1,
      receiverName: orderInput.value.receiverName,
      receiverPhone: orderInput.value.receiverPhone,
      receiverAddress: orderInput.value.receiverAddress,
    });
    selectedProduct.value = null;
    runtime.value = await kkumzApi.getSiteRuntime(currentSlug.value);
  } finally {
    placingOrder.value = false;
  }
}

async function handleSubmitCmsForm() {
  if (!cmsFormInput.value.applicantName.trim()) return;
  submittingForm.value = true;
  try {
    const dynSummary = Object.entries(dynamicFieldValues.value)
      .filter(([, v]) => v !== undefined && v !== null && String(v).trim() !== '')
      .map(([k, v]) => `${k}:${Array.isArray(v) ? v.join(',') : v}`)
      .join(' | ');
    const finalOptionValue =
      cmsFormInput.value.optionValue ||
      dynamicFieldValues.value.roomOrPackage ||
      dynSummary ||
      '온라인 신청';

    formSubmitResult.value = await kkumzApi.submitCmsForm(currentSlug.value, {
      formId: cmsForm.value?.id,
      applicantName: cmsFormInput.value.applicantName,
      phone: cmsFormInput.value.phone,
      optionValue: String(finalOptionValue),
      message: cmsFormInput.value.message,
      extraFields: { ...dynamicFieldValues.value },
    });
    runtime.value = normalizeRuntimePayload(await kkumzApi.getSiteRuntime(currentSlug.value));
  } finally {
    submittingForm.value = false;
  }
}

function askPreset(q: string) {
  conciergeQuestion.value = q;
  handleAskConcierge();
}

async function handleAskConcierge() {
  if (!conciergeQuestion.value.trim()) return;
  askingConcierge.value = true;
  try {
    const res = await kkumzApi.askSiteConcierge(currentSlug.value, conciergeQuestion.value);
    conciergeAnswer.value = res.answer;
  } finally {
    askingConcierge.value = false;
  }
}

watch(
  () => route.params.siteSlug,
  () => {
    loadRuntime();
  },
);

onMounted(() => {
  loadRuntime();
});
</script>

<style scoped>
.site-header-logo {
  height: 42px;
  max-width: 220px;
}
.site-header-logo :deep(svg) {
  height: 100%;
  width: auto;
  max-width: 100%;
  object-fit: contain;
  display: block;
}
</style>
