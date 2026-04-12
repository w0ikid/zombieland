package kz.kbtu.yarmaq.zombiemanager.web;

import kz.kbtu.yarmaq.zombiemanager.dto.DistrictDTO;
import kz.kbtu.yarmaq.zombiemanager.dto.SortieOutcome;
import kz.kbtu.yarmaq.zombiemanager.service.DistrictService;
import kz.kbtu.yarmaq.zombiemanager.service.SortieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;
    private final SortieService sortieService;

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

    @PutMapping("/{id}")
    public DistrictDTO updateDistrict(@PathVariable Integer id, @RequestBody DistrictDTO districtDTO) {
        return districtService.updateDistrict(id, districtDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDistrict(@PathVariable Integer id) {
        districtService.deleteDistrict(id);
    }

    @PostMapping("/{id}/sortie")
    public SortieOutcome sortie(@PathVariable Integer id, @RequestBody String action) {
        return sortieService.play(id, action);
    }
}
