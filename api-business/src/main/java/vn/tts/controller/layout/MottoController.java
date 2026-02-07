package vn.tts.controller.layout;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.layout.MottoResponse;
import vn.tts.service.layout.MottoService;

@Validated
@RestController
@RequestMapping("/v1/api/motto")
@RequiredArgsConstructor
@Tag(name = "Motto", description = "Luồng lấy thông tin Motto")
public class MottoController {
    private final MottoService mottoService;

    @Operation(summary = "Lấy thông tin tiêu ngữ mới nhất theo thời gian publish")
    @GetMapping
    public ResponseEntity<ResponseBase<MottoResponse>> getLatest() {
        return ResponseBase.success(mottoService.getLatest());
    }
}
