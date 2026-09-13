package com.icsd.movi.service;

import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.domain.enums.ScreeningState;
import com.icsd.movi.domain.model.Film;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.Screening;
import com.icsd.movi.domain.model.User;
import com.icsd.movi.domain.repository.FilmRepository;
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
public class ComprehensiveFlowTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private ScreeningService screeningService;

    @Autowired
    private FilmRepository filmRepository;

    @Test
    void testFullProgramAndScreeningLifecycle() {
        // Happy path: Comprehensive end-to-end integration test. Verifies the entire workflow from program creation to final publication.
        User admin = userService.registerUser("admin_flow_" + System.nanoTime(), "pass", "Admin Flow");
        User programmer = userService.registerUser("prog_flow_" + System.nanoTime(), "pass", "Programmer Flow");
        User staff = userService.registerUser("staff_flow_" + System.nanoTime(), "pass", "Staff Flow");
        User submitter = userService.registerUser("sub_flow_" + System.nanoTime(), "pass", "Submitter Flow");

        Film film = filmRepository.save(Film.builder()
                .title("Flow Movie " + System.nanoTime())
                .cast("Flow Cast")
                .genres("Drama")
                .duration(120)
                .build());

        Program program = programService.createProgram(admin, "Flow Project " + System.nanoTime(), "Flow Description", LocalDate.now(), LocalDate.now().plusMonths(6));
        assertNotNull(program.getId());
        assertTrue(program.getProgrammers().contains(admin));

        programService.addProgrammer(program.getId(), admin, programmer.getUsername());
        programService.addStaff(program.getId(), admin, staff.getUsername());

        String updatedName = program.getName() + " Updated";
        Program updatedProgram = programService.updateProgram(program.getId(), admin, updatedName, "New Description", LocalDate.now(), LocalDate.now().plusMonths(7));
        assertEquals(updatedName, updatedProgram.getName());

        List<Program> matchingPrograms = programService.searchPrograms("Flow", null, null, null, null, admin);
        assertFalse(matchingPrograms.isEmpty());

        programService.updateProgramState(program.getId(), admin, ProgramState.SUBMISSION);
        Screening screening = screeningService.createScreening(submitter, program.getId(), "Flow Screening", film.getId(), "Main Hall");
        assertEquals(ScreeningState.CREATED, screening.getState());

        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusMinutes(150);
        screening = screeningService.updateScreening(screening.getId(), submitter, "Updated Flow Screening", film.getId(), "Main Hall", start, end);
        assertEquals("Updated Flow Screening", screening.getName());

        screeningService.submitScreening(screening.getId(), submitter);
        screening = screeningService.getScreening(screening.getId(), admin);
        assertEquals(ScreeningState.SUBMITTED, screening.getState());

        programService.updateProgramState(program.getId(), admin, ProgramState.ASSIGNMENT);
        screeningService.assignHandler(screening.getId(), admin, staff.getUsername());
        assertEquals(staff, screeningService.getScreening(screening.getId(), admin).getHandler());

        programService.updateProgramState(program.getId(), admin, ProgramState.REVIEW);
        screeningService.reviewScreening(screening.getId(), staff, 8, "Good movie");
        assertEquals(ScreeningState.REVIEWED, screeningService.getScreening(screening.getId(), admin).getState());

        programService.updateProgramState(program.getId(), admin, ProgramState.SCHEDULING);
        screeningService.approveScreening(screening.getId(), submitter, "Approved for final selection");
        assertEquals(ScreeningState.APPROVED, screeningService.getScreening(screening.getId(), admin).getState());

        List<Screening> screenings = screeningService.searchScreenings(program.getId(), "Flow", null, null, null, null, admin, true);
        assertFalse(screenings.isEmpty());

        programService.updateProgramState(program.getId(), admin, ProgramState.FINAL_PUBLICATION);
        screeningService.finalSubmitScreening(screening.getId(), submitter);
        programService.updateProgramState(program.getId(), admin, ProgramState.DECISION);
        screeningService.acceptScreening(screening.getId(), admin);
        assertEquals(ScreeningState.SCHEDULED, screeningService.getScreening(screening.getId(), admin).getState());

        programService.updateProgramState(program.getId(), admin, ProgramState.ANNOUNCED);
        assertEquals(ProgramState.ANNOUNCED, programService.getProgram(program.getId(), null).getState());

        Program secondProgram = programService.createProgram(admin, "Project Two " + System.nanoTime(), "Desc", LocalDate.now(), LocalDate.now().plusDays(2));
        programService.addProgrammer(secondProgram.getId(), admin, programmer.getUsername());
        programService.removeProgrammer(secondProgram.getId(), admin, programmer.getUsername());
        programService.addStaff(secondProgram.getId(), admin, staff.getUsername());
        programService.removeStaff(secondProgram.getId(), admin, staff.getUsername());
        programService.deleteProgram(secondProgram.getId(), admin);
    }
}
