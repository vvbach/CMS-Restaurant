package vn.tts.service.category;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.category.AboutCategoryEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.exception.AppBadRequestException;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.FilterPayload;
import vn.tts.model.payload.category.AboutCategoryPayload;
import vn.tts.model.payload.category.AboutCategoryUpdatePayload;
import vn.tts.model.payload.status.DeletePayload;
import vn.tts.model.payload.status.RejectPayload;
import vn.tts.model.payload.status.UnpublishPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.category.AboutCategoryHistoryResponse;
import vn.tts.model.response.category.AboutCategoryResponse;
import vn.tts.repository.category.AboutCategoryRepository;
import vn.tts.repository.category.CategoryPageRepository;
import vn.tts.service.BaseService;
import vn.tts.service.base.CrudService;
import vn.tts.service.base.PublishableHistoryService;
import vn.tts.service.base.PublishingService;
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
public class AboutCategoryService extends BaseService implements
        CrudService<AboutCategoryResponse, AboutCategoryPayload, AboutCategoryUpdatePayload>,
        PublishingService,
        PublishableHistoryService<AboutCategoryHistoryResponse>
{
    private final AboutCategoryRepository aboutCategoryRepository;
    private final CategoryPageRepository categoryPageRepository;
    private final ModelMapper modelMapper;

    private final static String TOPIC_PUBLISH = "topic_about_category_publish";
    private final static String TOPIC_UNPUBLISH = "topic_about_category_unpublish";
    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final PublishingUtils<AboutCategoryEntity, AboutCategoryRepository> publishingUtils;
    private final QueryService<AboutCategoryEntity, AboutCategoryResponse, AboutCategoryRepository> queryService;
    private final ValidateEntityService<AboutCategoryEntity, AboutCategoryRepository> validateEntityService;
    private final PublishableHistoryUtils<AboutCategoryEntity, AboutCategoryHistoryResponse, AboutCategoryRepository> publishableHistoryUtils;

    @Autowired
    public AboutCategoryService(
            AboutCategoryRepository aboutCategoryRepository,
            CategoryPageRepository categoryPageRepository,
            ModelMapper modelMapper,
            KafkaTemplate<String, Object> kafkaTemplate,
            BaseService baseService
    ) {
        this.aboutCategoryRepository = aboutCategoryRepository;
        this.categoryPageRepository = categoryPageRepository;
        this.modelMapper = modelMapper;
        this.publishingUtils = new PublishingUtils<>(aboutCategoryRepository, kafkaTemplate, baseService);
        this.queryService = new QueryService<>(aboutCategoryRepository, baseService);
        this.validateEntityService = new ValidateEntityService<>(aboutCategoryRepository, baseService);
        this.publishableHistoryUtils = new PublishableHistoryUtils<>(aboutCategoryRepository);
    }

    public List<AboutCategoryResponse> findAll() {
        return queryService.findAll(this::getResponse);
    }

    public List<AboutCategoryResponse> findAllById(UUID categoryPageId) {
        return aboutCategoryRepository.findAllByCategoryPageId(categoryPageId)
                .parallelStream().map(this::getResponse).toList();
    }

    public AboutCategoryResponse findById(UUID id) {
        return queryService.findById(id, this::getResponse);
    }

    public PaginationResponse<List<AboutCategoryResponse>> filter(FilterPayload payload, UUID categoryPageId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt));
        Page<AboutCategoryEntity> data = aboutCategoryRepository.filter(
                payload,
                categoryPageId,
                Objects.isNull(payload.getFormDate()) ? null : payload.getFormDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(),
                pageable);

        PaginationResponse<List<AboutCategoryResponse>> response = new PaginationResponse<>();
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
    public AboutCategoryResponse create(AboutCategoryPayload payload) {
        validateCategoryPage(payload.getCategoryPageId());
        AboutCategoryEntity entity = new AboutCategoryEntity(
                payload.getCategoryPageId(),
                payload.getTitle(),
                payload.getSubtitle(),
                payload.getDescription()
        );
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(entity.getId())
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail()))
                .action("CREATE")
                .message("About Category has been created.")
                .build(),
            TOPIC_NOTIFY
        );
        return getResponse(aboutCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public AboutCategoryResponse update(UUID id, AboutCategoryUpdatePayload payload) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkUpdate(entity, "validate.article.status.is.draft.update");

        entity.setTitle(payload.getTitle());
        entity.setSubtitle(payload.getSubtitle());
        entity.setDescription(payload.getDescription());
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("UPDATE")
                .message("About Category has been updated.")
                .build(),
            TOPIC_NOTIFY
        );
        return getResponse(aboutCategoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID id, DeletePayload payload) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");

        publishingUtils.checkDelete(entity, "validate.article.status.is.draft.delete");

        entity.setIsDelete(DeleteEnum.YES);
        entity.setDeletionReason(payload.getReason());
        aboutCategoryRepository.save(entity);
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("DELETE")
                .message("About Category has been deleted.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    public List<AboutCategoryHistoryResponse> history(UUID id) {
        return publishableHistoryUtils.getUpdatedHistoryRevisions(id,
                        (entity) -> modelMapper.map(entity, AboutCategoryHistoryResponse.class))
                .stream().map(Pair::getFirst).toList();
    }

    @Override
    @Transactional
    public void reject(UUID id, RejectPayload payload) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkReject(entity, "validate.article.is.draft.reject");
        publishingUtils.rejectEntity(entity, payload);
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("REJECT")
                .message("About Category has been rejected.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void submitForApproval(UUID id) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkPendingApproval(entity, "validate.article.is.draft");
        publishingUtils.pendingApproveEntity(entity);
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("PENDING APPROVAL")
                .message("About Category is pending approval.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkApprove(entity, "validate.article.is.draft.approve");
        publishingUtils.approveEntity(entity);
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("APPROVE")
                .message("About Category has been approved.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkPublish(entity, "validate.article.is.draft.publish");
        publishingUtils.publishEntity(entity);
        publishingUtils.kafkaSendTopic(entity, TOPIC_PUBLISH);
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("PUBLISH")
                .message("About Category has been published.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void unpublish(UUID id, UnpublishPayload payload) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkUnpublish(entity, "validate.article.is.unpublish");
        publishingUtils.unpublishEntity(entity, payload);
        publishingUtils.kafkaSendTopic(id, TOPIC_UNPUBLISH);
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("UNPUBLISH")
                .message("About Category has been unpublished.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    @Override
    @Transactional
    public void revertToDraft(UUID id) {
        AboutCategoryEntity entity = validateEntityService.checkAndDetail(id, "message.entity.not.found");
        publishingUtils.checkDraft(entity, "validate.article.is.draft.unpublish.draft");
        publishingUtils.revertToDraftEntity(entity);
        
        publishingUtils.kafkaSendTopic(
            SendEmailEvent.builder()
                .entityId(id)
                .entityType("About Category")
                .email(List.of(getUserDetail().getEmail(), getUserDetailById(entity.getCreatedBy()).getEmail()))
                .action("DRAFT")
                .message("About Category has been updated to draft state.")
                .build(),
            TOPIC_NOTIFY
        );
    }

    private AboutCategoryResponse getResponse(AboutCategoryEntity entity) {
        return modelMapper.map(entity, AboutCategoryResponse.class);
    }

    private void validateCategoryPage(UUID categoryPageId) {
        if (!categoryPageRepository.existsById(categoryPageId))
            throw new AppBadRequestException("categoryPageId", getMessage("validate.category.page.not.exist"));
    }
}
