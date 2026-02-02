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
import vn.tts.model.payload.category.AboutCategoryPayload;
import vn.tts.model.payload.category.AboutCategoryUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.AboutCategoryHistoryResponse;
import vn.tts.model.response.category.AboutCategoryResponse;
import vn.tts.service.category.AboutCategoryService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/about-category")
@RequiredArgsConstructor
@Tag(name = "About Category", description = "Controller About Category")
public class AboutCategoryController {
    private final AboutCategoryService categoryBestFoodService;

    @Operation(summary = "Lấy tất cả thông tin About Category (có thể tìm theo ID trang Danh mục)")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<AboutCategoryResponse>>> findAll(
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId
    ) {
        if (categoryPageId == null) {
            return ResponseBase.success(categoryBestFoodService.findAll());
        } else {
            return ResponseBase.success(categoryBestFoodService.findAllById(categoryPageId));
        }
    }

    @Operation(summary = "Xem chi tiết thông tin About Category")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<AboutCategoryResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(categoryBestFoodService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin About Category")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<AboutCategoryResponse>> create(
            @RequestBody @Valid AboutCategoryPayload payload) {
        return ResponseBase.success(categoryBestFoodService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin About Category")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<AboutCategoryResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid AboutCategoryUpdatePayload payload) {
        return ResponseBase.success(categoryBestFoodService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin About Category")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        categoryBestFoodService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<AboutCategoryHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(categoryBestFoodService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin About Category")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<AboutCategoryResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(categoryBestFoodService.filter(payload, categoryPageId, page, pageSize));
    }

    @Operation(summary = "Từ chối bản ghi About Category")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        categoryBestFoodService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        categoryBestFoodService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi About Category")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        categoryBestFoodService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản thông tin About Category")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        categoryBestFoodService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Huỷ xuất bản thông tin About Category")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        categoryBestFoodService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        categoryBestFoodService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}