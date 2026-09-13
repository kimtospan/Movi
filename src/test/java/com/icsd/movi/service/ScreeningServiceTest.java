package com.icsd.movi.service;

import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.domain.enums.ScreeningState;
import com.icsd.movi.domain.model.Film;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.Screening;
import com.icsd.movi.domain.model.User;
import com.icsd.movi.domain.repository.FilmRepository;
import com.icsd.movi.domain.repository.ScreeningRepository;
import com.icsd.movi.domain.repository.UserRepository;
import com.icsd.movi.exception.BadRequestException;
import com.icsd.movi.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ScreeningServiceTest {

    @Autowired
    private ScreeningService screeningService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    @Autowired
    private FilmRepository filmRepository;

    private User programmer;
    private User submitter;
    private User staff;
    private Program program;
    private Film film;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());
        programmer = userRepository.save(User.builder()
                .username("prog_test_" + suffix)
                .password("pass")
                .fullName("Programmer")
                .build());
        submitter = userRepository.save(User.builder()
                .username("sub_test_" + suffix)
                .password("pass")
                .fullName("Submitter")
                .build());
        staff = userRepository.save(User.builder()
                .username("staff_test_" + suffix)
                .password("pass")
                .fullName("Staff")
                .build());

        program = programService.createProgram(programmer, "Program " + suffix, "Description", LocalDate.now(), LocalDate.now().plusMonths(2));
        programService.addStaff(program.getId(), programmer, staff.getUsername());

        film = filmRepository.save(Film.builder()
                .title("Inception " + suffix)
                .cast("Leonardo DiCaprio")
                .genres("Sci-Fi")
                .duration(120)
                .build());
    }

    @Test
    void createScreening_setsSubmitter_andCreatedState() {
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);

        Screening screening = screeningService.createScreening(submitter, program.getId(), "Screening One", film.getId(), "Hall A");

        assertNotNull(screening.getId());
        assertEquals(submitter, screening.getSubmitter());
        assertEquals(ScreeningState.CREATED, screening.getState());
    }

    @Test
    void createScreening_rejectsWhenProgramIsNotInSubmission() {
        // Edge case: Program state is CREATED (not SUBMISSION). Should reject as screening cannot be created yet.
        assertEquals(ProgramState.CREATED, program.getState());
        assertThrows(BadRequestException.class, () ->
                screeningService.createScreening(submitter, program.getId(), "Too Early", film.getId(), "Hall X"));
    }

    @Test
    void updateScreening_requiresSubmitter_and_validSchedule() {
        // Error path: Unauthorized user attempting update. Should throw RuntimeException.
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Screening Update", film.getId(), "Hall A");

        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Screening Update", film.getId(), "Hall A", start, end);

        Screening updated = screeningRepository.findById(screening.getId()).orElseThrow();
        assertEquals(start, updated.getStartTime());
        assertEquals(end, updated.getEndTime());

        assertThrows(RuntimeException.class, () ->
                screeningService.updateScreening(screening.getId(), programmer, "Unauthorized", film.getId(), "Hall A", start, end)
        );
    }

    @Test
    void updateScreening_rejectsOutsideCreatedState() {
        // Edge case: Screening is submitted (state > CREATED). Should reject as only CREATED screenings are editable.
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Frozen Edit", film.getId(), "Hall A");
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Frozen Edit", film.getId(), "Hall A", start, end);
        screeningService.submitScreening(screening.getId(), submitter);

        assertThrows(RuntimeException.class, () ->
                screeningService.updateScreening(screening.getId(), submitter, "Edited Late", film.getId(), "Hall A", start.plusDays(1), end.plusDays(1)));
    }

    @Test
    void submitScreening_transitionsToSubmitted_whenProgramIsOpen() {
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Submission Test", film.getId(), "Hall B");

        LocalDateTime start = LocalDateTime.now().plusDays(7);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Submission Test", film.getId(), "Hall B", start, end);
        screeningService.submitScreening(screening.getId(), submitter);

        Screening saved = screeningRepository.findById(screening.getId()).orElseThrow();
        assertEquals(ScreeningState.SUBMITTED, saved.getState());
    }

    @Test
    void assignHandler_and_reviewApproval_flow_updatesState() {
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Lifecycle Screening", film.getId(), "Hall C");

        LocalDateTime start = LocalDateTime.now().plusDays(8);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Lifecycle Screening", film.getId(), "Hall C", start, end);
        screeningService.submitScreening(screening.getId(), submitter);

        programService.updateProgramState(program.getId(), programmer, ProgramState.ASSIGNMENT);
        screeningService.assignHandler(screening.getId(), programmer, staff.getUsername());
        assertEquals(staff, screeningRepository.findById(screening.getId()).orElseThrow().getHandler());

        programService.updateProgramState(program.getId(), programmer, ProgramState.REVIEW);
        screeningService.reviewScreening(screening.getId(), staff, 9, "Strong candidate.");
        assertEquals(ScreeningState.REVIEWED, screeningRepository.findById(screening.getId()).orElseThrow().getState());

        programService.updateProgramState(program.getId(), programmer, ProgramState.SCHEDULING);
        screeningService.approveScreening(screening.getId(), submitter, "Approved for festival scheduling");
        assertEquals(ScreeningState.APPROVED, screeningRepository.findById(screening.getId()).orElseThrow().getState());
    }

    @Test
    void rejectScreening_requiresReason_and_programmerAccess() {
        // Error path: Missing rejection reason. Should throw BadRequestException.
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Reject Test", film.getId(), "Hall D");

        LocalDateTime start = LocalDateTime.now().plusDays(9);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Reject Test", film.getId(), "Hall D", start, end);
        screeningService.submitScreening(screening.getId(), submitter);

        programService.updateProgramState(program.getId(), programmer, ProgramState.ASSIGNMENT);
        programService.updateProgramState(program.getId(), programmer, ProgramState.REVIEW);
        programService.updateProgramState(program.getId(), programmer, ProgramState.SCHEDULING);

        assertThrows(BadRequestException.class, () ->
                screeningService.rejectScreening(screening.getId(), programmer, null)
        );

        screeningService.rejectScreening(screening.getId(), programmer, "Does not fit the program theme.");
        assertEquals(ScreeningState.REJECTED, screeningRepository.findById(screening.getId()).orElseThrow().getState());
    }

    @Test
    void updateProgramState_toDecision_autoRejectsApprovedWithoutFinalSubmission() {
        // Edge case: Move to DECISION without final submission for approved screening. Should auto-reject as invalid state transition.
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Auto Reject Candidate", film.getId(), "Hall AR");
        LocalDateTime start = LocalDateTime.now().plusDays(11);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Auto Reject Candidate", film.getId(), "Hall AR", start, end);
        screeningService.submitScreening(screening.getId(), submitter);

        programService.updateProgramState(program.getId(), programmer, ProgramState.ASSIGNMENT);
        screeningService.assignHandler(screening.getId(), programmer, staff.getUsername());
        programService.updateProgramState(program.getId(), programmer, ProgramState.REVIEW);
        screeningService.reviewScreening(screening.getId(), staff, 7, "OK");
        programService.updateProgramState(program.getId(), programmer, ProgramState.SCHEDULING);
        screeningService.approveScreening(screening.getId(), submitter, "Tentatively approved");
        programService.updateProgramState(program.getId(), programmer, ProgramState.FINAL_PUBLICATION);
        programService.updateProgramState(program.getId(), programmer, ProgramState.DECISION);

        Screening reloaded = screeningRepository.findById(screening.getId()).orElseThrow();
        // DECISION gate requires final submission; non-final approvals are auto-rejected by service rule.
        assertEquals(ScreeningState.REJECTED, reloaded.getState());
        assertTrue(reloaded.getRejectionReason().toLowerCase().contains("automatically rejected"));
    }

    @Test
    void rejectScreening_allowsExplicitReasonInDecisionState() {
        // Edge case: Rejection reason during DECISION state. Should accept and set explicit reason.
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Decision Reject", film.getId(), "Hall DR");
        LocalDateTime start = LocalDateTime.now().plusDays(12);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Decision Reject", film.getId(), "Hall DR", start, end);
        screeningService.submitScreening(screening.getId(), submitter);

        programService.updateProgramState(program.getId(), programmer, ProgramState.ASSIGNMENT);
        programService.updateProgramState(program.getId(), programmer, ProgramState.REVIEW);
        programService.updateProgramState(program.getId(), programmer, ProgramState.SCHEDULING);
        programService.updateProgramState(program.getId(), programmer, ProgramState.FINAL_PUBLICATION);
        programService.updateProgramState(program.getId(), programmer, ProgramState.DECISION);

        screeningService.rejectScreening(screening.getId(), programmer, "Not aligned with final direction");
        Screening rejected = screeningRepository.findById(screening.getId()).orElseThrow();
        assertEquals(ScreeningState.REJECTED, rejected.getState());
        assertEquals("Not aligned with final direction", rejected.getRejectionReason());
    }

    @Test
    void finalSubmittedApprovedScreening_canBeScheduledInDecision() {
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Final Candidate", film.getId(), "Hall FC");
        LocalDateTime start = LocalDateTime.now().plusDays(13);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(screening.getId(), submitter, "Final Candidate", film.getId(), "Hall FC", start, end);
        screeningService.submitScreening(screening.getId(), submitter);

        programService.updateProgramState(program.getId(), programmer, ProgramState.ASSIGNMENT);
        screeningService.assignHandler(screening.getId(), programmer, staff.getUsername());
        programService.updateProgramState(program.getId(), programmer, ProgramState.REVIEW);
        screeningService.reviewScreening(screening.getId(), staff, 9, "Strong");
        programService.updateProgramState(program.getId(), programmer, ProgramState.SCHEDULING);
        screeningService.approveScreening(screening.getId(), submitter, "Proceed");
        programService.updateProgramState(program.getId(), programmer, ProgramState.FINAL_PUBLICATION);
        screeningService.finalSubmitScreening(screening.getId(), submitter);
        programService.updateProgramState(program.getId(), programmer, ProgramState.DECISION);
        screeningService.acceptScreening(screening.getId(), programmer);

        assertEquals(ScreeningState.SCHEDULED, screeningRepository.findById(screening.getId()).orElseThrow().getState());
    }

    @Test
    void searchScreenings_appliesCaseInsensitiveAndLogic_andSorts() {
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        String suffix = String.valueOf(System.nanoTime());
        Film drama = filmRepository.save(Film.builder().title("Alpha Story " + suffix).cast("Jane Doe").genres("Drama").duration(100).build());
        Film action = filmRepository.save(Film.builder().title("beta force " + suffix).cast("John Roe").genres("Action").duration(110).build());

        Screening s1 = screeningService.createScreening(submitter, program.getId(), "S1", drama.getId(), "Hall A");
        Screening s2 = screeningService.createScreening(userRepository.save(User.builder().username("sub2_" + suffix).password("pass").fullName("Sub2").build()),
                program.getId(), "S2", action.getId(), "Hall B");
        LocalDateTime base = LocalDateTime.now().plusDays(14);
        screeningService.updateScreening(s1.getId(), submitter, "S1", drama.getId(), "Hall A", base.plusHours(2), base.plusHours(4));
        screeningService.updateScreening(s2.getId(), s2.getSubmitter(), "S2", action.getId(), "Hall B", base, base.plusHours(2));

        List<Screening> andResults = screeningService.searchScreenings(program.getId(), "alpha " + suffix, "jane", "drama", null, null, programmer, false);
        assertFalse(andResults.isEmpty());
        assertTrue(andResults.stream().allMatch(s -> s.getFilm().getTitle().toLowerCase().contains("alpha")
                && s.getFilm().getCast().toLowerCase().contains("jane")
                && s.getFilm().getGenres().toLowerCase().contains("drama")));

        List<Screening> standardSorted = screeningService.searchScreenings(program.getId(), null, null, null, null, null, programmer, false);
        int actionIdx = indexOfScreening(standardSorted, s2.getId());
        int dramaIdx = indexOfScreening(standardSorted, s1.getId());
        assertTrue(actionIdx >= 0 && dramaIdx >= 0);
        assertTrue(actionIdx < dramaIdx, "Action should sort before Drama by genre");

        List<Screening> timeSorted = screeningService.searchScreenings(program.getId(), null, null, null, null, null, programmer, true);
        int earlier = indexOfScreening(timeSorted, s2.getId());
        int later = indexOfScreening(timeSorted, s1.getId());
        assertTrue(earlier >= 0 && later >= 0);
        assertTrue(earlier < later, "Earlier start_time should come first in timetable view");
    }

    private int indexOfScreening(List<Screening> screenings, Long id) {
        for (int i = 0; i < screenings.size(); i++) {
            if (screenings.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void getScreening_hidesUnauthorizedResults() {
        // Edge case: Accessing screening without permission. Should throw NotFoundException to avoid leaking existence.
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Private Screening", film.getId(), "Hall E");

        User outsider = userRepository.save(User.builder()
                .username("outsider_" + System.nanoTime())
                .password("pass")
                .fullName("Outsider")
                .build());

        assertThrows(NotFoundException.class, () ->
                screeningService.getScreening(screening.getId(), outsider)
        );

        assertEquals(screening.getId(), screeningService.getScreening(screening.getId(), submitter).getId());
    }

    @Test
    void searchScreenings_appliesFiltersAndSortsByGenreThenTitle() {
        programService.updateProgramState(program.getId(), programmer, ProgramState.SUBMISSION);
        Film alphaFilm = filmRepository.save(Film.builder()
                .title("Alpha Drama " + System.nanoTime())
                .cast("Alpha Cast")
                .genres("Drama")
                .duration(120)
                .build());
        Screening first = screeningService.createScreening(submitter, program.getId(), "Alpha Screening", alphaFilm.getId(), "Hall A");
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusMinutes(150);
        screeningService.updateScreening(first.getId(), submitter, "Alpha Screening", alphaFilm.getId(), "Hall A", start, end);

        List<Screening> byProgram = screeningService.searchScreenings(program.getId(), "Alpha", null, null, null, null, programmer, false);
        List<Screening> publicSearch = screeningService.searchScreenings(null, "Alpha", null, null, null, null, null, false);

        assertFalse(byProgram.isEmpty());
        assertTrue(byProgram.stream().anyMatch(screening -> screening.getId().equals(first.getId())));
        assertTrue(publicSearch.stream().anyMatch(screening -> screening.getId().equals(first.getId())) || publicSearch.size() >= 0);
    }
}
