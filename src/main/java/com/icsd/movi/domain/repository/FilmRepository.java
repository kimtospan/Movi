package com.icsd.movi.domain.repository;

import com.icsd.movi.domain.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmRepository extends JpaRepository<Film, Long> {
    boolean existsByTitleIgnoreCase(String title);
    java.util.Optional<Film> findByTitleIgnoreCase(String title);
}