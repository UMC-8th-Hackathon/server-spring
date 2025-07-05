package com.umc.domain.perfume.repository;

import com.umc.domain.perfume.entity.Perfume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfumeRepository extends JpaRepository<Perfume, Long> {
}