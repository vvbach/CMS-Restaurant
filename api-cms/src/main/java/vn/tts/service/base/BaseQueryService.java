package vn.tts.service.base;


import vn.tts.model.payload.FilterPayload;
import vn.tts.model.response.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface BaseQueryService<ResponseT> {
    List<ResponseT> findAll();

    ResponseT findById(UUID id);

    PaginationResponse<List<ResponseT>> filter(
            FilterPayload payload,
            Integer page,
            Integer pageSize
    );
}
