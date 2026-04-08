package kz.kbtu.yarmaq.zombiemanager.service;

import kz.kbtu.yarmaq.zombiemanager.dto.DistrictDTO;

import java.util.List;
import java.util.UUID;

public interface DistrictService {
    DistrictDTO createDistrict(DistrictDTO districtDTO);
    DistrictDTO getDistrictById(Integer id);
    DistrictDTO getDistrictByYarmaqAccountId(UUID yarmaqAccountId);
    List<DistrictDTO> getAllDistricts();
    DistrictDTO updateDistrict(Integer id, DistrictDTO districtDTO);
    void deleteDistrict(Integer id);
}
