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
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.ContactInfoPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.contact.ContactInfoHistoryResponse;
import vn.tts.model.response.contact.ContactInfoResponse;
import vn.tts.service.ContactInfoService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/contact-info")
@RequiredArgsConstructor
@Tag(name = "Contact Info", description = "controller banner thông tin liên hệ")
public class ContactInfoController {

    private final ContactInfoService contactInfoService;

    @Operation(summary = "Lấy tất cả thông tin liên hệ")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<ContactInfoResponse>>> findAll() {
        return ResponseBase.success(contactInfoService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin liên hệ")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<ContactInfoResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(contactInfoService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin liên hệ")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<ContactInfoResponse>> create(
            @RequestBody @Valid ContactInfoPayload payload) {
        return ResponseBase.success(contactInfoService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin liên hệ")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<ContactInfoResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid ContactInfoPayload payload) {
        return ResponseBase.success(contactInfoService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin liên hệ")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        contactInfoService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<ContactInfoHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(contactInfoService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin liên hệ")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<ContactInfoResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(contactInfoService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Từ chối bản ghi liên hệ")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        contactInfoService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        contactInfoService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi liên hệ")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        contactInfoService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản thông tin liên hệ")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        contactInfoService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Huỷ xuất bản thông tin liên hệ")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        contactInfoService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        contactInfoService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}