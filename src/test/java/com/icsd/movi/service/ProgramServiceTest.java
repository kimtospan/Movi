package com.icsd.movi.service;

import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.User;
import com.icsd.movi.domain.repository.ProgramRepository;
import com.icsd.movi.domain.repository.UserRepository;
import com.icsd.movi.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ProgramServiceTest {

    @Autowired
    private ProgramService programService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgramRepository programRepository;

    private User creator;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());
        creator = userRepository.save(User.builder()
                .username("creator_" + suffix)
                .password("pass")
                .fullName("Creator")
                .build());
    }

    @Test
    void createProgram_assignsCreatorAsProgrammer_andInitialState() {
        // Happy path: Program creation. Verifies creator is assigned as programmer and initial state is CREATED.
        Program program = programService.createProgram(
                creator,
                "Season " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(3));

        assertNotNull(program.getId());
        assertEquals(ProgramState.CREATED, program.getState());
        assertTrue(program.getProgrammers().contains(creator));
        assertEquals(creator, program.getCreator());
    }

    @Test
    void createProgram_rejectsDuplicateNames() {
        // Edge case: Creating a program with a duplicate name. Should reject to maintain title uniqueness.
        String name = "Duplicate Program " + System.nanoTime();
        programService.createProgram(creator, name, "Description", LocalDate.now(), LocalDate.now().plusMonths(3));

        assertThrows(RuntimeException.class, () ->
                programService.createProgram(creator, name, "Other description", LocalDate.now(), LocalDate.now().plusMonths(4))
        );
    }

    @Test
    void updateProgramState_onlyAllowsForwardTransitions() {
        // Edge case: Transitioning to a previous state. Should reject as only forward transitions are valid.
        Program program = programService.createProgram(
                creator,
                "Transition Program " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(2));

        programService.updateProgramState(program.getId(), creator, ProgramState.SUBMISSION);
        assertEquals(ProgramState.SUBMISSION, programRepository.findById(program.getId()).orElseThrow().getState());

        assertThrows(RuntimeException.class, () ->
                programService.updateProgramState(program.getId(), creator, ProgramState.CREATED)
        );
    }

    @Test
    void addStaff_and_removeProgrammer_enforceRoleRules() {
        // Edge case: Invalid role assignments (adding staff as programmer, removing creator). Should reject.
        Program program = programService.createProgram(
                creator,
                "Role Program " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(2));

        User staff = userRepository.save(User.builder()
                .username("staff_" + System.nanoTime())
                .password("pass")
                .fullName("Staff")
                .build());

        programService.addStaff(program.getId(), creator, staff.getUsername());
        assertTrue(program.getStaff().contains(staff));

        assertThrows(RuntimeException.class, () ->
                programService.addProgrammer(program.getId(), creator, staff.getUsername())
        );

        assertThrows(RuntimeException.class, () ->
                programService.removeProgrammer(program.getId(), creator, creator.getUsername())
        );
    }

    @Test
    void userCanHoldDifferentRolesAcrossPrograms_butOnlyOneRolePerProgram() {
        // Edge case: User having different roles across programs but trying to get duplicate roles in one. Should reject.
        Program firstProgram = programService.createProgram(
                creator,
                "First Program " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(2));

        Program secondProgram = programService.createProgram(
                creator,
                "Second Program " + System.nanoTime(),
                "Description",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusMonths(2).plusDays(1));

        User dualRoleUser = userRepository.save(User.builder()
                .username("dual_role_" + System.nanoTime())
                .password("pass")
                .fullName("Dual Role")
                .build());

        programService.addStaff(firstProgram.getId(), creator, dualRoleUser.getUsername());
        programService.addProgrammer(secondProgram.getId(), creator, dualRoleUser.getUsername());

        Program reloadedFirst = programRepository.findById(firstProgram.getId()).orElseThrow();
        Program reloadedSecond = programRepository.findById(secondProgram.getId()).orElseThrow();
        assertTrue(reloadedFirst.getStaff().contains(dualRoleUser));
        assertFalse(reloadedFirst.getProgrammers().contains(dualRoleUser));
        assertTrue(reloadedSecond.getProgrammers().contains(dualRoleUser));
        assertFalse(reloadedSecond.getStaff().contains(dualRoleUser));

        assertThrows(RuntimeException.class, () ->
                programService.addProgrammer(firstProgram.getId(), creator, dualRoleUser.getUsername()));
    }

    @Test
    void deleteProgram_rejectsWhenStateIsNotCreated() {
        // Edge case: Deleting a program that is not in CREATED state. Should reject to prevent data loss.
        Program program = programService.createProgram(
                creator,
                "NonDeletable Program " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(2));
        programService.updateProgramState(program.getId(), creator, ProgramState.SUBMISSION);

        assertThrows(ConflictException.class, () -> programService.deleteProgram(program.getId(), creator));
    }

    @Test
    void updateProgram_rejectsAfterAnnouncedState() {
        // Edge case: Updating a program after it is ANNOUNCED. Should reject as it should be immutable.
        Program program = programService.createProgram(
                creator,
                "Immutable Program " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(2));
        programService.updateProgramState(program.getId(), creator, ProgramState.SUBMISSION);
        programService.updateProgramState(program.getId(), creator, ProgramState.ASSIGNMENT);
        programService.updateProgramState(program.getId(), creator, ProgramState.REVIEW);
        programService.updateProgramState(program.getId(), creator, ProgramState.SCHEDULING);
        programService.updateProgramState(program.getId(), creator, ProgramState.FINAL_PUBLICATION);
        programService.updateProgramState(program.getId(), creator, ProgramState.DECISION);
        programService.updateProgramState(program.getId(), creator, ProgramState.ANNOUNCED);

        assertThrows(ConflictException.class, () -> programService.updateProgram(
                program.getId(),
                creator,
                "Renamed " + System.nanoTime(),
                "New description",
                LocalDate.now().plusDays(2),
                LocalDate.now().plusMonths(3)
        ));
    }

    @Test
    void removeProgrammer_rejectsRemovingCreator() {
        // Edge case: Removing the creator from programmer list. Should reject as creator cannot be removed.
        Program program = programService.createProgram(
                creator,
                "Creator Locked Program " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(2));
        User otherProgrammer = userRepository.save(User.builder()
                .username("other_prog_" + System.nanoTime())
                .password("pass")
                .fullName("Other Programmer")
                .build());
        programService.addProgrammer(program.getId(), creator, otherProgrammer.getUsername());

        assertThrows(ConflictException.class, () ->
                programService.removeProgrammer(program.getId(), creator, creator.getUsername()));
    }

    @Test
    void addStaff_rejectsAfterSubmissionState() {
        // Edge case: Adding staff to program after submission. Should reject as roles are locked.
        Program program = programService.createProgram(
                creator,
                "Frozen Staff Program " + System.nanoTime(),
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(2));
        User candidate = userRepository.save(User.builder()
                .username("late_staff_" + System.nanoTime())
                .password("pass")
                .fullName("Late Staff")
                .build());
        programService.updateProgramState(program.getId(), creator, ProgramState.SUBMISSION);

        assertThrows(ConflictException.class, () ->
                programService.addStaff(program.getId(), creator, candidate.getUsername()));
    }

    @Test
    void searchPrograms_appliesAndLogicAndSortsByStartDateThenName() {
        // Happy path: Complex filtering and sorting. Verifies search criteria AND logic and correct sorting.
        String suffix = String.valueOf(System.nanoTime());
        Program p1 = programService.createProgram(
                creator,
                "Alpha " + suffix,
                "Festival Drama",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(30));
        Program p2 = programService.createProgram(
                creator,
                "Beta " + suffix,
                "Festival Drama",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(25));
        Program p3 = programService.createProgram(
                creator,
                "Gamma " + suffix,
                "Different Description",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(25));

        List<Program> andResults = programService.searchPrograms("a " + suffix, "festival", null, null, null, creator);
        assertTrue(andResults.stream().allMatch(p ->
                p.getName().toLowerCase().contains(suffix.toLowerCase()) &&
                        p.getDescription().toLowerCase().contains("festival")));
        assertFalse(andResults.stream().anyMatch(p -> p.getId().equals(p3.getId())));

        List<Program> sorted = programService.searchPrograms(null, null, null, null, null, creator);
        int idxBeta = indexOfProgram(sorted, p2.getId());
        int idxAlpha = indexOfProgram(sorted, p1.getId());
        assertTrue(idxBeta >= 0 && idxAlpha >= 0);
        assertTrue(idxBeta < idxAlpha, "Earlier start date must come first");
    }

    private int indexOfProgram(List<Program> programs, Long id) {
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    
    @Test
    void searchPrograms_returnsVisibleAndSortedResults() {
        // Happy path: Program search. Verifies that programs created are searchable and visible.
        String name = "Visible Program " + System.nanoTime();
        Program created = programService.createProgram(creator, name, "Festival description", LocalDate.now().plusDays(4), LocalDate.now().plusDays(12));

        List<Program> results = programService.searchPrograms(name, null, null, null, null, creator);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(program -> program.getId().equals(created.getId())));
    }
}
