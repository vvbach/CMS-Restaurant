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
import vn.tts.model.payload.category.CategoryPagePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.CategoryPageHistoryResponse;
import vn.tts.model.response.category.CategoryPageResponse;
import vn.tts.service.category.CategoryPageService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category-page")
@RequiredArgsConstructor
@Tag(name = "Category Page", description = "controller của category page")
public class CategoryPageController {
    private final CategoryPageService categoryPageService;

    @Operation(summary = "Lấy tất cả thông tin category page")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<CategoryPageResponse>>> findAll() {
        return ResponseBase.success(categoryPageService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin category page")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<CategoryPageResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(categoryPageService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin category page")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<CategoryPageResponse>> create(
            @RequestBody @Valid CategoryPagePayload payload) {
        return ResponseBase.success(categoryPageService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin category page")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<CategoryPageResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryPagePayload payload) {
        return ResponseBase.success(categoryPageService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin category page")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        categoryPageService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<CategoryPageHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(categoryPageService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin category page")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<CategoryPageResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(categoryPageService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Reject bản ghi category page")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        categoryPageService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        categoryPageService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi category page")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        categoryPageService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish thông tin category page")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        categoryPageService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Unpublish thông tin category page")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        categoryPageService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        categoryPageService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
