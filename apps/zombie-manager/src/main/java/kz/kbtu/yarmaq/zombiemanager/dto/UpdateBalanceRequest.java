package kz.kbtu.yarmaq.zombiemanager.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBalanceRequest {
    private Double amount;
    private String description;
}
