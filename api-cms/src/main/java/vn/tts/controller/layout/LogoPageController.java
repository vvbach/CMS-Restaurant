package vn.tts.controller.layout;

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
import vn.tts.model.payload.layout.LogoPagePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.layout.LogoPageHistoryResponse;
import vn.tts.model.response.layout.LogoPageResponse;
import vn.tts.service.layout.LogoPageService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/logo-page")
@RequiredArgsConstructor
@Tag(name = "Logo Page", description = "controller logo trang web")
public class LogoPageController {
    private final LogoPageService logoPageService;

    @Operation(summary = "Lấy tất cả thông tin logo page")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<LogoPageResponse>>> findAll() {
        return ResponseBase.success(logoPageService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin logo page")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<LogoPageResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(logoPageService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin logo page")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_CREATE')")
    public ResponseEntity<ResponseBase<LogoPageResponse>> create(
            @RequestBody @Valid LogoPagePayload payload) {
        return ResponseBase.success(logoPageService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin logo page")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<LogoPageResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid LogoPagePayload payload) {
        return ResponseBase.success(logoPageService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin logo page")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        logoPageService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<LogoPageHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(logoPageService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin logo page")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<LogoPageResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(logoPageService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Reject records logo page")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        logoPageService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        logoPageService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt records logo page")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        logoPageService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish thông tin logo page")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        logoPageService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Unpublish thông tin logo page")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        logoPageService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        logoPageService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}