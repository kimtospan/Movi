package com.icsd.movi.service;

import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.domain.enums.ScreeningState;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.Film;
import com.icsd.movi.domain.model.Review;
import com.icsd.movi.domain.model.Screening;
import com.icsd.movi.domain.model.User;
import com.icsd.movi.domain.repository.ProgramRepository;
import com.icsd.movi.domain.repository.ScreeningRepository;
import com.icsd.movi.domain.repository.UserRepository;
import com.icsd.movi.domain.repository.FilmRepository;
import com.icsd.movi.logging.AuditLogger;
import com.icsd.movi.security.RateLimiter;
import com.icsd.movi.exception.BadRequestException;
import com.icsd.movi.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icsd.movi.domain.repository.specification.ScreeningSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningService implements IScreeningService {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final AuditLogger auditLogger;
    private final RateLimiter rateLimiter;
    private final SearchPipeline searchPipeline;

    @Override
    public Screening getById(Long id) {
        return screeningRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Screening not found"));
    }
    @Transactional
    public Screening createScreening(User user, Long programId, String name, Long filmId, String auditoriumName) {
        Program program = programRepository.findById(programId).orElseThrow(() -> new NotFoundException("Program not found"));

        if (program.getState() != ProgramState.SUBMISSION) {
            throw new BadRequestException("Screenings can only be created while the program is in SUBMISSION state");
        }
        

        if (program.getProgrammers().contains(user) || program.getStaff().contains(user)) {
            throw new RuntimeException("Programmers and Staff cannot be submitters in the same program");
        }

        // Prevent duplicate execution
        if (screeningRepository.existsByProgramAndName(program, name)) {
            throw new RuntimeException("Screening with this name already exists in the program");
        }
        Film film = filmRepository.findById(filmId).orElseThrow(() -> new NotFoundException("Film not found"));
        
        Screening screening = Screening.builder()
                .name(name)
                .program(program)
                .submitter(user)
                .state(ScreeningState.CREATED)
                .film(film)
                .auditoriumName(auditoriumName)
                .build();
        screening = screeningRepository.save(screening);
        program.getScreenings().add(screening);
        auditLogger.logAction("CREATE_SCREENING", "Screening ID: " + screening.getId(), user.getUsername());
        return screening;
    }

    @Transactional
    public Screening updateScreening(Long id, User user, String name, Long filmId, String auditoriumName, LocalDateTime start, LocalDateTime end) {
        Screening screening = getById(id);
        if (!screening.getSubmitter().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }
        if (screening.getState() != ScreeningState.CREATED) {
            throw new RuntimeException("Screening can only be updated in CREATED state");
        }
        if (start == null || end == null) {
            throw new BadRequestException("Start time and end time are required");
        }
        Film film = filmRepository.findById(filmId).orElseThrow(() -> new NotFoundException("Film not found"));

        if (!end.isAfter(start) || film.getDuration() == null || film.getDuration() <= 0) {
            throw new BadRequestException("Screening times and duration must be positive and ordered");
        }
        if (java.time.Duration.between(start, end).toMinutes() < film.getDuration()) {
            throw new BadRequestException("End time must be at least duration minutes after start time");
        }
        ensureNoScheduleOverlap(screening, start, end, auditoriumName);

        screening.setName(name);
        screening.setFilm(film);
        screening.setAuditoriumName(auditoriumName);
        screening.setStartTime(start);
        screening.setEndTime(end);
        return screeningRepository.save(screening);
    }

    private void ensureNoScheduleOverlap(Screening candidate, LocalDateTime start, LocalDateTime end, String auditoriumName) {
        boolean overlaps = screeningRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(candidate.getId()))
                .filter(existing -> existing.getState() != ScreeningState.REJECTED)
                .filter(existing -> auditoriumName != null && auditoriumName.equals(existing.getAuditoriumName()))
                .anyMatch(existing -> existing.getStartTime() != null && existing.getEndTime() != null
                        && start.isBefore(existing.getEndTime()) && end.isAfter(existing.getStartTime()));
        if (overlaps) {
            throw new BadRequestException("Screening timeslot overlaps another screening in the auditorium");
        }
    }

    @Transactional
    public void submitScreening(Long id, User user) {
        if (!rateLimiter.isAllowed(user.getUsername(), "SUBMIT_SCREENING")) {
            throw new RuntimeException("Rate limit exceeded for submissions");
        }
        Screening screening = getById(id);
        if (!screening.getSubmitter().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }
        Program program = screening.getProgram();
        if (program.getState() != ProgramState.SUBMISSION) {
            throw new RuntimeException("Program must be in SUBMISSION state");
        }
        if (screening.getState() != ScreeningState.CREATED) {
            throw new BadRequestException("Only CREATED screenings can be submitted");
        }
        // derived end time via film duration
        if (screening.getFilm() == null || screening.getAuditoriumName() == null || screening.getStartTime() == null || screening.getFilm().getDuration() == null) {
            throw new RuntimeException("Screening information is incomplete");
        }
        
        // derived end time
        if (screening.getEndTime() == null) {
            screening.setEndTime(screening.getStartTime().plusMinutes(screening.getFilm().getDuration()));
        }
        
        // End time check: difference with start time must be >= film duration
        if (Duration.between(screening.getStartTime(), screening.getEndTime()).toMinutes() < screening.getFilm().getDuration()) {
            throw new BadRequestException("End time must be after start time by at least film duration");
        }
        
        screening.setState(ScreeningState.SUBMITTED);
        screeningRepository.save(screening);
        auditLogger.logAction("SUBMIT_SCREENING", "Screening ID: " + id, user.getUsername());
    }

    @Transactional
    public void withdrawScreening(Long id, User user) {
        Screening screening = screeningRepository.findById(id).orElseThrow();
        if (!screening.getSubmitter().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }
        if (screening.getState() != ScreeningState.CREATED) {
            throw new RuntimeException("Screening can only be withdrawn in CREATED state");
        }
        screeningRepository.delete(screening);
    }

    @Transactional
    public void assignHandler(Long id, User user, String handlerUsername) {
        Screening screening = screeningRepository.findById(id).orElseThrow();
        Program program = screening.getProgram();
        if (!program.getProgrammers().contains(user)) {
            throw new RuntimeException("Unauthorized");
        }
        if (program.getState() != ProgramState.ASSIGNMENT) {
            throw new RuntimeException("Handler assignment allowed only in ASSIGNMENT state");
        }
        if (screening.getHandler() != null) {
            throw new BadRequestException("A screening can have only one handler");
        }
        User handler = userRepository.findByUsername(handlerUsername).orElseThrow();
        if (!program.getStaff().contains(handler)) {
            throw new RuntimeException("User must be in the program's STAFF set");
        }
        screening.setHandler(handler);
        screeningRepository.save(screening);
    }

    @Transactional
    public void reviewScreening(Long id, User user, Integer score, String comments) {
        Screening screening = getById(id);
        if (!user.equals(screening.getHandler())) {
            throw new RuntimeException("Unauthorized: Only assigned handler can review");
        }
        if (screening.getProgram().getState() != ProgramState.REVIEW) {
            throw new RuntimeException("Review allowed only in REVIEW state");
        }
        if (screening.getState() != ScreeningState.SUBMITTED) {
            throw new BadRequestException("Only submitted screenings can be reviewed");
        }
        if (score == null || score < 0 || score > 10) {
            throw new BadRequestException("Review score must be between 0 and 10");
        }
        
        Review review = Review.builder()
                .screening(screening)
                .reviewer(user)
                .score(score)
                .comments(comments)
                .build();
        screening.setReview(review);
        screening.setState(ScreeningState.REVIEWED);
        screeningRepository.save(screening);
    }

    @Transactional
    public void approveScreening(Long id, User user, String notes) {
        Screening screening = getById(id);
        if (!screening.getSubmitter().equals(user)) {
            throw new RuntimeException("Unauthorized: Only the screening submitter can approve it");
        }
        if (screening.getProgram().getState() != ProgramState.SCHEDULING) {
            throw new RuntimeException("Approval allowed only in SCHEDULING state");
        }
        if (screening.getStartTime() == null || screening.getEndTime() == null || screening.getAuditoriumName() == null) {
            throw new BadRequestException("A screening requires a complete schedule before approval");
        }
        ensureNoScheduleOverlap(screening, screening.getStartTime(), screening.getEndTime(), screening.getAuditoriumName());

        screening.setState(ScreeningState.APPROVED);
        screening.setConditionalNotes(notes);
        screeningRepository.save(screening);
    }

    @Transactional
    public void rejectScreening(Long id, User user, String reason) {
        Screening screening = screeningRepository.findById(id).orElseThrow();
        Program program = screening.getProgram();
        if (!program.getProgrammers().contains(user)) {
            throw new RuntimeException("Unauthorized");
        }
        if (program.getState() != ProgramState.SCHEDULING && program.getState() != ProgramState.DECISION) {
            throw new RuntimeException("Rejection allowed only in SCHEDULING or DECISION states");
        }
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("A rejection reason is required");
        }
        screening.setState(ScreeningState.REJECTED);
        screening.setRejectionReason(reason);
        screeningRepository.save(screening);
    }

    @Transactional
    public void finalSubmitScreening(Long id, User user) {
        Screening screening = screeningRepository.findById(id).orElseThrow();
        // Check User Is Submitter
        if (!screening.getSubmitter().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }
        // Check Program is in state FINAL PUBLICATION
        if (screening.getProgram().getState() != ProgramState.FINAL_PUBLICATION) {
             throw new RuntimeException("Final submission allowed only in FINAL_PUBLICATION state"); // Wait, state is FINAL_PUBLICATION in my enum, text says FINAL_PUBLICATION
        }
        // Check screening state is APPROVED
        if (screening.getState() != ScreeningState.APPROVED) {
            throw new RuntimeException("Only approved screenings can be finally submitted");
        }
        screening.setState(ScreeningState.FINAL_SUBMIT);
        screening.setFinallySubmitted(true);
        screeningRepository.save(screening);
        auditLogger.logAction("FINAL_SUBMIT_SCREENING", "Screening ID: " + id, user.getUsername());
    }

    @Transactional
    public void acceptScreening(Long id, User user) {
        Screening screening = getById(id);
        if (!screening.getProgram().getProgrammers().contains(user)) {
            throw new RuntimeException("Unauthorized");
        }
        if (screening.getProgram().getState() != ProgramState.DECISION) {
            throw new RuntimeException("Acceptance allowed only in DECISION state");
        }
        if (screening.getState() != ScreeningState.FINAL_SUBMIT) {
            throw new RuntimeException("Screening must be finally submitted to be accepted");
        }

        screening.setState(ScreeningState.SCHEDULED);
        screeningRepository.save(screening);
        auditLogger.logAction("ACCEPT_SCREENING", "Screening ID: " + id, user.getUsername());
    }

    @Transactional(readOnly = true)
    public List<Screening> searchScreenings(Long programId, String title, String cast, String genre, LocalDateTime start, LocalDateTime end, User user, boolean sortByTime) {
        Specification<Screening> spec = ScreeningSpecification.search(programId, title, cast, genre, start, end);
        Comparator<Screening> sortComparator = sortByTime
                ? Comparator.comparing(Screening::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                : Comparator
                .comparing((Screening s) -> s.getFilm() == null ? null : s.getFilm().getGenres(), Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(s -> s.getFilm() == null ? null : s.getFilm().getTitle(), Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        return searchPipeline.execute(
                        () -> {
                            if (user != null && !rateLimiter.isAllowed(user.getUsername(), "SEARCH_SCREENINGS")) {
                                throw new RuntimeException("Rate limit exceeded for searches");
                            }
                        },
                        spec,
                        screeningRepository,
                        user,
                        this::isVisibleToUser
                ).stream()
                .sorted(sortComparator)
                .toList();
    }

    // If Screening exists but user does not have clearance, return "Screening Not Found"
    // So as to not leak existance.
    @Transactional(readOnly = true)
    public Screening getScreening(Long id, User user) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Screening not found"));
        if (!isVisibleToUser(screening, user)) {
            throw new NotFoundException("Screening not found");
        }
        return screening;
    }

    // Source of truth for user clearance
    private boolean isVisibleToUser(Screening s, User user) {
        // Visitor/User: only SCHEDULED screenings of ANNOUNCED programs
        if (s.getState() == ScreeningState.SCHEDULED && s.getProgram().getState() == ProgramState.ANNOUNCED) {
            return true;
        }
        if (user == null) return false;
        
        // PROGRAMMER: all screenings in their program
        if (s.getProgram().getProgrammers().contains(user)) {
            return true;
        }
        // STAFF: only assigned screenings (plus visitor rights, already handled above)
        if (user.equals(s.getHandler())) {
            return true;
        }
        // SUBMITTER: only own screenings
        if (user.equals(s.getSubmitter())) {
            return true;
        }
        
        return false;
    }
}
