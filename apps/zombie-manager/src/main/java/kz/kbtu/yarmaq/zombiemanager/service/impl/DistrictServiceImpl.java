package kz.kbtu.yarmaq.zombiemanager.service.impl;

import kz.kbtu.yarmaq.zombiemanager.domain.District;
import kz.kbtu.yarmaq.zombiemanager.dto.DistrictDTO;
import kz.kbtu.yarmaq.zombiemanager.dto.ResourceDTO;
import kz.kbtu.yarmaq.zombiemanager.dto.DistrictCriticalEvent;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictRepository;
import kz.kbtu.yarmaq.zombiemanager.service.DistrictService;
import kz.kbtu.yarmaq.zombiemanager.producer.DistrictKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final DistrictKafkaProducer districtKafkaProducer;

    @Override
    @Transactional
    public DistrictDTO createDistrict(DistrictDTO dto) {
        // Extract owner (userId) from JWT
        String ownerId;
        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            ownerId = jwtToken.getTokenAttributes().get("sub").toString();
        } else {
            throw new RuntimeException("User not authenticated or not a JWT token");
        }

        log.info("Creating district: {} for owner: {}", dto.getName(), ownerId);

        District district = District.builder()
                .name(dto.getName())
                .owner(ownerId)
                .lat(dto.getLat())
                .lng(dto.getLng())
                .survivalIndex(dto.getSurvivalIndex() != null ? dto.getSurvivalIndex() : 100)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        District saved = districtRepository.save(district);

        if (saved.getSurvivalIndex() < 40) {
            sendCriticalNotification(saved);
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictDTO getDistrictById(Integer id) {
        return districtRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("District not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistrictDTO> getAllDistricts() {
        return districtRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DistrictDTO updateDistrict(Integer id, DistrictDTO dto) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("District not found with id: " + id));

        int oldIndex = district.getSurvivalIndex();
        district.setName(dto.getName());
        district.setLat(dto.getLat());
        district.setLng(dto.getLng());
        if (dto.getSurvivalIndex() != null) district.setSurvivalIndex(dto.getSurvivalIndex());
        if (dto.getIsActive() != null) district.setIsActive(dto.getIsActive());

        District updated = districtRepository.save(district);

        if (updated.getSurvivalIndex() < 40 && oldIndex >= 40) {
            sendCriticalNotification(updated);
        }

        return mapToDTO(updated);
    }

    private void sendCriticalNotification(District district) {
        districtKafkaProducer.sendCriticalEvent(DistrictCriticalEvent.builder()
                .districtId(district.getId())
                .districtName(district.getName())
                .survivalIndex(district.getSurvivalIndex())
                .ownerId(district.getOwner())
                .message("Critical survival index: " + district.getSurvivalIndex())
                .build());
    }

    @Override
    @Transactional
    public void deleteDistrict(Integer id) {
        districtRepository.deleteById(id);
    }

    private DistrictDTO mapToDTO(District district) {
        return DistrictDTO.builder()
                .id(district.getId())
                .name(district.getName())
                .owner(district.getOwner())
                .lat(district.getLat())
                .lng(district.getLng())
                .survivalIndex(district.getSurvivalIndex())
                .isActive(district.getIsActive())
                .resources(district.getResources() != null ? district.getResources().stream()
                        .map(r -> ResourceDTO.builder()
                                .id(r.getId())
                                .type(r.getType())
                                .amount(r.getAmount())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}
