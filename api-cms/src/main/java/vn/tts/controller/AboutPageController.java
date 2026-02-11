package vn.tts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.payload.AboutPagePayload;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.about.AboutPageHistoryResponse;
import vn.tts.model.response.about.AboutPageResponse;
import vn.tts.service.AboutPageService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/about")
@RequiredArgsConstructor
@Tag(name = "About Page", description = "Controller quản lý thông tin Page trang About")
public class AboutPageController {
    private final AboutPageService aboutPageService;

    @Operation(description = "lấy toàn bộ about banner article")
    @GetMapping()
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<AboutPageResponse>>> getAll() {
        return ResponseBase.success(aboutPageService.findAll());
    }

    @Operation(description = "lấy thông tin about banner article theo id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<AboutPageResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(aboutPageService.findById(id));
    }

    @Operation(summary = "Lấy lịch sử của một about banner article")
    @GetMapping("/history/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<AboutPageHistoryResponse>>> getHistory(@PathVariable UUID id) {
        return ResponseBase.success(aboutPageService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin liên hệ")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<AboutPageResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(aboutPageService.filter(payload, page, pageSize));
    }

    @Operation(description = "tạo about banner article mới")
    @PostMapping()
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<AboutPageResponse>> create(@RequestBody AboutPagePayload payload) {
        return ResponseBase.success(aboutPageService.create(payload));
    }

    @Operation(description = "cập nhật thông tin about banner article")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<AboutPageResponse>> update(@PathVariable UUID id, @RequestBody AboutPagePayload payload) {
        return ResponseBase.success(aboutPageService.update(id, payload));
    }

    @Operation(description = "xóa about banner article theo id")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(@PathVariable UUID id, @RequestBody DeletePayload payload) {
        aboutPageService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Reject about banner article")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(@PathVariable UUID id, @RequestBody RejectPayload payload) {
        aboutPageService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt about banner article")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) {
        aboutPageService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Đồng ý about banner article")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) {
        aboutPageService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish about banner article")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) {
        aboutPageService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Hủy xuất bản about banner article")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(@PathVariable UUID id, UnpublishPayload payload) {
        aboutPageService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để sửa và xuất bản lại")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) {
        aboutPageService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
