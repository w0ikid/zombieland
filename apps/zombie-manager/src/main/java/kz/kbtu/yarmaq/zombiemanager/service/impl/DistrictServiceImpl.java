package kz.kbtu.yarmaq.zombiemanager.service.impl;

import kz.kbtu.yarmaq.zombiemanager.client.AccountClient;
import kz.kbtu.yarmaq.zombiemanager.domain.District;
import kz.kbtu.yarmaq.zombiemanager.dto.AccountResponse;
import kz.kbtu.yarmaq.zombiemanager.dto.DistrictDTO;
import kz.kbtu.yarmaq.zombiemanager.dto.ResourceDTO;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictRepository;
import kz.kbtu.yarmaq.zombiemanager.service.DistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final AccountClient accountClient;

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

        // Verify account exists in accounts-service and get its ID
        UUID yarmaqAccountId;
        try {
            AccountResponse account = accountClient.getAccountByTypeAndCurrency(
                    "USER",
                    "KZT"
            );
            yarmaqAccountId = account.getId();
            log.info("Linked yarmaq account found: {}", yarmaqAccountId);
        } catch (Exception e) {
            log.error("Failed to verify yarmaq account for user {}: {}", ownerId, e.getMessage());
            throw new RuntimeException("Could not verify Yarmaq account: " + e.getMessage());
        }

        District district = District.builder()
                .name(dto.getName())
                .owner(ownerId)
                .yarmaqAccountId(yarmaqAccountId)
                .lat(dto.getLat())
                .lng(dto.getLng())
                .survivalIndex(dto.getSurvivalIndex() != null ? dto.getSurvivalIndex() : 100)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        District saved = districtRepository.save(district);
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
    public DistrictDTO getDistrictByYarmaqAccountId(UUID yarmaqAccountId) {
        return districtRepository.findByYarmaqAccountId(yarmaqAccountId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("District not found for account: " + yarmaqAccountId));
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

        district.setName(dto.getName());
        district.setLat(dto.getLat());
        district.setLng(dto.getLng());
        if (dto.getSurvivalIndex() != null) district.setSurvivalIndex(dto.getSurvivalIndex());
        if (dto.getIsActive() != null) district.setIsActive(dto.getIsActive());

        District updated = districtRepository.save(district);
        return mapToDTO(updated);
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
                .yarmaqAccountId(district.getYarmaqAccountId())
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
