package vn.tts.service.base;


import vn.tts.model.payload.status.DeletePayload;

import java.util.UUID;

public interface CrudService<
        ResponseT,
        CreatePayloadT,
        UpdatePayloadT
        > {
    ResponseT create(CreatePayloadT payload);

    ResponseT update(UUID id, UpdatePayloadT payload);

    void delete(UUID id, DeletePayload payload);
}
