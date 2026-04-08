package kz.kbtu.yarmaq.zombiemanager.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private UUID id;
    private String userId;
    private String name;
    private String type;
    private String currency;
    private Double balance;
    private OffsetDateTime createdAt;
}
