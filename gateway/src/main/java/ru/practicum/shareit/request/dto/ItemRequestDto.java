package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {
    private Long id;

    @NotBlank
    private String description;
    private LocalDateTime created;
    private List<RequestItemDto> items;
}

