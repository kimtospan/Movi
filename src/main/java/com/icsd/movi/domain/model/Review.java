package com.icsd.movi.domain.model;

import com.icsd.movi.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"screening"})
@ToString(exclude = {"screening"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comments;
}
