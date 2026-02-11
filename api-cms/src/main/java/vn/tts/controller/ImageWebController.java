package vn.tts.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.tts.entity.ImageWebEntity;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.payload.*;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.image.ImageWebHistoryResponse;
import vn.tts.model.response.image.ImageWebResponse;
import vn.tts.service.ImageWebService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/image-web")
@RequiredArgsConstructor
@Tag(name = "User", description = "controller cho việc update user")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(description = "Test 1 luồng upload ảnh lên trang web", name = "test image")
public class ImageWebController {

    private final ImageWebService imageWebService;

    @Operation(summary = "Thêm mới bài viết ")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseBase<Void>> create(
            @RequestPart(value = "file") MultipartFile file,
            @RequestParam("description") String description
    ) throws Exception {

        imageWebService.create(file, ImageWebPayload.builder()
                .description(description)
                .build());
        return ResponseBase.success(null);
    }

    @Operation(summary = "cập nhật bài viết ")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseBase<Void>> update(
            @PathVariable UUID id,
            @Parameter(description = "Tệp ảnh upload lên server",
                    schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestPart("description") String description
    ) throws Exception {
        imageWebService.update(id, file,  ImageWebPayload.builder()
                .description(description).build());
        return ResponseBase.success(null);
    }

    @Operation(summary = "xem chi tiết một bài viết")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBase<ImageWebResponse>> findById(@PathVariable UUID id) throws Exception {
        return ResponseBase.success(imageWebService.findById(id));
    }

    @Operation(summary = "lấy tất cả bài viết")
    @GetMapping()
    public ResponseEntity<ResponseBase<List<ImageWebResponse>>> findAll() throws Exception {
        return ResponseBase.success(imageWebService.findAll());
    }

    @Operation(summary = "xoá bài viết ")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseBase<String>> delete(
            @PathVariable UUID id,
            @RequestBody @Valid DeletePayload payload
    ) throws Exception {
        imageWebService.delete(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Xem lịch sử thay đổi ")
    @DeleteMapping("/history/{id}")
    public ResponseEntity<ResponseBase<List<ImageWebHistoryResponse>>> history(
            @PathVariable UUID id
    ) {
        return ResponseBase.success(imageWebService.history(id));
    }

    @Operation(summary = "tìm kiếm ")
    @PostMapping("/filter")
    public ResponseEntity<ResponseBase<PaginationResponse<List<ImageWebResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(imageWebService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Reject bài viết ")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ResponseBase<Void>> reject(
            @PathVariable UUID id,
            @RequestBody @Valid RejectPayload payload
    ) throws Exception {
        imageWebService.reject(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "chuyển trạng thái chờ duyệt ")
    @PostMapping("/{id}/pending-approval")
    public ResponseEntity<ResponseBase<Void>> submitForApproval(@PathVariable UUID id) throws Exception {
        imageWebService.submitForApproval(id);
        return ResponseBase.success(null);
    }

    // POST /api/images/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<ResponseBase<Void>> approve(@PathVariable UUID id) throws Exception {
        imageWebService.approve(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Publish ảnh ")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ResponseBase<Void>> publish(@PathVariable UUID id) throws Exception {
        imageWebService.publish(id);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Unpublish ")
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ResponseBase<Void>> unpublish(
            @PathVariable UUID id,
            @RequestBody @Valid UnpublishPayload payload
    ) throws Exception {
        imageWebService.unpublish(id, payload);
        return ResponseBase.success(null);
    }

    @Operation(summary = "Chuyển về trạng thái nháp để sửa và xuất bản lại ")
    @PostMapping("/{id}/draft")
    public ResponseEntity<ResponseBase<Void>> revertToDraft(
            @PathVariable UUID id
    ) throws Exception {
        imageWebService.revertToDraft(id);
        return ResponseBase.success(null);
    }
}
