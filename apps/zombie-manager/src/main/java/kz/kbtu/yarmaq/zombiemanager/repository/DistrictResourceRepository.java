package kz.kbtu.yarmaq.zombiemanager.repository;

import kz.kbtu.yarmaq.zombiemanager.domain.DistrictResource;
import kz.kbtu.yarmaq.zombiemanager.domain.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictResourceRepository extends JpaRepository<DistrictResource, Integer> {
    List<DistrictResource> findByDistrictId(Integer districtId);
    Optional<DistrictResource> findByDistrictIdAndType(Integer districtId, ResourceType type);
}
