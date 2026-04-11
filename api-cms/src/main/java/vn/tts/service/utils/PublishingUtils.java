package vn.tts.service.utils;

import com.nimbusds.jose.shaded.gson.GsonBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.kafka.core.KafkaTemplate;
import vn.tts.config.InstantAdapter;
import vn.tts.entity.PublishableEntity;
import vn.tts.enums.ContentStatus;
import vn.tts.exception.AppBadRequestException;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.service.BaseService;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class PublishingUtils<
        EntityT extends PublishableEntity,
        RepositoryT extends JpaRepository<EntityT, UUID> & RevisionRepository<EntityT, UUID, Integer>
        > {
    private final RepositoryT repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BaseService baseService;

    @Transactional
    public void rejectEntity(EntityT entity, RejectPayload payload) {
        entity.setStatus(ContentStatus.REJECTED);
        entity.setRejectionReason(payload.getReason());
        repository.save(entity);
    }

    @Transactional
    public void pendingApproveEntity(EntityT entity) {
        entity.setStatus(ContentStatus.PENDING_APPROVAL);
        entity.setRejectionReason(null);
        repository.save(entity);
    }

    @Transactional
    public void approveEntity(EntityT entity) {
        entity.setStatus(ContentStatus.APPROVED);
        repository.save(entity);
    }

    @Transactional
    public void publishEntity(EntityT entity) {
        entity.setStatus(ContentStatus.PUBLISHED);
        entity.setUnpublishReason(null);
        repository.save(entity);
    }

    @Transactional
    public void unpublishEntity(EntityT entity, UnpublishPayload payload) {
        entity.setStatus(ContentStatus.UNPUBLISHED);
        entity.setUnpublishReason(payload.getReason());
        repository.save(entity);
    }

    @Transactional
    public void revertToDraftEntity(EntityT entity) {
        entity.setStatus(ContentStatus.DRAFT);
        repository.save(entity);
    }

    public void checkForUpdate(EntityT entity, String validateUpdateMessage) {
        if (!ContentStatus.DRAFT.equals(entity.getStatus()))
            throw new AppBadRequestException("status", baseService.getMessage(validateUpdateMessage));
    }

    public void checkForDelete(EntityT entity, String validateDeleteMessage) {
        if (!ContentStatus.DRAFT.equals(entity.getStatus()))
            throw new AppBadRequestException("status", baseService.getMessage(validateDeleteMessage));
    }

    public void checkForReject(EntityT entity, String validateRejectMessage) {
        if (!ContentStatus.PENDING_APPROVAL.equals(entity.getStatus()) &&
            !ContentStatus.APPROVED.equals(entity.getStatus())) {
            throw new AppBadRequestException("status", baseService.getMessage(validateRejectMessage));
        }
    }

    public void checkForPendingApproval(EntityT entity, String validatePendingApprovalMessage) {
        if (!ContentStatus.DRAFT.equals(entity.getStatus())
            && !ContentStatus.REJECTED.equals(entity.getStatus())) {
            throw new AppBadRequestException("status", baseService.getMessage(validatePendingApprovalMessage));
        }
    }

    public void checkForApprove(EntityT entity, String validateApproveMessage) {
        if (!ContentStatus.PENDING_APPROVAL.equals(entity.getStatus())) {
            throw new AppBadRequestException("status", baseService.getMessage(validateApproveMessage));
        }
    }

    public void checkForPublish(EntityT entity, String validatePublishMessage) {
        if (!ContentStatus.APPROVED.equals(entity.getStatus()) &&
            !ContentStatus.UNPUBLISHED.equals(entity.getStatus())) {
            throw new AppBadRequestException("status", baseService.getMessage(validatePublishMessage));
        }
    }

    public void checkForUnpublish(EntityT entity, String validateUnpublishMessage) {
        if (!ContentStatus.PUBLISHED.equals(entity.getStatus()))
            throw new AppBadRequestException("status", baseService.getMessage(validateUnpublishMessage));
    }

    public void checkForDraft(EntityT entity, String validateDraftMessage) {
        if (!ContentStatus.UNPUBLISHED.equals(entity.getStatus()))
            throw new AppBadRequestException("status", baseService.getMessage(validateDraftMessage));
    }

    public void kafkaSendTopic(Object subject, String topic) {
        String json = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create()
                .toJson(subject);

        log.info("Sending to Kafka topic [{}]: {}", topic, json);

        kafkaTemplate.send(topic, json)
                .exceptionally(ex -> {
                    log.error("Kafka send failed: {}", ex.getMessage(), ex);
                    throw new RuntimeException(ex.getMessage());
                });
    }
}
