export type ThemeArchetype =
  | 'EDITORIAL_BOTANICAL'
  | 'DARK_TECH_SHOWCASE'
  | 'MAGAZINE_HYBRID'
  | 'COMMUNITY_KINETIC'
  | 'SCENIC_HOSPITALITY';

export type SurfaceMode = 'LIGHT' | 'WARM' | 'DARK';
export type FontPreset = 'MODERN_SANS' | 'SERIF_EDITORIAL' | 'TECH_MONO' | 'BOLD_SANS';
export type HeroStyle = 'EDITORIAL_STORY' | 'PROMO_COMMERCE' | 'SPLIT_VISUAL' | 'FULL_SCENIC';

export type SectionKey =
  | 'CATALOG_GRID'
  | 'SPEC_COMPARISON'
  | 'SUBSCRIPTION_TIERS'
  | 'SESSION_TIMETABLE'
  | 'PRICING_MATRIX'
  | 'SPACE_TOUR_GALLERY'
  | 'STEP_GUIDE'
  | 'EDITORIAL_FEED'
  | 'REVIEWS_POLL'
  | 'FAQ_LOCATION'
  | 'DYNAMIC_FORM';

export interface StorefrontCartItem {
  productId: number | string;
  name: string;
  pic: string;
  unitPrice: number;
  quantity: number;
  selectedOption?: string;
  unit?: string;
  stock?: number;
}

export interface StorefrontCopyOverrides {
  navCatalogLabel?: string;
  navSpecLabel?: string;
  navSubscriptionLabel?: string;
  navTimetableLabel?: string;
  navPricingLabel?: string;
  navGalleryLabel?: string;
  navEditorialLabel?: string;
  navFaqLabel?: string;
  navFormLabel?: string;
  catalogKicker?: string;
  catalogTitle?: string;
  catalogSubtitle?: string;
  productPriceLabel?: string;
  productCtaLabel?: string;
  specKicker?: string;
  specTitle?: string;
  specSubtitle?: string;
  subscriptionKicker?: string;
  subscriptionTitle?: string;
  subscriptionSubtitle?: string;
  timetableKicker?: string;
  timetableTitle?: string;
  timetableSubtitle?: string;
  pricingKicker?: string;
  pricingTitle?: string;
  pricingSubtitle?: string;
  stepKicker?: string;
  stepTitle?: string;
  stepSubtitle?: string;
  editorialKicker?: string;
  editorialTitle?: string;
  editorialSubtitle?: string;
  reviewKicker?: string;
  reviewTitle?: string;
  faqKicker?: string;
  faqTitle?: string;
  formKicker?: string;
  conciergeTitle?: string;
  conciergeSubtitle?: string;
  conciergePresets?: string[];
}

export interface ResolvedThemeSpec {
  archetype: ThemeArchetype;
  surfaceMode: SurfaceMode;
  fontPreset: FontPreset;
  heroStyle: HeroStyle;
  isDark: boolean;
  isWarm: boolean;
  headerVariant: 'CENTERED_EDITORIAL' | 'DARK_TECH' | 'KINETIC_BOLD' | 'SCENIC_CLEAN';
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  surfaceColor: string;
  radius: string;
  cssVars: Record<string, string>;
  rootClasses: string[];
}

const ARCHETYPE_SET = new Set<string>([
  'EDITORIAL_BOTANICAL',
  'DARK_TECH_SHOWCASE',
  'MAGAZINE_HYBRID',
  'COMMUNITY_KINETIC',
  'SCENIC_HOSPITALITY',
]);

const HERO_STYLE_SET = new Set<string>([
  'EDITORIAL_STORY',
  'PROMO_COMMERCE',
  'SPLIT_VISUAL',
  'FULL_SCENIC',
]);

