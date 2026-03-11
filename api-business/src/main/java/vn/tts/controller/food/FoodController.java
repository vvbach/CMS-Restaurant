package vn.tts.controller.food;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.food.FoodResponse;
import vn.tts.service.food.FoodService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/food")
@RequiredArgsConstructor
@Tag(name = "Food", description = "Luồng lấy thông tin Food")
public class FoodController {
    private final FoodService foodService;

    @Operation(summary = "tìm kiếm Food")
    @PostMapping("/filter")
    public ResponseEntity<ResponseBase<PaginationResponse<List<FoodResponse>>>> filter(
            @RequestBody @Valid FilterPayload payload,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(foodService.filter(payload, page, pageSize));
    }

    @Operation(summary = "Tìm kiếm food bằng id")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBase<FoodResponse>> getById(@PathVariable("id") UUID id) {
        return ResponseBase.success(foodService.getById(id));
    }

    @Operation(summary = "Lấy tất cả food")
    @GetMapping
    public ResponseEntity<ResponseBase<List<FoodResponse>>> getAll() {
        return ResponseBase.success(foodService.getAll());
    }

    @Operation(summary = "Lấy food theo category id")
    @GetMapping("/category/{id}")
    public ResponseEntity<ResponseBase<List<FoodResponse>>> findByCategoryName(@PathVariable("id") UUID id) {
        return ResponseBase.success(foodService.findByCategoryId(id));
    }
}
