package com.icsd.movi.controller;

import com.icsd.movi.domain.model.User;
import com.icsd.movi.dto.ScreeningDto;
import com.icsd.movi.service.IScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScreeningController {

    private final IScreeningService screeningService;
    private final AuthUtils authUtils;

    @GetMapping("/screenings/{id}")
    public ScreeningDto getScreening(@PathVariable Long id) {
        User user = getCurrentUser();
        return ScreeningDto.fromEntity(screeningService.getScreening(id, user), user);
    }

    @GetMapping("/screenings")
    public List<ScreeningDto> searchScreenings(
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String cast,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean sortByTime
    ) {
        User user = getCurrentUser();
        return screeningService.searchScreenings(programId, title, cast, genre, start, end, user, sortByTime)
                .stream()
                .map(s -> ScreeningDto.fromEntity(s, user))
                .collect(Collectors.toList());
    }

    @PostMapping("/screenings")
    public ScreeningDto createScreening(@Valid @RequestBody ScreeningRequest request) {
        User user = requireCurrentUser();
        return ScreeningDto.fromEntity(screeningService.createScreening(user, request.programId(), request.name(), request.filmId(), request.auditoriumName()), user);
    }

    @PutMapping("/screenings/{id}")
    public ScreeningDto updateScreening(@PathVariable Long id, @Valid @RequestBody ScreeningUpdateRequest request) {
        User user = requireCurrentUser();
        return ScreeningDto.fromEntity(screeningService.updateScreening(id, user, request.name(), request.filmId(), request.auditoriumName(), request.startTime(), request.endTime()), user);
    }

    @PostMapping("/screenings/{id}/submit")
    public void submit(@PathVariable Long id) { screeningService.submitScreening(id, requireCurrentUser()); }

    @DeleteMapping("/screenings/{id}")
    public void withdraw(@PathVariable Long id) { screeningService.withdrawScreening(id, requireCurrentUser()); }

    @PostMapping("/screenings/{id}/handler")
    public void assignHandler(@PathVariable Long id, @RequestBody HandlerRequest request) { screeningService.assignHandler(id, requireCurrentUser(), request.username()); }

    @PostMapping("/screenings/{id}/review")
    public void review(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) { screeningService.reviewScreening(id, requireCurrentUser(), request.score(), request.comments()); }

    @PostMapping("/screenings/{id}/approve")
    public void approve(@PathVariable Long id, @RequestBody NotesRequest request) { screeningService.approveScreening(id, requireCurrentUser(), request.notes()); }

    @PostMapping("/screenings/{id}/reject")
    public void reject(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) { screeningService.rejectScreening(id, requireCurrentUser(), request.reason()); }

    @PostMapping("/screenings/{id}/final-submit")
    public void finalSubmit(@PathVariable Long id) { screeningService.finalSubmitScreening(id, requireCurrentUser()); }

    @PostMapping("/screenings/{id}/accept")
    public void accept(@PathVariable Long id) { screeningService.acceptScreening(id, requireCurrentUser()); }

    private User getCurrentUser() {
        return authUtils.getCurrentUser();
    }

    private User requireCurrentUser() {
        return authUtils.requireCurrentUser();
    }

    public record ScreeningRequest(@NotNull Long programId, @NotBlank String name, @NotNull Long filmId, String auditoriumName) {}
    public record ScreeningUpdateRequest(@NotBlank String name, @NotNull Long filmId, @NotBlank String auditoriumName, @NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime) {}
    public record HandlerRequest(@NotBlank String username) {}
    public record ReviewRequest(@NotNull Integer score, @NotBlank String comments) {}
    public record NotesRequest(String notes) {}
    public record ReasonRequest(@NotBlank String reason) {}
}
