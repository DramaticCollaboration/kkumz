package com.empasy.sync.kkumz.domain;

import com.empasy.sync.kkumz.domain.KkumzDTO.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static com.empasy.sync.kkumz.domain.KkumzJsonAndTextUtils.*;

/**
 * Kkumz 라이브 멀티사이트 런타임(/s/:siteSlug) 렌더링, SyncShop 주문·SyncCMS 폼 연동 및 AI 컨시어지를 담당하는 협력 컴포넌트입니다.
 */
@Slf4j
@RequiredArgsConstructor
class KkumzStorefrontRuntimeSupport {

    private final KkumzService root;

    Map<String, Object> getSiteRuntime(String siteSlug) {
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteSlug);
        String siteId = String.valueOf(site.get("id"));
        String ideaId = String.valueOf(site.get("ideaId"));
        String siteType = String.valueOf(site.get("siteType"));
        int activeVer = site.get("activeVersion") instanceof Number n ? n.intValue() : 1;

        String currentUser = root.accessMeta().resolveCurrentUsernameFromRequest();
        String ownerUser = site.get("ownerUsername") != null ? String.valueOf(site.get("ownerUsername")).trim() : "";
        boolean isOwner = currentUser != null && !currentUser.isBlank() && currentUser.equalsIgnoreCase(ownerUser);
        boolean canEdit = isOwner || (currentUser != null && !currentUser.isBlank() && root.accessMeta().canEditSite(site, currentUser));
        boolean isPublished = root.accessMeta().isSitePubliclyPublished(site);
        boolean canViewAsMember = canEdit || (currentUser != null && !currentUser.isBlank() && root.accessMeta().canViewSite(site, currentUser) && !isPublished);

        if (root.accessMeta().isHttpRequestActive() && !isPublished && !canViewAsMember) {
            throw new SecurityException("[403] 아직 배포되지 않았거나 비공개 상태인 사이트입니다.");
        }

        Integer publishedVer = root.accessMeta().resolvePublishedVersion(site);
        boolean hasUnpublishedChanges = !isPublished || (publishedVer != null && activeVer > publishedVer);
        int targetVer = (!canViewAsMember && isPublished && publishedVer != null && publishedVer > 0)
                ? publishedVer
                : activeVer;
        boolean isPreviewMode = !isPublished || (publishedVer != null && targetVer != publishedVer);

        if (!canViewAsMember) {
            root.jdbcTemplate().update("UPDATE kkumz_site SET view_count = view_count + 1 WHERE id = ?", siteId);
        }

        Map<String, Object> activeRel = root.accessMeta().getLatestRelease(siteId, targetVer);
        List<Map<String, Object>> allReleases = root.jdbcTemplate().queryForList("""
                SELECT id, version_no AS "versionNo", release_note AS "releaseNote", deploy_mode AS "deployMode",
                       build_status AS "buildStatus", created_by AS "createdBy", created_at AS "createdAt"
                FROM kkumz_site_release
                WHERE site_id = ?
                ORDER BY version_no DESC
                """, siteId);

        Map<String, Object> layoutSchema = new LinkedHashMap<>(parseJsonObject(String.valueOf(activeRel.get("layoutSchemaJson"))));
        List<Map<String, Object>> ideaAssets = root.assets().listIdeaAssetsInternal(ideaId);
        List<Map<String, Object>> catalog = root.accessMeta().listFeatureCatalog();

        List<String> enabledFeatures = new ArrayList<>();
        if (layoutSchema.get("enabledFeatures") instanceof List<?> efList) {
            for (Object o : efList) {
                if (o != null && !String.valueOf(o).isBlank()) {
                    enabledFeatures.add(String.valueOf(o).trim());
                }
            }
        }
        if (enabledFeatures.isEmpty() && ideaId != null && !ideaId.isBlank() && !"null".equalsIgnoreCase(ideaId)) {
            try {
                List<Map<String, Object>> ideaRows = root.jdbcTemplate().queryForList(
                        "SELECT enabled_features_json::text AS \"ef\" FROM kkumz_idea WHERE id = ? LIMIT 1",
                        ideaId
                );
                if (!ideaRows.isEmpty()) {
                    enabledFeatures.addAll(parseJsonStringList(String.valueOf(ideaRows.get(0).get("ef"))));
                }
            } catch (Exception ignored) {
            }
        }
        layoutSchema.put("enabledFeatures", enabledFeatures);

