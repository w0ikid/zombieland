package kz.kbtu.yarmaq.zombiemanager.dto;

import kz.kbtu.yarmaq.zombiemanager.domain.ResourceType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceDTO {
    private Integer id;
    private ResourceType type;
    private Double amount;
}
