package kz.kbtu.yarmaq.zombiemanager.repository;

import kz.kbtu.yarmaq.zombiemanager.domain.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findAllByIsActiveTrue();
}
