package com.icsd.movi.domain.repository;

import com.icsd.movi.domain.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.User;

public interface ScreeningRepository extends JpaRepository<Screening, Long>, JpaSpecificationExecutor<Screening> {
    boolean existsByProgramAndName(Program program, String name);
}
