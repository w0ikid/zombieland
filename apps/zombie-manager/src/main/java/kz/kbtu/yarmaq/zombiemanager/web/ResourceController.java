package kz.kbtu.yarmaq.zombiemanager.web;

import kz.kbtu.yarmaq.zombiemanager.dto.ResourceDTO;
import kz.kbtu.yarmaq.zombiemanager.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/districts/{districtId}/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'support')")
    public ResourceDTO addResource(@PathVariable Integer districtId, @RequestBody ResourceDTO resourceDTO) {
        return resourceService.addResourceToDistrict(districtId, resourceDTO);
    }

    @GetMapping
    public List<ResourceDTO> getResources(@PathVariable Integer districtId) {
        return resourceService.getResourcesByDistrictId(districtId);
    }
}
