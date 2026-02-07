package vn.tts.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.ResponseBase;
import vn.tts.service.ImageWebService;

@Validated
@RestController
@RequestMapping("/v1/api/image-web")
@RequiredArgsConstructor
@Tag(name = "Image-WEB", description = "controller test image")
@Tag(description = "Test 1 luồng upload ảnh lên trang web", name = "test image")
public class ImageWebController {

    private final ImageWebService imageWebService;

    @Operation(summary = "tìm kiếm ")
    @PostMapping("/filter")
    public ResponseEntity<ResponseBase<PaginationResponse<?>>> filter(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        return ResponseBase.success(imageWebService.filter( page, pageSize));
    }

}
