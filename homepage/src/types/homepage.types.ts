export interface RichFeature {
  badgeNo: string;
  title: string;
  description: string;
}

export interface ProcessStep {
  stepNo: number;
  title: string;
  description: string;
}

export interface FaqItem {
  question: string;
  answer: string;
}

export interface CliSimulation {
  command: string;
  roleTag: string;
  roleSummary: string;
  outputLines: string[];
}

export interface DemoAccount {
  roleTitle: string;
  accountId: string;
  demoAccessCode: string;
  scopeSummary: string;
}

export interface ProductEntryLink {
  id: string;
  label: string;
  badgeText: string;
  cloudUrl: string;
  isPrimary: boolean;
  isMobileViewport?: boolean;
}

export interface DownloadAsset {
  id: string;
  label: string;
  versionBadge: string;
  platformBadge: string;
  channelType?: string;
  targetServerLabel?: string;
  resetWarning?: string;
  description: string;
  downloadUrl: string;
  buttonLabel?: string;
  isDirectDownload?: boolean;
  isPrimary?: boolean;
}

export interface StandaloneArchInfo {
  badge: string;
  title: string;
  summary: string;
  components: string[];
  highlights: string[];
}

export interface EcosystemArchInfo {
  badge: string;
  title: string;
  summary: string;
  connectedAgents: string[];
  highlights: string[];
}

export interface DockerEndpointItem {
  label: string;
  url: string;
  note: string;
}

export interface DockerGuideInfo {
  workdir: string;
  devCommand: string;
  localCommand: string;
  healthCheckCmd: string;
  endpoints: DockerEndpointItem[];
}

export interface SiblingProductItem {
  id: string;
  name: string;
  subtitle: string;
  tierBadge: string;
  subdomain: string;
  iconBoxClass: string;
  faIconClass: string;
  portalUrl: string;
  corporateUrl: string;
}

export interface StandaloneProductData {
  id: string;
  domain: string;
  name: string;
  subtitle: string;
  englishRoleBadge: string;
  tierBadge: string;
  subdomain: string;
  repoPath: string;
  branchBadge?: string;
  canonicalApiPrefix: string;
  docUrl: string;
  proposalPdfUrl: string;
  corporateSolutionUrl: string;
  portalProductUrl: string;
  iconBoxClass: string;
  faIconClass: string;
  accentColor: string;
  techTags: string[];
  summary: string;
  coreCapabilities: string[];
  vsLegacyTitle: string;
  vsLegacyPoints: string[];
  vsAgentTitle: string;
  vsAgentPoints: string[];
  richFeatures: RichFeature[];
  processSteps: ProcessStep[];
  faqs: FaqItem[];
  cliSimulation: CliSimulation;
  mcpTools: string[];
  dbTables: string[];
  demoAccounts: DemoAccount[];
  entryLinks: ProductEntryLink[];
  downloadAssets?: DownloadAsset[];
  resetScheduleNotice: string;
  impactValue: string;
  impactTitle: string;
  impactSummary: string;
  standaloneArch: StandaloneArchInfo;
  ecosystemArch: EcosystemArchInfo;
  dockerGuide: DockerGuideInfo;
  siblings: SiblingProductItem[];
}
