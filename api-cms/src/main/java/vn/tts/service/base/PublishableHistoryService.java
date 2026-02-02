package vn.tts.service.base;


import vn.tts.model.response.PublishableHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface PublishableHistoryService<HistoryT extends PublishableHistoryResponse> {
    List<HistoryT> history(UUID id);
}
