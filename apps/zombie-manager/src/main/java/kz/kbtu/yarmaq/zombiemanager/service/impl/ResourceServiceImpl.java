package kz.kbtu.yarmaq.zombiemanager.service.impl;

import kz.kbtu.yarmaq.zombiemanager.domain.District;
import kz.kbtu.yarmaq.zombiemanager.domain.DistrictResource;
import kz.kbtu.yarmaq.zombiemanager.dto.ResourceDTO;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictRepository;
import kz.kbtu.yarmaq.zombiemanager.repository.DistrictResourceRepository;
import kz.kbtu.yarmaq.zombiemanager.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final DistrictResourceRepository resourceRepository;
    private final DistrictRepository districtRepository;

    @Override
    @Transactional
    public ResourceDTO addResourceToDistrict(Integer districtId, ResourceDTO dto) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new RuntimeException("District not found with id: " + districtId));

        DistrictResource resource = resourceRepository.findByDistrictIdAndType(districtId, dto.getType())
                .orElse(DistrictResource.builder()
                        .district(district)
                        .type(dto.getType())
                        .amount(0.0)
                        .build());

        resource.setAmount(resource.getAmount() + dto.getAmount());
        DistrictResource saved = resourceRepository.save(resource);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceDTO> getResourcesByDistrictId(Integer districtId) {
        return resourceRepository.findByDistrictId(districtId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResourceDTO updateResourceAmount(Integer districtId, ResourceDTO dto) {
        DistrictResource resource = resourceRepository.findByDistrictIdAndType(districtId, dto.getType())
                .orElseThrow(() -> new RuntimeException("Resource not found in district " + districtId));

        resource.setAmount(dto.getAmount());
        DistrictResource updated = resourceRepository.save(resource);
        return mapToDTO(updated);
    }

    private ResourceDTO mapToDTO(DistrictResource resource) {
        return ResourceDTO.builder()
                .id(resource.getId())
                .type(resource.getType())
                .amount(resource.getAmount())
                .build();
    }
}
