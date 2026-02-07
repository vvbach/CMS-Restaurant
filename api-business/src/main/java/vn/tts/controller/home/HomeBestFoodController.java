package vn.tts.controller.home;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.home.HomeBestFoodResponse;
import vn.tts.service.home.HomeBestFoodService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/v1/api/home-best-food")
@RequiredArgsConstructor
@Tag(name = "Home Best Food", description = "Luồng lấy thông tin Best Food trang Home")
public class HomeBestFoodController {
    private final HomeBestFoodService homeBestFoodService;

    @Operation(summary = "Lấy 12 Best Food trang Home mới nhất theo thời gian publish")
    @GetMapping
    public ResponseEntity<ResponseBase<List<HomeBestFoodResponse>>> getLatest() {
        return ResponseBase.success(homeBestFoodService.getHomeBestFoodResponses());
    }
}
