package vn.tts.service.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import vn.tts.entity.BaseEntity;
import vn.tts.exception.AppBadRequestException;
import vn.tts.service.BaseService;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
public class QueryService<
        EntityT extends BaseEntity,
        ResponseT,
        RepositoryT extends JpaRepository<EntityT, UUID>
                & RevisionRepository<EntityT, UUID, Integer>
        >{
    private final RepositoryT repository;
    private final BaseService baseService;

    public List<ResponseT> findAll(Function<EntityT, ResponseT> getResponse) {
        return repository.findAll().parallelStream()
                .map(getResponse)
                .toList();
    }

    public ResponseT findById(UUID id, Function<EntityT, ResponseT> getResponse) {
        EntityT entity = repository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("id", baseService.getMessage("message.entity.not.found")));

        return getResponse.apply(entity);
    }
}
