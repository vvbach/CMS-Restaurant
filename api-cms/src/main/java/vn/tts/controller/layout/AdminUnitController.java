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
import vn.tts.model.payload.layout.AdminUnitPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.layout.AdminUnitHistoryResponse;
import vn.tts.model.response.layout.AdminUnitResponse;
import vn.tts.service.layout.AdminUnitService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/admin-unit")
@RequiredArgsConstructor
@Tag(name = "Admin Unit", description = "controller đơn vị quản lý")
public class AdminUnitController {
    private final AdminUnitService adminUnitService;

    @Operation(summary = "Lấy tất cả thông tin đơn vị quản lý")
    @GetMapping
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<AdminUnitResponse>>> findAll() {
        return ResponseBase.success(adminUnitService.findAll());
    }

    @Operation(summary = "Xem chi tiết thông tin đơn vị quản lý")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<AdminUnitResponse>> findById(@PathVariable UUID id) {
        return ResponseBase.success(adminUnitService.findById(id));
    }

    @Operation(summary = "Tạo mới thông tin đơn vị quản lý")
    @PostMapping
    @PreAuthorize("hasAuthority('UI_ADD')")
    public ResponseEntity<ResponseBase<AdminUnitResponse>> create(
            @RequestBody @Valid AdminUnitPayload payload) {
        return ResponseBase.success(adminUnitService.create(payload));
    }

    @Operation(summary = "Cập nhật thông tin đơn vị quản lý")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_UPDATE')")
    public ResponseEntity<ResponseBase<AdminUnitResponse>> update(
            @PathVariable UUID id,
            @RequestBody @Valid AdminUnitPayload payload) {
        return ResponseBase.success(adminUnitService.update(id, payload));
    }

    @Operation(summary = "Xoá thông tin đơn vị quản lý")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UI_DELETE')")
    public ResponseEntity<ResponseBase<Void>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) {
        adminUnitService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<List<AdminUnitHistoryResponse>>> history(
            @PathVariable UUID id) {
        return ResponseBase.success(adminUnitService.history(id));
    }

    @Operation(summary = "Tìm kiếm thông tin đơn vị quản lý")
    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('UI_READ')")
    public ResponseEntity<ResponseBase<PaginationResponse<List<AdminUnitResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(adminUnitService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Từ chối bản ghi đơn vị quản lý")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('UI_REJECT')")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        adminUnitService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển trạng thái chờ duyệt")
    @PostMapping("/{id}/pending-approval")
    @PreAuthorize("hasAuthority('UI_PENDING_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        adminUnitService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Phê duyệt bản ghi đơn vị quản lý")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('UI_APPROVE')")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        adminUnitService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xuất bản thông tin đơn vị quản lý")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('UI_PUBLISH')")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        adminUnitService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Huỷ xuất bản thông tin đơn vị quản lý")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('UI_UNPUBLISH')")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        adminUnitService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để chỉnh sửa")
    @PostMapping("/{id}/draft")
    @PreAuthorize("hasAuthority('UI_DRAFT')")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(@PathVariable UUID id) throws Exception {
        adminUnitService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
