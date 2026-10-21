package com.gntfh.sirh_api.onboarding.repository;

import com.gntfh.sirh_api.onboarding.entity.ModeleTache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModeleTacheRepository extends JpaRepository<ModeleTache, Long> {

    List<ModeleTache> findByModeleIdOrderByOrdreAsc(Long modeleId);

    void deleteByModeleId(Long modeleId);
}