export function resolveThemeSpec(runtime: Record<string, any> | null | undefined): ResolvedThemeSpec {
  const tokens = runtime?.designTokens || {};
  const layout = runtime?.layoutSchema || {};
  const rawArchetype = String(runtime?.themeArchetype || layout.themeArchetype || '').toUpperCase();
  const archetype: ThemeArchetype = ARCHETYPE_SET.has(rawArchetype)
    ? (rawArchetype as ThemeArchetype)
    : inferArchetypeFromRuntime(runtime);

  const rawHero = String(runtime?.heroStyle || layout.heroStyle || '').toUpperCase();
  const heroStyle: HeroStyle = HERO_STYLE_SET.has(rawHero)
    ? (rawHero as HeroStyle)
    : defaultHeroForArchetype(archetype);

  const rawSurfaceMode = String(runtime?.surfaceMode || tokens.surfaceMode || '').toUpperCase();
  const surfaceMode: SurfaceMode =
    rawSurfaceMode === 'DARK' || rawSurfaceMode === 'WARM' || rawSurfaceMode === 'LIGHT'
      ? (rawSurfaceMode as SurfaceMode)
      : defaultSurfaceForArchetype(archetype);

  const rawFont = String(runtime?.fontPreset || tokens.fontPreset || '').toUpperCase();
  const fontPreset: FontPreset =
    rawFont === 'SERIF_EDITORIAL' || rawFont === 'TECH_MONO' || rawFont === 'BOLD_SANS' || rawFont === 'MODERN_SANS'
      ? (rawFont as FontPreset)
      : defaultFontForArchetype(archetype);

  const isDark = surfaceMode === 'DARK';
  const isWarm = surfaceMode === 'WARM';
  const primaryColor = String(tokens.primaryColor || tokens.primary || '#14532d');
  const secondaryColor = String(tokens.secondaryColor || tokens.secondary || '#15803d');
  const accentColor = String(tokens.accentColor || tokens.accent || '#d97706');
  const surfaceColor = String(
    tokens.surfaceColor ||
      tokens.surface ||
      (isDark ? '#09090b' : isWarm ? '#f8f5ef' : '#ffffff'),
  );
  const radius = String(
    tokens.radius ||
      (archetype === 'EDITORIAL_BOTANICAL'
        ? '6px'
        : archetype === 'SCENIC_HOSPITALITY'
          ? '16px'
          : '10px'),
  );
  const numericRadius = parseInt(radius, 10) || 10;
  const radiusLg = `${Math.min(28, Math.max(8, numericRadius + 6))}px`;

  const headerVariant =
    archetype === 'EDITORIAL_BOTANICAL' || archetype === 'MAGAZINE_HYBRID'
      ? 'CENTERED_EDITORIAL'
      : archetype === 'DARK_TECH_SHOWCASE'
        ? 'DARK_TECH'
        : archetype === 'COMMUNITY_KINETIC'
          ? 'KINETIC_BOLD'
          : 'SCENIC_CLEAN';

  const cssVars: Record<string, string> = {
    '--sf-primary': primaryColor,
    '--sf-secondary': secondaryColor,
    '--sf-accent': accentColor,
    '--sf-bg': surfaceColor,
    '--sf-radius': radius,
    '--sf-radius-lg': radiusLg,
  };

  return {
    archetype,
    surfaceMode,
    fontPreset,
    heroStyle,
    isDark,
    isWarm,
    headerVariant,
    primaryColor,
    secondaryColor,
    accentColor,
    surfaceColor,
    radius,
    cssVars,
    rootClasses: [
      'sf-root',
      `sf-theme-${archetype.toLowerCase()}`,
      `sf-surface-${surfaceMode.toLowerCase()}`,
      `sf-font-${fontPreset.toLowerCase()}`,
    ],
  };
}

function inferArchetypeFromRuntime(runtime: Record<string, any> | null | undefined): ThemeArchetype {
  const siteType = String(runtime?.siteType || '').toUpperCase();
  const category = String(runtime?.category || '').toUpperCase();
  const enabled: string[] = Array.isArray(runtime?.enabledFeatures) ? runtime.enabledFeatures : [];
  if (category === 'TECH_HARDWARE' || enabled.includes('SHOP_SPEC_COMPARISON')) {
    return 'DARK_TECH_SHOWCASE';
  }
  if (category === 'COMMUNITY_MEDIA' || siteType === 'CMS_SITE') {
    return 'COMMUNITY_KINETIC';
  }
  if (enabled.includes('SHOP_PRICING_MATRIX')) {
    return 'SCENIC_HOSPITALITY';
  }
  if (siteType === 'COMMERCE') {
    return 'EDITORIAL_BOTANICAL';
  }
  return 'MAGAZINE_HYBRID';
}

function defaultHeroForArchetype(archetype: ThemeArchetype): HeroStyle {
  if (archetype === 'DARK_TECH_SHOWCASE') return 'PROMO_COMMERCE';
  if (archetype === 'EDITORIAL_BOTANICAL') return 'EDITORIAL_STORY';
  if (archetype === 'MAGAZINE_HYBRID') return 'SPLIT_VISUAL';
  return 'FULL_SCENIC';
}

