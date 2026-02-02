package vn.tts.service;

import vn.tts.model.response.PublishableHistoryResponse;
import vn.tts.model.response.PublishableResponse;
import vn.tts.service.base.BaseQueryService;
import vn.tts.service.base.CrudService;
import vn.tts.service.base.PublishableHistoryService;
import vn.tts.service.base.PublishingService;

public interface PublishableService<
        ResponseT extends PublishableResponse,
        CreatePayloadT,
        UpdatePayloadT,
        HistoryT extends PublishableHistoryResponse
        >
        extends CrudService<ResponseT, CreatePayloadT, UpdatePayloadT>,
        BaseQueryService<ResponseT>,
        PublishingService,
        PublishableHistoryService<HistoryT> {
}
