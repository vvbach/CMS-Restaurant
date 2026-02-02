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
import vn.tts.model.payload.category.CategoryStatisticPayload;
import vn.tts.model.payload.category.CategoryStatisticUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.CategoryStatisticHistoryResponse;
import vn.tts.model.response.category.CategoryStatisticResponse;
import vn.tts.service.category.CategoryStatisticService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category-statistic")
@RequiredArgsConstructor
@Tag(name = "Category Statistic", description = "Controller Category Statistic")
public class CategoryStatisticController {
    private final CategoryStatisticService categoryStatisticService;

    @Operation(summary = "Lấy tất cả thông tin Category Statistic (có thể tìm theo ID trang Danh mục)")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<CategoryStatisticResponse>>> findAll(
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId
    ) {
        if (categoryPageId == null) {
            return ResponseBase.success(categoryStatisticService.findAll());
        } else {
            return ResponseBase.success(categoryStatisticService.findAll(categoryPageId));
        }
    }

    @Operation(summary = "Xem chi tiết thông tin Category Statistic")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<CategoryStatisticResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(categoryStatisticService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin Category Statistic")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<CategoryStatisticResponse>> create(
            @RequestBody @Valid CategoryStatisticPayload payload) {
        return ResponseBase.success(categoryStatisticService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin Category Statistic")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<CategoryStatisticResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid CategoryStatisticUpdatePayload payload) {
        return ResponseBase.success(categoryStatisticService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin Category Statistic")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        categoryStatisticService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<CategoryStatisticHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(categoryStatisticService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin Category Statistic")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<CategoryStatisticResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(name = "categoryPageId", required = false) UUID categoryPageId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(categoryStatisticService.filter(payload, categoryPageId, page, pageSize));
    }

    @Operation(summary = "Từ chối bản ghi Category Statistic")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        categoryStatisticService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        categoryStatisticService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi Category Statistic")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        categoryStatisticService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản thông tin thống kê danh mục")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        categoryStatisticService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Huỷ xuất bản thông tin thống kê danh mục")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        categoryStatisticService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        categoryStatisticService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}