        List<Map<String, Object>> activeFeatureCards = new ArrayList<>();
        for (Map<String, Object> fc : catalog) {
            String fCode = String.valueOf(fc.getOrDefault("featureCode", ""));
            if (enabledFeatures.contains(fCode)) {
                activeFeatureCards.add(fc);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> featureBlocks = layoutSchema.get("featureBlocks") instanceof Map<?, ?> fb
                ? new LinkedHashMap<>((Map<String, Object>) fb)
                : new LinkedHashMap<>();

        String siteBrandName = String.valueOf(site.getOrDefault("siteName", "브랜드"));
        String siteBrandSlogan = String.valueOf(site.getOrDefault("brandSlogan", ""));
        String rawCategory = String.valueOf(site.getOrDefault("category", ""));
        String siteCategoryLabel = root.accessMeta().resolveHumanCategoryLabel(rawCategory);
        Map<String, Object> designTokens = new LinkedHashMap<>(parseJsonObject(String.valueOf(activeRel.get("designTokensJson"))));
        List<Map<String, Object>> webideFiles = castMapList(parseJsonList(String.valueOf(activeRel.get("webideFilesJson"))));

        String themeArchetype = resolveThemeArchetype(
                layoutSchema.get("themeArchetype") != null ? String.valueOf(layoutSchema.get("themeArchetype")) : null,
                siteType,
                rawCategory,
                enabledFeatures
        );
        String heroStyle = layoutSchema.get("heroStyle") != null && !String.valueOf(layoutSchema.get("heroStyle")).isBlank()
                ? String.valueOf(layoutSchema.get("heroStyle"))
                : resolveDefaultHeroStyleForArchetype(themeArchetype, siteType);
        String surfaceMode = resolveSurfaceMode(designTokens, themeArchetype);
        String fontPreset = resolveFontPreset(designTokens, themeArchetype);
        designTokens.putIfAbsent("surfaceMode", surfaceMode);
        designTokens.putIfAbsent("fontPreset", fontPreset);
        layoutSchema.putIfAbsent("themeArchetype", themeArchetype);
        layoutSchema.putIfAbsent("heroStyle", heroStyle);

        Map<String, Object> runtime = new LinkedHashMap<>();
        site.put("isOwner", isOwner);
        site.put("canEdit", canEdit);
        site.put("isPublished", isPublished);
        site.put("publishedVersion", publishedVer);
        site.put("hasUnpublishedChanges", hasUnpublishedChanges);
        site.put("renderedVersion", targetVer);
        site.put("isPreviewMode", isPreviewMode);
        site.put("categoryLabel", siteCategoryLabel);
        runtime.putAll(site);
        runtime.put("isOwner", isOwner);
        runtime.put("canEdit", canEdit);
        runtime.put("isPublished", isPublished);
        runtime.put("publishedVersion", publishedVer);
        runtime.put("publishedAt", site.get("publishedAt"));
        runtime.put("hasUnpublishedChanges", hasUnpublishedChanges);
        runtime.put("renderedVersion", targetVer);
        runtime.put("isPreviewMode", isPreviewMode);
        runtime.put("brandName", site.get("siteName"));
        runtime.put("tagline", site.get("brandSlogan"));
        runtime.put("categoryLabel", siteCategoryLabel);
        runtime.put("site", site);
        runtime.put("activeVersion", activeVer);
        runtime.put("deployMode", activeRel.get("deployMode"));
        runtime.put("releaseNote", activeRel.get("releaseNote"));
        runtime.put("designTokens", designTokens);
        runtime.put("layoutSchema", layoutSchema);
        runtime.put("themeArchetype", themeArchetype);
        runtime.put("surfaceMode", surfaceMode);
        runtime.put("fontPreset", fontPreset);
        runtime.put("heroStyle", heroStyle);
        runtime.put("copyOverrides", layoutSchema.get("copyOverrides") instanceof Map<?, ?> co ? co : Map.of());
        runtime.put("sectionOrder", layoutSchema.get("sectionOrder") instanceof List<?> so ? so : List.of());
        runtime.put("customCss", extractSanitizedCustomCss(webideFiles));
        runtime.put("enabledFeatures", enabledFeatures);
        runtime.put("activeFeatureCards", activeFeatureCards);
        runtime.put("webideFiles", webideFiles);
        runtime.put("releases", allReleases);
        runtime.put("screenSpecs", root.ideaSpec().listIdeaScreensInternal(ideaId));
        runtime.put("assets", ideaAssets);
        runtime.put("products", List.of());
        runtime.put("categories", List.of());
        runtime.put("coupons", List.of());
        runtime.put("articles", List.of());
        runtime.put("banners", List.of());
        runtime.put("attributes", List.of());
        runtime.put("skus", List.of());
        runtime.put("brandProfile", Map.of());
        runtime.put("companyAddress", Map.of());

        String resolvedSlug = String.valueOf(site.get("siteSlug"));
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;

        Map<String, Object> commerceData = Map.of();
        if (shopSiteId != null || "COMMERCE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)
                || hasEngineFeature(enabledFeatures, catalog, "SYNC_SHOP")) {
            commerceData = loadCommerceData(shopSiteId, resolvedSlug);
            runtime.put("commerce", commerceData);
            List<Map<String, Object>> skuList = commerceData.get("skus") instanceof List<?> sl
                    ? castMapList(sl)
                    : List.of();
            runtime.put("skus", skuList);
            if (commerceData.get("products") instanceof List<?> pList) {
                runtime.put("products", attachSkusToProducts(castMapList(pList), skuList));
            }
            if (commerceData.get("categories") instanceof List<?> cList) {
                runtime.put("categories", cList);
            }
            if (commerceData.get("coupons") instanceof List<?> cpList) {
                runtime.put("coupons", cpList);
            }
            if (commerceData.get("attributes") instanceof List<?> attrList) {
                runtime.put("attributes", attrList);
            }
            if (commerceData.get("brandProfile") instanceof Map<?, ?> bp) {
                runtime.put("brandProfile", bp);
            }
            if (commerceData.get("companyAddress") instanceof Map<?, ?> ca) {
                runtime.put("companyAddress", ca);
            }
        }

        if (cmsSiteId != null || "CMS_SITE".equalsIgnoreCase(siteType) || "HYBRID".equalsIgnoreCase(siteType)
                || hasEngineFeature(enabledFeatures, catalog, "SYNC_CMS")) {
            Map<String, Object> cmsData = loadCmsData(cmsSiteId, resolvedSlug);
            runtime.put("cms", cmsData);
            if (cmsData.get("articles") instanceof List<?> aList) {
                runtime.put("articles", aList);
            }
            if (cmsData.get("banners") instanceof List<?> bList) {
                runtime.put("banners", bList);
            }
        }

        if (layoutSchema.get("customProducts") instanceof List<?> cp && !cp.isEmpty()) {
            runtime.put("products", cp);
        }
        if (layoutSchema.get("customArticles") instanceof List<?> ca && !ca.isEmpty()) {
            runtime.put("articles", ca);
        } else if (runtime.get("articles") instanceof List<?> existingArts && existingArts.isEmpty()
                && commerceData.get("subjects") instanceof List<?> subjectList && !subjectList.isEmpty()) {
            runtime.put("articles", subjectList);
        }

        hydrateFeatureBlocksStrict(
                siteBrandName,
                siteBrandSlogan,
                rawCategory,
                enabledFeatures,
                ideaAssets,
                commerceData,
                featureBlocks
        );
        layoutSchema.put("featureBlocks", featureBlocks);
        runtime.put("featureBlocks", featureBlocks);

        boolean allowForm = !Boolean.FALSE.equals(layoutSchema.get("showCmsForm"))
                && (layoutSchema.get("customCmsForm") instanceof Map<?, ?>
                || (runtime.get("cms") instanceof Map<?, ?> cm && cm.get("forms") instanceof List<?> fl && !fl.isEmpty())
                || hasAnyFormFeature(enabledFeatures));
        if (allowForm) {
            if (layoutSchema.get("customCmsForm") instanceof Map<?, ?> cf && !cf.isEmpty()) {
                runtime.put("cmsForm", new LinkedHashMap<>((Map<String, Object>) cf));
            } else if (runtime.get("cms") instanceof Map<?, ?> cmsMap && cmsMap.get("forms") instanceof List<?> fList && !fList.isEmpty()) {
                runtime.put("cmsForm", new LinkedHashMap<>((Map<String, Object>) fList.get(0)));
            }
        }
        runtime.put("galleryImages", root.assets().resolveGalleryAssets(layoutSchema, ideaAssets));
        runtime.put("downloadableDocs", root.assets().resolveDocumentAssets(layoutSchema, ideaAssets));

        runtime.put("recruits", root.communityAdmin().listSiteRecruitsInternal(siteId));
        runtime.put("teamMembers", root.communityAdmin().listSiteTeamMembersInternal(siteId));
        runtime.put("comments", root.communityAdmin().listSiteCommentsInternal(siteId));

        return runtime;
    }

    Map<String, Object> createCommerceOrder(String siteSlug, CommerceOrderRequest req) {
        if (req == null || req.getProductId() == null) {
            throw new IllegalArgumentException("주문 상품 ID(productId)는 필수입니다.");
        }
        if (req.getReceiverName() == null || req.getReceiverName().isBlank()) {
            throw new IllegalArgumentException("수령인 성함(receiverName)은 필수입니다.");
        }
        if (req.getReceiverPhone() == null || req.getReceiverPhone().isBlank()) {
            throw new IllegalArgumentException("수령인 연락처(receiverPhone)는 필수입니다.");
        }
        if (req.getReceiverAddress() == null || req.getReceiverAddress().isBlank()) {
            throw new IllegalArgumentException("배송지 주소(receiverAddress)는 필수입니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteSlug);
        root.accessMeta().assertSiteReadable(site);
        Long shopSiteId = site.get("shopSiteId") != null ? ((Number) site.get("shopSiteId")).longValue() : null;
        if (shopSiteId == null) {
            throw new IllegalStateException("해당 사이트에 매핑된 SyncShop 스토어 ID(shopSiteId)가 없습니다: " + siteSlug);
        }

        int quantity = (req.getQuantity() != null && req.getQuantity() > 0) ? req.getQuantity() : 1;
        String receiverName = req.getReceiverName().trim();
        String receiverPhone = req.getReceiverPhone().trim();
        String receiverAddress = req.getReceiverAddress().trim();
        String resolvedSlug = String.valueOf(site.get("siteSlug"));

        if (root.syncServiceApiClient() != null) {
            try {
                Map<String, Object> orderPayload = new LinkedHashMap<>();
                orderPayload.put("productId", req.getProductId());
                orderPayload.put("quantity", quantity);
                orderPayload.put("receiverName", receiverName);
                orderPayload.put("receiverPhone", receiverPhone);
                orderPayload.put("receiverAddress", receiverAddress);
                orderPayload.put("note", req.getNote() != null ? req.getNote() : "");
                if (req.getDiscountAmount() != null) {
                    orderPayload.put("discountAmount", req.getDiscountAmount());
                }
                Map<String, Object> apiResult = root.syncServiceApiClient().createShopOrder(shopSiteId, resolvedSlug, orderPayload);
                String orderSn = String.valueOf(apiResult.getOrDefault("orderSn", "KKUMZ-" + Instant.now().toEpochMilli()));
                String productName = String.valueOf(apiResult.getOrDefault("productName", "브랜드 상품"));
                Object payAmount = apiResult.getOrDefault("payAmount", 0);

                root.accessMeta().createNotification(
                        String.valueOf(site.get("ownerUsername")),
                        "COMMERCE_ORDER",
                        "[" + site.get("siteName") + "] 신규 주문 접수 (" + orderSn + ")",
                        receiverName + " 고객님이 '" + productName + "' " + quantity + "건(" + payAmount + "원)을 주문했습니다.",
                        "/s/" + resolvedSlug
                );
                apiResult.put("shopSiteId", shopSiteId);
                apiResult.putIfAbsent("status", "PAID_READY_FOR_SHIPMENT");
                apiResult.putIfAbsent("orderedAt", Timestamp.from(Instant.now()));
                return apiResult;
            } catch (Exception e) {
                log.warn("SyncShop 주문 REST API 호출 실패, 폴백 검증 진행: {}", e.getMessage());
                if (root.shopJdbcTemplate() == null) {
                    throw new IllegalStateException("SyncShop 주문 API 연동 오류: " + e.getMessage(), e);
                }
            }
        }

        if (root.shopJdbcTemplate() == null) {
            throw new IllegalStateException("SyncShop 서비스 API 클라이언트 또는 데이터베이스 연결이 초기화되지 않았습니다.");
        }

        String orderSn = "KKUMZ-" + Instant.now().toEpochMilli();

        List<Map<String, Object>> prodRows = root.shopJdbcTemplate().queryForList("""
                SELECT id, name, pic, product_sn, brand_name, product_category_id, price, promotion_price, stock
                FROM pms_product WHERE id = ? AND site_id = ? AND delete_status = 0
                """, req.getProductId(), shopSiteId);

        if (prodRows.isEmpty()) {
            throw new IllegalArgumentException("주문 대상 상품이 존재하지 않습니다: productId=" + req.getProductId() + ", shopSiteId=" + shopSiteId);
        }

        Map<String, Object> prod = prodRows.get(0);
        Long resolvedProductId = ((Number) prod.get("id")).longValue();
        String productName = String.valueOf(prod.get("name"));
        String productPic = String.valueOf(prod.getOrDefault("pic", ""));
        String productSn = String.valueOf(prod.getOrDefault("product_sn", ""));
        String brandName = String.valueOf(prod.getOrDefault("brand_name", site.get("siteName")));
        Long categoryId = prod.get("product_category_id") != null ? ((Number) prod.get("product_category_id")).longValue() : null;
        BigDecimal promo = (BigDecimal) prod.get("promotion_price");
        BigDecimal base = (BigDecimal) prod.get("price");
        BigDecimal unitPrice = (promo != null && promo.compareTo(BigDecimal.ZERO) > 0) ? promo : base;
        int currentStock = prod.get("stock") != null ? ((Number) prod.get("stock")).intValue() : 0;
        if (currentStock < quantity) {
            throw new IllegalStateException("상품 재고가 부족합니다: 현재 재고=" + currentStock + ", 요청 수량=" + quantity);
        }

        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal requestedDiscount = req.getDiscountAmount() != null ? req.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal appliedDiscount = requestedDiscount.compareTo(BigDecimal.ZERO) > 0
                ? requestedDiscount.min(totalAmount)
                : BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.subtract(appliedDiscount);
        Long orderId = root.shopJdbcTemplate().queryForObject("""
                INSERT INTO oms_order (
                    site_id, member_id, order_sn, create_time, member_username,
                    total_amount, pay_amount, freight_amount, promotion_amount, integration_amount, coupon_amount, discount_amount,
                    pay_type, source_type, status, order_type,
                    receiver_name, receiver_phone, receiver_detail_address, note, confirm_status, delete_status
                ) VALUES (?, 1001, ?, CURRENT_TIMESTAMP, ?, ?, ?, 0.00, 0.00, 0.00, ?, ?, 1, 1, 1, 0, ?, ?, ?, ?, 0, 0)
                RETURNING id
                """,
                Long.class,
                shopSiteId, orderSn, receiverName, totalAmount, payAmount, appliedDiscount, appliedDiscount,
                receiverName, receiverPhone, receiverAddress,
                req.getNote() != null ? req.getNote() : ""
        );

        root.shopJdbcTemplate().update("""
                INSERT INTO oms_order_item (
                    order_id, order_sn, product_id, product_pic, product_name, product_brand,
                    product_sn, product_price, product_quantity, product_category_id, real_amount
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                orderId, orderSn, resolvedProductId, productPic, productName, brandName,
                productSn, unitPrice, quantity, categoryId, payAmount
        );

        root.shopJdbcTemplate().update("""
                UPDATE pms_product SET stock = GREATEST(0, stock - ?), sale = sale + ?
                WHERE id = ?
                """,
                quantity, quantity, resolvedProductId
        );

        int remainingStock = Math.max(0, currentStock - quantity);

        root.accessMeta().createNotification(
                String.valueOf(site.get("ownerUsername")),
                "COMMERCE_ORDER",
                "[" + site.get("siteName") + "] 신규 주문 접수 (" + orderSn + ")",
                receiverName + " 고객님이 '" + productName + "' " + quantity + "건(" + payAmount + "원)을 주문했습니다.",
                "/s/" + site.get("siteSlug")
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("orderSn", orderSn);
        result.put("shopSiteId", shopSiteId);
        result.put("productId", resolvedProductId);
        result.put("productName", productName);
        result.put("quantity", quantity);
        result.put("unitPrice", unitPrice);
        result.put("totalAmount", totalAmount);
        result.put("discountAmount", appliedDiscount);
        result.put("payAmount", payAmount);
        result.put("remainingStock", remainingStock);
        result.put("receiverName", receiverName);
        result.put("note", req.getNote() != null ? req.getNote() : "");
        result.put("status", "PAID_READY_FOR_SHIPMENT");
        result.put("orderedAt", Timestamp.from(Instant.now()));
        return result;
    }

    Map<String, Object> submitCmsForm(String siteSlug, CmsFormSubmitRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("신청 폼 요청 데이터(req)가 비어 있습니다.");
        }
        String applicant = (req.getApplicantName() != null && !req.getApplicantName().isBlank())
                ? req.getApplicantName().trim()
                : (req.getExtraFields() != null && req.getExtraFields().get("applicant_name") != null
                ? String.valueOf(req.getExtraFields().get("applicant_name")).trim()
                : "");
        if (applicant.isBlank()) {
            throw new IllegalArgumentException("신청자 성함(applicantName)은 필수입니다.");
        }
        String phone = (req.getPhone() != null && !req.getPhone().isBlank())
                ? req.getPhone().trim()
                : (req.getExtraFields() != null && req.getExtraFields().get("phone") != null
                ? String.valueOf(req.getExtraFields().get("phone")).trim()
                : "");
        if (phone.isBlank()) {
            throw new IllegalArgumentException("신청자 연락처(phone)는 필수입니다.");
        }
        Map<String, Object> site = root.accessMeta().findSiteByIdOrSlug(siteSlug);
        root.accessMeta().assertSiteReadable(site);
        Long cmsSiteId = site.get("cmsSiteId") != null ? ((Number) site.get("cmsSiteId")).longValue() : null;
        if (cmsSiteId == null) {
            throw new IllegalStateException("해당 사이트에 매핑된 SyncCMS 사이트 ID(cmsSiteId)가 없습니다: " + siteSlug);
        }
        String resolvedSlug = String.valueOf(site.get("siteSlug"));

        Map<String, Object> payload = new LinkedHashMap<>();
        if (req.getExtraFields() != null) {
            payload.putAll(req.getExtraFields());
        }
        payload.put("applicantName", applicant);
        payload.put("applicant_name", applicant);
        payload.put("phone", phone);
        if (req.getFormId() != null) {
            payload.put("formId", req.getFormId());
        }
        if (req.getOptionValue() != null && !req.getOptionValue().isBlank()) {
            payload.put("optionValue", req.getOptionValue().trim());
            payload.put("option_value", req.getOptionValue().trim());
        }
        if (req.getMessage() != null && !req.getMessage().isBlank()) {
            payload.put("message", req.getMessage().trim());
        }

        if (root.syncServiceApiClient() != null) {
            try {
                Map<String, Object> apiResult = root.syncServiceApiClient().submitCmsForm(cmsSiteId, resolvedSlug, payload);
                root.accessMeta().createNotification(
                        String.valueOf(site.get("ownerUsername")),
                        "CMS_FORM_SUBMIT",
                        "[" + site.get("siteName") + "] 신규 온라인 신청 접수",
                        applicant + "님이 참가/예약 신청서를 제출했습니다.",
                        "/s/" + resolvedSlug
                );
                apiResult.put("cmsSiteId", cmsSiteId);
                apiResult.put("applicantName", applicant);
                apiResult.put("submittedData", payload);
                apiResult.putIfAbsent("status", "RECEIVED");
                apiResult.putIfAbsent("submittedAt", Timestamp.from(Instant.now()));
                return apiResult;
            } catch (Exception e) {
                log.warn("SyncCMS 폼 접수 REST API 호출 실패, 폴백 검증 진행: {}", e.getMessage());
                if (root.cmsJdbcTemplate() == null) {
                    throw new IllegalStateException("SyncCMS 폼 접수 API 연동 오류: " + e.getMessage(), e);
                }
            }
        }

        if (root.cmsJdbcTemplate() == null) {
            throw new IllegalStateException("SyncCMS 서비스 API 클라이언트 또는 데이터베이스 연결이 초기화되지 않았습니다.");
        }

        Long formId = req.getFormId();
        if (formId == null) {
            List<Map<String, Object>> forms = root.cmsJdbcTemplate().queryForList(
                    "SELECT id FROM cms_form WHERE site_id = ? AND status = 1 ORDER BY id ASC LIMIT 1",
                    cmsSiteId
            );
            if (forms.isEmpty()) {
                throw new IllegalStateException("해당 사이트에 활성화된 CMS 신청 폼이 존재하지 않습니다: cmsSiteId=" + cmsSiteId);
            }
            formId = ((Number) forms.get(0).get("id")).longValue();
        }

        Long submissionId = root.cmsJdbcTemplate().queryForObject("""
                INSERT INTO cms_form_data (
                    form_id, submission_data, client_ip, status, create_date
                ) VALUES (?, ?::jsonb, '127.0.0.1', 'RECEIVED', CURRENT_TIMESTAMP)
                RETURNING id
                """,
                Long.class,
                formId, toJson(payload)
        );

        Integer cnt = root.cmsJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM cms_form_data WHERE form_id = ?",
                Integer.class, formId
        );
        int totalSubmissions = cnt != null ? cnt : 1;

        root.accessMeta().createNotification(
                String.valueOf(site.get("ownerUsername")),
                "CMS_FORM_SUBMIT",
                "[" + site.get("siteName") + "] 신규 온라인 신청 접수",
                applicant + "님이 참가/예약 신청서를 제출했습니다.",
                "/s/" + site.get("siteSlug")
        );

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("submissionId", submissionId);
        res.put("formId", formId);
        res.put("cmsSiteId", cmsSiteId);
        res.put("applicantName", applicant);
        res.put("submittedData", payload);
        res.put("totalSubmissions", totalSubmissions);
        res.put("status", "RECEIVED");
        res.put("submittedAt", Timestamp.from(Instant.now()));
        return res;
    }

    Map<String, Object> askSiteConcierge(String siteSlug, SiteConciergeRequest req) {
        if (req == null || req.getQuestion() == null || req.getQuestion().isBlank()) {
            throw new IllegalArgumentException("질문 내용(question)은 필수입니다.");
        }
        Map<String, Object> runtime = getSiteRuntime(siteSlug);
        @SuppressWarnings("unchecked")
        Map<String, Object> site = (Map<String, Object>) runtime.get("site");
        String siteName = String.valueOf(site.get("siteName"));
        String slogan = String.valueOf(site.get("brandSlogan"));
        String ideaId = String.valueOf(site.get("ideaId"));
        String question = req.getQuestion().trim();
        String enrichedQuestion = root.assets().buildConciergeQuestionWithContext(question, ideaId, runtime);

        String answer = root.ensureAiGateway().executeStagePrompt("CONCIERGE", Map.of(
                "siteName", siteName,
                "brandSlogan", slogan,
                "question", enrichedQuestion
        ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("siteSlug", siteSlug);
        response.put("siteName", siteName);
        response.put("question", question);
        response.put("answer", answer);
        response.put("answeredAt", Timestamp.from(Instant.now()));
        return response;
    }

    private Map<String, Object> loadCommerceData(Long shopSiteId) {
        return loadCommerceData(shopSiteId, null);
    }

    private Map<String, Object> loadCommerceData(Long shopSiteId, String siteSlug) {
        if (shopSiteId == null && (siteSlug == null || siteSlug.isBlank())) {
            return Map.of(
                    "shopSiteId", 0L,
                    "products", List.of(),
                    "banners", List.of(),
                    "categories", List.of(),
                    "coupons", List.of()
            );
        }
        if (root.syncServiceApiClient() != null) {
            try {
                Map<String, Object> apiData = root.syncServiceApiClient().fetchShopBootstrap(shopSiteId, siteSlug);
                if (apiData != null && !apiData.isEmpty()) {
                    apiData.putIfAbsent("shopSiteId", shopSiteId != null ? shopSiteId : 0L);
                    apiData.putIfAbsent("products", List.of());
                    apiData.putIfAbsent("banners", List.of());
                    apiData.putIfAbsent("categories", List.of());
                    apiData.putIfAbsent("coupons", List.of());
                    return apiData;
                }
            } catch (Exception e) {
                log.warn("SyncShop 부트스트랩 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (shopSiteId == null || root.shopJdbcTemplate() == null) {
            return Map.of(
                    "shopSiteId", 0L,
                    "products", List.of(),
                    "banners", List.of(),
                    "categories", List.of(),
                    "coupons", List.of()
            );
        }
        List<Map<String, Object>> products = root.shopJdbcTemplate().queryForList("""
                SELECT id,
                       site_id AS "siteId",
                       name,
                       sub_title AS "subTitle",
                       description,
                       pic,
                       product_sn AS "productSn",
                       brand_name AS "brandName",
                       product_category_id AS "productCategoryId",
                       product_category_name AS "productCategoryName",
                       product_category_name AS "categoryName",
                       price,
                       promotion_price AS "promotionPrice",
                       original_price AS "originalPrice",
                       stock,
                       sale,
                       unit,
                       note,
                       keywords,
                       album_pics AS "albumPics",
                       detail_title AS "detailTitle",
                       detail_desc AS "detailDesc",
                       detail_html AS "detailHtml"
                FROM pms_product
                WHERE site_id = ? AND delete_status = 0 AND publish_status = 1
                ORDER BY sort DESC, id ASC
                """, shopSiteId);

        List<Map<String, Object>> banners = root.shopJdbcTemplate().queryForList("""
                SELECT id, name, pic, url, note
                FROM sms_home_advertise
                WHERE site_id = ? AND status = 1
                ORDER BY sort DESC, id ASC
                """, shopSiteId);

        List<Map<String, Object>> categories = root.shopJdbcTemplate().queryForList("""
                SELECT id, name, icon, product_count AS "productCount"
                FROM pms_product_category
                WHERE site_id = ? AND show_status = 1
                ORDER BY sort DESC, id ASC
                """, shopSiteId);

        List<Map<String, Object>> coupons = root.shopJdbcTemplate().queryForList("""
                SELECT id, name, amount, min_point AS "minPoint", code, note
                FROM sms_coupon
                WHERE site_id = ?
                ORDER BY id ASC
                """, shopSiteId);

        List<Map<String, Object>> reviews = root.shopJdbcTemplate().queryForList("""
                SELECT id,
                       product_id AS "productId",
                       member_nick_name AS "author",
                       product_name AS "productName",
                       star AS "rating",
                       product_attribute AS "tag",
                       content AS "comment",
                       content,
                       to_char(create_time, 'YYYY.MM') AS "date",
                       issue_tags_json::text AS "issueTagsJson"
                FROM pms_comment
                WHERE site_id = ? AND show_status = 1
                ORDER BY create_time DESC, id DESC
                LIMIT 12
                """, shopSiteId);

        List<Map<String, Object>> faqs = root.shopJdbcTemplate().queryForList("""
                SELECT id,
                       title AS "q",
                       content AS "a",
                       read_count AS "readCount"
                FROM cms_help
                WHERE site_id = ? AND show_status = 1
                ORDER BY id ASC
                LIMIT 12
                """, shopSiteId);

        List<Map<String, Object>> subjects = root.shopJdbcTemplate().queryForList("""
                SELECT id,
                       title,
                       pic AS "cover",
                       pic AS "thumbnailUrl",
                       description AS "summary",
                       description,
                       content AS "bodyHtml",
                       category_name AS "categoryName",
                       read_count AS "clicks"
                FROM cms_subject
                WHERE site_id = ? AND show_status = 1
                ORDER BY id ASC
                LIMIT 8
                """, shopSiteId);

        List<Map<String, Object>> attributes = root.shopJdbcTemplate().queryForList("""
                SELECT id,
                       product_attribute_category_id AS "categoryId",
                       name,
                       input_list AS "inputList",
                       type,
                       sort
                FROM pms_product_attribute
                WHERE site_id = ?
                ORDER BY sort DESC, id ASC
                LIMIT 24
                """, shopSiteId);

        List<Map<String, Object>> skus = root.shopJdbcTemplate().queryForList("""
                SELECT s.id,
                       s.product_id AS "productId",
                       s.sku_code AS "skuCode",
                       s.price,
                       s.promotion_price AS "promotionPrice",
                       s.stock,
                       s.pic,
                       s.sp_data AS "spData"
                FROM pms_sku_stock s
                JOIN pms_product p ON p.id = s.product_id
                WHERE p.site_id = ? AND p.delete_status = 0
                ORDER BY s.id ASC
                LIMIT 48
                """, shopSiteId);

        List<Map<String, Object>> brandRows = root.shopJdbcTemplate().queryForList("""
                SELECT id, name, logo, big_pic AS "bigPic", brand_story AS "brandStory"
                FROM pms_brand
                WHERE site_id = ? AND show_status = 1
                ORDER BY sort DESC, id ASC
                LIMIT 1
                """, shopSiteId);

        List<Map<String, Object>> addrRows = root.shopJdbcTemplate().queryForList("""
                SELECT address_name AS "addressName",
                       name AS "contactName",
                       phone,
                       province,
                       city,
                       region,
                       detail_address AS "detailAddress"
                FROM oms_company_address
                WHERE site_id = ?
                ORDER BY id ASC
                LIMIT 1
                """, shopSiteId);

        Map<String, Object> commerce = new LinkedHashMap<>();
        commerce.put("shopSiteId", shopSiteId);
        commerce.put("products", products);
        commerce.put("banners", banners);
        commerce.put("categories", categories);
        commerce.put("coupons", coupons);
        commerce.put("reviews", reviews);
        commerce.put("faqs", faqs);
        commerce.put("subjects", subjects);
        commerce.put("attributes", attributes);
        commerce.put("skus", skus);
        commerce.put("brandProfile", brandRows.isEmpty() ? Map.of() : brandRows.get(0));
        commerce.put("companyAddress", addrRows.isEmpty() ? Map.of() : addrRows.get(0));
        return commerce;
    }

    private Map<String, Object> loadCmsData(Long cmsSiteId) {
        return loadCmsData(cmsSiteId, null);
    }

    private Map<String, Object> loadCmsData(Long cmsSiteId, String siteSlug) {
        if (cmsSiteId == null && (siteSlug == null || siteSlug.isBlank())) {
            return Map.of(
                    "cmsSiteId", 0L,
                    "articles", List.of(),
                    "banners", List.of(),
                    "forms", List.of()
            );
        }
        if (root.syncServiceApiClient() != null) {
            try {
                Map<String, Object> apiData = root.syncServiceApiClient().fetchCmsBootstrap(cmsSiteId, siteSlug);
                if (apiData != null && !apiData.isEmpty()) {
                    apiData.putIfAbsent("cmsSiteId", cmsSiteId != null ? cmsSiteId : 0L);
                    apiData.putIfAbsent("articles", List.of());
                    apiData.putIfAbsent("banners", List.of());
                    apiData.putIfAbsent("forms", List.of());
                    return apiData;
                }
            } catch (Exception e) {
                log.warn("SyncCMS 부트스트랩 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (cmsSiteId == null || root.cmsJdbcTemplate() == null) {
            return Map.of(
                    "cmsSiteId", 0L,
                    "articles", List.of(),
                    "banners", List.of(),
                    "forms", List.of()
            );
        }
        List<Map<String, Object>> articles = root.cmsJdbcTemplate().queryForList("""
                SELECT c.id,
                       c.site_id AS "siteId",
                       c.title,
                       c.description,
                       c.description AS "summary",
                       COALESCE(a.text, c.description) AS "bodyHtml",
                       COALESCE(a.text, c.description) AS "contentHtml",
                       c.cover,
                       c.cover AS "thumbnailUrl",
                       c.author,
                       c.author AS "authorName",
                       cat.name AS "categoryName",
                       c.clicks,
                       c.clicks AS "viewCount",
                       c.publish_date AS "publishedTime"
                FROM cms_content c
                LEFT JOIN cms_content_attribute a ON a.content_id = c.id
                LEFT JOIN cms_category cat ON cat.id = c.category_id
                WHERE c.site_id = ? AND c.status = 1 AND c.disabled = FALSE
                ORDER BY c.sort DESC, c.publish_date DESC, c.id ASC
                """, cmsSiteId);

        List<Map<String, Object>> banners = root.cmsJdbcTemplate().queryForList("""
                SELECT id,
                       title,
                       sub_title AS "subTitle",
                       image_url AS "imageUrl",
                       link_url AS "linkUrl",
                       zone_code AS "position"
                FROM cms_banner
                WHERE site_id = ? AND status = 1
                ORDER BY sort DESC, id ASC
                """, cmsSiteId);

        List<Map<String, Object>> forms = root.cmsJdbcTemplate().queryForList("""
                SELECT id,
                       title,
                       title AS "formName",
                       code,
                       code AS "formCode",
                       description,
                       schema_json::text AS "fieldsJson"
                FROM cms_form
                WHERE site_id = ? AND status = 1
                ORDER BY id ASC
                """, cmsSiteId);
        forms.forEach(f -> f.put("fields", parseJsonList(String.valueOf(f.remove("fieldsJson")))));

        Map<String, Object> cms = new LinkedHashMap<>();
        cms.put("cmsSiteId", cmsSiteId);
        cms.put("articles", articles);
        cms.put("banners", banners);
        cms.put("forms", forms);
        return cms;
    }

    Long provisionSyncShopSite(String siteSlug, String brandName, String slogan) {
        if (root.syncServiceApiClient() != null) {
            try {
                Map<String, Object> apiRes = root.syncServiceApiClient().provisionShopSite(
                        siteSlug, brandName, slogan, "#4F46E5", "SIGNATURE"
                );
                if (apiRes != null && apiRes.get("shopSiteId") instanceof Number num) {
                    return num.longValue();
                }
            } catch (Exception e) {
                log.warn("SyncShop 스토어 프로비저닝 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (root.shopJdbcTemplate() == null) {
            return null;
        }
        List<Map<String, Object>> existing = root.shopJdbcTemplate().queryForList(
                "SELECT id FROM shop_site WHERE site_slug = ? LIMIT 1", siteSlug
        );
        Long shopSiteId;
        if (!existing.isEmpty()) {
            shopSiteId = ((Number) existing.get(0).get("id")).longValue();
        } else {
            String safeName = brandName != null && brandName.length() > 120 ? brandName.substring(0, 120) : brandName;
            String safeSlogan = slogan != null && slogan.length() > 250 ? slogan.substring(0, 250) : slogan;
            shopSiteId = root.shopJdbcTemplate().queryForObject("""
                    INSERT INTO shop_site (site_slug, site_name, site_type, brand_slogan, status, create_time, update_time)
                    VALUES (?, ?, 'COMMERCE', ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    RETURNING id
                    """,
                    Long.class,
                    siteSlug, safeName, safeSlogan
            );
        }
        if (shopSiteId == null) {
            return null;
        }

        List<Map<String, Object>> existingCats = root.shopJdbcTemplate().queryForList(
                "SELECT id, name FROM pms_product_category WHERE site_id = ? ORDER BY id ASC LIMIT 1",
                shopSiteId
        );
        Long defaultCategoryId;
        String defaultCategoryName = "시그니처 컬렉션";
        if (!existingCats.isEmpty()) {
            defaultCategoryId = ((Number) existingCats.get(0).get("id")).longValue();
            if (existingCats.get(0).get("name") != null) {
                defaultCategoryName = String.valueOf(existingCats.get(0).get("name"));
            }
        } else {
            defaultCategoryId = root.shopJdbcTemplate().queryForObject("""
                    INSERT INTO pms_product_category (
                        site_id, parent_id, name, level, product_count, product_unit, nav_status, show_status, sort, description
                    ) VALUES (?, 0, ?, 0, 2, '개', 1, 1, 1, ?)
                    RETURNING id
                    """,
                    Long.class,
                    shopSiteId, defaultCategoryName, brandName + " 대표 시그니처 상품 라인업"
            );
        }

        Integer couponCount = root.shopJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM sms_coupon WHERE site_id = ?",
                Integer.class, shopSiteId
        );
        if (couponCount == null || couponCount == 0) {
            root.shopJdbcTemplate().update("""
                    INSERT INTO sms_coupon (
                        site_id, type, name, platform, count, amount, per_limit, min_point,
                        start_time, end_time, use_type, note, publish_count, use_count, receive_count, code
                    ) VALUES (?, 0, ?, 0, 500, 5000.00, 1, 30000.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '365 days', 0, '신규 오픈 기념 즉시 할인', 500, 0, 0, ?)
                    """,
                    shopSiteId, brandName + " 웰컴 5,000원 할인 쿠폰", "WELCOME-" + shopSiteId
            );
        }

        return shopSiteId;
    }

    Long provisionSyncCmsSite(String siteSlug, String brandName, String slogan) {
        if (root.syncServiceApiClient() != null) {
            try {
                Map<String, Object> apiRes = root.syncServiceApiClient().provisionCmsSite(siteSlug, brandName, slogan);
                if (apiRes != null && apiRes.get("cmsSiteId") instanceof Number num) {
                    return num.longValue();
                }
            } catch (Exception e) {
                log.warn("SyncCMS 멀티사이트 프로비저닝 REST API 호출 실패, 로컬 어댑터 폴백 수행: {}", e.getMessage());
            }
        }
        if (root.cmsJdbcTemplate() == null) {
            return null;
        }
        String sitePath = "/synccms/" + siteSlug + "/";
        String dynamicPath = "/s/" + siteSlug + "/";
        List<Map<String, Object>> existing = root.cmsJdbcTemplate().queryForList(
                "SELECT id FROM sys_site WHERE site_path = ? OR dynamic_path = ? OR name = ? ORDER BY id ASC LIMIT 1",
                sitePath, dynamicPath, brandName
        );
        Long cmsSiteId;
        if (!existing.isEmpty()) {
            cmsSiteId = ((Number) existing.get(0).get("id")).longValue();
        } else {
            Integer nextId = root.cmsJdbcTemplate().queryForObject("""
                    SELECT GREATEST(
                        COALESCE((SELECT MAX(id) FROM sys_site), 100),
                        COALESCE((SELECT MAX(site_id) FROM cms_category), 102)
                    ) + 1
                    """, Integer.class);
            short resolvedSiteId = nextId != null ? nextId.shortValue() : (short) 103;
            String safeName = brandName != null && brandName.length() > 50 ? brandName.substring(0, 50) : brandName;
            cmsSiteId = root.cmsJdbcTemplate().queryForObject("""
                    INSERT INTO sys_site (id, parent_id, name, use_static, site_path, use_ssi, dynamic_path, disabled)
                    VALUES (?, 1, ?, FALSE, ?, FALSE, ?, FALSE)
                    RETURNING id
                    """,
                    Long.class,
                    resolvedSiteId, safeName, sitePath, dynamicPath
            );
        }
        if (cmsSiteId == null) {
            return null;
        }

        short siteIdShort = cmsSiteId.shortValue();
        List<Map<String, Object>> existingCat = root.cmsJdbcTemplate().queryForList(
                "SELECT id FROM cms_category WHERE site_id = ? ORDER BY id ASC LIMIT 1",
                siteIdShort
        );
        Integer categoryId;
        if (!existingCat.isEmpty()) {
            categoryId = ((Number) existingCat.get(0).get("id")).intValue();
        } else {
            String safeCatCode = siteSlug.length() > 40 ? siteSlug.substring(0, 40) + "-news" : siteSlug + "-news";
            categoryId = root.cmsJdbcTemplate().queryForObject("""
                    INSERT INTO cms_category (
                        site_id, name, code, only_url, has_static, contain_child, allow_contribute, sort, hidden, disabled
                    ) VALUES (?, '브랜드 스토리', ?, FALSE, FALSE, TRUE, TRUE, 1, FALSE, FALSE)
                    RETURNING id
                    """,
                    Integer.class,
                    siteIdShort, safeCatCode
            );
        }

        Integer contentCount = root.cmsJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM cms_content WHERE site_id = ? AND disabled = FALSE",
                Integer.class, siteIdShort
        );
        if (contentCount == null || contentCount == 0) {
            String safeAuthor = brandName != null && brandName.length() > 50 ? brandName.substring(0, 50) : brandName;
            String title1 = "[브랜드 스토리] " + brandName + " — " + slogan;
            if (title1.length() > 250) {
                title1 = title1.substring(0, 250);
            }
            Map<String, Object> curatedMedia = root.accessMeta().resolveCuratedMediaForCategory("LIFESTYLE_HYBRID");
            String defaultHeroCover = curatedMedia.get("hero") != null ? String.valueOf(curatedMedia.get("hero")) : "";
            List<String> curatedCovers = curatedMedia.get("items") instanceof List<?> ci
                    ? ci.stream().map(String::valueOf).toList()
                    : List.of();
            String cover1 = !curatedCovers.isEmpty() ? curatedCovers.get(0) : defaultHeroCover;
            String cover2 = curatedCovers.size() > 1 ? curatedCovers.get(1) : defaultHeroCover;

            String desc1 = brandName + " 공식 공간에 오신 것을 환영합니다. " + slogan + "의 가치를 담아 정성껏 준비한 프로그램과 공간 이야기를 소개합니다.";
            Long contentId1 = root.cmsJdbcTemplate().queryForObject("""
                    INSERT INTO cms_content (
                        site_id, title, user_id, category_id, model_id, copied, contribute,
                        author, only_url, has_images, has_files, has_products, has_static,
                        description, cover, childs, scores, comments, clicks,
                        publish_date, create_date, sort, status, disabled, locale
                    ) VALUES (?, ?, 1, ?, 'article', FALSE, FALSE, ?, FALSE, TRUE, FALSE, FALSE, FALSE, ?, ?, 0, 95, 4, 128, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 100, 1, FALSE, 'ko')
                    RETURNING id
                    """,
                    Long.class,
                    siteIdShort, title1, categoryId, safeAuthor, desc1, cover1
            );
            String body1 = "<p>" + escapeXml(desc1) + "</p>";
            root.cmsJdbcTemplate().update(
                    "INSERT INTO cms_content_attribute (content_id, text, word_count) VALUES (?, ?, ?)",
                    contentId1, body1, desc1.length()
            );

            String title2 = "[이용 및 안내] " + brandName + " 주요 프로그램 및 이용 가이드";
            if (title2.length() > 250) {
                title2 = title2.substring(0, 250);
            }
            String desc2 = "방문 전 확인하실 수 있는 이용 시간, 신청 방법, 상세 안내 및 자주 묻는 질문을 정리했습니다. AI 컨시어지를 통해 언제든 문의하실 수 있습니다.";
            Long contentId2 = root.cmsJdbcTemplate().queryForObject("""
                    INSERT INTO cms_content (
                        site_id, title, user_id, category_id, model_id, copied, contribute,
                        author, only_url, has_images, has_files, has_products, has_static,
                        description, cover, childs, scores, comments, clicks,
                        publish_date, create_date, sort, status, disabled, locale
                    ) VALUES (?, ?, 1, ?, 'article', FALSE, FALSE, ?, FALSE, TRUE, FALSE, FALSE, FALSE, ?, ?, 0, 88, 2, 94, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 95, 1, FALSE, 'ko')
                    RETURNING id
                    """,
                    Long.class,
                    siteIdShort, title2, categoryId, safeAuthor, desc2, cover2
            );
            String body2 = "<p>" + escapeXml(desc2) + "</p>";
            root.cmsJdbcTemplate().update(
                    "INSERT INTO cms_content_attribute (content_id, text, word_count) VALUES (?, ?, ?)",
                    contentId2, body2, desc2.length()
            );
        }

        Integer bannerCount = root.cmsJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM cms_banner WHERE site_id = ? AND status = 1",
                Integer.class, siteIdShort
        );
        if (bannerCount == null || bannerCount == 0) {
            Map<String, Object> curatedBannerMedia = root.accessMeta().resolveCuratedMediaForCategory("LIFESTYLE_HYBRID");
            String defaultBannerImg = curatedBannerMedia.get("hero") != null ? String.valueOf(curatedBannerMedia.get("hero")) : "";
            String bannerTitle = (brandName + " — " + slogan);
            if (bannerTitle.length() > 195) {
                bannerTitle = bannerTitle.substring(0, 195);
            }
            root.cmsJdbcTemplate().update("""
                    INSERT INTO cms_banner (
                        site_id, zone_code, title, sub_title, image_url, link_url, start_date, end_date, sort, status
                    ) VALUES (?, 'MAIN_HERO', ?, '공식 브랜드 스토리 및 프로그램 안내', ?, '#section-form', '2025-01-01 00:00:00', '2030-12-31 23:59:59', 100, 1)
                    """,
                    siteIdShort, bannerTitle, defaultBannerImg
            );
        }

        ensureDefaultCmsForm(siteIdShort, siteSlug, brandName);
        return cmsSiteId;
    }

    private Long ensureDefaultCmsForm(short cmsSiteId, String siteSlug, String brandName) {
        List<Map<String, Object>> existingForm = root.cmsJdbcTemplate().queryForList(
                "SELECT id FROM cms_form WHERE site_id = ? AND status = 1 ORDER BY id ASC LIMIT 1",
                cmsSiteId
        );
        if (!existingForm.isEmpty()) {
            return ((Number) existingForm.get(0).get("id")).longValue();
        }
        String baseSlug = (siteSlug != null && !siteSlug.isBlank()) ? siteSlug : ("site-" + cmsSiteId);
        String safeFormCode = baseSlug.length() > 38 ? baseSlug.substring(0, 38) + "-apply" : baseSlug + "-apply";
        List<Map<String, Object>> codeCollision = root.cmsJdbcTemplate().queryForList(
                "SELECT id FROM cms_form WHERE code = ? LIMIT 1",
                safeFormCode
        );
        if (!codeCollision.isEmpty()) {
            safeFormCode = safeFormCode + "-" + cmsSiteId;
        }
        String defaultSchema = "[{\"field\":\"applicantName\",\"label\":\"신청자 성함\",\"type\":\"text\",\"required\":true},{\"field\":\"phone\",\"label\":\"연락처\",\"type\":\"tel\",\"required\":true},{\"field\":\"optionValue\",\"label\":\"희망 일정 / 프로그램 선택\",\"type\":\"text\",\"required\":false},{\"field\":\"message\",\"label\":\"문의 및 요청사항\",\"type\":\"textarea\",\"required\":false}]";
        return root.cmsJdbcTemplate().queryForObject("""
                INSERT INTO cms_form (
                    site_id, code, title, description, schema_json, privacy_policy, status, create_date
                ) VALUES (?, ?, ?, ?, ?::jsonb, '신청 접수 및 안내 연락 목적으로만 수집·이용됩니다.', 1, CURRENT_TIMESTAMP)
                RETURNING id
                """,
                Long.class,
                cmsSiteId,
                safeFormCode,
                brandName + " 온라인 예약 · 문의 신청",
                "희망하시는 일정이나 문의사항을 남겨주시면 확인 후 신속히 안내해 드립니다.",
                defaultSchema
        );
    }

    private List<Map<String, Object>> attachSkusToProducts(
            List<Map<String, Object>> products,
            List<Map<String, Object>> skus
    ) {
        if (products.isEmpty() || skus.isEmpty()) {
            return products;
        }
        Map<Long, List<Map<String, Object>>> skusByProd = new LinkedHashMap<>();
        for (Map<String, Object> sku : skus) {
            Object pidObj = sku.get("productId");
            if (pidObj instanceof Number n) {
                Map<String, Object> copy = new LinkedHashMap<>(sku);
                Object spRaw = copy.get("spData");
                if (spRaw instanceof String spStr && !spStr.isBlank()) {
                    copy.put("specs", parseJsonList(spStr));
                }
                skusByProd.computeIfAbsent(n.longValue(), k -> new ArrayList<>()).add(copy);
            }
        }
        List<Map<String, Object>> enriched = new ArrayList<>();
        for (Map<String, Object> prod : products) {
            Map<String, Object> copy = new LinkedHashMap<>(prod);
            Object idObj = copy.get("id");
            if (idObj instanceof Number n) {
                List<Map<String, Object>> matchedSkus = skusByProd.getOrDefault(n.longValue(), List.of());
                if (!matchedSkus.isEmpty()) {
                    copy.put("skus", matchedSkus);
                    List<String> optionLabels = new ArrayList<>();
                    for (Map<String, Object> s : matchedSkus) {
                        if (s.get("specs") instanceof List<?> specList) {
                            for (Object sp : specList) {
                                if (sp instanceof Map<?, ?> spMap && spMap.get("value") != null) {
                                    String label = String.valueOf(spMap.get("value")).trim();
                                    if (!label.isBlank() && !optionLabels.contains(label)) {
                                        optionLabels.add(label);
                                    }
                                }
                            }
                        }
                    }
                    if (!optionLabels.isEmpty()) {
                        copy.put("options", optionLabels);
                    }
                }
            }
            enriched.add(copy);
        }
        return enriched;
    }

    @SuppressWarnings("unchecked")
    void hydrateFeatureBlocksStrict(
            String brandName,
            String brandSlogan,
            String category,
            List<String> enabledFeatures,
            List<Map<String, Object>> assets,
            Map<String, Object> commerceData,
            Map<String, Object> featureBlocks
    ) {
        Set<String> enabledSet = enabledFeatures != null ? new LinkedHashSet<>(enabledFeatures) : Set.of();

        // 1. pricingMatrix: SHOP_PRICING_MATRIX 활성 시에만 정규화 유지 (비활성 사이트 오염 차단)
        Object rawPm = featureBlocks.get("pricingMatrix");
        if (rawPm instanceof List<?> pmList && !pmList.isEmpty()) {
            List<Map<String, Object>> normalizedRows = new ArrayList<>();
            for (Object item : pmList) {
                if (item instanceof Map<?, ?> m) {
                    normalizedRows.add(new LinkedHashMap<>((Map<String, Object>) m));
                }
            }
            featureBlocks.put("pricingMatrix", Map.of(
                    "title", brandName + " 상세 요금표",
                    "columns", defaultPricingColumns(),
                    "rows", normalizedRows
            ));
        } else if (rawPm instanceof String) {
            featureBlocks.remove("pricingMatrix");
        }
        if (!enabledSet.isEmpty() && !enabledSet.contains("SHOP_PRICING_MATRIX")
                && !(rawPm instanceof Map<?, ?> pmMap && pmMap.get("rows") instanceof List<?> pmRows && !pmRows.isEmpty())) {
            featureBlocks.remove("pricingMatrix");
        }

        // 2. spaceTour: CMS_SPACE_TOUR 활성 시 + 실제 업로드 자산이 있을 때만 구성 (ed-1.jpg 소파 폴백 영구 제거)
        if (!(featureBlocks.get("spaceTour") instanceof List<?> st && !st.isEmpty())) {
            if (enabledSet.contains("CMS_SPACE_TOUR") && assets != null && !assets.isEmpty()) {
                List<Map<String, Object>> spots = new ArrayList<>();
                int limit = Math.min(4, assets.size());
                for (int i = 0; i < limit; i++) {
                    Map<String, Object> a = assets.get(i);
                    String fileUrl = a.get("fileUrl") != null ? String.valueOf(a.get("fileUrl")).trim() : "";
                    if (!fileUrl.isBlank()) {
                        String caption = String.valueOf(a.getOrDefault("caption", brandName + " 공간 #" + (i + 1)));
                        spots.add(Map.of(
                                "zone", "ZONE 0" + (i + 1),
                                "title", caption,
                                "desc", brandSlogan,
                                "description", brandSlogan,
                                "imageUrl", fileUrl
                        ));
                    }
                }
                if (!spots.isEmpty()) {
                    featureBlocks.put("spaceTour", spots);
                }
            }
        }

        // 3. reviews: SyncShop pms_comment 실구매 리뷰 자동 연동 (가짜 민박 리뷰 제거)
        if (!(featureBlocks.get("reviews") instanceof List<?> rv && !rv.isEmpty())) {
            if (commerceData != null && commerceData.get("reviews") instanceof List<?> shopReviews && !shopReviews.isEmpty()) {
                featureBlocks.put("reviews", shopReviews);
            }
        }

        // 4. faqItems: SyncShop cms_help 도메인 FAQ 자동 연동 (가짜 주차/바비큐 FAQ 제거)
        if (!(featureBlocks.get("faqItems") instanceof List<?> fq && !fq.isEmpty())) {
            if (commerceData != null && commerceData.get("faqs") instanceof List<?> shopFaqs && !shopFaqs.isEmpty()) {
                featureBlocks.put("faqItems", shopFaqs);
            }
        }

        // 5. specComparison: SHOP_SPEC_COMPARISON 활성 시 SyncShop pms_product_attribute 실데이터로 구성
        if (featureBlocks.get("specComparison") instanceof String) {
            featureBlocks.remove("specComparison");
        }
        if (!(featureBlocks.get("specComparison") instanceof Map<?, ?> sc && !sc.isEmpty())
                && enabledSet.contains("SHOP_SPEC_COMPARISON")
                && commerceData != null
                && commerceData.get("attributes") instanceof List<?> attrList && !attrList.isEmpty()) {
            List<Map<String, Object>> specRows = new ArrayList<>();
            for (Map<String, Object> attr : castMapList(attrList)) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("tier", String.valueOf(attr.getOrDefault("name", "")));
                row.put("spec1", String.valueOf(attr.getOrDefault("inputList", "")));
                row.put("note", Integer.valueOf(1).equals(attr.get("type")) ? "제품 사양 파라미터" : "선택 옵션");
                specRows.add(row);
            }
            featureBlocks.put("specComparison", Map.of(
                    "title", brandName + " 상세 사양 · 옵션 매트릭스",
                    "columns", List.of("항목", "제원 / 옵션", "구분"),
                    "rows", specRows
            ));
        }

        // 6. 문자열 플레이스홀더 블록 정리 (AI가 미사용 블록을 안내 문자열로 출력한 경우 제거)
        for (String blockKey : List.of("subscriptionTiers", "sessionTimetable")) {
            if (featureBlocks.get(blockKey) instanceof String) {
                featureBlocks.remove(blockKey);
            }
        }
    }
}
