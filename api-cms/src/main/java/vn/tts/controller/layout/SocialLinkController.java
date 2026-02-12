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
import vn.tts.model.payload.layout.SocialLinkPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.layout.SocialLinkHistoryResponse;
import vn.tts.model.response.layout.SocialLinkResponse;
import vn.tts.service.layout.SocialLinkService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/social-link")
@RequiredArgsConstructor
@Tag(name = "Social Link", description = "controller mạng xã hội")
public class SocialLinkController {
    private final SocialLinkService socialLinkService;

    @Operation(summary = "Lấy tất cả thông tin mạng xã hội")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<SocialLinkResponse>>> findAll() {
        return ResponseBase.success(socialLinkService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin mạng xã hội")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<SocialLinkResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(socialLinkService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin mạng xã hội")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<SocialLinkResponse>> create(
            @RequestBody @Valid SocialLinkPayload payload) {
        return ResponseBase.success(socialLinkService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin mạng xã hội")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<SocialLinkResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid SocialLinkPayload payload) {
        return ResponseBase.success(socialLinkService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin mạng xã hội")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        socialLinkService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<SocialLinkHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(socialLinkService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin mạng xã hội")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<SocialLinkResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(socialLinkService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Reject records mạng xã hội")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        socialLinkService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        socialLinkService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt records mạng xã hội")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        socialLinkService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish thông tin mạng xã hội")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        socialLinkService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Unpublish thông tin mạng xã hội")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        socialLinkService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        socialLinkService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}