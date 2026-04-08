package kz.kbtu.yarmaq.zombiemanager.service;

import kz.kbtu.yarmaq.zombiemanager.dto.ResourceDTO;

import java.util.List;

public interface ResourceService {
    ResourceDTO addResourceToDistrict(Integer districtId, ResourceDTO resourceDTO);
    List<ResourceDTO> getResourcesByDistrictId(Integer districtId);
    ResourceDTO updateResourceAmount(Integer districtId, ResourceDTO resourceDTO);
}
