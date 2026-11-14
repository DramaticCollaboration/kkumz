<template>
  <div
    class="canvas-panel"
    :class="{ 'is-busy-locked': busy }"
    :inert="busy || undefined"
  >
    <div class="non-dev-banner">
      <div>
        <strong>일반 창업자·마케터 안내: 코드 편집 없이 바로 6단계로 이동하실 수 있습니다.</strong>
        <p>4단계에서 설정한 디자인·상품·매거진·예약폼 구성만으로도 완성도 높은 라이브 사이트가 배포됩니다. 이 단계는 프론트엔드 개발자가 Vue 3 코드를 직접 커스텀하거나 AI 코드 자가치유를 실행할 때만 사용하세요.</p>
      </div>
      <button type="button" class="primary-btn" @click="emit('go-step', 6)">
        코드 수정 건너뛰고 6단계로 이동 →
      </button>
    </div>

    <div class="panel-header">
      <div>
        <span class="step-kicker">STEP 05 · WEBIDE CODE STUDIO &amp; AI SELF-HEALING</span>
        <h2>고급 모드 WebIDE 코드 편집기 &amp; AI 빌드 오류 자동 패치</h2>
        <p>Vue 3 컴포넌트와 테마 JSON 코드를 직접 커스텀·파일 추가하고, 빌드 오류 발생 시 AI로 즉시 복구합니다.</p>
      </div>
      <div class="header-btns">
        <button type="button" class="secondary-btn" :disabled="busy" @click="emit('sync-step4-to-webide')">
          4단계 비주얼 설정 → 코드로 재동기화
        </button>
        <input
          :value="webIdeCommitMsg"
          type="text"
          class="hint-input"
          placeholder="빌드 커밋 메모"
          @input="(e: any) => emit('update:webIdeCommitMsg', e.target.value)"
        />
        <button type="button" class="secondary-btn" :disabled="busy" @click="emit('save-and-build')">
          코드 저장 &amp; 번들 빌드
        </button>
        <button
          v-if="webIdeData?.buildStatus === 'FAILED'"
          type="button"
          class="heal-btn"
          :disabled="busy"
          @click="emit('ai-fix-build')"
        >
          AI 빌드 오류 자동 패치 실행
        </button>
        <button type="button" class="primary-btn" @click="emit('go-step', 6)">
          6단계: 빌드 &amp; 배포 →
        </button>
      </div>
    </div>

    <div class="git-repo-workbench" v-if="activeSiteSlug">
      <div class="git-wb-head">
        <div class="git-wb-title-col">
          <div class="git-wb-badge-row">
            <span class="git-provider-pill">GITHUB GITOPS &amp; DEVELOPER KEY VAULT</span>
            <span v-if="gitRepoInfo" class="git-status-pill active">
              {{ gitRepoInfo.githubRemoteSynced ? 'GitHub 원격 연동됨' : 'Managed Git 저장소 활성' }}
              · {{ gitRepoInfo.visibility }}
              · branch: {{ gitRepoInfo.defaultBranch }}
              · commit: {{ gitRepoInfo.lastCommitSha?.slice(0, 7) }}
            </span>
            <span v-else class="git-status-pill pending">미생성</span>
          </div>
          <h3>
            {{
              gitRepoInfo
                ? `Git 리포지토리 ${gitRepoInfo.repoOwner}/${gitRepoInfo.repoName} & 사용자 전용 액세스 키`
                : 'GitHub 리포지토리 생성 & 현재 사용자 전용 개발 키 발급'
            }}
          </h3>
          <p>
            {{
              gitRepoInfo
                ? '발급된 단일 리포지토리 격리형 Ed25519 SSH Deploy Key와 사이트 런타임 API Key로 로컬 IDE에서 즉시 클론·실행·푸시할 수 있습니다.'
                : '현재 사이트 코드를 즉시 실행 가능한 Vue 3 + Vite 프로젝트로 구성하여 Git 리포지토리를 생성하고, 사용자 전용 SSH 배포 키와 사이트 API 키를 발급합니다.'
            }}
          </p>
        </div>

        <div class="git-wb-actions">
          <template v-if="!gitRepoInfo">
            <button
              type="button"
              class="secondary-btn"
              :disabled="busy"
              @click="emit('update:showGitConfigForm', !showGitConfigForm)"
            >
              {{ showGitConfigForm ? '저장소 옵션 접기 ▲' : '저장소 · GitHub 토큰 옵션 ▼' }}
            </button>
            <button
              type="button"
              class="primary-btn"
              :disabled="busy"
              @click="emit('provision-git-repo')"
            >
              {{ busy ? '생성 및 키 발급 중...' : 'GitHub 리포지토리 생성 & 전용 키 발급' }}
            </button>
          </template>
          <template v-else>
            <button
              type="button"
              class="primary-btn"
              :disabled="busy"
              @click="emit('push-webide-to-git')"
            >
              {{ busy ? '푸시 동기화 중...' : '현재 WebIDE 코드 → Git Commit & Push' }}
            </button>
            <a
              :href="sshKeyDownloadUrl"
              class="secondary-btn link-btn"
              download
            >
              SSH 개인키 다운로드 ↓
            </a>
            <a
              :href="gitArchiveDownloadUrl"
              class="secondary-btn link-btn"
              download
            >
              실행 가능 Git 번들 다운로드 ↓
            </a>
            <button
              type="button"
              class="secondary-btn"
              :disabled="busy"
              @click="emit('rotate-git-keys')"
            >
              전용 키 재발급
            </button>
            <button
              type="button"
              class="secondary-btn"
              :disabled="busy"
              @click="emit('update:showGitConfigForm', !showGitConfigForm)"
            >
              {{ showGitConfigForm ? '설정 닫기 ▲' : '원격 설정 ▼' }}
            </button>
          </template>
        </div>
      </div>

      <div v-if="showGitConfigForm || !gitRepoInfo" class="git-config-bar">
        <div class="git-config-grid">
          <div class="mini-field">
            <label>GitHub Organization / Owner 계정명</label>
            <input
              v-model="gitForm.repoOwner"
              type="text"
              placeholder="예: kkumz-sites 또는 본인 GitHub ID"
            />
          </div>
          <div class="mini-field">
            <label>리포지토리 이름</label>
            <input
              v-model="gitForm.repoName"
              type="text"
              :placeholder="activeSiteSlug || 'my-brand-site'"
            />
          </div>
          <div class="mini-field">
            <label>기본 브랜치 / 공개 범위</label>
            <div class="inline-pair">
              <input v-model="gitForm.defaultBranch" type="text" placeholder="main" />
              <select v-model="gitForm.visibility">
                <option value="PRIVATE">PRIVATE</option>
                <option value="PUBLIC">PUBLIC</option>
              </select>
            </div>
          </div>
          <div class="mini-field">
            <label>GitHub Personal Access Token</label>
            <input
              v-model="gitForm.githubPatToken"
              type="password"
              autocomplete="off"
              placeholder="ghp_..."
            />
          </div>
        </div>
        <div v-if="gitRepoInfo" class="git-config-foot">
          <button
            type="button"
            class="secondary-btn"
            :disabled="busy"
            @click="emit('provision-git-repo')"
          >
            변경된 리포지토리 설정으로 재프로비저닝 실행
          </button>
        </div>
      </div>

      <div v-if="gitRepoInfo" class="git-credentials-grid">
        <div class="git-cred-card">
          <div class="cred-card-head">
            <strong>1. Git 리포지토리 주소 &amp; 전용 SSH Deploy Key</strong>
            <span class="cred-scope-chip">단일 리포지토리 격리</span>
          </div>

          <div class="cred-row">
            <label>SSH Clone URL</label>
            <div class="cred-copy-box">
              <code>{{ gitRepoInfo.cloneSshUrl }}</code>
              <button type="button" class="tiny-add-btn" @click="emit('copy-text', gitRepoInfo.cloneSshUrl, 'SSH Clone URL')">
                복사
              </button>
            </div>
          </div>

          <div class="cred-row">
            <label>원클릭 로컬 터미널 Git Clone 명령어</label>
            <div class="cred-copy-box">
              <code>{{ gitRepoInfo.quickCloneCommand }}</code>
              <button type="button" class="tiny-add-btn" @click="emit('copy-text', gitRepoInfo.quickCloneCommand, 'Git Clone 명령어')">
                명령어 복사
              </button>
            </div>
          </div>

          <div class="cred-row">
            <label>SSH Deploy Key 지문 &amp; 공개키</label>
            <div class="cred-copy-box">
              <code>{{ gitRepoInfo.sshKeyFingerprint }} · {{ gitRepoInfo.sshPublicKey }}</code>
              <button type="button" class="tiny-add-btn" @click="emit('copy-text', gitRepoInfo.sshPublicKey, 'SSH 공개키')">
                공개키 복사
              </button>
            </div>
          </div>

          <div class="cred-row">
            <div class="cred-label-split">
              <label>사용자 전용 SSH 개인키</label>
              <div class="cred-mini-btns">
                <button type="button" class="tiny-add-btn" @click="showSshPrivateKey = !showSshPrivateKey">
                  {{ showSshPrivateKey ? '개인키 숨기기' : '개인키 내용 보기' }}
                </button>
                <button type="button" class="tiny-add-btn" @click="emit('copy-text', gitRepoInfo.sshPrivateKeyPem, 'SSH 개인키')">
                  개인키 복사
                </button>
              </div>
            </div>
            <pre v-if="showSshPrivateKey" class="ssh-pem-preview">{{ gitRepoInfo.sshPrivateKeyPem }}</pre>
          </div>
        </div>

        <div class="git-cred-card">
          <div class="cred-card-head">
            <strong>2. 로컬 개발 연동용 Site Runtime API Key</strong>
            <span class="cred-scope-chip">SyncShop · SyncCMS · 업로드 자산 연동</span>
          </div>
          <p class="cred-helper-desc">
            클론한 Vue 3 프로젝트를 로컬에서 실행할 때 서버의 업로드 사진, 객실·상품, 매거진, 예약폼 데이터를 안전하게 호출하는 전용 키입니다.
          </p>

          <div class="cred-row">
            <label>Site Access Key</label>
            <div class="cred-copy-box">
              <code>{{ gitRepoInfo.siteAccessKey }}</code>
              <button type="button" class="tiny-add-btn" @click="emit('copy-text', gitRepoInfo.siteAccessKey, 'Site Access Key')">
                복사
              </button>
            </div>
          </div>

          <div class="cred-row">
            <label>Site Secret Key</label>
            <div class="cred-copy-box">
              <code>
                {{
                  showSiteSecretKey
                    ? gitRepoInfo.siteSecretKey
                    : (gitRepoInfo.siteSecretKey?.slice(0, 12) + '••••••••••••••••••••')
                }}
              </code>
              <button type="button" class="tiny-add-btn" @click="showSiteSecretKey = !showSiteSecretKey">
                {{ showSiteSecretKey ? '숨김' : '표시' }}
              </button>
              <button type="button" class="tiny-add-btn" @click="emit('copy-text', gitRepoInfo.siteSecretKey, 'Site Secret Key')">
                복사
              </button>
            </div>
          </div>

          <div v-if="gitRepoInfo.commitHistory?.length" class="cred-row">
            <label>최근 Git 커밋 동기화 이력 · {{ gitRepoInfo.commitHistory.length }}건</label>
            <div class="git-commit-list">
              <div
                v-for="commit in gitRepoInfo.commitHistory.slice(0, 3)"
                :key="commit.sha"
                class="git-commit-item"
              >
                <span class="commit-sha">{{ commit.shortSha || commit.sha?.slice(0, 7) }}</span>
                <span class="commit-msg">{{ commit.message }}</span>
                <span class="commit-meta">{{ commit.author }} · {{ commit.fileCount }} files</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="webide-grid" v-if="webIdeData">
      <div class="file-explorer">
        <div class="explorer-head-row">
          <div class="explorer-title">PROJECT FILES</div>
          <button type="button" class="tiny-add-btn" @click="emit('add-file')">+ 파일 추가</button>
        </div>
        <div
          v-for="(file, idx) in webIdeFiles"
          :key="file.path + idx"
          class="file-btn-row"
          :class="{ active: activeFileIndex === idx }"
        >
          <button
            type="button"
            class="file-btn"
            :class="{ active: activeFileIndex === idx }"
            @click="emit('update:activeFileIndex', idx)"
          >
            {{ file.path }}
          </button>
          <button
            v-if="webIdeFiles.length > 1"
            type="button"
            class="file-del-btn"
            title="파일 삭제"
            @click.stop="emit('remove-file', idx)"
          >
            ×
          </button>
        </div>
      </div>

      <div class="editor-area">
        <div class="editor-head" v-if="activeWebIdeFile">
          <div class="editor-path-wrap">
            <input
              v-model="activeWebIdeFile.path"
              type="text"
              class="file-path-input"
              title="파일 경로 직접 편집"
            />
          </div>
          <div class="editor-head-right">
            <button
              v-if="activeWebIdeFile.path.endsWith('.json') || activeWebIdeFile.language === 'json'"
              type="button"
              class="tiny-add-btn"
              @click="emit('format-json')"
            >
              JSON 포맷 정렬
            </button>
            <span class="lang-tag">{{ activeWebIdeFile.language }}</span>
          </div>
        </div>
        <textarea
          v-if="activeWebIdeFile"
          v-model="activeWebIdeFile.content"
          class="code-editor"
          spellcheck="false"
        ></textarea>
      </div>

      <div class="console-area">
        <div class="console-head">
          <span>BUILD CONSOLE</span>
          <span class="status-chip" :class="webIdeData.buildStatus?.toLowerCase()">
            {{ webIdeData.buildStatus === 'FAILED' ? '빌드 실패' : '빌드 정상' }}
          </span>
        </div>
        <pre class="console-log">{{ webIdeData.buildLog }}</pre>
      </div>
    </div>
    <div v-else class="empty-step-box">
      <strong>WebIDE 프로젝트 소스 파일을 아직 불러오지 못했습니다.</strong>
      <p>사이트 프로비저닝 및 WebIDE 파일 로드를 다시 시도하거나, 바로 6단계 배포로 이동할 수 있습니다.</p>
      <button type="button" class="primary-btn" :disabled="busy" @click="emit('go-step', 5)">
        {{ busy ? '불러오는 중...' : 'WebIDE 파일 다시 불러오기' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

defineProps<{
  busy: boolean;
  activeSiteSlug: string;
  webIdeData: any | null;
  webIdeFiles: Array<{ path: string; language: string; content: string }>;
  activeFileIndex: number;
  activeWebIdeFile: { path: string; language: string; content: string } | null;
  webIdeCommitMsg: string;
  gitRepoInfo: any | null;
  showGitConfigForm: boolean;
  gitForm: {
    repoOwner: string;
    repoName: string;
    defaultBranch: string;
    visibility: string;
    githubPatToken: string;
  };
  sshKeyDownloadUrl: string;
  gitArchiveDownloadUrl: string;
}>();

const emit = defineEmits<{
  (e: 'go-step', stepNo: number): void;
  (e: 'sync-step4-to-webide'): void;
  (e: 'update:webIdeCommitMsg', val: string): void;
  (e: 'save-and-build'): void;
  (e: 'ai-fix-build'): void;
  (e: 'update:showGitConfigForm', val: boolean): void;
  (e: 'provision-git-repo'): void;
  (e: 'push-webide-to-git'): void;
  (e: 'rotate-git-keys'): void;
  (e: 'copy-text', text: string | undefined, label: string): void;
  (e: 'add-file'): void;
  (e: 'remove-file', idx: number): void;
  (e: 'update:activeFileIndex', idx: number): void;
  (e: 'format-json'): void;
}>();

const showSshPrivateKey = ref(false);
const showSiteSecretKey = ref(false);
</script>

<style scoped src="./studio-view.css"></style>
