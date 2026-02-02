package vn.tts.controller.home;

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
import vn.tts.model.payload.home.FeaturedCategoryPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.home.FeaturedCategoryHistoryResponse;
import vn.tts.model.response.home.FeaturedCategoryResponse;
import vn.tts.service.home.FeaturedCategoryService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/featured-category")
@RequiredArgsConstructor
@Tag(name = "Featured Category", description = "Controller Featured Category")
public class FeaturedCategoryController {
    private final FeaturedCategoryService featuredCategoryService;

    @Operation(summary = "Lấy tất cả thông tin Featured Category")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<FeaturedCategoryResponse>>> findAll() {
        return ResponseBase.success(featuredCategoryService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin Featured Category")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<FeaturedCategoryResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(featuredCategoryService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin Featured Category")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<FeaturedCategoryResponse>> create(
            @RequestBody @Valid FeaturedCategoryPayload payload) {
        return ResponseBase.success(featuredCategoryService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin Featured Category")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<FeaturedCategoryResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid FeaturedCategoryPayload payload) {
        return ResponseBase.success(featuredCategoryService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin Featured Category")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        featuredCategoryService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<FeaturedCategoryHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(featuredCategoryService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin Featured Category")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<FeaturedCategoryResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(featuredCategoryService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Từ chối bản ghi Featured Category")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        featuredCategoryService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        featuredCategoryService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi Featured Category")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        featuredCategoryService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản thông tin Featured Category")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        featuredCategoryService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Huỷ xuất bản thông tin Featured Category")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        featuredCategoryService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        featuredCategoryService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
