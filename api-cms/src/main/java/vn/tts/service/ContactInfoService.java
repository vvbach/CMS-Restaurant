package vn.tts.service;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.ContactInfoEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.ContactInfoPayload;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.contact.ContactInfoHistoryResponse;
import vn.tts.model.response.contact.ContactInfoResponse;
import vn.tts.repository.ContactInfoRepository;
import vn.tts.service.utils.PublishableHistoryUtils;
import vn.tts.service.utils.PublishingUtils;
import vn.tts.service.utils.QueryService;
import vn.tts.service.utils.ValidateEntityService;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class ContactInfoService extends BaseService implements PublishableService<
        ContactInfoResponse,
        ContactInfoPayload,
        ContactInfoPayload,
        ContactInfoHistoryResponse
        >
{
    private final ContactInfoRepository contactInfoRepository;
    private final MinioService minioService;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_contact_info_publish";
    private final static String TOPIC_UNPUBLISH = "topic_contact_info_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<ContactInfoEntity, ContactInfoRepository> publishingUtils;
    private final QueryService<ContactInfoEntity, ContactInfoResponse, ContactInfoRepository> queryService;
    private final ValidateEntityService<ContactInfoEntity, ContactInfoRepository> validateEntityService;
    private final PublishableHistoryUtils<ContactInfoEntity, ContactInfoHistoryResponse, ContactInfoRepository> publishableHistoryUtils;

    public ContactInfoService(
            KafkaTemplate<String, Object> kafkaTemplate,
            ContactInfoRepository contactInfoRepository,
            MinioService minioService,
            ModelMapper modelMapper,
            BaseService baseService
    ) {
        this.contactInfoRepository = contactInfoRepository;
        this.minioService = minioService;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(contactInfoRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(contactInfoRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(contactInfoRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(contactInfoRepository);
    }

    @Override
    public List<ContactInfoResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    @Override
    public ContactInfoResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    @Override
    public PaginationResponse<List<ContactInfoResponse>> filter(FilterPayload payload, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<ContactInfoEntity> data = contactInfoRepository.filter(payload,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<ContactInfoResponse>> response = new PaginationResponse<>();
        response.setData(
                data.getContent()
                        .stream()
                        .map(this::getResponse)
                        .toList()
        );
        response.setTotal(data.getTotalElements());
        return response;
    }

    @Override
    @Transactional
    public ContactInfoResponse create(ContactInfoPayload payload) {
        ContactInfoEntity entity = new ContactInfoEntity(
                payload.getText(),
                payload.getImageUrl(),
                payload.getAddress(),
                payload.getEmail(),
                payload.getPhoneNumber()
        );
        
        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(entity.getId())
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail()))
                        .action("CREATE")
                        .message("Contact Info has been created.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(contactInfoRepository.save(entity));
    }

    @Override
    @Transactional
    public ContactInfoResponse update(UUID id, ContactInfoPayload payload) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setText(payload.getText());
        entity.setImageUrl(payload.getImageUrl());
        entity.setAddress(payload.getAddress());
        entity.setEmail(payload.getEmail());
        entity.setPhoneNumber(payload.getPhoneNumber());

        contactInfoRepository.save(entity);
        
        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UPDATE")
                        .message("Contact Info content has been updated.")
                        .build(),
                TOPIC_NOTIFY
        );

        return getResponse(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());

        contactInfoRepository.save(entity);
        
        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DELETE")
                        .message("Contact Info has been deleted.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    public List<ContactInfoHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, ContactInfoHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkReject(entity, "validate.article.status.is.draft.reject");

        publishingUtils.rejectEntity(entity, payload);
        
        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("REJECT")
                        .message("Contact Info has been rejected.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPendingApproval(entity, "validate.article.status.is.draft");

        publishingUtils.pendingApproveEntity(entity);
        
        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PENDING APPROVAL")
                        .message("Contact Info has been updated to pending approval stage.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkApprove(entity, "validate.article.status.is.draft.approve");

        publishingUtils.approveEntity(entity);
        
        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("APPROVE")
                        .message("Contact Info has been approved.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkPublish(entity, "validate.article.status.is.draft.publish");

        publishingUtils.publishEntity(entity);

        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("PUBLISH")
                        .message("Contact Info has been published.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUnpublish(entity, "validate.article.status.is.unpublish");

        publishingUtils.unpublishEntity(entity, payload);

        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("UNPUBLISH")
                        .message("Contact Info has been unpublished.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        ContactInfoEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDraft(entity, "validate.article.status.is.draft.unpublish.draft");

        publishingUtils.revertToDraftEntity(entity);

        publishingUtils.kafkaSendTopic(
                SendEmailEvent.builder()
                        .entityId(id)
                        .entityType("Contact Info")
                        .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                        .action("DRAFT")
                        .message("Contact Info has been updated to draft state.")
                        .build(),
                TOPIC_NOTIFY
        );
    }

    private ContactInfoResponse getResponse(ContactInfoEntity entity) {
        String imageUrl;
        try {
            imageUrl = minioService.getPreSignedUrl(entity.getImageUrl());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return ContactInfoResponse.builder()
                .id(entity.getId())
                .text(entity.getText())
                .imageUrl(imageUrl)
                .address(entity.getAddress())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .isDelete(entity.getIsDelete())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .deletionReason(entity.getDeletionReason())
                .unpublishReason(entity.getUnpublishReason())
                .createdByName(entity.getCreatedByName())
                .createdAt(entity.getCreatedAt())
                .updatedByName(entity.getUpdatedByName())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
