package kz.kbtu.yarmaq.zombiemanager.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictDTO {
    private Integer id;
    private String name;
    private String owner;
    private UUID yarmaqAccountId;
    private Double lat;
    private Double lng;
    private Integer survivalIndex;
    private Boolean isActive;
    private List<ResourceDTO> resources;
}
