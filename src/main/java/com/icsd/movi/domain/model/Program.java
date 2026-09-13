package com.icsd.movi.domain.model;

import com.icsd.movi.domain.base.BaseEntity;
import com.icsd.movi.domain.enums.ProgramState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "programs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "description", "startDate"})
})
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"programmers", "staff", "screenings"})
@ToString(exclude = {"programmers", "staff", "screenings"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramState state;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
            name = "program_programmers",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> programmers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "program_staff",
            joinColumns = @JoinColumn(name = "program_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> staff = new HashSet<>();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Screening> screenings = new HashSet<>();

}
