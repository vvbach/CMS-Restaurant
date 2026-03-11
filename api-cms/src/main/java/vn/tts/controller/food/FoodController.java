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
import vn.tts.model.payload.food.FoodFilterPayload;
import vn.tts.model.payload.food.FoodPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.food.FoodHistoryResponse;
import vn.tts.model.response.food.FoodResponse;
import vn.tts.service.food.FoodService;

import java.util.List;
import java.util.UUID;


@Validated
@RestController
@RequestMapping("/v1/api/food")
@RequiredArgsConstructor
@Tag(name = "Food", description = "Controller quản lý thông tin món ăn")
public class FoodController {
    private final FoodService foodService;

    @Operation(description = "lấy toàn bộ món ăn")
    @GetMapping()
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<List<FoodResponse>>> getAll() {
        return ResponseBase.success(foodService.findAll());
    }

    @Operation(description = "lấy thông tin món ăn theo id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<FoodResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(foodService.findById(id));
    }

    @Operation(description = "lấy danh sách món ăn theo id danh mục")
    @GetMapping("/category/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<List<FoodResponse>>> findByCategoryId(@PathVariable UUID id) {
        return ResponseBase.success(foodService.findByCategoryId(id));
    }

    @Operation(description = "lấy danh sách món ăn theo tên danh mục")
    @GetMapping("/category")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<List<FoodResponse>>> findByCategoryName(@RequestParam String name) {
        return ResponseBase.success(foodService.findByCategoryName(name));
    }

    @Operation(summary = "Tìm kiếm sản phẩm")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<FoodResponse>>>> filter(
            @RequestBody @Valid FoodFilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(foodService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Lấy lịch sử của một món ăn")
    @GetMapping("/history/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ResponseBase<List<FoodHistoryResponse>>> getHistory(@PathVariable UUID id) {
        return ResponseBase.success(foodService.history(id));
    }

    @Operation(description = "tạo món ăn mới")
    @PostMapping()
    @PreAuthorize("hasAuthority('PRODUCT_ADD')")
    public ResponseEntity<ResponseBase<FoodResponse>> create(@RequestBody @Valid FoodPayload payload) {
        return ResponseBase.success(foodService.create(payload));
    }

    @Operation(description = "cập nhật thông tin món ăn")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ResponseBase<FoodResponse>> update(@PathVariable UUID id, @RequestBody @Valid FoodPayload payload) {
        return ResponseBase.success(foodService.update(id, payload));
    }

    @Operation(description = "xóa món ăn theo id")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(@PathVariable UUID id, @RequestBody @Valid DeletePayload payload) {
        foodService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Reject món ăn")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PRODUCT_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(@PathVariable UUID id, @RequestBody RejectPayload payload) {
        foodService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt món ăn")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('PRODUCT_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) {
        foodService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Đồng ý món ăn")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PRODUCT_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) {
        foodService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish món ăn")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('PRODUCT_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) {
        foodService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Hủy xuất bản món ăn")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('PRODUCT_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(@PathVariable UUID id, UnpublishPayload payload) {
        foodService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để sửa và xuất bản lại")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('PRODUCT_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) {
        foodService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}