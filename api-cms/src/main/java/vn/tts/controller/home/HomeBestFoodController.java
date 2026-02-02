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
import vn.tts.model.payload.home.HomeBestFoodPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.home.HomeBestFoodHistoryResponse;
import vn.tts.model.response.home.HomeBestFoodResponse;
import vn.tts.service.home.HomeBestFoodService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/home-best-food")
@RequiredArgsConstructor
@Tag(name = "Home Best Food", description = "Controller Home Best Food")
public class HomeBestFoodController {
    private final HomeBestFoodService homeBestFoodService;

    @Operation(summary = "Lấy tất cả thông tin Best Food trang Home")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<HomeBestFoodResponse>>> findAll() {
        return ResponseBase.success(homeBestFoodService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin Best Food trang Home")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<HomeBestFoodResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(homeBestFoodService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin Best Food trang Home")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<HomeBestFoodResponse>> create(
            @RequestBody @Valid HomeBestFoodPayload payload) {
        return ResponseBase.success(homeBestFoodService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin Best Food trang Home")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<HomeBestFoodResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid HomeBestFoodPayload payload) {
        return ResponseBase.success(homeBestFoodService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin Best Food trang Home")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        homeBestFoodService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<HomeBestFoodHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(homeBestFoodService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin Best Food trang Home")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<HomeBestFoodResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(homeBestFoodService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Từ chối bản ghi Best Food trang Home")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        homeBestFoodService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        homeBestFoodService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi Best Food trang Home")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        homeBestFoodService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản thông tin Best Recipe trang Home")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        homeBestFoodService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Huỷ xuất bản thông tin Best Recipe trang Home")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        homeBestFoodService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        homeBestFoodService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
