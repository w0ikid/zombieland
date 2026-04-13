package kz.kbtu.yarmaq.zombiemanager.scheduler;

import kz.kbtu.yarmaq.zombiemanager.domain.District;
import kz.kbtu.yarmaq.zombiemanager.domain.DistrictResource;
import kz.kbtu.yarmaq.zombiemanager.domain.ResourceType;
import kz.kbtu.yarmaq.zombiemanager.dto.DistrictCriticalEvent;
import kz.kbtu.yarmaq.zombiemanager.producer.DistrictKafkaProducer;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictRepository;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistrictSimulationJob {

    private final DistrictRepository districtRepository;
    private final DistrictResourceRepository resourceRepository;
    private final DistrictKafkaProducer districtKafkaProducer;
    private final Random random = new Random();

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void simulateOneCycle() {
        log.info("Starting district simulation cycle...");
        List<District> activeDistricts = districtRepository.findAllByIsActiveTrue();

        if (activeDistricts.isEmpty()) {
            log.info("No active districts to simulate.");
            return;
        }

        // Randomly pick a resource type for this cycle
        ResourceType[] resourceTypes = ResourceType.values();
        ResourceType targetResource = resourceTypes[random.nextInt(resourceTypes.length)];
        log.info("Simulation target resource for this cycle: {}", targetResource);

        for (District district : activeDistricts) {
            processDistrict(district, targetResource);
        }
    }

    private void processDistrict(District district, ResourceType resourceType) {
        int oldSurvivalIndex = district.getSurvivalIndex();
        
        DistrictResource resource = resourceRepository.findByDistrictIdAndType(district.getId(), resourceType)
                .orElseGet(() -> DistrictResource.builder()
                        .district(district)
                        .type(resourceType)
                        .amount(0.0)
                        .build());

        if (resource.getAmount() >= 5.0) {
            // SUCCESS: Consume resource and improve survival
            resource.setAmount(resource.getAmount() - 5.0);
            district.setSurvivalIndex(Math.min(100, district.getSurvivalIndex() + 3));
            log.debug("District {}: Consumed 5 {}, Survival +3. New Survival: {}", 
                    district.getName(), resourceType, district.getSurvivalIndex());
        } else {
            // FAILURE: Insufficient resources, lose survival
            district.setSurvivalIndex(Math.max(0, district.getSurvivalIndex() - 2));
            log.debug("District {}: Insufficient {}, Survival -2. New Survival: {}", 
                    district.getName(), resourceType, district.getSurvivalIndex());
        }

        resourceRepository.save(resource);

        // Check for state transitions
        if (district.getSurvivalIndex() <= 0) {
            district.setIsActive(false);
            log.warn("District {} has perished and is now INACTIVE.", district.getName());
        }

        districtRepository.save(district);

        // Notify if survival becomes critical
        if (district.getSurvivalIndex() < 40 && oldSurvivalIndex >= 40) {
            sendCriticalNotification(district);
        }
    }

    private void sendCriticalNotification(District district) {
        log.info("Sending critical notification for district: {}", district.getName());
        districtKafkaProducer.sendCriticalEvent(DistrictCriticalEvent.builder()
                .districtId(district.getId())
                .districtName(district.getName())
                .survivalIndex(district.getSurvivalIndex())
                .ownerId(district.getOwner())
                .message("Simulation Alert: District survival index has fallen to " + district.getSurvivalIndex())
                .build());
    }
}
