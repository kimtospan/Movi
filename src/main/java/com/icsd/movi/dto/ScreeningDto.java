package com.icsd.movi.dto;

import com.icsd.movi.domain.enums.ScreeningState;
import com.icsd.movi.domain.model.Screening;
import com.icsd.movi.domain.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScreeningDto {
    private Long id;
    private String name;
    private Long programId;
    private Long filmId;
    private ScreeningState state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String filmTitle;
    private String filmCast;
    private String filmGenres;
    private Integer filmDuration;
    private String auditoriumName;
    private String submitterName;
    private String handlerName;
    private Integer reviewScore;
    private String reviewComments;
    private String rejectionReason;
    private String conditionalNotes;

    public static ScreeningDto fromEntity(Screening s, User user) {
        // Check if user has full access
        boolean hasFullAccess = false;
        if (user != null) {
            if (s.getProgram().getProgrammers().contains(user) || 
                user.equals(s.getSubmitter()) || 
                user.equals(s.getHandler())) {
                hasFullAccess = true;
            }
        }

        if (hasFullAccess) {
            ScreeningDtoBuilder builder = ScreeningDto.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .programId(s.getProgram().getId())
                    .filmId(s.getFilm().getId())
                    .state(s.getState())
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .filmTitle(s.getFilm().getTitle())
                    .filmCast(s.getFilm().getCast())
                    .filmGenres(s.getFilm().getGenres())
                    .filmDuration(s.getFilm().getDuration())
                    .auditoriumName(s.getAuditoriumName())
                    .submitterName(s.getSubmitter().getFullName())
                    .rejectionReason(s.getRejectionReason())
                    .conditionalNotes(s.getConditionalNotes());
            
            if (s.getHandler() != null) {
                builder.handlerName(s.getHandler().getFullName());
            }
            if (s.getReview() != null) {
                builder.reviewScore(s.getReview().getScore())
                       .reviewComments(s.getReview().getComments());
            }
            return builder.build();
        } else {
            // Redacted view (Visitor/Public)
            return ScreeningDto.builder()
                    .filmId(s.getFilm().getId())
                    .filmTitle(s.getFilm().getTitle())
                    .filmGenres(s.getFilm().getGenres())
                    .startTime(s.getStartTime())
                    .auditoriumName(s.getAuditoriumName())
                    .build();
        }
    }

    public Screening toEntity() {
        return Screening.builder()
                .name(this.name)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .auditoriumName(this.auditoriumName)
                .build();
    }
}
