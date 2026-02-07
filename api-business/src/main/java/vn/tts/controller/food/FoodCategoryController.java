package vn.tts.controller.food;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.FoodCategoryResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.service.food.FoodCategoryService;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/food-category")
@RequiredArgsConstructor
@Tag(name = "Food Category", description = "Luồng lấy thông tin Food Category")
public class FoodCategoryController {
    private final FoodCategoryService foodCategoryService;

    @Operation(summary = "Lấy tất cả các danh mục Food")
    @GetMapping
    public ResponseEntity<ResponseBase<List<FoodCategoryResponse>>> getAll() {
        return ResponseBase.success(foodCategoryService.findAll());
    }

    @Operation(summary = "Lấy chi tiết một danh mục")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBase<FoodCategoryResponse>> findById(@PathVariable("id") UUID id) {
        return ResponseBase.success(foodCategoryService.getResponseById(id));
    }
}
