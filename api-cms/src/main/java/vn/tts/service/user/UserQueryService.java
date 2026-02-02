package vn.tts.service.user;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.tts.entity.BaseEntity;
import vn.tts.entity.UserEntity;
import vn.tts.exception.UserNotFoundException;
import vn.tts.model.payload.user.SearchUserPayload;
import vn.tts.model.response.PaginationResponse;
import vn.tts.model.response.user.UserAuditResponse;
import vn.tts.model.response.user.UserDetailResponse;
import vn.tts.model.response.user.UserHistoryResponse;
import vn.tts.repository.UserRepository;
import vn.tts.service.BaseService;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQueryService extends BaseService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public PaginationResponse<List<UserDetailResponse>> listUser(SearchUserPayload payload, Integer page, Integer pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, BaseEntity.Fields.createdAt);
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        Page<UserDetailResponse> data = userRepository.filter(
                payload,
                Objects.isNull(payload.getFromDate()) ? null : payload.getFromDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                Objects.isNull(payload.getToDate()) ? null : payload.getToDate()
                        .plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                pageable
        ).map(entity -> {
            UserDetailResponse res = modelMapper.map(entity, UserDetailResponse.class);
            res.setUserId(entity.getId());
            return res;
        });

        PaginationResponse<List<UserDetailResponse>> response = new PaginationResponse<>();
        response.setData(data.getContent());
        response.setTotal(data.getTotalElements());
        return response;
    }

    public List<UserHistoryResponse> getUserHistory(UUID userId) {
        return userRepository.findRevisions(userId).stream()
                .map(r -> {
                    UserEntity entity = r.getEntity();
                    return new UserHistoryResponse(
                            r.getRequiredRevisionInstant(),
                            r.getMetadata().getRevisionType(),
                            entity.getId(),
                            entity.getUsername(),
                            entity.getFullName(),
                            entity.getAvatar(),
                            entity.getEmail(),
                            entity.getPhone(),
                            entity.getGender(),
                            entity.getStatus()
                    );
                }).toList();
    }

    public UserAuditResponse getUserAudit(UUID id) throws Exception {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("message.user.not.found"));

        UserAuditResponse response = modelMapper.map(
                user,
                UserAuditResponse.class
        );
        if (response.getAvatar() != null && !response.getAvatar().isEmpty())
            response.setAvatar(minioService.getPreSignedUrl(response.getAvatar()));

        response.setHistory(getUserHistory(id));
        return response;
    }
}
