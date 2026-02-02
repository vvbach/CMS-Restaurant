package vn.tts.controller.food;

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
import vn.tts.model.payload.food.FoodCategoryPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.food.FoodCategoryHistoryResponse;
import vn.tts.model.response.food.FoodCategoryResponse;
import vn.tts.service.food.FoodCategoryService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/category")
@RequiredArgsConstructor
@Tag(name = "Food Category", description = "Controller quản lý danh mục món ăn")
public class FoodCategoryController {
    private final FoodCategoryService foodCategoryService;

    @Operation(description = "lấy toàn bộ danh mục món ăn")
    @GetMapping()
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<List<FoodCategoryResponse>>> findAll() {
        return ResponseBase.success(foodCategoryService.findAll());
    }

    @Operation(description = "lấy thông tin danh mục món ăn theo id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<FoodCategoryResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(foodCategoryService.findById(id));
    }

    @Operation(summary = "Lấy lịch sử của một danh mục món ăn")
    @GetMapping("/history/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<List<FoodCategoryHistoryResponse>>> getHistory(@PathVariable UUID id) {
        return ResponseBase.success(foodCategoryService.history(id));
    }

    @Operation(description = "tạo danh mục món ăn mới")
    @PostMapping()
    @PreAuthorize("hasAuthority('PRODUCT_ADD')")
    public ResponseEntity<ResponseBase<FoodCategoryResponse>> create(@RequestBody FoodCategoryPayload payload) {
        return ResponseBase.success(foodCategoryService.create(payload));
    }

    @Operation(description = "cập nhật thông tin danh mục món ăn")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ResponseBase<FoodCategoryResponse>> update(@PathVariable UUID id, @RequestBody FoodCategoryPayload payload) {
        return ResponseBase.success(foodCategoryService.update(id, payload));
    }

    @Operation(description = "xóa danh mục món ăn theo id")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(@PathVariable UUID id, DeletePayload payload) {
        foodCategoryService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Tìm kiếm danh mục")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<FoodCategoryResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(foodCategoryService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Từ chối danh mục món ăn")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PRODUCT_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(@PathVariable UUID id, @RequestBody RejectPayload payload) {
        foodCategoryService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt danh mục món ăn")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('PRODUCT_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) {
        foodCategoryService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Đồng ý danh mục món ăn")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PRODUCT_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) {
        foodCategoryService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản danh mục món ăn")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('PRODUCT_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) {
        foodCategoryService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Hủy xuất bản danh mục món ăn")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('PRODUCT_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(@PathVariable UUID id, UnpublishPayload payload) {
        foodCategoryService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để sửa và xuất bản lại")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('PRODUCT_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) {
        foodCategoryService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
