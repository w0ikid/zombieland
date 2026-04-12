package kz.kbtu.yarmaq.zombiemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictCriticalEvent {
    private Integer districtId;
    private String districtName;
    private Integer survivalIndex;
    private String ownerId;
    private String message;
}
