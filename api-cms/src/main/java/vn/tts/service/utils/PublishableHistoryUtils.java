package vn.tts.service.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.util.Pair;
import vn.tts.entity.PublishableEntity;
import vn.tts.enums.ContentHistoryStatus;
import vn.tts.enums.ContentStatus;
import vn.tts.enums.DeleteEnum;
import vn.tts.model.response.PublishableHistoryResponse;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
public class PublishableHistoryUtils<
        EntityT extends PublishableEntity,
        HistoryT extends PublishableHistoryResponse,
        RepositoryT extends JpaRepository<EntityT, UUID> & RevisionRepository<EntityT, UUID, Integer>
        > {
    private final RepositoryT repository;

    public List<Pair<HistoryT, Revision<Integer, EntityT>>> getUpdatedHistoryRevisions(
            UUID id,
            Function<EntityT, HistoryT> mapRevisionToHistory
    ) {
        List<Pair<HistoryT, Revision<Integer, EntityT>>> responses = repository.findRevisions(id)
                .stream().parallel().map(r -> {
                    EntityT entity = r.getEntity();

                    HistoryT history = updateRevisionStatus(mapRevisionToHistory.apply(entity), entity, r);

                    return Pair.of(history, r);
                }).toList();

        updateHistoryStatuses(responses.stream().map(Pair::getFirst).toList());

        return responses;
    }

    private HistoryT updateRevisionStatus(HistoryT res, EntityT entity, Revision<Integer, EntityT> r) {
        ContentStatus status = entity.getStatus();
        if (entity.getIsDelete().equals(DeleteEnum.YES))
            res.setStatus(ContentHistoryStatus.DELETED);
        else if (r.getMetadata().getRevisionType() == RevisionMetadata.RevisionType.INSERT
                 || status == ContentStatus.DRAFT)
            res.setStatus(ContentHistoryStatus.CREATED);
        else
            res.setStatus(ContentHistoryStatus.valueOf(status.toString()));

        res.setEventDate(r.getRequiredRevisionInstant());

        return res;
    }

    private void updateHistoryStatuses(List<HistoryT> histories) {
        if (histories == null || histories.isEmpty()) return;

        ContentHistoryStatus prevOriginal = null;

        for (HistoryT hist : histories) {
            if (hist == null) continue;

            ContentHistoryStatus original = hist.getStatus();

            if (prevOriginal == null) {
                hist.setStatus(ContentHistoryStatus.CREATED);
                prevOriginal = original;
                continue;
            }

            if (original.equals(ContentHistoryStatus.DELETED)) {
                prevOriginal = original;
                continue;
            }

            if (original.equals(prevOriginal)) {
                hist.setStatus(ContentHistoryStatus.UPDATED);
            }

            prevOriginal = original;
        }
    }
}