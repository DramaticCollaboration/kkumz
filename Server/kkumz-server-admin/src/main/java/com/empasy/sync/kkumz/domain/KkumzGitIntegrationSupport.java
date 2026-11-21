package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;
import tools.jackson.databind.JsonNode;

/**
 * Kkumz 사이트별 GitHub 리포지토리 프로비저닝, 양방향 Push 동기화, Ed25519 배포 키 회전 및 ZIP 내보내기를 담당하는 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzGitIntegrationSupport {

    private final KkumzService root;

    Map<String, Object> getSiteGitRepo(String siteIdOrSlug) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        Map<String, Object> repo = findSiteGitRepoNullable(siteId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("site", site);
        result.put("provisioned", repo != null);
        result.put("gitRepo", repo);
        return result;
    }

    Map<String, Object> provisionSiteGitRepo(String siteIdOrSlug, ProvisionGitRepoRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, activeVer);

        String actor = (req != null && req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername().trim()
                : String.valueOf(site.get("ownerUsername"));
        String repoOwner = (req != null && req.getRepoOwner() != null && !req.getRepoOwner().isBlank())
                ? sanitizeGitIdentifier(req.getRepoOwner().trim(), "kkumz-sites")
                : "kkumz-sites";
        String repoName = (req != null && req.getRepoName() != null && !req.getRepoName().isBlank())
                ? sanitizeGitIdentifier(req.getRepoName().trim(), siteSlug)
                : sanitizeGitIdentifier(siteSlug, "kkumz-site");
        String defaultBranch = (req != null && req.getDefaultBranch() != null && !req.getDefaultBranch().isBlank())
                ? sanitizeGitIdentifier(req.getDefaultBranch().trim(), "main")
                : "main";
        String visibility = (req != null && req.getVisibility() != null && "PUBLIC".equalsIgnoreCase(req.getVisibility().trim()))
                ? "PUBLIC"
                : "PRIVATE";
        boolean includeScaffold = req == null || req.getIncludeFullViteScaffold() == null || req.getIncludeFullViteScaffold();

        // 1. 사이트 전용 런타임 API 액세스키 (kkumz_api_access_key) 발급 또는 조회
        Map<String, Object> siteApiKey = ensureSiteRuntimeApiKey(siteId, siteSlug, siteName, actor, false);
        String siteApiKeyId = String.valueOf(siteApiKey.get("id"));
        String siteAccessKey = String.valueOf(siteApiKey.get("accessKey"));
        String siteSecretKey = String.valueOf(siteApiKey.get("secretKey"));

        // 2. 리포지토리 단일 범위 Ed25519 SSH Deploy Key 쌍 생성
        String keyTitle = "kkumz-deploy-key@" + siteSlug + " (" + actor + ")";
        Map<String, String> sshKeyPair = generateEd25519SshKeyPair(keyTitle);
        String sshPublicKey = sshKeyPair.get("publicKey");
        String sshPrivateKeyPem = sshKeyPair.get("privateKeyPem");
        String sshFingerprint = sshKeyPair.get("fingerprint");

        String repoHtmlUrl = "https://github.com/" + repoOwner + "/" + repoName;
        String cloneSshUrl = "git@github.com:" + repoOwner + "/" + repoName + ".git";
        String cloneHttpsUrl = "https://github.com/" + repoOwner + "/" + repoName + ".git";

        // 3. 실행 가능한(Runnable) Vue 3 + Vite 프로젝트 스캐폴딩 파일 구성
        List<Map<String, Object>> currentFiles = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );
        List<Map<String, Object>> fullRepoFiles = includeScaffold
                ? buildRunnableGitProjectFiles(site, currentFiles, repoOwner, repoName, defaultBranch, cloneSshUrl, siteAccessKey, siteSecretKey)
                : currentFiles;

        String commitMsg = "feat(scaffold): initialize " + siteName + " (" + siteSlug + ") runnable Vue 3 + Vite repository";
        String commitSha = calculateGitCommitSha(siteId, repoName, commitMsg, fullRepoFiles);

        // 4. 선택적 GitHub REST API 원격 저장소 생성 및 Deploy Key 등록
        String patToken = (req != null && req.getGithubPatToken() != null && !req.getGithubPatToken().isBlank())
                ? req.getGithubPatToken().trim()
                : resolveConfiguredGitHubToken();
        boolean remoteSynced = false;
        String syncMode = "MANAGED_GIT";
        String remoteNotice = "Kkumz Managed Git 저장소 프로비저닝 및 사용자 전용 키(SSH Deploy Key + Site API Key) 발급 완료";

        if (patToken != null && !patToken.isBlank()) {
            Map<String, Object> ghSync = syncWithGitHubRemoteApi(
                    patToken, repoOwner, repoName, defaultBranch, "PRIVATE".equals(visibility),
                    siteName + " — Built with Kkumz AI Studio (/s/" + siteSlug + ")",
                    keyTitle, sshPublicKey, fullRepoFiles, commitMsg
            );
            if (Boolean.TRUE.equals(ghSync.get("synced"))) {
                remoteSynced = true;
                syncMode = "GITHUB_API";
                if (ghSync.get("htmlUrl") != null) repoHtmlUrl = String.valueOf(ghSync.get("htmlUrl"));
                if (ghSync.get("sshUrl") != null) cloneSshUrl = String.valueOf(ghSync.get("sshUrl"));
                if (ghSync.get("cloneUrl") != null) cloneHttpsUrl = String.valueOf(ghSync.get("cloneUrl"));
                if (ghSync.get("owner") != null) repoOwner = String.valueOf(ghSync.get("owner"));
                if (ghSync.get("commitSha") != null) commitSha = String.valueOf(ghSync.get("commitSha"));
                remoteNotice = String.valueOf(ghSync.getOrDefault("message", "GitHub 원격 저장소 생성 및 Deploy Key 등록 완료"));
            } else if (ghSync.get("message") != null) {
                remoteNotice = String.valueOf(ghSync.get("message"));
            }
        }

        // 5. 로컬 디스크 Git 작업 트리 동기화
        writeGitRepoFilesToWorkspace(siteSlug, repoOwner, repoName, defaultBranch, commitSha, commitMsg, fullRepoFiles, sshPublicKey);

        // 6. 커밋 히스토리 기록 및 DB 영속화
        Map<String, Object> commitEntry = new LinkedHashMap<>();
        commitEntry.put("sha", commitSha);
        commitEntry.put("shortSha", commitSha.substring(0, 7));
        commitEntry.put("message", commitMsg);
        commitEntry.put("author", actor);
        commitEntry.put("branch", defaultBranch);
        commitEntry.put("fileCount", fullRepoFiles.size());
        commitEntry.put("committedAt", Instant.now().toString());
        List<Map<String, Object>> history = new ArrayList<>();
        history.add(commitEntry);

        Map<String, Object> existingRepo = findSiteGitRepoNullable(siteId);
        if (existingRepo != null) {
            if (existingRepo.get("commitHistory") instanceof List<?> prevList) {
                for (Object item : prevList) {
                    if (item instanceof Map<?, ?> m && history.size() < 15) {
                        Map<String, Object> copy = new LinkedHashMap<>();
                        m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                        history.add(copy);
                    }
                }
            }
            root.jdbcTemplate().update("""
                    UPDATE kkumz_site_git_repo
                    SET repo_owner = ?, repo_name = ?, default_branch = ?, visibility = ?,
                        repo_html_url = ?, clone_ssh_url = ?, clone_https_url = ?,
                        ssh_key_title = ?, ssh_key_fingerprint = ?, ssh_public_key = ?, ssh_private_key_pem = ?,
                        site_api_key_id = ?, site_access_key = ?, site_secret_key = ?,
                        last_commit_sha = ?, last_commit_message = ?, sync_mode = ?, github_remote_synced = ?,
                        commit_history_json = ?::jsonb, key_rotated_at = CURRENT_TIMESTAMP,
                        last_synced_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                    WHERE site_id = ?
                    """,
                    repoOwner, repoName, defaultBranch, visibility,
                    repoHtmlUrl, cloneSshUrl, cloneHttpsUrl,
                    keyTitle, sshFingerprint, sshPublicKey, sshPrivateKeyPem,
                    siteApiKeyId, siteAccessKey, siteSecretKey,
                    commitSha, commitMsg, syncMode, remoteSynced,
                    toJson(history), siteId
            );
        } else {
            String gitRepoId = "git-" + siteId;
            root.jdbcTemplate().update("""
                    INSERT INTO kkumz_site_git_repo (
                        id, site_id, provider, repo_owner, repo_name, default_branch, visibility,
                        repo_html_url, clone_ssh_url, clone_https_url,
                        ssh_key_title, ssh_key_fingerprint, ssh_public_key, ssh_private_key_pem,
                        site_api_key_id, site_access_key, site_secret_key,
                        last_commit_sha, last_commit_message, sync_mode, github_remote_synced,
                        commit_history_json, key_rotated_at, last_synced_at, created_by, created_at, updated_at
                    ) VALUES (?, ?, 'GITHUB', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    gitRepoId, siteId, repoOwner, repoName, defaultBranch, visibility,
                    repoHtmlUrl, cloneSshUrl, cloneHttpsUrl,
                    keyTitle, sshFingerprint, sshPublicKey, sshPrivateKeyPem,
                    siteApiKeyId, siteAccessKey, siteSecretKey,
                    commitSha, commitMsg, syncMode, remoteSynced,
                    toJson(history), actor
            );
        }

        // 7. 최신 릴리즈의 webide_files_json에도 실행 가능 스캐폴딩 파일 세트 반영 및 빌드 로그 기록
        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String buildLog = "[" + nowTime + "] [Git-Provision] Repository " + repoOwner + "/" + repoName + " (" + defaultBranch + ") initialized.\n"
                + "[" + nowTime + "] [SSH-KeyGen] Ed25519 Deploy Key issued (" + sshFingerprint + ").\n"
                + "[" + nowTime + "] [Runtime-Key] Site SDK Access Key issued (" + siteAccessKey + ").\n"
                + "[" + nowTime + "] [Commit] " + commitSha.substring(0, 7) + " - " + commitMsg + " (" + fullRepoFiles.size() + " files, " + durationMs + "ms).\n"
                + "[" + nowTime + "] [Sync-Status] " + remoteNotice;

        root.jdbcTemplate().update("""
                UPDATE kkumz_site_release
                SET webide_files_json = ?::jsonb, build_log = ?
                WHERE id = ?
                """,
                toJson(fullRepoFiles), buildLog, latestRel.get("id")
        );

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_build_job (
                    id, site_id, release_id, job_type, deploy_mode, status, duration_ms,
                    ai_patch_applied, build_log, triggered_by, started_at, finished_at
                ) VALUES (?, ?, ?, 'GIT_REPO_PROVISION', 'CUSTOM_BUNDLE', 'SUCCESS', ?, FALSE, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                "job-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, latestRel.get("id"), durationMs, buildLog, actor
        );

        Map<String, Object> workspace = root.buildDeploy().getWebIdeFiles(siteSlug);
        workspace.put("notice", remoteNotice);
        return workspace;
    }

    Map<String, Object> pushSiteToGitRepo(String siteIdOrSlug, GitPushSyncRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        long startNs = System.nanoTime();
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));

        Map<String, Object> existingRepo = findSiteGitRepoNullable(siteId);
        if (existingRepo == null) {
            ProvisionGitRepoRequest provReq = ProvisionGitRepoRequest.builder()
                    .githubPatToken(req != null ? req.getGithubPatToken() : null)
                    .username(req != null ? req.getUsername() : null)
                    .includeFullViteScaffold(true)
                    .build();
            return provisionSiteGitRepo(siteSlug, provReq);
        }

        if (req != null && req.getFiles() != null && !req.getFiles().isEmpty()) {
            UpdateWebIdeFilesRequest updateReq = UpdateWebIdeFilesRequest.builder()
                    .files(req.getFiles())
                    .commitMessage(req.getCommitMessage())
                    .username(req.getUsername())
                    .build();
            root.buildDeploy().updateWebIdeFiles(siteSlug, updateReq);
        }

        int activeVer = ((Number) root.accessMeta().findSiteByIdOrSlug(siteId).get("activeVersion")).intValue();
        Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, activeVer);
        List<Map<String, Object>> files = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );

        String actor = (req != null && req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername().trim()
                : String.valueOf(site.get("ownerUsername"));
        String repoOwner = String.valueOf(existingRepo.get("repoOwner"));
        String repoName = String.valueOf(existingRepo.get("repoName"));
        String defaultBranch = String.valueOf(existingRepo.get("defaultBranch"));
        String commitMsg = (req != null && req.getCommitMessage() != null && !req.getCommitMessage().isBlank())
                ? req.getCommitMessage().trim()
                : "feat(studio): sync WebIDE changes for " + siteName + " (v" + activeVer + ")";
        String commitSha = calculateGitCommitSha(siteId, repoName, commitMsg + ":" + Instant.now(), files);

        String patToken = (req != null && req.getGithubPatToken() != null && !req.getGithubPatToken().isBlank())
                ? req.getGithubPatToken().trim()
                : resolveConfiguredGitHubToken();
        boolean remoteSynced = Boolean.TRUE.equals(existingRepo.get("githubRemoteSynced"));
        String syncMode = String.valueOf(existingRepo.get("syncMode"));
        String remoteNotice = "Git 저장소 커밋(" + commitSha.substring(0, 7) + ") 및 번들 동기화 완료";

        if (patToken != null && !patToken.isBlank()) {
            Map<String, Object> ghSync = syncWithGitHubRemoteApi(
                    patToken, repoOwner, repoName, defaultBranch,
                    "PRIVATE".equalsIgnoreCase(String.valueOf(existingRepo.get("visibility"))),
                    siteName + " — Built with Kkumz AI Studio (/s/" + siteSlug + ")",
                    String.valueOf(existingRepo.get("sshKeyTitle")),
                    String.valueOf(existingRepo.get("sshPublicKey")),
                    files, commitMsg
            );
            if (Boolean.TRUE.equals(ghSync.get("synced"))) {
                remoteSynced = true;
                syncMode = "GITHUB_API";
                if (ghSync.get("commitSha") != null) commitSha = String.valueOf(ghSync.get("commitSha"));
                remoteNotice = String.valueOf(ghSync.getOrDefault("message", "GitHub 원격 브랜치(" + defaultBranch + ")로 커밋 푸시 완료"));
            } else if (ghSync.get("message") != null) {
                remoteNotice = String.valueOf(ghSync.get("message"));
            }
        }

        writeGitRepoFilesToWorkspace(
                siteSlug, repoOwner, repoName, defaultBranch, commitSha, commitMsg, files,
                String.valueOf(existingRepo.get("sshPublicKey"))
        );

        Map<String, Object> commitEntry = new LinkedHashMap<>();
        commitEntry.put("sha", commitSha);
        commitEntry.put("shortSha", commitSha.substring(0, 7));
        commitEntry.put("message", commitMsg);
        commitEntry.put("author", actor);
        commitEntry.put("branch", defaultBranch);
        commitEntry.put("fileCount", files.size());
        commitEntry.put("committedAt", Instant.now().toString());

        List<Map<String, Object>> history = new ArrayList<>();
        history.add(commitEntry);
        if (existingRepo.get("commitHistory") instanceof List<?> prevList) {
            for (Object item : prevList) {
                if (item instanceof Map<?, ?> m && history.size() < 15) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                    history.add(copy);
                }
            }
        }

        root.jdbcTemplate().update("""
                UPDATE kkumz_site_git_repo
                SET last_commit_sha = ?, last_commit_message = ?, sync_mode = ?, github_remote_synced = ?,
                    commit_history_json = ?::jsonb, last_synced_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE site_id = ?
                """,
                commitSha, commitMsg, syncMode, remoteSynced, toJson(history), siteId
        );

        long durationMs = Math.max(1L, (System.nanoTime() - startNs) / 1_000_000L);
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String pushLog = "[" + nowTime + "] [Git-Push] " + repoOwner + "/" + repoName + " (" + defaultBranch + ") <- " + commitSha.substring(0, 7) + "\n"
                + "[" + nowTime + "] [Commit-Msg] " + commitMsg + " (" + files.size() + " files, " + durationMs + "ms)\n"
                + "[" + nowTime + "] [Result] " + remoteNotice;

        root.jdbcTemplate().update(
                "UPDATE kkumz_site_release SET build_log = ? WHERE id = ?",
                pushLog, latestRel.get("id")
        );

        Map<String, Object> workspace = root.buildDeploy().getWebIdeFiles(siteSlug);
        workspace.put("notice", remoteNotice);
        return workspace;
    }

    Map<String, Object> rotateSiteGitKeys(String siteIdOrSlug, RotateGitKeysRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));

        Map<String, Object> existingRepo = findSiteGitRepoNullable(siteId);
        if (existingRepo == null) {
            return provisionSiteGitRepo(siteSlug, ProvisionGitRepoRequest.builder()
                    .username(req != null ? req.getUsername() : null)
                    .githubPatToken(req != null ? req.getGithubPatToken() : null)
                    .includeFullViteScaffold(true)
                    .build());
        }

        boolean rotateSsh = req == null || req.getRotateSshKey() == null || req.getRotateSshKey();
        boolean rotateApi = req == null || req.getRotateSiteApiKey() == null || req.getRotateSiteApiKey();
        String actor = (req != null && req.getUsername() != null && !req.getUsername().isBlank())
                ? req.getUsername().trim()
                : String.valueOf(site.get("ownerUsername"));

        String sshKeyTitle = String.valueOf(existingRepo.get("sshKeyTitle"));
        String sshPublicKey = String.valueOf(existingRepo.get("sshPublicKey"));
        String sshPrivateKeyPem = String.valueOf(existingRepo.get("sshPrivateKeyPem"));
        String sshFingerprint = String.valueOf(existingRepo.get("sshKeyFingerprint"));

        if (rotateSsh) {
            sshKeyTitle = "kkumz-deploy-key@" + siteSlug + " (rotated by " + actor + ")";
            Map<String, String> newPair = generateEd25519SshKeyPair(sshKeyTitle);
            sshPublicKey = newPair.get("publicKey");
            sshPrivateKeyPem = newPair.get("privateKeyPem");
            sshFingerprint = newPair.get("fingerprint");

            String patToken = (req != null && req.getGithubPatToken() != null && !req.getGithubPatToken().isBlank())
                    ? req.getGithubPatToken().trim()
                    : resolveConfiguredGitHubToken();
            if (patToken != null && !patToken.isBlank()) {
                replaceGitHubDeployKey(
                        patToken,
                        String.valueOf(existingRepo.get("repoOwner")),
                        String.valueOf(existingRepo.get("repoName")),
                        sshKeyTitle,
                        sshPublicKey
                );
            }
        }

        String siteApiKeyId = existingRepo.get("siteApiKeyId") != null ? String.valueOf(existingRepo.get("siteApiKeyId")) : null;
        String siteAccessKey = String.valueOf(existingRepo.get("siteAccessKey"));
        String siteSecretKey = String.valueOf(existingRepo.get("siteSecretKey"));

        if (rotateApi) {
            Map<String, Object> rotatedKey = ensureSiteRuntimeApiKey(siteId, siteSlug, siteName, actor, true);
            siteApiKeyId = String.valueOf(rotatedKey.get("id"));
            siteAccessKey = String.valueOf(rotatedKey.get("accessKey"));
            siteSecretKey = String.valueOf(rotatedKey.get("secretKey"));
        }

        root.jdbcTemplate().update("""
                UPDATE kkumz_site_git_repo
                SET ssh_key_title = ?, ssh_key_fingerprint = ?, ssh_public_key = ?, ssh_private_key_pem = ?,
                    site_api_key_id = ?, site_access_key = ?, site_secret_key = ?,
                    key_rotated_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE site_id = ?
                """,
                sshKeyTitle, sshFingerprint, sshPublicKey, sshPrivateKeyPem,
                siteApiKeyId, siteAccessKey, siteSecretKey, siteId
        );

        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, activeVer);
        List<Map<String, Object>> files = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );
        for (Map<String, Object> f : files) {
            if (".env.example".equals(String.valueOf(f.get("path")))) {
                f.put("content", buildDotEnvExampleContent(siteSlug, siteAccessKey, siteSecretKey));
            }
        }
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String rotateLog = "[" + nowTime + "] [Key-Rotation] Rotated developer keys for /s/" + siteSlug + ".\n"
                + "[" + nowTime + "] [SSH-Key] Fingerprint: " + sshFingerprint + "\n"
                + "[" + nowTime + "] [Site-API-Key] AccessKey: " + siteAccessKey + " (previous secret revoked)";
        root.jdbcTemplate().update(
                "UPDATE kkumz_site_release SET webide_files_json = ?::jsonb, build_log = ? WHERE id = ?",
                toJson(files), rotateLog, latestRel.get("id")
        );

        Map<String, Object> workspace = root.buildDeploy().getWebIdeFiles(siteSlug);
        workspace.put("notice", "사용자 전용 SSH Deploy Key 및 사이트 런타임 API Secret Key가 안전하게 재발급(회전)되었습니다.");
        return workspace;
    }

    StoredAssetPayload downloadSiteSshPrivateKey(String siteIdOrSlug) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        Map<String, Object> repo = findSiteGitRepoNullable(siteId);
        if (repo == null) {
            provisionSiteGitRepo(siteSlug, ProvisionGitRepoRequest.builder().includeFullViteScaffold(true).build());
            repo = findSiteGitRepoNullable(siteId);
        }
        String pem = String.valueOf(repo.get("sshPrivateKeyPem"));
        return StoredAssetPayload.builder()
                .bytes(pem.getBytes(StandardCharsets.UTF_8))
                .mimeType("application/x-pem-file")
                .originalName(siteSlug + "-deploy-key.pem")
                .build();
    }

    StoredAssetPayload downloadSiteGitArchive(String siteIdOrSlug) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String siteSlug = String.valueOf(site.get("siteSlug"));
        Map<String, Object> repo = findSiteGitRepoNullable(siteId);
        if (repo == null) {
            provisionSiteGitRepo(siteSlug, ProvisionGitRepoRequest.builder().includeFullViteScaffold(true).build());
            repo = findSiteGitRepoNullable(siteId);
        }
        int activeVer = ((Number) root.accessMeta().findSiteByIdOrSlug(siteId).get("activeVersion")).intValue();
        Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, activeVer);
        List<Map<String, Object>> files = normalizeAndFormatWebIdeFiles(
                parseJsonList(String.valueOf(latestRel.get("webideFilesJson")))
        );

        String repoName = String.valueOf(repo.get("repoName"));
        String cloneSshUrl = String.valueOf(repo.get("cloneSshUrl"));
        String defaultBranch = String.valueOf(repo.get("defaultBranch"));
        String sshPrivateKeyPem = String.valueOf(repo.get("sshPrivateKeyPem"));
        String sshPublicKey = String.valueOf(repo.get("sshPublicKey"));
        String siteAccessKey = String.valueOf(repo.get("siteAccessKey"));
        String siteSecretKey = String.valueOf(repo.get("siteSecretKey"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            String rootPrefix = repoName + "/";
            for (Map<String, Object> file : files) {
                String relPath = String.valueOf(file.getOrDefault("path", "")).replaceAll("^/+", "");
                if (relPath.isBlank()) continue;
                String content = String.valueOf(file.getOrDefault("content", ""));
                addZipTextEntry(zos, rootPrefix + relPath, content);
            }
            addZipTextEntry(zos, rootPrefix + ".env.local", buildDotEnvExampleContent(siteSlug, siteAccessKey, siteSecretKey));
            addZipTextEntry(zos, rootPrefix + ".ssh/" + siteSlug + "-deploy-key.pem", sshPrivateKeyPem);
            addZipTextEntry(zos, rootPrefix + ".ssh/" + siteSlug + "-deploy-key.pub", sshPublicKey + "\n");
            String setupSh = "#!/usr/bin/env bash\n"
                    + "set -e\n"
                    + "chmod 600 .ssh/" + siteSlug + "-deploy-key.pem\n"
                    + "git init -b " + defaultBranch + "\n"
                    + "git config core.sshCommand \"ssh -i $(pwd)/.ssh/" + siteSlug + "-deploy-key.pem -o IdentitiesOnly=yes\"\n"
                    + "git remote remove origin 2>/dev/null || true\n"
                    + "git remote add origin " + cloneSshUrl + "\n"
                    + "echo \"[Kkumz Git] SSH Deploy Key & Remote (" + cloneSshUrl + ") configured.\"\n";
            addZipTextEntry(zos, rootPrefix + "setup-git-key.sh", setupSh);
            zos.finish();
            return StoredAssetPayload.builder()
                    .bytes(baos.toByteArray())
                    .mimeType("application/zip")
                    .originalName(repoName + "-git-bundle.zip")
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Git 프로젝트 번들 ZIP 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    Map<String, Object> findSiteGitRepoNullable(String siteId) {
        try {
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                    SELECT id,
                           site_id AS "siteId",
                           provider,
                           repo_owner AS "repoOwner",
                           repo_name AS "repoName",
                           default_branch AS "defaultBranch",
                           visibility,
                           repo_html_url AS "repoHtmlUrl",
                           clone_ssh_url AS "cloneSshUrl",
                           clone_https_url AS "cloneHttpsUrl",
                           ssh_key_title AS "sshKeyTitle",
                           ssh_key_fingerprint AS "sshKeyFingerprint",
                           ssh_public_key AS "sshPublicKey",
                           ssh_private_key_pem AS "sshPrivateKeyPem",
                           site_api_key_id AS "siteApiKeyId",
                           site_access_key AS "siteAccessKey",
                           site_secret_key AS "siteSecretKey",
                           last_commit_sha AS "lastCommitSha",
                           last_commit_message AS "lastCommitMessage",
                           sync_mode AS "syncMode",
                           github_remote_synced AS "githubRemoteSynced",
                           commit_history_json::text AS "commitHistoryJson",
                           key_rotated_at AS "keyRotatedAt",
                           last_synced_at AS "lastSyncedAt",
                           created_by AS "createdBy",
                           created_at AS "createdAt",
                           updated_at AS "updatedAt"
                    FROM kkumz_site_git_repo
                    WHERE site_id = ?
                    LIMIT 1
                    """, siteId);
            if (rows.isEmpty()) {
                return null;
            }
            Map<String, Object> row = new LinkedHashMap<>(rows.get(0));
            String commitHistoryJson = String.valueOf(row.remove("commitHistoryJson"));
            row.put("commitHistory", parseJsonList(commitHistoryJson));
            String slug = String.valueOf(row.getOrDefault("repoName", "site"));
            String sshUrl = String.valueOf(row.getOrDefault("cloneSshUrl", ""));
            String branch = String.valueOf(row.getOrDefault("defaultBranch", "main"));
            row.put("quickCloneCommand",
                    "GIT_SSH_COMMAND=\"ssh -i ~/.ssh/" + slug + "-deploy-key.pem -o IdentitiesOnly=yes\" git clone -b " + branch + " " + sshUrl);
            return row;
        } catch (Exception e) {
            log.debug("kkumz_site_git_repo 조회 스킵: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> ensureSiteRuntimeApiKey(
            String siteId,
            String siteSlug,
            String siteName,
            String actor,
            boolean forceRotate
    ) {
        String keyMarker = "[" + siteSlug + "]";
        List<Map<String, Object>> existing = root.jdbcTemplate().queryForList("""
                SELECT id, access_key AS "accessKey", secret_key AS "secretKey"
                FROM kkumz_api_access_key
                WHERE direction = 'INBOUND' AND service_code = 'SITE_GIT_SDK' AND key_name LIKE ?
                ORDER BY created_at DESC
                LIMIT 1
                """, "%" + keyMarker + "%");

        if (!existing.isEmpty()) {
            Map<String, Object> found = new LinkedHashMap<>(existing.get(0));
            if (forceRotate) {
                String newSecret = "sk_site_" + UUID.randomUUID().toString().replace("-", "")
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                root.jdbcTemplate().update(
                        "UPDATE kkumz_api_access_key SET secret_key = ?, status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        newSecret, found.get("id")
                );
                found.put("secretKey", newSecret);
            }
            return found;
        }

        String keyId = "ak-site-" + UUID.randomUUID().toString().substring(0, 8);
        String cleanSlug = siteSlug.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
        if (cleanSlug.length() > 10) {
            cleanSlug = cleanSlug.substring(0, 10);
        }
        String accessKey = "ak_site_" + cleanSlug + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String secretKey = "sk_site_" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        List<String> scopes = List.of(
                "kkumz:site:read",
                "kkumz:site:deploy",
                "kkumz:assets:read",
                "shop:product:read",
                "shop:order:write",
                "cms:content:read",
                "cms:form:submit"
        );
        String keyName = siteName + " " + keyMarker + " Git 개발자 SDK 런타임 키";
        String desc = "5단계 WebIDE 및 외부 Git 클론 환경에서 /s/" + siteSlug + " 사이트 실데이터(자산·상품·예약·AI컨시어지)를 호출하기 위한 전용 키";

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_api_access_key (
                    id, key_name, direction, service_code, base_url, access_key, secret_key,
                    scopes_json, rate_limit_rpm, status, call_count, expires_at, description, created_by, created_at, updated_at
                ) VALUES (?, ?, 'INBOUND', 'SITE_GIT_SDK', ?, ?, ?, ?::jsonb, 1200, 'ACTIVE', 0, CURRENT_TIMESTAMP + INTERVAL '365 days', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                keyId, keyName, "/s/" + siteSlug, accessKey, secretKey, toJson(scopes), desc, actor
        );

        Map<String, Object> created = new LinkedHashMap<>();
        created.put("id", keyId);
        created.put("accessKey", accessKey);
        created.put("secretKey", secretKey);
        return created;
    }

    private List<Map<String, Object>> buildRunnableGitProjectFiles(
            Map<String, Object> site,
            List<Map<String, Object>> existingFiles,
            String repoOwner,
            String repoName,
            String defaultBranch,
            String cloneSshUrl,
            String siteAccessKey,
            String siteSecretKey
    ) {
        String siteSlug = String.valueOf(site.get("siteSlug"));
        String siteName = String.valueOf(site.get("siteName"));
        String brandSlogan = String.valueOf(site.getOrDefault("brandSlogan", ""));

        Map<String, Map<String, Object>> byPath = new LinkedHashMap<>();
        for (Map<String, Object> f : existingFiles) {
            String p = String.valueOf(f.getOrDefault("path", "")).trim();
            if (!p.isEmpty()) {
                byPath.put(p, new LinkedHashMap<>(f));
            }
        }

        // 핵심 3대 파일(src/App.vue, src/theme.json, src/layout.json) 우선 배치 후 실행용 스캐폴딩 파일 구성
        if (!byPath.containsKey("package.json")) {
            String pkgJson = """
                    {
                      "name": "@kkumz-sites/%s",
                      "version": "1.0.0",
                      "private": true,
                      "description": "%s — Kkumz AI Studio Custom Site Bundle",
                      "type": "module",
                      "scripts": {
                        "dev": "vite --port 5173",
                        "build": "vite build",
                        "preview": "vite preview --port 4173"
                      },
                      "dependencies": {
                        "vue": "^3.5.13"
                      },
                      "devDependencies": {
                        "@vitejs/plugin-vue": "^5.2.1",
                        "typescript": "^5.7.3",
                        "vite": "^6.0.7"
                      }
                    }
                    """.formatted(siteSlug, escapeJsonString(siteName));
            byPath.put("package.json", new LinkedHashMap<>(Map.of(
                    "path", "package.json",
                    "language", "json",
                    "content", pkgJson.trim()
            )));
        }

        if (!byPath.containsKey("vite.config.ts")) {
            String viteConfig = """
                    import { defineConfig, loadEnv } from 'vite';
                    import vue from '@vitejs/plugin-vue';

                    export default defineConfig(({ mode }) => {
                      const env = loadEnv(mode, process.cwd(), '');
                      const apiTarget = env.VITE_KKUMZ_API_TARGET || '%s';
                      const siteAccessKey = env.VITE_KKUMZ_SITE_ACCESS_KEY || '%s';

                      return {
                        plugins: [vue()],
                        server: {
                          host: '0.0.0.0',
                          port: 5173,
                          proxy: {
                            '/api/v1/kkumz': {
                              target: apiTarget,
                              changeOrigin: true,
                              headers: {
                                'X-Kkumz-Site-Key': siteAccessKey,
                              },
                            },
                          },
                        },
                      };
                    });
                    """.formatted(root.resolveKkumzPublicBaseUrl(), siteAccessKey);
            byPath.put("vite.config.ts", new LinkedHashMap<>(Map.of(
                    "path", "vite.config.ts",
                    "language", "typescript",
                    "content", viteConfig.trim()
            )));
        }

        if (!byPath.containsKey("index.html")) {
            String indexHtml = """
                    <!DOCTYPE html>
                    <html lang="ko">
                      <head>
                        <meta charset="UTF-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                        <title>%s — %s</title>
                      </head>
                      <body>
                        <div id="app"></div>
                        <script type="module" src="/src/main.ts"></script>
                      </body>
                    </html>
                    """.formatted(escapeXml(siteName), escapeXml(brandSlogan));
            byPath.put("index.html", new LinkedHashMap<>(Map.of(
                    "path", "index.html",
                    "language", "html",
                    "content", indexHtml.trim()
            )));
        }

        if (!byPath.containsKey("src/main.ts")) {
            String mainTs = """
                    import { createApp, h, reactive } from 'vue';
                    import App from './App.vue';
                    import localTheme from './theme.json';
                    import localLayout from './layout.json';

                    const state = reactive({
                      tokens: (localTheme as any).designTokens || localTheme || {},
                      layout: (localLayout as any).layoutSchema || localLayout || {},
                      runtime: null as Record<string, any> | null,
                    });

                    async function bootstrap() {
                      try {
                        const res = await fetch('/api/v1/kkumz/sites/%s/runtime', {
                          headers: {
                            'X-Kkumz-Site-Key': import.meta.env.VITE_KKUMZ_SITE_ACCESS_KEY || '',
                          },
                        });
                        if (res.ok) {
                          const json = await res.json();
                          state.runtime = json?.result || json?.data || null;
                        }
                      } catch (err) {
                        console.warn('[Kkumz SDK] 로컬 정적 스키마(theme.json / layout.json)로 렌더링합니다.', err);
                      }

                      createApp({
                        render: () => h(App, { tokens: state.tokens, layout: state.layout, runtime: state.runtime }),
                      }).mount('#app');
                    }

                    bootstrap();
                    """.formatted(siteSlug);
            byPath.put("src/main.ts", new LinkedHashMap<>(Map.of(
                    "path", "src/main.ts",
                    "language", "typescript",
                    "content", mainTs.trim()
            )));
        }

        byPath.put(".env.example", new LinkedHashMap<>(Map.of(
                "path", ".env.example",
                "language", "properties",
                "content", buildDotEnvExampleContent(siteSlug, siteAccessKey, siteSecretKey)
        )));

        String readmeMd = """
                # %s (`/s/%s`)

                > %s
                > Kkumz 5단계 WebIDE 및 Git 개발자 워크스페이스에서 프로비저닝된 **Vue 3 + Vite** 공식 소스 리포지토리입니다.

                ## 1. 발급된 전용 키로 Git Clone 실행

                발급받은 `%s-deploy-key.pem` 파일을 `~/.ssh/` 경로에 저장한 뒤 아래 명령어로 클론하세요.

                ```bash
                chmod 600 ~/.ssh/%s-deploy-key.pem
                GIT_SSH_COMMAND="ssh -i ~/.ssh/%s-deploy-key.pem -o IdentitiesOnly=yes" git clone -b %s %s
                cd %s
                ```

                ## 2. 로컬 개발 서버 실행 (Kkumz 실데이터 자동 연동)

                ```bash
                cp .env.example .env.local
                pnpm install
                pnpm dev
                ```

                - 로컬 실행 시 `.env.local`에 설정된 사이트 전용 런타임 키(`%s`)를 통해 Kkumz 백엔드의 업로드 사진·문서(`/api/v1/kkumz/assets/files/...`), SyncShop 상품, SyncCMS 매거진·예약폼 API가 실시간 연동됩니다.
                """.formatted(
                siteName, siteSlug, brandSlogan,
                siteSlug, siteSlug, siteSlug, defaultBranch, cloneSshUrl, repoName,
                siteAccessKey
        );
        byPath.put("README.md", new LinkedHashMap<>(Map.of(
                "path", "README.md",
                "language", "markdown",
                "content", readmeMd.trim()
        )));

        return new ArrayList<>(byPath.values());
    }

    private String resolveConfiguredGitHubToken() {
        String envToken = System.getenv("GITHUB_PAT_TOKEN");
        if (envToken != null && !envToken.isBlank()) {
            return envToken.trim();
        }
        try {
            List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                    SELECT secret_key
                    FROM kkumz_api_access_key
                    WHERE direction = 'OUTBOUND' AND service_code = 'GITHUB' AND status = 'ACTIVE'
                    ORDER BY updated_at DESC
                    LIMIT 1
                    """);
            if (!rows.isEmpty() && rows.get(0).get("secret_key") != null) {
                String token = String.valueOf(rows.get(0).get("secret_key")).trim();
                if (!token.isBlank() && !token.startsWith("sk_")) {
                    return token;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Map<String, Object> syncWithGitHubRemoteApi(
            String patToken,
            String requestedOwner,
            String repoName,
            String branch,
            boolean isPrivate,
            String description,
            String sshKeyTitle,
            String sshPublicKey,
            List<Map<String, Object>> files,
            String commitMessage
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            String targetOwner = (requestedOwner == null || requestedOwner.isBlank())
                    ? "kkumz-sites"
                    : requestedOwner.trim();

            // 1. 토큰 소유자 또는 Organization 확인
            HttpRequest userReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user"))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> userRes = client.send(userReq, HttpResponse.BodyHandlers.ofString());
            if (userRes.statusCode() != 200) {
                result.put("synced", false);
                result.put("message", "GitHub 토큰 인증 실패(HTTP " + userRes.statusCode() + ") — Kkumz Managed Git 저장소 모드로 프로비저닝되었습니다.");
                return result;
            }
            JsonNode userNode = root.objectMapper().readTree(userRes.body());
            String loginUser = userNode.path("login").asText("");

            // 2. 리포지토리 존재 확인 또는 kkumz-sites Organization 하위에 신규 생성
            HttpRequest getRepoReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + targetOwner + "/" + repoName))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> getRepoRes = client.send(getRepoReq, HttpResponse.BodyHandlers.ofString());
            JsonNode repoNode;
            if (getRepoRes.statusCode() == 404) {
                String createUri = targetOwner.equalsIgnoreCase(loginUser)
                        ? "https://api.github.com/user/repos"
                        : "https://api.github.com/orgs/" + targetOwner + "/repos";
                Map<String, Object> createPayload = Map.of(
                        "name", repoName,
                        "description", description,
                        "private", isPrivate,
                        "auto_init", true
                );
                HttpRequest createReq = HttpRequest.newBuilder()
                        .uri(URI.create(createUri))
                        .header("Authorization", "Bearer " + patToken)
                        .header("Accept", "application/vnd.github+json")
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Kkumz-Studio-Git-Agent")
                        .POST(HttpRequest.BodyPublishers.ofString(toJson(createPayload)))
                        .build();
                HttpResponse<String> createRes = client.send(createReq, HttpResponse.BodyHandlers.ofString());
                if (createRes.statusCode() != 201 && createRes.statusCode() != 200) {
                    result.put("synced", false);
                    result.put("message", "GitHub(" + targetOwner + ") 리포지토리 생성 응답(HTTP " + createRes.statusCode() + ": " + createRes.body() + ") — 로컬 Managed Git 모드로 저장되었습니다.");
                    return result;
                }
                repoNode = root.objectMapper().readTree(createRes.body());
                // GitHub가 auto_init 초기 커밋 생성을 완료하도록 잠깐 대기
                Thread.sleep(600L);
            } else if (getRepoRes.statusCode() == 200) {
                repoNode = root.objectMapper().readTree(getRepoRes.body());
            } else {
                result.put("synced", false);
                result.put("message", "GitHub 리포지토리 조회 응답(HTTP " + getRepoRes.statusCode() + ") — 로컬 Managed Git 모드로 저장되었습니다.");
                return result;
            }

            // 3. SSH Deploy Key 교체 및 등록 (Write 권한)
            replaceGitHubDeployKey(patToken, targetOwner, repoName, sshKeyTitle, sshPublicKey);

            // 4. Git Data Tree API로 전체 프로젝트 파일 단일 원자적 커밋 푸시 (실패 시 Contents API 폴백)
            String remoteCommitSha = pushFilesViaGitDataTreeApi(client, patToken, targetOwner, repoName, branch, files, commitMessage);
            if (remoteCommitSha == null) {
                for (Map<String, Object> f : files) {
                    String path = String.valueOf(f.getOrDefault("path", "")).replaceAll("^/+", "");
                    if (path.isBlank()) continue;
                    String contentB64 = Base64.getEncoder().encodeToString(
                            String.valueOf(f.getOrDefault("content", "")).getBytes(StandardCharsets.UTF_8)
                    );
                    String contentUrl = "https://api.github.com/repos/" + targetOwner + "/" + repoName + "/contents/" + path;
                    HttpRequest checkFileReq = HttpRequest.newBuilder()
                            .uri(URI.create(contentUrl + "?ref=" + branch))
                            .header("Authorization", "Bearer " + patToken)
                            .header("Accept", "application/vnd.github+json")
                            .header("User-Agent", "Kkumz-Studio-Git-Agent")
                            .GET()
                            .build();
                    HttpResponse<String> checkFileRes = client.send(checkFileReq, HttpResponse.BodyHandlers.ofString());
                    Map<String, Object> putPayload = new LinkedHashMap<>();
                    putPayload.put("message", commitMessage);
                    putPayload.put("content", contentB64);
                    putPayload.put("branch", branch);
                    if (checkFileRes.statusCode() == 200) {
                        JsonNode existingFile = root.objectMapper().readTree(checkFileRes.body());
                        if (existingFile.hasNonNull("sha")) {
                            putPayload.put("sha", existingFile.get("sha").asText());
                        }
                    }
                    HttpRequest putFileReq = HttpRequest.newBuilder()
                            .uri(URI.create(contentUrl))
                            .header("Authorization", "Bearer " + patToken)
                            .header("Accept", "application/vnd.github+json")
                            .header("Content-Type", "application/json")
                            .header("User-Agent", "Kkumz-Studio-Git-Agent")
                            .PUT(HttpRequest.BodyPublishers.ofString(toJson(putPayload)))
                            .build();
                    HttpResponse<String> putFileRes = client.send(putFileReq, HttpResponse.BodyHandlers.ofString());
                    if (putFileRes.statusCode() == 200 || putFileRes.statusCode() == 201) {
                        JsonNode commitNode = root.objectMapper().readTree(putFileRes.body()).path("commit");
                        if (commitNode.hasNonNull("sha")) {
                            remoteCommitSha = commitNode.get("sha").asText();
                        }
                    }
                }
            }

            result.put("synced", true);
            result.put("owner", targetOwner);
            if (remoteCommitSha != null && !remoteCommitSha.isBlank()) {
                result.put("commitSha", remoteCommitSha);
            }
            result.put("htmlUrl", repoNode.path("html_url").asText("https://github.com/" + targetOwner + "/" + repoName));
            result.put("sshUrl", repoNode.path("ssh_url").asText("git@github.com:" + targetOwner + "/" + repoName + ".git"));
            result.put("cloneUrl", repoNode.path("clone_url").asText("https://github.com/" + targetOwner + "/" + repoName + ".git"));
            result.put("message", "GitHub 원격 저장소(https://github.com/" + targetOwner + "/" + repoName + ") 생성, 전용 SSH Deploy Key 등록 및 소스 푸시가 완료되었습니다.");
            return result;
        } catch (Exception e) {
            log.warn("GitHub 원격 API 연동 중 예외 발생 (Managed Git 모드로 폴백): {}", e.getMessage());
            result.put("synced", false);
            result.put("message", "Kkumz Managed Git 저장소 및 전용 배포 키 프로비저닝 완료 (오프라인/로컬 번들 즉시 사용 가능)");
            return result;
        }
    }

    private void replaceGitHubDeployKey(
            String patToken,
            String owner,
            String repoName,
            String sshKeyTitle,
            String sshPublicKey
    ) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            String keysUrl = "https://api.github.com/repos/" + owner + "/" + repoName + "/keys";
            HttpRequest listReq = HttpRequest.newBuilder()
                    .uri(URI.create(keysUrl))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> listRes = client.send(listReq, HttpResponse.BodyHandlers.ofString());
            if (listRes.statusCode() == 200) {
                JsonNode arr = root.objectMapper().readTree(listRes.body());
                if (arr.isArray()) {
                    for (JsonNode k : arr) {
                        String title = k.path("title").asText("");
                        long keyId = k.path("id").asLong(0L);
                        if (keyId > 0 && title.startsWith("kkumz-deploy-key@")) {
                            HttpRequest delReq = HttpRequest.newBuilder()
                                    .uri(URI.create(keysUrl + "/" + keyId))
                                    .header("Authorization", "Bearer " + patToken)
                                    .header("Accept", "application/vnd.github+json")
                                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                                    .DELETE()
                                    .build();
                            client.send(delReq, HttpResponse.BodyHandlers.ofString());
                        }
                    }
                }
            }
            Map<String, Object> keyPayload = Map.of(
                    "title", sshKeyTitle,
                    "key", sshPublicKey,
                    "read_only", false
            );
            HttpRequest keyReq = HttpRequest.newBuilder()
                    .uri(URI.create(keysUrl))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(keyPayload)))
                    .build();
            client.send(keyReq, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.warn("GitHub Deploy Key 등록 경고 ({}/{}): {}", owner, repoName, e.getMessage());
        }
    }

    private String pushFilesViaGitDataTreeApi(
            HttpClient client,
            String patToken,
            String owner,
            String repoName,
            String branch,
            List<Map<String, Object>> files,
            String commitMessage
    ) {
        try {
            String refUrl = "https://api.github.com/repos/" + owner + "/" + repoName + "/git/ref/heads/" + branch;
            HttpRequest refReq = HttpRequest.newBuilder()
                    .uri(URI.create(refUrl))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .GET()
                    .build();
            HttpResponse<String> refRes = client.send(refReq, HttpResponse.BodyHandlers.ofString());
            if (refRes.statusCode() != 200) {
                return null;
            }
            String parentCommitSha = root.objectMapper().readTree(refRes.body()).path("object").path("sha").asText("");
            if (parentCommitSha.isBlank()) {
                return null;
            }

            List<Map<String, Object>> treeItems = new ArrayList<>();
            for (Map<String, Object> f : files) {
                String path = String.valueOf(f.getOrDefault("path", "")).replaceAll("^/+", "");
                if (path.isBlank()) continue;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("path", path);
                item.put("mode", "100644");
                item.put("type", "blob");
                item.put("content", String.valueOf(f.getOrDefault("content", "")));
                treeItems.add(item);
            }

            Map<String, Object> treePayload = new LinkedHashMap<>();
            treePayload.put("base_tree", parentCommitSha);
            treePayload.put("tree", treeItems);

            HttpRequest treeReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repoName + "/git/trees"))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(treePayload)))
                    .build();
            HttpResponse<String> treeRes = client.send(treeReq, HttpResponse.BodyHandlers.ofString());
            if (treeRes.statusCode() != 201 && treeRes.statusCode() != 200) {
                return null;
            }
            String newTreeSha = root.objectMapper().readTree(treeRes.body()).path("sha").asText("");
            if (newTreeSha.isBlank()) {
                return null;
            }

            Map<String, Object> commitPayload = new LinkedHashMap<>();
            commitPayload.put("message", commitMessage);
            commitPayload.put("tree", newTreeSha);
            commitPayload.put("parents", List.of(parentCommitSha));

            HttpRequest commitReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repoName + "/git/commits"))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(commitPayload)))
                    .build();
            HttpResponse<String> commitRes = client.send(commitReq, HttpResponse.BodyHandlers.ofString());
            if (commitRes.statusCode() != 201 && commitRes.statusCode() != 200) {
                return null;
            }
            String newCommitSha = root.objectMapper().readTree(commitRes.body()).path("sha").asText("");
            if (newCommitSha.isBlank()) {
                return null;
            }

            Map<String, Object> updateRefPayload = Map.of("sha", newCommitSha, "force", true);
            HttpRequest updateRefReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repoName + "/git/refs/heads/" + branch))
                    .header("Authorization", "Bearer " + patToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kkumz-Studio-Git-Agent")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(toJson(updateRefPayload)))
                    .build();
            HttpResponse<String> updateRefRes = client.send(updateRefReq, HttpResponse.BodyHandlers.ofString());
            if (updateRefRes.statusCode() == 200) {
                return newCommitSha;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void writeGitRepoFilesToWorkspace(
            String siteSlug,
            String repoOwner,
            String repoName,
            String branch,
            String commitSha,
            String commitMessage,
            List<Map<String, Object>> files,
            String sshPublicKey
    ) {
        try {
            Path repoDir = root.assets().resolveUploadDir().resolve("git-repos").resolve(siteSlug);
            Files.createDirectories(repoDir);
            for (Map<String, Object> f : files) {
                String relPath = String.valueOf(f.getOrDefault("path", "")).replaceAll("^/+", "");
                if (relPath.isBlank() || relPath.contains("..")) continue;
                Path target = repoDir.resolve(relPath);
                if (target.getParent() != null) {
                    Files.createDirectories(target.getParent());
                }
                Files.writeString(target, String.valueOf(f.getOrDefault("content", "")), StandardCharsets.UTF_8);
            }
            Path gitMetaDir = repoDir.resolve(".git");
            Files.createDirectories(gitMetaDir.resolve("refs").resolve("heads"));
            Files.writeString(gitMetaDir.resolve("HEAD"), "ref: refs/heads/" + branch + "\n", StandardCharsets.UTF_8);
            Files.writeString(gitMetaDir.resolve("refs").resolve("heads").resolve(branch), commitSha + "\n", StandardCharsets.UTF_8);
            Files.writeString(gitMetaDir.resolve("COMMIT_EDITMSG"), commitMessage + "\n", StandardCharsets.UTF_8);
            Files.writeString(gitMetaDir.resolve("authorized_deploy_key.pub"), sshPublicKey + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("로컬 Git 작업 디렉터리 기록 경고 (siteSlug={}): {}", siteSlug, e.getMessage());
        }
    }
}
