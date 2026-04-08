package kz.kbtu.yarmaq.zombiemanager.web;

import kz.kbtu.yarmaq.zombiemanager.dto.DistrictDTO;
import kz.kbtu.yarmaq.zombiemanager.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DistrictDTO createDistrict(@RequestBody DistrictDTO districtDTO) {
        return districtService.createDistrict(districtDTO);
    }

    @GetMapping
    public List<DistrictDTO> getAllDistricts() {
        return districtService.getAllDistricts();
    }

    @GetMapping("/{id}")
    public DistrictDTO getDistrictById(@PathVariable Integer id) {
        return districtService.getDistrictById(id);
    }

    @GetMapping("/account/{yarmaqAccountId}")
    public DistrictDTO getDistrictByYarmaqAccountId(@PathVariable UUID yarmaqAccountId) {
        return districtService.getDistrictByYarmaqAccountId(yarmaqAccountId);
    }

    @PutMapping("/{id}")
    public DistrictDTO updateDistrict(@PathVariable Integer id, @RequestBody DistrictDTO districtDTO) {
        return districtService.updateDistrict(id, districtDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDistrict(@PathVariable Integer id) {
        districtService.deleteDistrict(id);
    }
}
