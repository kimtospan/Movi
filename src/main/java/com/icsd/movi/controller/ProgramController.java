package com.icsd.movi.controller;

import com.icsd.movi.domain.model.User;
import com.icsd.movi.dto.ProgramDto;
import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.service.IProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProgramController {

    private final IProgramService programService;
    private final AuthUtils authUtils;

    @GetMapping("/programs/{id}")
    public ProgramDto getProgram(@PathVariable Long id) {
        User user = getCurrentUser();
        return ProgramDto.fromEntity(programService.getProgram(id, user), user);
    }

    @GetMapping("/programs")
    public List<ProgramDto> searchPrograms(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String filmTitle,
            @RequestParam(required = false) String auditorium
    ) {
        User user = getCurrentUser();
        return programService.searchPrograms(name, description, date, filmTitle, auditorium, user)
                .stream()
                .map(p -> ProgramDto.fromEntity(p, user))
                .collect(Collectors.toList());
    }

    @PostMapping("/programs")
    public ProgramDto createProgram(@Valid @RequestBody ProgramRequest request) {
        User user = requireCurrentUser();
        return ProgramDto.fromEntity(programService.createProgram(user, request.name(), request.description(), request.startDate(), request.endDate()), user);
    }

    @PutMapping("/programs/{id}")
    public ProgramDto updateProgram(@PathVariable Long id, @Valid @RequestBody ProgramRequest request) {
        User user = requireCurrentUser();
        return ProgramDto.fromEntity(programService.updateProgram(id, user, request.name(), request.description(), request.startDate(), request.endDate()), user);
    }

    @DeleteMapping("/programs/{id}")
    public void deleteProgram(@PathVariable Long id) { programService.deleteProgram(id, requireCurrentUser()); }

    @PostMapping("/programs/{id}/programmers")
    public void addProgrammer(@PathVariable Long id, @Valid @RequestBody UserRequest request) { programService.addProgrammer(id, requireCurrentUser(), request.username()); }

    @DeleteMapping("/programs/{id}/programmers/{username}")
    public void removeProgrammer(@PathVariable Long id, @PathVariable String username) { programService.removeProgrammer(id, requireCurrentUser(), username); }

    @PostMapping("/programs/{id}/staff")
    public void addStaff(@PathVariable Long id, @Valid @RequestBody UserRequest request) { programService.addStaff(id, requireCurrentUser(), request.username()); }

    @DeleteMapping("/programs/{id}/staff/{username}")
    public void removeStaff(@PathVariable Long id, @PathVariable String username) { programService.removeStaff(id, requireCurrentUser(), username); }

    @PostMapping("/programs/{id}/state")
    public void transitionState(@PathVariable Long id, @RequestBody StateRequest request) { programService.updateProgramState(id, requireCurrentUser(), request.state()); }

    private User getCurrentUser() {
        return authUtils.getCurrentUser();
    }

    private User requireCurrentUser() {
        return authUtils.requireCurrentUser();
    }

    public record ProgramRequest(@NotBlank String name, @NotBlank String description, @NotNull LocalDate startDate, @NotNull LocalDate endDate) {}
    public record UserRequest(@NotBlank String username) {}
    public record StateRequest(@NotNull ProgramState state) {}
}
