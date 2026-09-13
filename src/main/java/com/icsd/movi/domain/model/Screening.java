package com.icsd.movi.domain.model;

import com.icsd.movi.domain.base.BaseEntity;
import com.icsd.movi.domain.enums.ScreeningState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "screenings")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"program", "review"})
@ToString(exclude = {"program", "review"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screening extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScreeningState state;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    // Auditorium Information
    private String auditoriumName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    @ManyToOne
    @JoinColumn(name = "handler_id")
    private User handler;

    @OneToOne(mappedBy = "screening", cascade = CascadeType.ALL)
    private Review review;

    private String rejectionReason;

    @Column(columnDefinition = "TEXT")
    private String conditionalNotes; // for approval (line 64)

    private boolean finallySubmitted;
}
