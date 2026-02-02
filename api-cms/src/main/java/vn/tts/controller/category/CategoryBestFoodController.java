package vn.tts.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.category.CategoryBestFoodPayload;
import vn.tts.model.payload.category.CategoryBestFoodUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.category.CategoryBestFoodHistoryResponse;
import vn.tts.model.response.category.CategoryBestFoodResponse;
import vn.tts.service.category.CategoryBestFoodService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category-best-food")
@RequiredArgsConstructor
@Tag(name = "Category Best Food", description = "Controller Category Best Food")
public class CategoryBestFoodController {
    private final CategoryBestFoodService categoryBestFoodService;

    @Operation(summary = "Lấy tất cả thông tin Best Food trang Category (có thể tìm theo ID trang Danh mục)")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<CategoryBestFoodResponse>>> findAll(
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId
    ) {
        if (categoryPageId == null) {
            return ResponseBase.success(categoryBestFoodService.findAll());
        } else {
            return ResponseBase.success(categoryBestFoodService.findAllByCategoryPageId(categoryPageId));
        }
    }

    @Operation(summary = "Xem chi tiết thông tin Best Food trang Category")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<CategoryBestFoodResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(categoryBestFoodService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin Best Food trang Category")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<CategoryBestFoodResponse>> create(
            @RequestBody @Valid CategoryBestFoodPayload payload) {
        return ResponseBase.success(categoryBestFoodService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin Best Food trang Category")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<CategoryBestFoodResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryBestFoodUpdatePayload payload) {
        return ResponseBase.success(categoryBestFoodService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin Best Food trang Category")
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
    public ResponseEntity<ResponseBase<List<CategoryBestFoodHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(categoryBestFoodService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin Best Food trang Category")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<CategoryBestFoodResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(categoryBestFoodService.filter(payload, categoryPageId, page, pageSize));
    }

    @Operation(summary = "Từ chối bản ghi Best Food trang Category")
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

    @Operation(summary = "Phê duyệt bản ghi Best Food trang Category")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        categoryBestFoodService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản thông tin Best Recipe trang Category")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        categoryBestFoodService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Huỷ xuất bản thông tin Best Recipe trang Category")
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
