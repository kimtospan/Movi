package com.icsd.movi.domain.model;

import com.icsd.movi.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "films")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String title;

    @Column(name = "film_cast")
    private String cast;
    private String genres;
    private Integer duration;
}