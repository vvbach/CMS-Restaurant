package vn.tts.controller.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.ResponseBase;
import vn.tts.model.response.category.AboutCategoryResponse;
import vn.tts.service.category.AboutCategoryService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/api/about-category")
@RequiredArgsConstructor
@Tag(name = "About Category", description = "Luồng lấy thông tin About Category")
public class AboutCategoryController {
    private final AboutCategoryService aboutCategoryService;

    @Operation(summary = "Lấy thông tin About Category theo id Category Page mới nhất theo thời gian publish")
    @GetMapping("/{categoryPageId}")
    public ResponseEntity<ResponseBase<AboutCategoryResponse>> getLatest(
            @PathVariable(name = "categoryPageId") UUID categoryPageId
    ) {
        return ResponseBase.success(aboutCategoryService.getAboutCategoryResponse(categoryPageId));
    }
}
