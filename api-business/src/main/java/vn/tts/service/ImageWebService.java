package vn.tts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.ImageWebEntity;
import vn.tts.model.response.PaginationResponse;
import vn.tts.repository.ImageWebRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageWebService {

    private final ImageWebRepository imageWebRepository;
    private final MinioService minioService;

    public PaginationResponse<?> filter(Integer page, Integer pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, BaseEntity.Fields.publicationDate);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        Page<ImageWebEntity> lstImage = imageWebRepository.findAll(pageable);
        lstImage.getContent().parallelStream().forEach(imageWebEntity -> {
            try {
                imageWebEntity.setPathImage(minioService.getPreSignedUrl(imageWebEntity.getPathImage()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        return PaginationResponse.builder()
                .total(lstImage.getTotalElements())
                .data(lstImage.getContent()).build();
    }
}