function defaultSurfaceForArchetype(archetype: ThemeArchetype): SurfaceMode {
  if (archetype === 'DARK_TECH_SHOWCASE') return 'DARK';
  if (archetype === 'COMMUNITY_KINETIC') return 'LIGHT';
  return 'WARM';
}

function defaultFontForArchetype(archetype: ThemeArchetype): FontPreset {
  if (archetype === 'DARK_TECH_SHOWCASE') return 'TECH_MONO';
  if (archetype === 'COMMUNITY_KINETIC') return 'BOLD_SANS';
  return 'SERIF_EDITORIAL';
}

export function resolveGridColumnsClass(count: number, maxCols: 2 | 3 | 4 = 4): string {
  if (count <= 1) return 'grid-cols-1';
  if (count === 2 || maxCols === 2) return 'grid-cols-1 md:grid-cols-2';
  if (count === 3 || maxCols === 3) return 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3';
  return 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4';
}

export function displayProductPrice(prod: Record<string, any> | null | undefined): number {
  const promo = Number(prod?.promotionPrice || 0);
  if (promo > 0) return promo;
  const price = Number(prod?.price || 0);
  return price > 0 ? price : 0;
}

export function formatKrw(value: number | string | null | undefined): string {
  const n = Number(value || 0);
  return Number.isFinite(n) ? `${n.toLocaleString('ko-KR')}원` : '0원';
}

const DEFAULT_SECTION_ORDER_BY_ARCHETYPE: Record<ThemeArchetype, SectionKey[]> = {
  EDITORIAL_BOTANICAL: [
    'CATALOG_GRID',
    'STEP_GUIDE',
    'EDITORIAL_FEED',
    'REVIEWS_POLL',
    'FAQ_LOCATION',
    'DYNAMIC_FORM',
  ],
  DARK_TECH_SHOWCASE: [
    'CATALOG_GRID',
    'SPEC_COMPARISON',
    'EDITORIAL_FEED',
    'REVIEWS_POLL',
    'FAQ_LOCATION',
    'DYNAMIC_FORM',
  ],
  MAGAZINE_HYBRID: [
    'EDITORIAL_FEED',
    'CATALOG_GRID',
    'SUBSCRIPTION_TIERS',
    'STEP_GUIDE',
    'REVIEWS_POLL',
    'FAQ_LOCATION',
    'DYNAMIC_FORM',
  ],
  COMMUNITY_KINETIC: [
    'SESSION_TIMETABLE',
    'EDITORIAL_FEED',
    'SPACE_TOUR_GALLERY',
    'REVIEWS_POLL',
    'DYNAMIC_FORM',
    'FAQ_LOCATION',
  ],
  SCENIC_HOSPITALITY: [
    'CATALOG_GRID',
    'PRICING_MATRIX',
    'SPACE_TOUR_GALLERY',
    'STEP_GUIDE',
    'REVIEWS_POLL',
    'FAQ_LOCATION',
    'DYNAMIC_FORM',
  ],
};

export function resolveOrderedSections(
  archetype: ThemeArchetype,
  explicitOrder: unknown,
  availability: Record<SectionKey, boolean>,
): SectionKey[] {
  const validKeys = new Set<SectionKey>([
    'CATALOG_GRID',
    'SPEC_COMPARISON',
    'SUBSCRIPTION_TIERS',
    'SESSION_TIMETABLE',
    'PRICING_MATRIX',
    'SPACE_TOUR_GALLERY',
    'STEP_GUIDE',
    'EDITORIAL_FEED',
    'REVIEWS_POLL',
    'FAQ_LOCATION',
    'DYNAMIC_FORM',
  ]);
  const ordered: SectionKey[] = [];
  if (Array.isArray(explicitOrder)) {
    for (const raw of explicitOrder) {
      const k = String(raw || '').toUpperCase() as SectionKey;
      if (validKeys.has(k) && availability[k] && !ordered.includes(k)) {
        ordered.push(k);
      }
    }
  }
  const defaults = DEFAULT_SECTION_ORDER_BY_ARCHETYPE[archetype] || DEFAULT_SECTION_ORDER_BY_ARCHETYPE.MAGAZINE_HYBRID;
  for (const k of defaults) {
    if (availability[k] && !ordered.includes(k)) {
      ordered.push(k);
    }
  }
  for (const k of validKeys) {
    if (availability[k] && !ordered.includes(k)) {
      ordered.push(k);
    }
  }
  return ordered;
}
