package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;

/**
 * Kkumz 업로드 미디어·로고·문서 자산 관리, 텍스트 추출 및 사이트 프로비저닝 자산 바인딩을 담당하는 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzAssetSupport {

    private final KkumzService root;

    List<Map<String, Object>> listIdeaAssets(String ideaId) {
        root.accessMeta().assertIdeaReadable(ideaId);
        return listIdeaAssetsInternal(ideaId);
    }

    List<Map<String, Object>> listIdeaAssetsInternal(String ideaId) {
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                SELECT id,
                       idea_id AS "ideaId",
                       asset_type AS "assetType",
                       original_name AS "originalName",
                       stored_name AS "storedName",
                       file_url AS "fileUrl",
                       mime_type AS "mimeType",
                       file_size AS "fileSize",
                       caption,
                       extracted_text AS "extractedText",
                       sort_no AS "sortNo",
                       uploaded_by AS "uploadedBy",
                       created_at AS "createdAt"
                FROM kkumz_idea_asset
                WHERE idea_id = ?
                ORDER BY sort_no ASC, created_at ASC
                """, ideaId);
        for (Map<String, Object> row : rows) {
            String mime = String.valueOf(row.getOrDefault("mimeType", "")).toLowerCase(Locale.ROOT);
            String orig = String.valueOf(row.getOrDefault("originalName", "")).toLowerCase(Locale.ROOT);
            boolean isImg = mime.startsWith("image/")
                    || orig.matches(".*\\.(jpg|jpeg|png|webp|gif|svg|avif)$");
            row.put("isImage", isImg);
            String ext = extractExtension(orig);
            row.put("fileExt", ext);
            String fUrl = String.valueOf(row.getOrDefault("fileUrl", ""));
            row.put("downloadUrl", fUrl.isBlank() ? "" : (fUrl + "?download=true"));
        }
        return rows;
    }

    Map<String, Object> uploadIdeaAssets(
            String ideaId,
            List<MultipartFile> files,
            MultipartFile singleFile,
            String assetType,
            String caption,
            Boolean applyToPrompt,
            String uploadedBy
    ) {
        root.accessMeta().assertIdeaEditable(ideaId);
        Map<String, Object> idea = root.ideaSpec().getIdeaDetail(ideaId);
        List<MultipartFile> allFiles = new ArrayList<>();
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) {
                    allFiles.add(f);
                }
            }
        }
        if (singleFile != null && !singleFile.isEmpty()) {
            allFiles.add(singleFile);
        }
        if (allFiles.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 선택되지 않았습니다.");
        }

        Path uploadRoot = resolveUploadDir();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 저장소 디렉터리를 생성할 수 없습니다: " + e.getMessage(), e);
        }

        Integer currentMaxSort = root.jdbcTemplate().queryForObject(
                "SELECT COALESCE(MAX(sort_no), 0) FROM kkumz_idea_asset WHERE idea_id = ?",
                Integer.class, ideaId
        );
        int sortSeq = (currentMaxSort != null ? currentMaxSort : 0);
        String actor = (uploadedBy != null && !uploadedBy.isBlank())
                ? uploadedBy.trim()
                : String.valueOf(idea.getOrDefault("authorUsername", "admin"));

        List<String> extractedSnippets = new ArrayList<>();
        List<Map<String, Object>> uploadedAssets = new ArrayList<>();

        for (MultipartFile mf : allFiles) {
            sortSeq++;
            String origName = mf.getOriginalFilename() != null && !mf.getOriginalFilename().isBlank()
                    ? Paths.get(mf.getOriginalFilename()).getFileName().toString()
                    : "asset-" + sortSeq;
            String ext = extractExtension(origName);
            String mimeType = resolveMimeType(mf.getContentType(), ext);
            String resolvedType = classifyAssetType(assetType, origName, ext, mimeType);
            String assetId = "ast-" + UUID.randomUUID().toString().substring(0, 8);
            String storedName = ideaId + "-" + assetId + (ext.isEmpty() ? "" : "." + ext);

            byte[] bytes;
            try {
                bytes = mf.getBytes();
                Files.write(uploadRoot.resolve(storedName), bytes);
            } catch (IOException e) {
                throw new IllegalStateException("파일 저장 중 오류가 발생했습니다 (" + origName + "): " + e.getMessage(), e);
            }

            String prefix = root.storagePublicPrefix() != null && !root.storagePublicPrefix().isBlank()
                    ? root.storagePublicPrefix().replaceAll("/+$", "")
                    : "/api/v1/kkumz/assets/files";
            String fileUrl = prefix + "/" + storedName;
            String resolvedCaption = (caption != null && !caption.isBlank())
                    ? caption.trim()
                    : stripExtension(origName);

            String extractedText = null;
            if ("DOC".equalsIgnoreCase(resolvedType) || isTextOrDocumentFile(ext, mimeType)) {
                extractedText = extractDocumentText(bytes, ext, mimeType);
                if (extractedText != null && !extractedText.isBlank()) {
                    extractedSnippets.add("[" + origName + "]\n" + extractedText);
                }
            }

            root.jdbcTemplate().update("""
                    INSERT INTO kkumz_idea_asset (
                        id, idea_id, asset_type, original_name, stored_name, file_url,
                        mime_type, file_size, caption, extracted_text, sort_no, uploaded_by, created_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """,
                    assetId, ideaId, resolvedType, origName, storedName, fileUrl,
                    mimeType, (long) bytes.length, resolvedCaption, extractedText, sortSeq, actor
            );

            if ("LOGO".equalsIgnoreCase(resolvedType)) {
                registerUploadedLogoCandidate(ideaId, resolvedCaption, fileUrl, bytes, ext, mimeType);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", assetId);
            item.put("ideaId", ideaId);
            item.put("assetType", resolvedType);
            item.put("originalName", origName);
            item.put("storedName", storedName);
            item.put("fileUrl", fileUrl);
            item.put("downloadUrl", fileUrl + "?download=true");
            item.put("fileExt", ext);
            item.put("mimeType", mimeType);
            item.put("fileSize", (long) bytes.length);
            item.put("caption", resolvedCaption);
            item.put("extractedText", extractedText);
            item.put("sortNo", sortSeq);
            uploadedAssets.add(item);
        }

        if (Boolean.TRUE.equals(applyToPrompt) && !extractedSnippets.isEmpty()) {
            appendExtractedTextToIdeaPrompt(ideaId, String.join("\n\n", extractedSnippets));
        }

        syncIdeaAssetsToDeployedSiteIfPresent(ideaId);

        Map<String, Object> detail = root.ideaSpec().getIdeaDetail(ideaId);
        detail.put("uploadedAssets", uploadedAssets);
        return detail;
    }

    Map<String, Object> updateIdeaAsset(String ideaId, String assetId, UpdateIdeaAssetRequest req) {
        root.accessMeta().assertIdeaEditable(ideaId);
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                SELECT id, original_name AS "originalName", stored_name AS "storedName",
                       file_url AS "fileUrl", mime_type AS "mimeType", caption, asset_type AS "assetType"
                FROM kkumz_idea_asset
                WHERE id = ? AND idea_id = ?
                """, assetId, ideaId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("수정할 업로드 자산을 찾을 수 없습니다: " + assetId);
        }
        Map<String, Object> existing = rows.get(0);
        String newType = (req != null && req.getAssetType() != null && !req.getAssetType().isBlank())
                ? req.getAssetType().trim().toUpperCase(Locale.ROOT)
                : String.valueOf(existing.get("assetType"));
        String newCaption = (req != null && req.getCaption() != null)
                ? req.getCaption().trim()
                : String.valueOf(existing.getOrDefault("caption", ""));

        if (req != null && req.getSortNo() != null) {
            root.jdbcTemplate().update("""
                    UPDATE kkumz_idea_asset
                    SET asset_type = ?, caption = ?, sort_no = ?
                    WHERE id = ? AND idea_id = ?
                    """, newType, newCaption, req.getSortNo(), assetId, ideaId);
        } else {
            root.jdbcTemplate().update("""
                    UPDATE kkumz_idea_asset
                    SET asset_type = ?, caption = ?
                    WHERE id = ? AND idea_id = ?
                    """, newType, newCaption, assetId, ideaId);
        }

        if ("LOGO".equalsIgnoreCase(newType)) {
            String storedName = String.valueOf(existing.get("storedName"));
            String origName = String.valueOf(existing.get("originalName"));
            String fileUrl = String.valueOf(existing.get("fileUrl"));
            String mimeType = String.valueOf(existing.get("mimeType"));
            byte[] bytes = new byte[0];
            try {
                Path p = resolveUploadDir().resolve(storedName);
                if (Files.exists(p)) {
                    bytes = Files.readAllBytes(p);
                }
            } catch (IOException ignored) {
            }
            registerUploadedLogoCandidate(ideaId, newCaption.isBlank() ? stripExtension(origName) : newCaption,
                    fileUrl, bytes, extractExtension(origName), mimeType);
        }

        syncIdeaAssetsToDeployedSiteIfPresent(ideaId);
        return root.ideaSpec().getIdeaDetail(ideaId);
    }

    Map<String, Object> deleteIdeaAsset(String ideaId, String assetId) {
        root.accessMeta().assertIdeaEditable(ideaId);
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
                "SELECT stored_name AS \"storedName\" FROM kkumz_idea_asset WHERE id = ? AND idea_id = ?",
                assetId, ideaId
        );
        if (!rows.isEmpty()) {
            String storedName = String.valueOf(rows.get(0).get("storedName"));
            try {
                Files.deleteIfExists(resolveUploadDir().resolve(storedName));
            } catch (IOException e) {
                log.warn("저장 파일 삭제 경고 ({}): {}", storedName, e.getMessage());
            }
        }
        root.jdbcTemplate().update("DELETE FROM kkumz_idea_asset WHERE id = ? AND idea_id = ?", assetId, ideaId);
        syncIdeaAssetsToDeployedSiteIfPresent(ideaId);
        return root.ideaSpec().getIdeaDetail(ideaId);
    }

    Map<String, Object> applyAssetToPrompt(String ideaId, String assetId) {
        root.accessMeta().assertIdeaEditable(ideaId);
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList("""
                SELECT original_name AS "originalName", caption, extracted_text AS "extractedText"
                FROM kkumz_idea_asset
                WHERE id = ? AND idea_id = ?
                """, assetId, ideaId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("대상 자산을 찾을 수 없습니다: " + assetId);
        }
        Map<String, Object> asset = rows.get(0);
        String origName = String.valueOf(asset.get("originalName"));
        String extracted = asset.get("extractedText") != null ? String.valueOf(asset.get("extractedText")).trim() : "";
        String caption = asset.get("caption") != null ? String.valueOf(asset.get("caption")).trim() : "";
        String snippet = !extracted.isBlank()
                ? "[첨부자료: " + origName + "]\n" + extracted
                : "[첨부자료: " + origName + "] " + caption;
        appendExtractedTextToIdeaPrompt(ideaId, snippet);
        return root.ideaSpec().getIdeaDetail(ideaId);
    }

    Map<String, Object> uploadCustomBrandLogo(String ideaId, MultipartFile file, String styleName) {
        root.accessMeta().assertIdeaEditable(ideaId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 로고 파일이 비어 있습니다.");
        }
        String label = (styleName != null && !styleName.isBlank()) ? styleName.trim() : null;
        return uploadIdeaAssets(ideaId, List.of(file), null, "LOGO", label, false, null);
    }

    StoredAssetPayload loadStoredAssetFile(String storedName) {
        if (storedName == null || storedName.isBlank() || storedName.contains("..") || storedName.matches(".*[\\\\/].*")) {
            throw new IllegalArgumentException("유효하지 않은 파일 식별자입니다.");
        }
        Path filePath = resolveUploadDir().resolve(storedName);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("요청한 업로드 파일이 존재하지 않습니다: " + storedName);
        }
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            List<Map<String, Object>> meta = root.jdbcTemplate().queryForList(
                    "SELECT original_name AS \"originalName\", mime_type AS \"mimeType\" FROM kkumz_idea_asset WHERE stored_name = ? LIMIT 1",
                    storedName
            );
            String origName = !meta.isEmpty() && meta.get(0).get("originalName") != null
                    ? String.valueOf(meta.get(0).get("originalName"))
                    : storedName;
            String mimeType = !meta.isEmpty() && meta.get(0).get("mimeType") != null
                    ? String.valueOf(meta.get(0).get("mimeType"))
                    : resolveMimeType(null, extractExtension(storedName));
            return StoredAssetPayload.builder()
                    .bytes(bytes)
                    .mimeType(mimeType)
                    .originalName(origName)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("업로드 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    Path resolveUploadDir() {
        String configured = (root.storageUploadDir() != null && !root.storageUploadDir().isBlank())
                ? root.storageUploadDir().trim()
                : "uploads/kkumz";
        return Paths.get(configured).toAbsolutePath().normalize();
    }

    private void appendExtractedTextToIdeaPrompt(String ideaId, String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return;
        }
        List<Map<String, Object>> rows = root.jdbcTemplate().queryForList(
                "SELECT prompt_text AS \"promptText\" FROM kkumz_idea WHERE id = ?",
                ideaId
        );
        if (rows.isEmpty()) {
            return;
        }
        String currentPrompt = rows.get(0).get("promptText") != null ? String.valueOf(rows.get(0).get("promptText")).trim() : "";
        if (currentPrompt.contains(snippet.trim())) {
            return;
        }
        String updated = currentPrompt.isEmpty()
                ? snippet.trim()
                : currentPrompt + "\n\n[업로드 자료 참고 내용]\n" + snippet.trim();
        root.jdbcTemplate().update(
                "UPDATE kkumz_idea SET prompt_text = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                updated, ideaId
        );
    }

    private void registerUploadedLogoCandidate(
            String ideaId,
            String label,
            String fileUrl,
            byte[] bytes,
            String ext,
            String mimeType
    ) {
        String svgCode;
        if ("svg".equalsIgnoreCase(ext) && bytes != null && bytes.length > 0) {
            String rawSvg = new String(bytes, StandardCharsets.UTF_8).trim();
            int svgIdx = rawSvg.indexOf("<svg");
            svgCode = svgIdx >= 0 ? rawSvg.substring(svgIdx) : rawSvg;
        } else {
            svgCode = "<svg viewBox=\"0 0 185 40\" xmlns=\"http://www.w3.org/2000/svg\">"
                    + "<image href=\"" + escapeXml(fileUrl) + "\" x=\"0\" y=\"0\" width=\"185\" height=\"40\" preserveAspectRatio=\"xMinYMid meet\"/>"
                    + "</svg>";
        }

        List<Map<String, Object>> existingLogos = root.jdbcTemplate().queryForList(
                """
                SELECT COALESCE(
                         (SELECT l.color_palette_json::text FROM kkumz_logo_suggestion l WHERE l.idea_id = i.id ORDER BY l.is_selected DESC, l.created_at ASC LIMIT 1),
                         NULLIF(i.design_tokens_json::text, '{}'),
                         (SELECT l2.color_palette_json::text FROM kkumz_logo_suggestion l2 ORDER BY l2.is_selected DESC, l2.created_at ASC LIMIT 1),
                         '{}'
                       ) AS "palette",
                       COALESCE(
                         (SELECT l.typography_json::text FROM kkumz_logo_suggestion l WHERE l.idea_id = i.id ORDER BY l.is_selected DESC, l.created_at ASC LIMIT 1),
                         (SELECT l2.typography_json::text FROM kkumz_logo_suggestion l2 ORDER BY l2.is_selected DESC, l2.created_at ASC LIMIT 1),
                         '{}'
                       ) AS "typo"
                FROM kkumz_idea i
                WHERE i.id = ?
                """,
                ideaId
        );
        String paletteJson = !existingLogos.isEmpty() && existingLogos.get(0).get("palette") != null
                ? String.valueOf(existingLogos.get(0).get("palette"))
                : "{}";
        String typoJson = !existingLogos.isEmpty() && existingLogos.get(0).get("typo") != null
                ? String.valueOf(existingLogos.get(0).get("typo"))
                : "{}";

        root.jdbcTemplate().update("UPDATE kkumz_logo_suggestion SET is_selected = FALSE WHERE idea_id = ?", ideaId);
        String logoId = "logo-up-" + UUID.randomUUID().toString().substring(0, 8);
        String styleName = (label != null && !label.isBlank()) ? "업로드 로고 · " + label : "업로드 보유 로고";

        root.jdbcTemplate().update("""
                INSERT INTO kkumz_logo_suggestion (
                    id, idea_id, style_name, svg_code, color_palette_json, typography_json, tags_json, is_selected, created_at
                ) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, '["보유로고","직접업로드"]'::jsonb, TRUE, CURRENT_TIMESTAMP)
                """,
                logoId, ideaId, styleName, svgCode, paletteJson, typoJson
        );

        root.jdbcTemplate().update("""
                UPDATE kkumz_idea
                SET selected_logo_svg = ?,
                    design_tokens_json = CASE
                        WHEN design_tokens_json = '{}'::jsonb THEN ?::jsonb
                        ELSE design_tokens_json
                    END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                svgCode, paletteJson, ideaId
        );
    }

    String buildPromptWithAssetContext(String basePrompt, List<Map<String, Object>> assets) {
        if (assets == null || assets.isEmpty()) {
            return basePrompt;
        }
        StringBuilder sb = new StringBuilder(basePrompt != null ? basePrompt : "");
        List<String> mediaCaptions = new ArrayList<>();
        List<String> docTexts = new ArrayList<>();
        for (Map<String, Object> a : assets) {
            String type = String.valueOf(a.getOrDefault("assetType", ""));
            String cap = a.get("caption") != null ? String.valueOf(a.get("caption")).trim() : "";
            String orig = String.valueOf(a.getOrDefault("originalName", ""));
            String extText = a.get("extractedText") != null ? String.valueOf(a.get("extractedText")).trim() : "";
            if (!extText.isBlank()) {
                docTexts.add("- " + orig + ": " + (extText.length() > 800 ? extText.substring(0, 800) : extText));
            } else if (!cap.isBlank()) {
                mediaCaptions.add("- [" + type + "] " + cap + " (" + orig + ")");
            }
        }
        if (!mediaCaptions.isEmpty()) {
            sb.append("\n\n[업로드된 미디어 자산 목록]\n").append(String.join("\n", mediaCaptions));
        }
        if (!docTexts.isEmpty()) {
            sb.append("\n\n[업로드된 참고 문서 텍스트]\n").append(String.join("\n", docTexts));
        }
        return sb.toString();
    }

    void applyUploadedAssetsToSiteProvisioning(
            String ideaId,
            String brandName,
            Map<String, Object> layoutSchema,
            Long shopSiteId,
            Long cmsSiteId
    ) {
        List<Map<String, Object>> assets = listIdeaAssetsInternal(ideaId);
        if (assets.isEmpty()) {
            return;
        }

        Map<String, Object> heroAsset = null;
        List<Map<String, Object>> productAssets = new ArrayList<>();
        List<Map<String, Object>> articleAssets = new ArrayList<>();
        List<Map<String, Object>> galleryAssets = new ArrayList<>();
        List<Map<String, Object>> docAssets = new ArrayList<>();

        for (Map<String, Object> a : assets) {
            String type = String.valueOf(a.getOrDefault("assetType", "GALLERY")).toUpperCase(Locale.ROOT);
            boolean isImage = Boolean.TRUE.equals(a.get("isImage"));
            switch (type) {
                case "HERO" -> {
                    if (heroAsset == null && isImage) {
                        heroAsset = a;
                    }
                    if (isImage) {
                        galleryAssets.add(a);
                    }
                }
                case "PRODUCT" -> {
                    if (isImage) {
                        productAssets.add(a);
                        galleryAssets.add(a);
                    }
                }
                case "ARTICLE" -> {
                    if (isImage) {
                        articleAssets.add(a);
                        galleryAssets.add(a);
                    }
                }
                case "GALLERY" -> {
                    if (isImage) {
                        galleryAssets.add(a);
                    }
                }
                case "DOC" -> docAssets.add(a);
                default -> {
                    if (isImage) {
                        galleryAssets.add(a);
                    }
                }
            }
        }

        if (heroAsset == null && !galleryAssets.isEmpty()) {
            heroAsset = galleryAssets.get(0);
        }

        if (heroAsset != null) {
            String heroUrl = String.valueOf(heroAsset.get("fileUrl"));
            String heroCap = heroAsset.get("caption") != null ? String.valueOf(heroAsset.get("caption")) : brandName;
            layoutSchema.put("heroBannerImage", heroUrl);
            layoutSchema.put("heroBannerCaption", Map.of(
                    "title", heroCap,
                    "sub", String.valueOf(heroAsset.getOrDefault("originalName", ""))
            ));
        }

        if (!galleryAssets.isEmpty()) {
            List<Map<String, Object>> galleryList = new ArrayList<>();
            for (Map<String, Object> g : galleryAssets) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", g.get("id"));
                item.put("fileUrl", g.get("fileUrl"));
                item.put("url", g.get("fileUrl"));
                item.put("originalName", g.get("originalName"));
                item.put("caption", g.get("caption") != null ? g.get("caption") : g.get("originalName"));
                item.put("assetType", g.get("assetType"));
                galleryList.add(item);
            }
            layoutSchema.put("galleryImages", galleryList);
            layoutSchema.put("showPhotoGallery", true);
        }

        if (!docAssets.isEmpty()) {
            List<Map<String, Object>> docList = new ArrayList<>();
            for (Map<String, Object> d : docAssets) {
                String fUrl = String.valueOf(d.get("fileUrl"));
                String origName = String.valueOf(d.getOrDefault("originalName", ""));
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", d.get("id"));
                item.put("fileUrl", fUrl);
                item.put("url", fUrl);
                item.put("downloadUrl", fUrl + (fUrl.contains("?") ? "&download=true" : "?download=true"));
                item.put("originalName", origName);
                item.put("name", origName);
                item.put("fileExt", extractExtension(origName));
                item.put("caption", d.get("caption") != null ? d.get("caption") : origName);
                item.put("fileSize", d.get("fileSize"));
                docList.add(item);
            }
            layoutSchema.put("downloadableDocs", docList);
        }

        if (!productAssets.isEmpty()) {
            List<Map<String, Object>> customProducts = new ArrayList<>();
            int idx = 1;
            for (Map<String, Object> pa : productAssets) {
                String prodName = pa.get("caption") != null && !String.valueOf(pa.get("caption")).isBlank()
                        ? String.valueOf(pa.get("caption"))
                        : stripExtension(String.valueOf(pa.get("originalName")));
                String picUrl = String.valueOf(pa.get("fileUrl"));
                Long prodId = (long) (90000 + idx);

                if (shopSiteId != null && root.shopJdbcTemplate() != null) {
                    List<Map<String, Object>> existProd = root.shopJdbcTemplate().queryForList(
                            "SELECT id, price, promotion_price AS \"promotionPrice\", stock, sub_title AS \"subTitle\", product_category_name AS \"categoryName\" FROM pms_product WHERE site_id = ? AND pic = ? LIMIT 1",
                            shopSiteId, picUrl
                    );
                    if (!existProd.isEmpty()) {
                        prodId = ((Number) existProd.get(0).get("id")).longValue();
                    } else {
                        prodId = root.shopJdbcTemplate().queryForObject("""
                                INSERT INTO pms_product (
                                    site_id, brand_name, name, pic, product_sn, delete_status, publish_status,
                                    new_status, recommand_status, verify_status, sort, sale, price, promotion_price,
                                    original_price, sub_title, description, stock, low_stock, unit, product_category_name
                                ) VALUES (?, ?, ?, ?, ?, 0, 1, 1, 1, 1, ?, 0, 0, 0, 0, ?, ?, 50, 5, '건', ?)
                                RETURNING id
                                """,
                                Long.class,
                                shopSiteId, brandName, prodName, picUrl,
                                "AST-" + pa.get("id"), idx,
                                prodName, prodName, brandName
                        );
                    }
                }

                Map<String, Object> prodMap = new LinkedHashMap<>();
                prodMap.put("id", prodId);
                prodMap.put("name", prodName);
                prodMap.put("pic", picUrl);
                prodMap.put("price", 0);
                prodMap.put("promotionPrice", 0);
                prodMap.put("subTitle", prodName);
                prodMap.put("stock", 50);
                prodMap.put("unit", "건");
                prodMap.put("brandName", brandName);
                prodMap.put("productCategoryName", brandName);
                customProducts.add(prodMap);
                idx++;
            }
            layoutSchema.put("customProducts", customProducts);
        }

        if (!articleAssets.isEmpty()) {
            List<Map<String, Object>> customArticles = new ArrayList<>();
            int idx = 1;
            for (Map<String, Object> aa : articleAssets) {
                String artTitle = aa.get("caption") != null && !String.valueOf(aa.get("caption")).isBlank()
                        ? String.valueOf(aa.get("caption"))
                        : stripExtension(String.valueOf(aa.get("originalName")));
                String coverUrl = String.valueOf(aa.get("fileUrl"));
                Long artId = (long) (80000 + idx);

                if (cmsSiteId != null && root.cmsJdbcTemplate() != null) {
                    List<Map<String, Object>> existArt = root.cmsJdbcTemplate().queryForList(
                            "SELECT id FROM cms_content WHERE site_id = ? AND cover = ? LIMIT 1",
                            cmsSiteId, coverUrl
                    );
                    if (!existArt.isEmpty()) {
                        artId = ((Number) existArt.get(0).get("id")).longValue();
                    } else {
                        List<Map<String, Object>> cats = root.cmsJdbcTemplate().queryForList(
                                "SELECT id FROM cms_category WHERE site_id = ? ORDER BY id ASC LIMIT 1",
                                cmsSiteId
                        );
                        Integer categoryId;
                        if (!cats.isEmpty()) {
                            categoryId = ((Number) cats.get(0).get("id")).intValue();
                        } else {
                            categoryId = root.cmsJdbcTemplate().queryForObject("""
                                    INSERT INTO cms_category (
                                        site_id, name, code, only_url, has_static, contain_child, allow_contribute, sort, hidden, disabled
                                    ) VALUES (?, '브랜드 매거진', ?, FALSE, FALSE, TRUE, TRUE, 1, FALSE, FALSE)
                                    RETURNING id
                                    """,
                                    Integer.class,
                                    cmsSiteId, "cat-" + cmsSiteId
                            );
                        }
                        String safeAuthor = brandName != null && brandName.length() > 50 ? brandName.substring(0, 50) : brandName;
                        String safeDesc = artTitle != null && artTitle.length() > 300 ? artTitle.substring(0, 300) : artTitle;
                        artId = root.cmsJdbcTemplate().queryForObject("""
                                INSERT INTO cms_content (
                                    site_id, title, user_id, category_id, model_id, copied, contribute,
                                    author, only_url, has_images, has_files, has_products, has_static,
                                    description, cover, childs, scores, comments, clicks,
                                    publish_date, create_date, sort, status, disabled
                                ) VALUES (?, ?, 1, ?, 'article', FALSE, FALSE, ?, FALSE, TRUE, FALSE, FALSE, FALSE, ?, ?, 0, 100, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, 1, FALSE)
                                RETURNING id
                                """,
                                Long.class,
                                cmsSiteId, artTitle, categoryId, safeAuthor, safeDesc, coverUrl, 100 - idx
                        );
                        String bodyHtml = "<p>" + escapeXml(artTitle) + "</p>";
                        root.cmsJdbcTemplate().update("""
                                INSERT INTO cms_content_attribute (content_id, text, word_count)
                                VALUES (?, ?, ?)
                                """,
                                artId, bodyHtml, artTitle != null ? artTitle.length() : 0
                        );
                    }
                }

                Map<String, Object> artMap = new LinkedHashMap<>();
                artMap.put("id", artId);
                artMap.put("title", artTitle);
                artMap.put("description", artTitle);
                artMap.put("summary", artTitle);
                artMap.put("cover", coverUrl);
                artMap.put("thumbnailUrl", coverUrl);
                artMap.put("author", brandName);
                artMap.put("categoryName", brandName);
                customArticles.add(artMap);
                idx++;
            }
            layoutSchema.put("customArticles", customArticles);
        }
    }

    void syncIdeaAssetsToDeployedSiteIfPresent(String ideaId) {
        List<Map<String, Object>> sites = root.jdbcTemplate().queryForList(
                "SELECT id, site_name AS \"siteName\", active_version AS \"activeVersion\", shop_site_id AS \"shopSiteId\", cms_site_id AS \"cmsSiteId\" FROM kkumz_site WHERE idea_id = ? ORDER BY created_at DESC LIMIT 1",
                ideaId
        );
        if (sites.isEmpty()) {
            return;
        }
        Map<String, Object> site = sites.get(0);
        String siteId = String.valueOf(site.get("id"));
        String siteName = String.valueOf(site.get("siteName"));
        int activeVer = ((Number) site.get("activeVersion")).intValue();
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;

        Map<String, Object> latestRel = root.accessMeta().getLatestRelease(siteId, activeVer);
        Map<String, Object> layoutSchema = new LinkedHashMap<>(parseJsonObject(String.valueOf(latestRel.get("layoutSchemaJson"))));

        applyUploadedAssetsToSiteProvisioning(ideaId, siteName, layoutSchema, shopSiteId, cmsSiteId);

        root.jdbcTemplate().update(
                "UPDATE kkumz_site_release SET layout_schema_json = ?::jsonb WHERE id = ?",
                toJson(layoutSchema), latestRel.get("id")
        );
    }

    List<Map<String, Object>> resolveGalleryAssets(Map<String, Object> layoutSchema, List<Map<String, Object>> ideaAssets) {
        if (layoutSchema != null && layoutSchema.get("galleryImages") instanceof List<?> gl && !gl.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object o : gl) {
                if (o instanceof Map<?, ?> m) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                    if (!copy.containsKey("fileUrl") && copy.get("url") != null) {
                        copy.put("fileUrl", copy.get("url"));
                    }
                    if (!copy.containsKey("url") && copy.get("fileUrl") != null) {
                        copy.put("url", copy.get("fileUrl"));
                    }
                    list.add(copy);
                }
            }
            if (!list.isEmpty()) {
                return list;
            }
        }
        List<Map<String, Object>> fromAssets = new ArrayList<>();
        if (ideaAssets != null) {
            for (Map<String, Object> a : ideaAssets) {
                String type = String.valueOf(a.getOrDefault("assetType", ""));
                if (Boolean.TRUE.equals(a.get("isImage")) && !"LOGO".equalsIgnoreCase(type)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", a.get("id"));
                    item.put("fileUrl", a.get("fileUrl"));
                    item.put("url", a.get("fileUrl"));
                    item.put("originalName", a.get("originalName"));
                    item.put("caption", a.get("caption") != null ? a.get("caption") : a.get("originalName"));
                    item.put("assetType", type);
                    fromAssets.add(item);
                }
            }
        }
        return fromAssets;
    }

    List<Map<String, Object>> resolveDocumentAssets(Map<String, Object> layoutSchema, List<Map<String, Object>> ideaAssets) {
        if (layoutSchema != null && layoutSchema.get("downloadableDocs") instanceof List<?> dl && !dl.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object o : dl) {
                if (o instanceof Map<?, ?> m) {
                    Map<String, Object> copy = new LinkedHashMap<>();
                    m.forEach((k, v) -> copy.put(String.valueOf(k), v));
                    if (!copy.containsKey("fileUrl") && copy.get("url") != null) {
                        copy.put("fileUrl", copy.get("url"));
                    }
                    if (!copy.containsKey("originalName") && copy.get("name") != null) {
                        copy.put("originalName", copy.get("name"));
                    }
                    if (!copy.containsKey("downloadUrl") && copy.get("fileUrl") != null) {
                        String fUrl = String.valueOf(copy.get("fileUrl"));
                        copy.put("downloadUrl", fUrl + (fUrl.contains("?") ? "&download=true" : "?download=true"));
                    }
                    list.add(copy);
                }
            }
            if (!list.isEmpty()) {
                return list;
            }
        }
        List<Map<String, Object>> fromAssets = new ArrayList<>();
        if (ideaAssets != null) {
            for (Map<String, Object> a : ideaAssets) {
                if ("DOC".equalsIgnoreCase(String.valueOf(a.getOrDefault("assetType", "")))) {
                    String fUrl = String.valueOf(a.get("fileUrl"));
                    String origName = String.valueOf(a.getOrDefault("originalName", ""));
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", a.get("id"));
                    item.put("fileUrl", fUrl);
                    item.put("url", fUrl);
                    item.put("downloadUrl", fUrl + (fUrl.contains("?") ? "&download=true" : "?download=true"));
                    item.put("originalName", origName);
                    item.put("name", origName);
                    item.put("fileExt", extractExtension(origName));
                    item.put("caption", a.get("caption") != null ? a.get("caption") : origName);
                    item.put("fileSize", a.get("fileSize"));
                    fromAssets.add(item);
                }
            }
        }
        return fromAssets;
    }

    String buildConciergeQuestionWithContext(String question, String ideaId, Map<String, Object> runtime) {
        StringBuilder ctx = new StringBuilder();
        List<Map<String, Object>> assets = listIdeaAssetsInternal(ideaId);
        for (Map<String, Object> a : assets) {
            if (a.get("extractedText") != null && !String.valueOf(a.get("extractedText")).isBlank()) {
                ctx.append("[참고문서 ").append(a.get("originalName")).append("] ")
                        .append(a.get("extractedText")).append("\n");
            }
        }
        if (ctx.isEmpty()) {
            return question;
        }
        return question + "\n\n(참고 브랜드 자료:\n" + ctx.toString().trim() + ")";
    }
}
