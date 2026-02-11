package vn.tts.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.category.CategoryMainBannerPayload;
import vn.tts.model.payload.category.CategoryMainBannerUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.CategoryMainBannerHistoryResponse;
import vn.tts.model.response.category.CategoryMainBannerResponse;
import vn.tts.service.category.CategoryMainBannerService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category-main-banner")
@RequiredArgsConstructor
@Tag(name = "Category Main Banner", description = "Controller Category Main Banner")
public class CategoryMainBannerController {
    private final CategoryMainBannerService categoryMainBannerService;

    @Operation(summary = "Lấy tất cả thông tin banner chính trang category")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<CategoryMainBannerResponse>>> findAll(
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId
    ) {
        if (categoryPageId == null) {
            return ResponseBase.success(categoryMainBannerService.findAll());
        } else {
            return ResponseBase.success(categoryMainBannerService.findAllByCategoryPageId(categoryPageId));
        }
    }

    @Operation(summary = "Xem chi tiết thông tin banner chính trang category")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<CategoryMainBannerResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(categoryMainBannerService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin banner chính trang category")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<CategoryMainBannerResponse>> create(
            @RequestBody @Valid CategoryMainBannerPayload payload) {
        return ResponseBase.success(categoryMainBannerService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin banner chính trang category")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<CategoryMainBannerResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryMainBannerUpdatePayload payload) {
        return ResponseBase.success(categoryMainBannerService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin banner chính trang category")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        categoryMainBannerService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<CategoryMainBannerHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(categoryMainBannerService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin banner chính trang category")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<CategoryMainBannerResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(categoryMainBannerService.filter(payload, categoryPageId, page, pageSize));
    }

    @Operation(summary = "Reject bản ghi banner chính trang category")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        categoryMainBannerService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        categoryMainBannerService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi banner chính trang category")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        categoryMainBannerService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish thông tin banner chính trang category")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        categoryMainBannerService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Unpublish thông tin banner chính trang category")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        categoryMainBannerService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        categoryMainBannerService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
