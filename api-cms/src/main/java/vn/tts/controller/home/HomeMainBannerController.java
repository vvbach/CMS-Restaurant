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
import vn.tts.model.payload.home.HomeMainBannerPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.home.HomeMainBannerHistoryResponse;
import vn.tts.model.response.home.HomeMainBannerResponse;
import vn.tts.service.home.HomeMainBannerService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/home-main-banner")
@RequiredArgsConstructor
@Tag(name = "Home Main Banner", description = "Controller Home Main Banner")
public class HomeMainBannerController {
    private final HomeMainBannerService homeMainBannerService;

    @Operation(summary = "Lấy tất cả thông tin banner chính trang home")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<HomeMainBannerResponse>>> findAll() {
        return ResponseBase.success(homeMainBannerService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin banner chính trang home")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<HomeMainBannerResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(homeMainBannerService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin banner chính trang home")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<HomeMainBannerResponse>> create(
            @RequestBody @Valid HomeMainBannerPayload payload) {
        return ResponseBase.success(homeMainBannerService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin banner chính trang home")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<HomeMainBannerResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid HomeMainBannerPayload payload) {
        return ResponseBase.success(homeMainBannerService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin banner chính trang home")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        homeMainBannerService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<HomeMainBannerHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(homeMainBannerService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin banner chính trang home")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<HomeMainBannerResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(homeMainBannerService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Reject bản ghi banner chính trang home")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        homeMainBannerService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        homeMainBannerService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi banner chính trang home")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        homeMainBannerService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish thông tin banner chính trang home")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        homeMainBannerService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Unpublish thông tin banner chính trang home")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        homeMainBannerService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        homeMainBannerService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
