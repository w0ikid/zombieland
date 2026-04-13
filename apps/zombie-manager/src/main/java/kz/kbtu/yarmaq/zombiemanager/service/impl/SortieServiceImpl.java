package kz.kbtu.yarmaq.zombiemanager.service.impl;

import kz.kbtu.yarmaq.zombiemanager.client.GroqClient;
import kz.kbtu.yarmaq.zombiemanager.domain.District;
import kz.kbtu.yarmaq.zombiemanager.domain.DistrictResource;
import kz.kbtu.yarmaq.zombiemanager.domain.ResourceType;
import kz.kbtu.yarmaq.zombiemanager.dto.SortieOutcome;
import kz.kbtu.yarmaq.zombiemanager.dto.DistrictCriticalEvent;
import kz.kbtu.yarmaq.zombiemanager.producer.DistrictKafkaProducer;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictRepository;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictResourceRepository;
import kz.kbtu.yarmaq.zombiemanager.service.SortieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SortieServiceImpl implements SortieService {

    private final DistrictRepository districtRepository;
    private final DistrictResourceRepository resourceRepository;
    private final GroqClient groqClient;
    private final DistrictKafkaProducer districtKafkaProducer;

    @Override
    @Transactional
    public SortieOutcome play(Integer districtId, String userAction) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new RuntimeException("District not found"));

        log.info("Sortie action on district {}: {}", districtId, userAction);

        // 1. Check is_active
        if (!Boolean.TRUE.equals(district.getIsActive())) {
            throw new RuntimeException("District is no longer active and cannot be played.");
        }

        // 2. Check ownership
        String ownerId = getCurrentUserId();
        if (district.getOwner() != null && !district.getOwner().equals(ownerId)) {
            throw new RuntimeException("You are not the owner of this district.");
        }

        // 3. Generate outcome from Groq AI
        SortieOutcome outcome = groqClient.generateOutcome(userAction);

        // 4. Apply resource updates and survival penalties
        int oldSurvivalIndex = district.getSurvivalIndex();
        if (outcome.getResources() != null) {
            for (Map.Entry<String, Double> entry : outcome.getResources().entrySet()) {
                try {
                    ResourceType type = ResourceType.valueOf(entry.getKey().toUpperCase());
                    Double change = entry.getValue();
                    
                    DistrictResource resource = resourceRepository.findByDistrictIdAndType(districtId, type)
                            .orElseGet(() -> DistrictResource.builder()
                                    .district(district)
                                    .type(type)
                                    .amount(0.0)
                                    .build());

                    double newAmount = (resource.getAmount() != null ? resource.getAmount() : 0.0) + change;
                    
                    if (newAmount < 0) {
                        double penalty = Math.abs(newAmount);
                        log.info("Insufficient {} for district {}. Resource debt: {}. Applying survival penalty.", type, districtId, penalty);
                        // Apply penalty to survival index
                        district.setSurvivalIndex(district.getSurvivalIndex() - (int) penalty);
                        newAmount = 0.0;
                    }
                    
                    resource.setAmount(newAmount);
                    resourceRepository.save(resource);
                    
                } catch (IllegalArgumentException e) {
                    log.warn("AI returned unknown resource type: {}. Ignoring.", entry.getKey());
                }
            }
        }

        // 5. Finalize survival index and check for game over
        if (district.getSurvivalIndex() <= 0) {
            district.setSurvivalIndex(0);
            district.setIsActive(false);
            log.warn("District {} has reached 0 survival and is now INACTIVE", districtId);
        }

        District saved = districtRepository.save(district);

        // 6. Check for critical survival threshold
        if (saved.getSurvivalIndex() < 40 && oldSurvivalIndex >= 40) {
            log.info("District {} reached critical survival status ({}). Sending notification.", saved.getName(), saved.getSurvivalIndex());
            districtKafkaProducer.sendCriticalEvent(DistrictCriticalEvent.builder()
                    .districtId(saved.getId())
                    .districtName(saved.getName())
                    .survivalIndex(saved.getSurvivalIndex())
                    .ownerId(saved.getOwner())
                    .message("Critical survival status after sortie: " + saved.getSurvivalIndex() + "%")
                    .build());
        }

        return outcome;
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getTokenAttributes().get("sub").toString();
        }
        return "system"; // Fallback for testing if security is not enabled or mocked
    }
}
