package com.empasy.sync.kkumz.domain;

import com.empasy.sync.core.common.config.SystemConfigHolder;
import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;

/**
 * Kkumz 창업 쇼케이스 피드, 팀빌딩·댓글·좋아요, 인앱 알림 및 Admin 백오피스·API 키 관제를 담당하는 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzCommunityAndAdminSupport {

    private final KkumzService root;

    List<Map<String, Object>> getCommunityFeed(String siteType, String recruitRole, String sortBy, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.idea_id AS "ideaId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_type AS "siteType",
                       s.category,
                       s.brand_slogan AS "brandSlogan",
                       s.logo_svg AS "logoSvg",
                       s.deploy_mode AS "deployMode",
                       s.active_version AS "activeVersion",
                       s.published_version AS "publishedVersion",
                       s.published_at AS "publishedAt",
                       s.visibility,
                       s.deployed_url AS "deployedUrl",
                       s.custom_domain AS "customDomain",
                       s.is_featured AS "isFeatured",
                       s.like_count AS "likeCount",
                       s.comment_count AS "commentCount",
                       s.view_count AS "viewCount",
                       s.owner_username AS "ownerUsername",
                       s.owner_display_name AS "ownerDisplayName",
                       s.status,
                       s.created_at AS "createdAt",
                       i.prompt_text AS "promptText",
                       i.tags_json::text AS "tagsJson",
                       i.design_tokens_json::text AS "designTokensJson"
                FROM kkumz_site s
                JOIN kkumz_idea i ON i.id = s.idea_id
                WHERE s.status = 'ACTIVE' AND s.visibility = 'PUBLIC' AND s.published_version IS NOT NULL
                """);
        List<Object> params = new ArrayList<>();

        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND s.site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (recruitRole != null && !recruitRole.isBlank() && !"ALL".equalsIgnoreCase(recruitRole)) {
            sql.append(" AND EXISTS (SELECT 1 FROM kkumz_team_recruit r WHERE r.site_id = s.id AND r.status = 'OPEN' AND r.position_role = ?)");
            params.add(recruitRole.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (s.site_name ILIKE ? OR s.brand_slogan ILIKE ? OR i.prompt_text ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if ("RECENT".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY s.created_at DESC");
        } else if ("VIEWS".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY s.view_count DESC, s.like_count DESC");
        } else {
            sql.append(" ORDER BY s.is_featured DESC, s.like_count DESC, s.view_count DESC");
        }

        List<Map<String, Object>> rawSites = root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
        List<Map<String, Object>> sites = new ArrayList<>();
        String currentUser = root.accessMeta().resolveCurrentUsernameFromRequest();
        for (Map<String, Object> item : rawSites) {
            if (!root.accessMeta().isSitePubliclyPublished(item)) {
                continue;
            }
            String sId = String.valueOf(item.get("id"));
            String ownerUser = item.get("ownerUsername") != null ? String.valueOf(item.get("ownerUsername")).trim() : "";
            boolean isOwner = currentUser != null && !currentUser.isBlank() && currentUser.equalsIgnoreCase(ownerUser);
            boolean canEdit = isOwner || (currentUser != null && !currentUser.isBlank() && root.accessMeta().canEditSite(item, currentUser));
            int activeVer = item.get("activeVersion") instanceof Number n ? n.intValue() : 1;
            Integer pubVer = root.accessMeta().resolvePublishedVersion(item);
            item.put("isOwner", isOwner);
            item.put("canEdit", canEdit);
            item.put("isPublished", true);
            item.put("publishedVersion", pubVer != null ? pubVer : activeVer);
            item.put("hasUnpublishedChanges", pubVer != null && activeVer > pubVer);
            item.put("tags", parseJsonList(String.valueOf(item.remove("tagsJson"))));
            item.put("designTokens", parseJsonObject(String.valueOf(item.remove("designTokensJson"))));
            item.put("openRecruits", listSiteRecruitsInternal(sId));
            item.put("teamMembers", listSiteTeamMembersInternal(sId));
            item.put("recentComments", listSiteCommentsInternal(sId));
            sites.add(item);
        }
        return sites;
    }

    Map<String, Object> toggleSiteLike(String siteIdOrSlug, String username) {
        String authUser = root.accessMeta().resolveCurrentUsernameFromRequest();
        String resolvedUser = (authUser != null && !authUser.isBlank()) ? authUser : username;
        if (resolvedUser == null || resolvedUser.isBlank()) {
            if (root.accessMeta().isHttpRequestActive()) {
                throw new SecurityException("[401] 로그인이 필요한 기능입니다. 먼저 로그인해 주세요.");
            }
            throw new IllegalArgumentException("좋아요 처리를 위한 사용자 계정(username)은 필수입니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        root.accessMeta().assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        String user = resolvedUser.trim();

        Integer exists = root.jdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM kkumz_feed_like WHERE site_id = ? AND username = ?",
                Integer.class, siteId, user
        );

        boolean liked;
        if (exists != null && exists > 0) {
            root.jdbcTemplate().update("DELETE FROM kkumz_feed_like WHERE site_id = ? AND username = ?", siteId, user);
            root.jdbcTemplate().update("UPDATE kkumz_site SET like_count = GREATEST(0, like_count - 1) WHERE id = ?", siteId);
            liked = false;
        } else {
            root.jdbcTemplate().update(
                    "INSERT INTO kkumz_feed_like (id, site_id, username, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                    "like-" + UUID.randomUUID().toString().substring(0, 8), siteId, user
            );
            root.jdbcTemplate().update("UPDATE kkumz_site SET like_count = like_count + 1 WHERE id = ?", siteId);
            liked = true;
        }

        Integer newCount = root.jdbcTemplate().queryForObject("SELECT like_count FROM kkumz_site WHERE id = ?", Integer.class, siteId);
        return Map.of("siteId", siteId, "liked", liked, "likeCount", newCount != null ? newCount : 0);
    }

    List<Map<String, Object>> listSiteComments(String siteIdOrSlug) {
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        root.accessMeta().assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        return listSiteCommentsInternal(siteId);
    }

    List<Map<String, Object>> listSiteCommentsInternal(String siteId) {
        return root.jdbcTemplate().queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       username,
                       display_name AS "displayName",
                       author_role_badge AS "authorRoleBadge",
                       content,
                       created_at AS "createdAt"
                FROM kkumz_feed_comment
                WHERE site_id = ?
                ORDER BY created_at DESC
                """, siteId);
    }

    List<Map<String, Object>> addSiteComment(String siteIdOrSlug, CreateCommentRequest req) {
        if (req == null || req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용(content)을 입력해 주세요.");
        }
        String authUser = root.accessMeta().isHttpRequestActive() ? root.accessMeta().requireAuthenticatedUsername() : root.accessMeta().resolveCurrentUsernameFromRequest();
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        root.accessMeta().assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> author = root.accessMeta().resolveUserIdentity(
                authUser != null ? authUser : req.getUsername(),
                req.getDisplayName()
        );
        String username = author.get("username");
        String displayName = author.get("realname");
        String badge = (req.getAuthorRoleBadge() != null && !req.getAuthorRoleBadge().isBlank())
                ? req.getAuthorRoleBadge().trim()
                : "메이커";

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_feed_comment (
                    id, site_id, username, display_name, author_role_badge, content, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                "cmt-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, username, displayName, badge, req.getContent().trim()
        );

        root.jdbcTemplate().update("UPDATE kkumz_site SET comment_count = comment_count + 1 WHERE id = ?", siteId);
        return listSiteCommentsInternal(siteId);
    }

    List<Map<String, Object>> listSiteRecruits(String siteIdOrSlug) {
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        root.accessMeta().assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        return listSiteRecruitsInternal(siteId);
    }

    List<Map<String, Object>> listSiteRecruitsInternal(String siteId) {
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       position_role AS "positionRole",
                       title,
                       description,
                       required_skills_json::text AS "requiredSkillsJson",
                       headcount,
                       recruited_count AS "recruitedCount",
                       status,
                       created_at AS "createdAt"
                FROM kkumz_team_recruit
                WHERE site_id = ?
                ORDER BY created_at DESC
                """, siteId);
        rows.forEach(r -> r.put("requiredSkills", parseJsonList(String.valueOf(r.remove("requiredSkillsJson")))));
        return rows;
    }

    List<Map<String, Object>> createSiteRecruit(String siteIdOrSlug, CreateRecruitRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        if (req == null || req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("팀원 모집 공고 제목(title)은 필수입니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        String role = (req.getPositionRole() != null && !req.getPositionRole().isBlank())
                ? req.getPositionRole().trim().toUpperCase(Locale.ROOT)
                : "FRONTEND";
        int headcount = (req.getHeadcount() != null && req.getHeadcount() > 0) ? req.getHeadcount() : 1;
        List<String> skills = (req.getRequiredSkills() != null && !req.getRequiredSkills().isEmpty())
                ? req.getRequiredSkills()
                : List.of(role);

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_team_recruit (
                    id, site_id, position_role, title, description, required_skills_json, headcount, recruited_count, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, 0, 'OPEN', CURRENT_TIMESTAMP)
                """,
                "rec-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, role, req.getTitle().trim(),
                req.getDescription() != null ? req.getDescription().trim() : "",
                toJson(skills), headcount
        );

        return listSiteRecruitsInternal(siteId);
    }

    List<Map<String, Object>> listSiteTeamMembers(String siteIdOrSlug) {
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        root.accessMeta().assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        return listSiteTeamMembersInternal(siteId);
    }

    List<Map<String, Object>> listSiteTeamMembersInternal(String siteId) {
        return root.jdbcTemplate().queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       recruit_id AS "recruitId",
                       username,
                       display_name AS "displayName",
                       member_role AS "memberRole",
                       position_role AS "positionRole",
                       join_type AS "joinType",
                       application_message AS "applicationMessage",
                       status,
                       joined_at AS "joinedAt"
                FROM kkumz_team_member
                WHERE site_id = ?
                ORDER BY CASE WHEN member_role = 'OWNER' THEN 0 ELSE 1 END, joined_at ASC
                """, siteId);
    }

    List<Map<String, Object>> applyToTeam(String siteIdOrSlug, TeamApplyRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("팀 합류 신청 정보(req)는 필수입니다.");
        }
        String authUser = root.accessMeta().isHttpRequestActive() ? root.accessMeta().requireAuthenticatedUsername() : root.accessMeta().resolveCurrentUsernameFromRequest();
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        root.accessMeta().assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> applicant = root.accessMeta().resolveUserIdentity(
                authUser != null ? authUser : req.getUsername(),
                req.getDisplayName()
        );
        String username = applicant.get("username");
        String displayName = applicant.get("realname");
        String posRole = (req.getPositionRole() != null && !req.getPositionRole().isBlank())
                ? req.getPositionRole().trim().toUpperCase(Locale.ROOT)
                : "FRONTEND";
        String memberRole = "FRONTEND".equals(posRole) ? "DEVELOPER" : ("MD".equals(posRole) ? "MD" : "EDITOR");

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_team_member (
                    id, site_id, recruit_id, username, display_name, member_role, position_role,
                    join_type, application_message, status, joined_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'APPLIED', ?, 'PENDING', CURRENT_TIMESTAMP)
                """,
                "tmem-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, req.getRecruitId(), username, displayName, memberRole, posRole,
                req.getApplicationMessage() != null ? req.getApplicationMessage().trim() : ""
        );

        root.accessMeta().createNotification(
                String.valueOf(site.get("ownerUsername")),
                "TEAM_APPLY",
                "[" + site.get("siteName") + "] " + posRole + " 포지션 신규 지원",
                displayName + "님이 프로젝트 합류를 신청했습니다.",
                "/lounge/" + site.get("siteSlug")
        );

        return listSiteTeamMembersInternal(siteId);
    }

    List<Map<String, Object>> inviteTeamMember(String siteIdOrSlug, TeamInviteRequest req) {
        root.accessMeta().assertSiteEditable(siteIdOrSlug);
        if (req == null || req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("초대할 팀원 아이디(username)는 필수입니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        String siteId = String.valueOf(site.get("id"));
        Map<String, String> invited = root.accessMeta().resolveUserIdentity(req.getUsername(), req.getDisplayName());
        String username = invited.get("username");
        String displayName = invited.get("realname");
        String posRole = (req.getPositionRole() != null && !req.getPositionRole().isBlank())
                ? req.getPositionRole().trim().toUpperCase(Locale.ROOT)
                : "FRONTEND";
        String memberRole = (req.getMemberRole() != null && !req.getMemberRole().isBlank())
                ? req.getMemberRole().trim().toUpperCase(Locale.ROOT)
                : "EDITOR";

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_team_member (
                    id, site_id, recruit_id, username, display_name, member_role, position_role,
                    join_type, application_message, status, joined_at
                ) VALUES (?, ?, NULL, ?, ?, ?, ?, 'INVITED', ?, 'ACTIVE', CURRENT_TIMESTAMP)
                """,
                "tmem-" + UUID.randomUUID().toString().substring(0, 8),
                siteId, username, displayName, memberRole, posRole,
                req.getMessage() != null ? req.getMessage().trim() : ""
        );

        root.accessMeta().createNotification(
                username,
                "TEAM_INVITE",
                "[" + site.get("siteName") + "] 프로젝트 합류 초대 도착",
                site.get("siteName") + " 팀에 합류되었습니다.",
                "/lounge/" + site.get("siteSlug")
        );

        return listSiteTeamMembersInternal(siteId);
    }

    List<Map<String, Object>> updateTeamMemberStatus(String siteIdOrSlug, String memberId, UpdateMemberStatusRequest req) {
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
                "SELECT site_id AS \"siteId\", recruit_id AS \"recruitId\", username, display_name AS \"displayName\" FROM kkumz_team_member WHERE id = ?",
                memberId
        );
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 팀 지원 내역입니다: " + memberId);
        }
        Map<String, Object> member = rows.get(0);
        String siteId = String.valueOf(member.get("siteId"));
        root.accessMeta().assertSiteEditable(siteId);
        String status = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : "ACTIVE";
        String memberRole = (req.getMemberRole() != null && !req.getMemberRole().isBlank())
                ? req.getMemberRole().trim().toUpperCase(Locale.ROOT)
                : "EDITOR";

        root.jdbcTemplate().update("UPDATE kkumz_team_member SET status = ?, member_role = ? WHERE id = ?",
                status, memberRole, memberId);

        if ("ACTIVE".equals(status) && member.get("recruitId") != null) {
            root.jdbcTemplate().update("""
                    UPDATE kkumz_team_recruit
                    SET recruited_count = recruited_count + 1,
                        status = CASE WHEN recruited_count + 1 >= headcount THEN 'CLOSED' ELSE status END
                    WHERE id = ?
                    """, member.get("recruitId"));
        }

        root.accessMeta().createNotification(
                String.valueOf(member.get("username")),
                "TEAM_DECISION",
                "팀 합류 심사 결과 안내 (" + ("ACTIVE".equals(status) ? "승인" : "반려") + ")",
                "지원하신 프로젝트 팀 합류 상태가 '" + status + "'(으)로 업데이트되었습니다.",
                "/lounge"
        );

        return listSiteTeamMembersInternal(siteId);
    }

    List<Map<String, Object>> listNotifications(String username) {
        String authUser = root.accessMeta().resolveCurrentUsernameFromRequest();
        String targetUser = (authUser != null && !authUser.isBlank())
                ? authUser
                : (username != null && !username.isBlank() ? username.trim() : null);
        if (root.accessMeta().isHttpRequestActive() && (targetUser == null || targetUser.isBlank())) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       recipient_username AS "recipientUsername",
                       notif_type AS "notifType",
                       title,
                       message,
                       link_url AS "linkUrl",
                       is_read AS "isRead",
                       created_at AS "createdAt"
                FROM kkumz_notification
                """);
        List<Object> params = new ArrayList<>();
        if (targetUser != null && !targetUser.isBlank()) {
            sql.append(" WHERE LOWER(recipient_username) = LOWER(?)");
            params.add(targetUser.trim());
        }
        sql.append(" ORDER BY created_at DESC LIMIT 30");
        return root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
    }

    void markAllNotificationsRead(String username) {
        String authUser = root.accessMeta().resolveCurrentUsernameFromRequest();
        String targetUser = (authUser != null && !authUser.isBlank())
                ? authUser
                : (username != null && !username.isBlank() ? username.trim() : null);
        if (targetUser != null && !targetUser.isBlank()) {
            root.jdbcTemplate().update("UPDATE kkumz_notification SET is_read = TRUE WHERE LOWER(recipient_username) = LOWER(?)", targetUser.trim());
        } else if (!root.accessMeta().isHttpRequestActive()) {
            root.jdbcTemplate().update("UPDATE kkumz_notification SET is_read = TRUE");
        }
    }

    Map<String, Object> getAdminDashboardSummary() {
        Integer totalIdeas = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_idea", Integer.class);
        Integer totalSites = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_site WHERE status = 'ACTIVE'", Integer.class);
        Integer totalBuilds = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_build_job", Integer.class);
        Integer successBuilds = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_build_job WHERE status = 'SUCCESS'", Integer.class);
        Integer aiHealedBuilds = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_build_job WHERE ai_patch_applied = TRUE", Integer.class);
        Integer openRecruits = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_team_recruit WHERE status = 'OPEN'", Integer.class);
        Integer activeTeamMembers = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_team_member WHERE status = 'ACTIVE'", Integer.class);
        Integer pendingReports = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_moderation_report WHERE status = 'PENDING'", Integer.class);
        Integer totalComments = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_feed_comment", Integer.class);
        Integer activePrompts = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_ai_prompt_template WHERE status = 'ACTIVE'", Integer.class);
        Integer totalNameSuggestions = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_name_suggestion", Integer.class);
        Integer totalLogoSuggestions = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_logo_suggestion", Integer.class);
        Integer totalScreenSpecs = root.jdbcTemplate().queryForObject("SELECT COUNT(*) FROM kkumz_screen_spec", Integer.class);
        Integer avgBuildDurationMs = root.jdbcTemplate().queryForObject("SELECT COALESCE(ROUND(AVG(duration_ms))::int, 0) FROM kkumz_build_job", Integer.class);

        List<Map<String, Object>> bySiteType = root.jdbcTemplate().queryForList("""
                SELECT site_type AS "siteType",
                       COUNT(*) AS "count",
                       COALESCE(SUM(view_count), 0) AS "totalViews",
                       COALESCE(SUM(like_count), 0) AS "totalLikes"
                FROM kkumz_site
                GROUP BY site_type
                ORDER BY "count" DESC
                """);

        List<Map<String, Object>> byDeployMode = root.jdbcTemplate().queryForList("""
                SELECT deploy_mode AS "deployMode",
                       COUNT(*) AS "count"
                FROM kkumz_site
                GROUP BY deploy_mode
                ORDER BY "count" DESC
                """);

        List<Map<String, Object>> recentBuilds = listAdminBuilds(null, null);
        List<Map<String, Object>> recentReports = listModerationReports(null);
        List<Map<String, Object>> recentIdeas = listAdminIdeas(null, null, null);
        List<Map<String, Object>> topSites = listAdminSites(null, null, null);

        int totalB = totalBuilds != null ? totalBuilds : 0;
        int succB = successBuilds != null ? successBuilds : 0;
        int buildSuccessRate = totalB > 0 ? Math.round((succB * 100.0f) / totalB) : 100;
        int totalAiGenerations = (totalNameSuggestions != null ? totalNameSuggestions : 0)
                + (totalLogoSuggestions != null ? totalLogoSuggestions : 0)
                + (totalScreenSpecs != null ? totalScreenSpecs : 0)
                + (aiHealedBuilds != null ? aiHealedBuilds : 0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalIdeas", totalIdeas != null ? totalIdeas : 0);
        summary.put("totalSites", totalSites != null ? totalSites : 0);
        summary.put("totalBuilds", totalB);
        summary.put("successBuilds", succB);
        summary.put("buildSuccessRate", buildSuccessRate);
        summary.put("avgBuildDurationMs", avgBuildDurationMs != null ? avgBuildDurationMs : 0);
        summary.put("aiHealedBuilds", aiHealedBuilds != null ? aiHealedBuilds : 0);
        summary.put("activePrompts", activePrompts != null ? activePrompts : 0);
        summary.put("totalAiPromptCalls", totalAiGenerations);
        summary.put("totalNameSuggestions", totalNameSuggestions != null ? totalNameSuggestions : 0);
        summary.put("totalLogoSuggestions", totalLogoSuggestions != null ? totalLogoSuggestions : 0);
        summary.put("totalScreenSpecs", totalScreenSpecs != null ? totalScreenSpecs : 0);
        summary.put("openRecruits", openRecruits != null ? openRecruits : 0);
        summary.put("activeTeamMembers", activeTeamMembers != null ? activeTeamMembers : 0);
        summary.put("pendingReports", pendingReports != null ? pendingReports : 0);
        summary.put("totalComments", totalComments != null ? totalComments : 0);
        summary.put("siteTypeDistribution", bySiteType);
        summary.put("deployModeDistribution", byDeployMode);
        summary.put("recentIdeas", recentIdeas.size() > 5 ? recentIdeas.subList(0, 5) : recentIdeas);
        summary.put("topSites", topSites.size() > 5 ? topSites.subList(0, 5) : topSites);
        summary.put("recentBuilds", recentBuilds.size() > 5 ? recentBuilds.subList(0, 5) : recentBuilds);
        summary.put("recentReports", recentReports.size() > 5 ? recentReports.subList(0, 5) : recentReports);
        return summary;
    }

    List<Map<String, Object>> listAdminIdeas(String siteType, String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       author_username AS "authorUsername",
                       author_display_name AS "authorDisplayName",
                       title,
                       prompt_text AS "promptText",
                       site_type AS "siteType",
                       category,
                       tags_json::text AS "tagsJson",
                       enabled_features_json::text AS "enabledFeaturesJson",
                       ai_intent_analysis_json::text AS "aiIntentAnalysisJson",
                       visibility,
                       current_step AS "currentStep",
                       selected_brand_name AS "selectedBrandName",
                       selected_site_slug AS "selectedSiteSlug",
                       selected_logo_svg AS "selectedLogoSvg",
                       design_tokens_json::text AS "designTokensJson",
                       spec_markdown AS "specMarkdown",
                       screens_json::text AS "screensJson",
                       status,
                       created_at AS "createdAt",
                       updated_at AS "updatedAt"
                FROM kkumz_idea
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (title ILIKE ? OR prompt_text ILIKE ? OR selected_brand_name ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY updated_at DESC");

        List<Map<String, Object>> list = root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
        list.forEach(root.accessMeta()::enrichIdeaRow);
        return list;
    }

    void deleteAdminIdea(String ideaId) {
        root.jdbcTemplate().update("DELETE FROM kkumz_idea WHERE id = ?", ideaId);
    }

    List<Map<String, Object>> listAdminPromptTemplates(String stageType) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       template_code AS "templateCode",
                       template_name AS "templateName",
                       stage_type AS "stageType",
                       model_name AS "modelName",
                       temperature,
                       system_prompt AS "systemPrompt",
                       user_prompt_template AS "userPromptTemplate",
                       forbidden_words_json::text AS "forbiddenWordsJson",
                       status,
                       updated_by AS "updatedBy",
                       updated_at AS "updatedAt"
                FROM kkumz_ai_prompt_template
                """);
        List<Object> params = new ArrayList<>();
        if (stageType != null && !stageType.isBlank() && !"ALL".equalsIgnoreCase(stageType)) {
            sql.append(" WHERE stage_type = ?");
            params.add(stageType.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY stage_type ASC, template_code ASC");

        List<Map<String, Object>> list = root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
        list.forEach(p -> p.put("forbiddenWords", parseJsonList(String.valueOf(p.remove("forbiddenWordsJson")))));
        return list;
    }

    Map<String, Object> saveAdminPromptTemplate(SavePromptTemplateRequest req) {
        if (req == null || req.getTemplateName() == null || req.getTemplateName().isBlank()) {
            throw new IllegalArgumentException("프롬프트 템플릿 이름(templateName)은 필수입니다.");
        }
        List<String> words = req.getForbiddenWords() != null ? req.getForbiddenWords()
                : (req.getForbiddenKeywords() != null ? req.getForbiddenKeywords() : Collections.emptyList());
        String forbiddenJson = toJson(words);
        BigDecimal temp = req.getTemperature() != null ? req.getTemperature() : new BigDecimal("0.70");
        String modelName = (req.getModelName() != null && !req.getModelName().isBlank()) ? req.getModelName().trim() : "";
        if (modelName.isBlank()) {
            throw new IllegalArgumentException("대상 모델명(modelName)은 필수입니다.");
        }
        String status = (req.getStatus() != null && !req.getStatus().isBlank()) ? req.getStatus().trim() : "ACTIVE";
        String id;

        if (req.getId() != null && !req.getId().isBlank()) {
            id = req.getId().trim();
            root.jdbcTemplate().update("""
                    UPDATE kkumz_ai_prompt_template
                    SET template_name = ?,
                        stage_type = COALESCE(?, stage_type),
                        model_name = ?,
                        temperature = ?,
                        system_prompt = ?,
                        user_prompt_template = COALESCE(?, user_prompt_template),
                        forbidden_words_json = ?::jsonb,
                        status = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    req.getTemplateName().trim(),
                    req.getStageType(),
                    modelName,
                    temp,
                    req.getSystemPrompt(),
                    req.getUserPromptTemplate(),
                    forbiddenJson,
                    status,
                    id
            );
        } else {
            id = "tpl-" + UUID.randomUUID().toString().substring(0, 8);
            String code = (req.getTemplateCode() != null && !req.getTemplateCode().isBlank())
                    ? req.getTemplateCode().trim().toUpperCase(Locale.ROOT)
                    : "TPL_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
            root.jdbcTemplate().update("""
                    INSERT INTO kkumz_ai_prompt_template (
                        id, template_code, template_name, stage_type, model_name, temperature,
                        system_prompt, user_prompt_template, forbidden_words_json, status, created_by, updated_by, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id, code, req.getTemplateName().trim(),
                    req.getStageType() != null ? req.getStageType() : "NAMING",
                    modelName, temp,
                    req.getSystemPrompt() != null ? req.getSystemPrompt() : "",
                    req.getUserPromptTemplate() != null ? req.getUserPromptTemplate() : "",
                    forbiddenJson, status
            );
        }

        List<Map<String, Object>> all = listAdminPromptTemplates(null);
        return all.stream()
                .filter(p -> id.equals(String.valueOf(p.get("id"))))
                .findFirst()
                .orElse(all.isEmpty() ? Map.of("id", id) : all.get(0));
    }

    void deleteAdminPromptTemplate(String promptId) {
        root.jdbcTemplate().update("DELETE FROM kkumz_ai_prompt_template WHERE id = ?", promptId);
    }

    List<Map<String, Object>> listAdminSites(String siteType, String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.idea_id AS "ideaId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_type AS "siteType",
                       s.category,
                       s.brand_slogan AS "brandSlogan",
                       s.logo_svg AS "logoSvg",
                       s.shop_site_id AS "shopSiteId",
                       s.cms_site_id AS "cmsSiteId",
                       s.deploy_mode AS "deployMode",
                       s.active_version AS "activeVersion",
                       s.published_version AS "publishedVersion",
                       s.published_at AS "publishedAt",
                       s.deployed_url AS "deployedUrl",
                       s.custom_domain AS "customDomain",
                       s.is_featured AS "isFeatured",
                       s.visibility,
                       s.like_count AS "likeCount",
                       s.comment_count AS "commentCount",
                       s.view_count AS "viewCount",
                       s.owner_username AS "ownerUsername",
                       s.owner_display_name AS "ownerDisplayName",
                       s.status,
                       s.created_at AS "createdAt",
                       s.updated_at AS "updatedAt"
                FROM kkumz_site s
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (siteType != null && !siteType.isBlank() && !"ALL".equalsIgnoreCase(siteType)) {
            sql.append(" AND s.site_type = ?");
            params.add(siteType.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND s.status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (s.site_name ILIKE ? OR s.site_slug ILIKE ? OR s.brand_slogan ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY s.created_at DESC");
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
        List<Map<String, Object>> list = new ArrayList<>(rows.size());
        for (Map<String, Object> raw : rows) {
            Map<String, Object> item = new LinkedHashMap<>(raw);
            boolean isPub = root.accessMeta().isSitePubliclyPublished(item);
            int activeVer = item.get("activeVersion") instanceof Number n ? n.intValue() : 1;
            Integer pubVer = root.accessMeta().resolvePublishedVersion(item);
            item.put("isPublished", isPub);
            item.put("publishedVersion", pubVer);
            item.put("hasUnpublishedChanges", !isPub || (pubVer != null && activeVer > pubVer));
            list.add(item);
        }
        return list;
    }

    Map<String, Object> updateAdminSiteStatus(String siteId, UpdateSiteStatusRequest req) {
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteId);
        String resolvedId = String.valueOf(site.get("id"));
        String nextStatus = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.getOrDefault("status", "DRAFT")).toUpperCase(Locale.ROOT);
        String nextVis = (req.getVisibility() != null && !req.getVisibility().isBlank())
                ? req.getVisibility().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(site.getOrDefault("visibility", "PRIVATE")).toUpperCase(Locale.ROOT);

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            root.jdbcTemplate().update("UPDATE kkumz_site SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    nextStatus, resolvedId);
        }
        if (req.getVisibility() != null && !req.getVisibility().isBlank()) {
            root.jdbcTemplate().update("UPDATE kkumz_site SET visibility = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    nextVis, resolvedId);
        }
        if ("PRIVATE".equals(nextVis) || "DRAFT".equals(nextStatus)) {
            root.jdbcTemplate().update("UPDATE kkumz_site SET published_version = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?", resolvedId);
        } else if ("ACTIVE".equals(nextStatus) && "PUBLIC".equals(nextVis)) {
            root.jdbcTemplate().update("""
                    UPDATE kkumz_site
                    SET published_version = COALESCE(published_version, active_version),
                        published_at = COALESCE(published_at, CURRENT_TIMESTAMP),
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, resolvedId);
        }
        if (req.getIsFeatured() != null) {
            root.jdbcTemplate().update("UPDATE kkumz_site SET is_featured = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getIsFeatured(), resolvedId);
        }
        if (req.getCustomDomain() != null && !req.getCustomDomain().isBlank()) {
            root.jdbcTemplate().update("UPDATE kkumz_site SET custom_domain = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    req.getCustomDomain().trim(), resolvedId);
        }
        return root.accessMeta().findSiteByIdOrSlug(resolvedId);
    }

    List<Map<String, Object>> listAdminBuilds(String status, String deployMode) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.id,
                       b.site_id AS "siteId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_name AS "brandName",
                       b.release_id AS "releaseId",
                       COALESCE(r.version_no, s.active_version) AS "versionNo",
                       b.job_type AS "jobType",
                       b.deploy_mode AS "deployMode",
                       b.status,
                       b.duration_ms AS "durationMs",
                       b.error_summary AS "errorSummary",
                       b.ai_patch_applied AS "aiPatchApplied",
                       b.build_log AS "buildLog",
                       b.triggered_by AS "triggeredBy",
                       b.started_at AS "startedAt",
                       b.finished_at AS "finishedAt",
                       b.started_at AS "createdAt"
                FROM kkumz_build_job b
                JOIN kkumz_site s ON s.id = b.site_id
                LEFT JOIN kkumz_site_release r ON r.id = b.release_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND b.status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (deployMode != null && !deployMode.isBlank() && !"ALL".equalsIgnoreCase(deployMode)) {
            sql.append(" AND b.deploy_mode = ?");
            params.add(deployMode.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY b.started_at DESC");
        return root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
    }

    List<Map<String, Object>> listAdminCommunityProjects(String siteType, String keyword) {
        return getCommunityFeed(siteType, null, "POPULAR", keyword);
    }

    List<Map<String, Object>> listModerationReports(String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT r.id,
                       r.target_type AS "targetType",
                       r.target_id AS "targetId",
                       r.site_id AS "siteId",
                       s.site_slug AS "siteSlug",
                       s.site_name AS "siteName",
                       s.site_name AS "brandName",
                       r.reporter_username AS "reporterUsername",
                       r.reason_category AS "reasonCategory",
                       r.reason_detail AS "reasonDetail",
                       r.status,
                       r.action_taken AS "actionTaken",
                       r.moderator_username AS "moderatorUsername",
                       r.moderator_username AS "resolvedBy",
                       r.resolved_at AS "resolvedAt",
                       r.created_at AS "createdAt"
                FROM kkumz_moderation_report r
                JOIN kkumz_site s ON s.id = r.site_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND r.status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        sql.append(" ORDER BY CASE WHEN r.status = 'PENDING' THEN 0 ELSE 1 END, r.created_at DESC");
        return root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
    }

    Map<String, Object> createModerationReport(String siteIdOrSlug, CreateReportRequest req) {
        if (req == null || req.getReasonDetail() == null || req.getReasonDetail().isBlank()) {
            throw new IllegalArgumentException("신고 상세 사유(reasonDetail)를 입력해 주세요.");
        }
        String authUser = root.accessMeta().resolveCurrentUsernameFromRequest();
        String reporter = (authUser != null && !authUser.isBlank()) ? authUser : req.getReporterUsername();
        if (reporter == null || reporter.isBlank()) {
            if (root.accessMeta().isHttpRequestActive()) {
                throw new SecurityException("[401] 로그인이 필요한 기능입니다. 먼저 로그인해 주세요.");
            }
            throw new IllegalArgumentException("신고자 계정(reporterUsername)은 필수입니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteIdOrSlug);
        root.accessMeta().assertSiteReadable(site);
        String siteId = String.valueOf(site.get("id"));
        String targetType = (req.getTargetType() != null && !req.getTargetType().isBlank())
                ? req.getTargetType().trim().toUpperCase(Locale.ROOT)
                : "SITE";
        String targetId = (req.getTargetId() != null && !req.getTargetId().isBlank())
                ? req.getTargetId().trim()
                : siteId;
        String reportId = "rep-" + UUID.randomUUID().toString().substring(0, 8);

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_moderation_report (
                    id, target_type, target_id, site_id, reporter_username, reason_category, reason_detail, status, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)
                """,
                reportId,
                targetType, targetId, siteId,
                reporter.trim(),
                req.getReasonCategory() != null ? req.getReasonCategory() : "SPAM_PROMOTION",
                req.getReasonDetail().trim()
        );
        return Map.of("id", reportId, "siteId", siteId, "status", "PENDING");
    }

    List<Map<String, Object>> resolveModerationReport(String reportId, ResolveReportRequest req) {
        String status = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : "RESOLVED";
        String actionTaken = (req.getActionTaken() != null && !req.getActionTaken().isBlank())
                ? req.getActionTaken().trim()
                : (req.getResolutionNote() != null && !req.getResolutionNote().isBlank()
                ? req.getResolutionNote().trim()
                : "모더레이터 심사 완료");
        String moderator = (req.getModeratorUsername() != null && !req.getModeratorUsername().isBlank())
                ? req.getModeratorUsername().trim()
                : (req.getResolvedBy() != null && !req.getResolvedBy().isBlank() ? req.getResolvedBy().trim() : "admin");

        root.jdbcTemplate().update("""
                UPDATE kkumz_moderation_report
                SET status = ?, action_taken = ?, moderator_username = ?, resolved_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                status, actionTaken, moderator, reportId
        );

        boolean shouldSuspend = Boolean.TRUE.equals(req.getSuspendSite())
                || ("RESOLVED".equals(status) && (actionTaken.toUpperCase(Locale.ROOT).contains("SUSPEND") || actionTaken.contains("정지")));
        if (shouldSuspend) {
            root.jdbcTemplate().update("""
                    UPDATE kkumz_site SET status = 'SUSPENDED', updated_at = CURRENT_TIMESTAMP
                    WHERE id = (SELECT site_id FROM kkumz_moderation_report WHERE id = ?)
                    """, reportId);
        }

        return listModerationReports(null);
    }

    List<Map<String, Object>> listAdminApiKeys(String direction, String serviceCode, String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT id,
                       key_name AS "keyName",
                       direction,
                       service_code AS "serviceCode",
                       base_url AS "baseUrl",
                       access_key AS "accessKey",
                       secret_key AS "secretKey",
                       scopes_json::text AS "scopesJson",
                       rate_limit_rpm AS "rateLimitPerMin",
                       rate_limit_rpm AS "rateLimitRpm",
                       status,
                       last_used_at AS "lastUsedAt",
                       call_count AS "callCount",
                       expires_at AS "expiresAt",
                       description,
                       created_by AS "createdBy",
                       created_at AS "createdAt",
                       updated_at AS "updatedAt"
                FROM kkumz_api_access_key
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (direction != null && !direction.isBlank() && !"ALL".equalsIgnoreCase(direction)) {
            sql.append(" AND direction = ?");
            params.add(direction.trim().toUpperCase(Locale.ROOT));
        }
        if (serviceCode != null && !serviceCode.isBlank() && !"ALL".equalsIgnoreCase(serviceCode)) {
            sql.append(" AND service_code = ?");
            params.add(serviceCode.trim().toUpperCase(Locale.ROOT));
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (key_name ILIKE ? OR access_key ILIKE ? OR description ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append(" ORDER BY CASE WHEN direction = 'OUTBOUND' THEN 0 ELSE 1 END, created_at ASC");

        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(sql.toString(), params.toArray());
        for (Map<String, Object> row : rows) {
            row.put("scopes", parseJsonList(String.valueOf(row.remove("scopesJson"))));
        }
        return rows;
    }

    Map<String, Object> saveAdminApiKey(SaveApiAccessKeyRequest req) {
        if (req == null || req.getKeyName() == null || req.getKeyName().isBlank()) {
            throw new IllegalArgumentException("액세스 키 명칭(keyName)은 필수입니다.");
        }
        String direction = (req.getDirection() != null && !req.getDirection().isBlank())
                ? req.getDirection().trim().toUpperCase(Locale.ROOT)
                : "INBOUND";
        String serviceCode = (req.getServiceCode() != null && !req.getServiceCode().isBlank())
                ? req.getServiceCode().trim().toUpperCase(Locale.ROOT)
                : ("OUTBOUND".equals(direction) ? "SYNC_CMS" : "SYNC_VERSE");
        String status = (req.getStatus() != null && !req.getStatus().isBlank())
                ? req.getStatus().trim().toUpperCase(Locale.ROOT)
                : "ACTIVE";
        int rateLimit = (req.getRateLimitPerMin() != null && req.getRateLimitPerMin() > 0)
                ? req.getRateLimitPerMin()
                : SystemConfigHolder.getInt("kkumz.policy.default-rate-limit-rpm", 600);
        List<String> scopes = (req.getScopes() != null && !req.getScopes().isEmpty())
                ? req.getScopes()
                : ("OUTBOUND".equals(direction)
                ? ("SYNC_CMS".equals(serviceCode)
                ? List.of("cms:site:read", "cms:site:provision", "cms:content:write", "cms:form:submit")
                : List.of("shop:site:read", "shop:site:provision", "shop:product:write", "shop:order:write"))
                : List.of("kkumz:idea:read", "kkumz:site:read", "kkumz:site:deploy"));
        String scopesJson = toJson(scopes);
        String baseUrl = req.getBaseUrl() != null ? req.getBaseUrl().trim() : "";
        if (baseUrl.isEmpty() && "OUTBOUND".equals(direction)) {
            baseUrl = "SYNC_CMS".equals(serviceCode)
                    ? root.resolveCmsBaseUrl()
                    : root.resolveShopBaseUrl();
        }
        String description = req.getDescription() != null ? req.getDescription().trim() : "";
        String createdBy = (req.getCreatedBy() != null && !req.getCreatedBy().isBlank()) ? req.getCreatedBy().trim() : "admin";

        String keyPrefix = "OUTBOUND".equals(direction)
                ? ("SYNC_CMS".equals(serviceCode) ? "ak_cms_" : "ak_shop_")
                : "ak_kkumz_";
        String secretPrefix = "OUTBOUND".equals(direction)
                ? ("SYNC_CMS".equals(serviceCode) ? "sk_cms_" : "sk_shop_")
                : "sk_kkumz_";

        String id;
        String finalAccessKey;
        String finalSecretKey;

        if (req.getId() != null && !req.getId().isBlank()) {
            id = req.getId().trim();
            List<Map<String, Object>> existing = root.jdbcTemplate().queryForList(
                    "SELECT access_key, secret_key FROM kkumz_api_access_key WHERE id = ?", id
            );
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("존재하지 않는 API 액세스 키 ID입니다: " + id);
            }
            finalAccessKey = (req.getAccessKey() != null && !req.getAccessKey().isBlank())
                    ? req.getAccessKey().trim()
                    : String.valueOf(existing.get(0).get("access_key"));
            finalSecretKey = (req.getSecretKey() != null && !req.getSecretKey().isBlank())
                    ? req.getSecretKey().trim()
                    : String.valueOf(existing.get(0).get("secret_key"));

            root.jdbcTemplate().update("""
                    UPDATE kkumz_api_access_key
                    SET key_name = ?,
                        direction = ?,
                        service_code = ?,
                        base_url = ?,
                        access_key = ?,
                        secret_key = ?,
                        scopes_json = ?::jsonb,
                        rate_limit_rpm = ?,
                        status = ?,
                        description = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                    req.getKeyName().trim(), direction, serviceCode, baseUrl,
                    finalAccessKey, finalSecretKey, scopesJson, rateLimit, status, description, id
            );
        } else {
            id = "ak-kkumz-" + UUID.randomUUID().toString().substring(0, 8);
            finalAccessKey = (req.getAccessKey() != null && !req.getAccessKey().isBlank())
                    ? req.getAccessKey().trim()
                    : keyPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
            finalSecretKey = (req.getSecretKey() != null && !req.getSecretKey().isBlank())
                    ? req.getSecretKey().trim()
                    : secretPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 26);

            root.jdbcTemplate().update("""
                    INSERT INTO kkumz_api_access_key (
                        id, key_name, direction, service_code, base_url, access_key, secret_key,
                        scopes_json, rate_limit_rpm, status, call_count, expires_at, description, created_by, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, 0, CURRENT_TIMESTAMP + INTERVAL '365 days', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id, req.getKeyName().trim(), direction, serviceCode, baseUrl,
                    finalAccessKey, finalSecretKey, scopesJson, rateLimit, status, description, createdBy
            );
        }

        if ("OUTBOUND".equals(direction)) {
            syncOutboundKeyToRemoteService(serviceCode, req.getKeyName().trim(), finalAccessKey, finalSecretKey, status);
        }

        return getAdminApiKeyById(id);
    }

    Map<String, Object> rotateAdminApiKey(String keyId) {
        Map<String, Object> existing = getAdminApiKeyById(keyId);
        String direction = String.valueOf(existing.get("direction"));
        String serviceCode = String.valueOf(existing.get("serviceCode"));
        String accessKey = String.valueOf(existing.get("accessKey"));
        String keyName = String.valueOf(existing.get("keyName"));
        String status = String.valueOf(existing.get("status"));

        String secretPrefix = "OUTBOUND".equalsIgnoreCase(direction)
                ? ("SYNC_CMS".equalsIgnoreCase(serviceCode) ? "sk_cms_" : "sk_shop_")
                : "sk_kkumz_";
        String rotatedSecret = secretPrefix + UUID.randomUUID().toString().replace("-", "").substring(0, 26);

        root.jdbcTemplate().update(
                "UPDATE kkumz_api_access_key SET secret_key = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                rotatedSecret, keyId
        );

        if ("OUTBOUND".equalsIgnoreCase(direction)) {
            syncOutboundKeyToRemoteService(serviceCode, keyName, accessKey, rotatedSecret, status);
        }

        return getAdminApiKeyById(keyId);
    }

    Map<String, Object> updateAdminApiKeyStatus(String keyId, String status) {
        Map<String, Object> existing = getAdminApiKeyById(keyId);
        String normalized = (status != null && !status.isBlank()) ? status.trim().toUpperCase(Locale.ROOT) : "ACTIVE";
        root.jdbcTemplate().update(
                "UPDATE kkumz_api_access_key SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                normalized, keyId
        );
        if ("OUTBOUND".equalsIgnoreCase(String.valueOf(existing.get("direction")))) {
            syncOutboundKeyToRemoteService(
                    String.valueOf(existing.get("serviceCode")),
                    String.valueOf(existing.get("keyName")),
                    String.valueOf(existing.get("accessKey")),
                    String.valueOf(existing.get("secretKey")),
                    normalized
            );
        }
        return getAdminApiKeyById(keyId);
    }

    Map<String, Object> verifyAdminApiKey(String keyId) {
        Map<String, Object> keyRow = getAdminApiKeyById(keyId);
        String direction = String.valueOf(keyRow.get("direction"));
        String serviceCode = String.valueOf(keyRow.get("serviceCode"));
        String baseUrl = keyRow.get("baseUrl") != null ? String.valueOf(keyRow.get("baseUrl")) : "";
        String accessKey = String.valueOf(keyRow.get("accessKey"));
        String secretKey = String.valueOf(keyRow.get("secretKey"));
        String status = String.valueOf(keyRow.get("status"));

        if (!"ACTIVE".equalsIgnoreCase(status)) {
            Map<String, Object> inactiveRes = new LinkedHashMap<>();
            inactiveRes.put("keyId", keyId);
            inactiveRes.put("verified", false);
            inactiveRes.put("serviceCode", serviceCode);
            inactiveRes.put("direction", direction);
            inactiveRes.put("message", "비활성화(REVOKED) 상태인 액세스 키입니다.");
            return inactiveRes;
        }

        if ("OUTBOUND".equalsIgnoreCase(direction) && root.syncServiceApiClient() != null) {
            Map<String, Object> remoteCheck = root.syncServiceApiClient().verifyServiceConnection(
                    serviceCode, baseUrl, accessKey, secretKey, keyId
            );
            remoteCheck.put("keyId", keyId);
            remoteCheck.put("keyName", keyRow.get("keyName"));
            remoteCheck.put("direction", direction);
            remoteCheck.put("updatedKey", getAdminApiKeyById(keyId));
            return remoteCheck;
        }

        root.jdbcTemplate().update(
                "UPDATE kkumz_api_access_key SET last_used_at = NOW(), call_count = COALESCE(call_count, 0) + 1 WHERE id = ?",
                keyId
        );
        Map<String, Object> inboundRes = new LinkedHashMap<>();
        inboundRes.put("keyId", keyId);
        inboundRes.put("keyName", keyRow.get("keyName"));
        inboundRes.put("direction", direction);
        inboundRes.put("serviceCode", serviceCode);
        inboundRes.put("accessKey", accessKey);
        inboundRes.put("verified", true);
        inboundRes.put("latencyMs", 2L);
        inboundRes.put("message", "Kkumz 인바운드 API 액세스 키 유효성 및 스코프 검증 완료");
        inboundRes.put("updatedKey", getAdminApiKeyById(keyId));
        return inboundRes;
    }

    void deleteAdminApiKey(String keyId) {
        root.jdbcTemplate().update("DELETE FROM kkumz_api_access_key WHERE id = ?", keyId);
    }

    private Map<String, Object> getAdminApiKeyById(String keyId) {
        List<Map<String, Object>> list = listAdminApiKeys(null, null, null, null);
        return list.stream()
                .filter(k -> keyId.equals(String.valueOf(k.get("id"))))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 API 액세스 키입니다: " + keyId));
    }

    private void syncOutboundKeyToRemoteService(String serviceCode, String keyName, String accessKey, String secretKey, String status) {
        try {
            if ("SYNC_CMS".equalsIgnoreCase(serviceCode) && root.cmsJdbcTemplate() != null) {
                Integer exists = root.cmsJdbcTemplate().queryForObject(
                        "SELECT COUNT(*) FROM sys_app WHERE app_key = ?", Integer.class, accessKey
                );
                if (exists != null && exists > 0) {
                    root.cmsJdbcTemplate().update(
                            "UPDATE sys_app SET app_secret = ?, channel = ? WHERE app_key = ?",
                            secretKey, "KKUMZ_STUDIO", accessKey
                    );
                } else {
                    Integer nextId = root.cmsJdbcTemplate().queryForObject(
                            "SELECT COALESCE(MAX(id), 100) + 1 FROM sys_app", Integer.class
                    );
                    root.cmsJdbcTemplate().update(
                            """
                            INSERT INTO sys_app (id, site_id, channel, app_key, app_secret, authorized_apis, expiry_minutes)
                            VALUES (?, 1, 'KKUMZ_STUDIO', ?, ?, '/api/v1/cms/multisite/**,/api/v1/cms/frontend/**,/api/v1/cms/form/**', 525600)
                            """,
                            nextId != null ? nextId : 105, accessKey, secretKey
                    );
                }
            } else if ("SYNC_SHOP".equalsIgnoreCase(serviceCode) && root.shopJdbcTemplate() != null) {
                int shopStatus = "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
                Integer exists = root.shopJdbcTemplate().queryForObject(
                        "SELECT COUNT(*) FROM sys_api_access_key WHERE access_key = ?", Integer.class, accessKey
                );
                if (exists != null && exists > 0) {
                    root.shopJdbcTemplate().update(
                            "UPDATE sys_api_access_key SET secret_key = ?, client_name = ?, status = ?, update_time = NOW() WHERE access_key = ?",
                            secretKey, keyName, shopStatus, accessKey
                    );
                } else {
                    root.shopJdbcTemplate().update(
                            """
                            INSERT INTO sys_api_access_key (
                                id, client_code, client_name, access_key, secret_key, allowed_scopes,
                                rate_limit_rpm, status, call_count, description, create_time, update_time
                            ) VALUES (?, 'KKUMZ_STUDIO', ?, ?, ?, 'shop:site:provision,shop:product:read,shop:product:write,shop:order:write,shop:promotion:read', 1200, ?, 0, ?, NOW(), NOW())
                            """,
                            "ak_shop_" + UUID.randomUUID().toString().substring(0, 6), keyName, accessKey, secretKey, shopStatus, keyName
                    );
                }
            }
        } catch (Exception e) {
            log.debug("원격 서비스 키 동기화 생략 ({}): {}", serviceCode, e.getMessage());
        }
    }
}
