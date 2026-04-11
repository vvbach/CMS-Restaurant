package vn.tts.service.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.tts.entity.BaseEntity;
import vn.tts.enums.DeleteEnum;
import vn.tts.exception.AppBadRequestException;
import vn.tts.service.BaseService;

import java.util.UUID;

@RequiredArgsConstructor
public class ValidateEntityService<EntityT extends BaseEntity, RepositoryT extends JpaRepository<EntityT, UUID>> {
    private final RepositoryT repository;
    private final BaseService baseService;

    public EntityT getValidEntity(UUID id, String entityNotFoundMessage) {
        EntityT entity = repository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("id", baseService.getMessage(entityNotFoundMessage)));

        if (DeleteEnum.YES.equals(entity.getIsDelete()))
            throw new AppBadRequestException("id", baseService.getMessage(entityNotFoundMessage));

        return entity;
    }
}
