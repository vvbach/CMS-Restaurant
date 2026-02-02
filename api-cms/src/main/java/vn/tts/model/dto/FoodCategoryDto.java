package vn.tts.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodCategoryDto {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private Short isDelete;
    private String createdByName;
    private Instant createdAt;
    private String updatedByName;
    private Instant updatedAt;
    private String deletionReason;
    private String rejectionReason;
    private String unpublishReason;
    private Integer rev;
}
