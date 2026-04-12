package kz.kbtu.yarmaq.zombiemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortieOutcome {
    private String description;
    private OutcomeType outcome;
    private Map<String, Double> resources;

    public enum OutcomeType {
        success, partial, fail
    }
}
