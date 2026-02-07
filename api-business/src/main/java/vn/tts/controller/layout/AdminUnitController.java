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
import vn.tts.model.response.layout.AdminUnitResponse;
import vn.tts.service.layout.AdminUnitService;

@Validated
@RestController
@RequestMapping("/v1/api/admin-unit")
@RequiredArgsConstructor
@Tag(name = "Admin Unit", description = "Luồng lấy thông tin Admin Unit")
public class AdminUnitController {
    private final AdminUnitService adminUnitService;

    @Operation(summary = "Lấy thông tin Admin Unit mới nhất theo thời gian publish")
    @GetMapping
    public ResponseEntity<ResponseBase<AdminUnitResponse>> getLatest() {
        return ResponseBase.success(adminUnitService.getLatest());
    }
}
