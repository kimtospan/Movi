package com.icsd.movi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.Screening;
import com.icsd.movi.domain.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@JsonPropertyOrder({"id", "name", "startDate", "endDate", "state", "description", "creatorName", "programmerNames", "staffNames", "auditoriumNames", "filmTitles", "creationDate"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgramDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProgramState state;
    private LocalDateTime creationDate;
    private String creatorName;
    private List<String> programmerNames;
    private List<String> staffNames;
    private List<String> auditoriumNames;
    private List<String> filmTitles;

    public static ProgramDto fromEntity(Program p, User user) {
        boolean hasPrivilegedAccess = user != null && (p.getProgrammers().contains(user) || p.getStaff().contains(user));

        ProgramDtoBuilder builder = ProgramDto.builder()
                .id(p.getId())
                .name(p.getName()) // Name and description are public info
                .description(p.getDescription())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .creationDate(p.getCreationDate())
                .state(p.getState())
                .programmerNames(p.getProgrammers().stream().map(User::getFullName).collect(Collectors.toList()))
                .auditoriumNames(p.getScreenings().stream()
                        .map(Screening::getAuditoriumName)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()))
                .filmTitles(p.getScreenings().stream()
                        .map(s -> s.getFilm().getTitle())
                        .distinct()
                        .collect(Collectors.toList()));

        // Extra details for PROGRAMMERs or STAFF of this program
        if (hasPrivilegedAccess) {
            builder.creatorName(p.getCreator().getFullName())
                   .staffNames(p.getStaff().stream().map(User::getFullName).collect(Collectors.toList()));
        }

        return builder.build();
    }

    public Program toEntity() {
        return Program.builder()
                .name(this.name)
                .description(this.description)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }
}
