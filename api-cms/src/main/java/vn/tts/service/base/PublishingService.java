package vn.tts.service.base;


import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;

import java.util.UUID;

public interface PublishingService {

    void submitForApproval(UUID id);

    void approve(UUID id);

    void reject(UUID id, RejectPayload payload);

    void publish(UUID id);

    void unpublish(UUID id, UnpublishPayload payload);

    void revertToDraft(UUID id);
}

