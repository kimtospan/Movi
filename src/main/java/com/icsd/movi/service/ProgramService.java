package com.icsd.movi.service;

import com.icsd.movi.domain.repository.specification.ProgramSpecification;
import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.domain.enums.ScreeningState;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.Screening;
import com.icsd.movi.domain.model.User;
import com.icsd.movi.domain.repository.ProgramRepository;
import com.icsd.movi.domain.repository.UserRepository;
import com.icsd.movi.exception.BadRequestException;
import com.icsd.movi.exception.ConflictException;
import com.icsd.movi.exception.ForbiddenException;
import com.icsd.movi.exception.NotFoundException;
import com.icsd.movi.logging.AuditLogger;
import com.icsd.movi.security.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramService implements IProgramService {

    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final AuditLogger auditLogger;
    private final RateLimiter rateLimiter;
    private final SearchPipeline searchPipeline;

    @Override
    public Program getById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
    }

    @Transactional
    public Program createProgram(User creator, String name, String description, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        if (programRepository.existsByName(name)) {
            throw new ConflictException("Program name must be unique");
        }
        if (programRepository.existsByNameAndDescriptionAndStartDate(name, description, startDate)) {
            throw new ConflictException("Program with these details already exists");
        }
        Program program = Program.builder()
                .name(name)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .state(ProgramState.CREATED)
                .creator(creator)
                .build();
        program.getProgrammers().add(creator);
        Program saved = programRepository.save(program);
        auditLogger.logAction("CREATE_PROGRAM", "Program ID: " + saved.getId(), creator.getUsername());
        return saved;
    }

    @Transactional
    public Program updateProgram(Long id, User user, String name, String description, LocalDate startDate, LocalDate endDate) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        if (!program.getProgrammers().contains(user)) {
            throw new ForbiddenException("Only PROGRAMMERS can update the program");
        }
        if (program.getState().ordinal() >= ProgramState.ANNOUNCED.ordinal()) {
            throw new ConflictException("Updates must be completed before the program reaches the ANNOUNCED state");
        }
        validateDates(startDate, endDate);
        if (programRepository.existsByNameAndIdNot(name, id)) {
            throw new ConflictException("Program name must be unique");
        }
        program.setName(name);
        program.setDescription(description);
        program.setStartDate(startDate);
        program.setEndDate(endDate);
        return programRepository.save(program);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new BadRequestException("Program end date must be on or after its start date");
        }
    }

    @Transactional
    public void addProgrammer(Long id, User user, String usernameToAdd) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        if (!program.getProgrammers().contains(user)) {
            throw new ForbiddenException("Unauthorized");
        }
        if (program.getState() == ProgramState.ANNOUNCED) {
            throw new ConflictException("Role updates must be completed before ANNOUNCED state");
        }
        User userToAdd = userRepository.findByUsername(usernameToAdd)
                .orElseThrow(() -> new NotFoundException("User not found"));
        checkRoleConflict(program, userToAdd);
        program.getProgrammers().add(userToAdd);
        programRepository.save(program);
    }

    @Transactional
    public void addStaff(Long id, User user, String usernameToAdd) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        if (!program.getProgrammers().contains(user)) {
            throw new ForbiddenException("Unauthorized");
        }
        if (program.getState().ordinal() >= ProgramState.SUBMISSION.ordinal()) {
            throw new ConflictException("STAFF set is frozen after reaching SUBMISSION state");
        }
        User userToAdd = userRepository.findByUsername(usernameToAdd)
                .orElseThrow(() -> new NotFoundException("User not found"));
        checkRoleConflict(program, userToAdd);
        program.getStaff().add(userToAdd);
        programRepository.save(program);
    }

    private void checkRoleConflict(Program program, User user) {
        if (program.getProgrammers().contains(user) || 
            program.getStaff().contains(user) || 
            program.getScreenings().stream().anyMatch(s -> s.getSubmitter().equals(user))) {
            throw new ConflictException("User already has a role in this program");
        }
    }

    @Transactional
    public void removeProgrammer(Long id, User user, String usernameToRemove) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        if (!program.getProgrammers().contains(user)) {
            throw new ForbiddenException("Unauthorized");
        }
        if (program.getState().ordinal() >= ProgramState.ANNOUNCED.ordinal()) {
            throw new ConflictException("Role updates must be completed before ANNOUNCED state");
        }
        User userToRemove = userRepository.findByUsername(usernameToRemove)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (userToRemove.equals(program.getCreator())) {
            throw new ConflictException("The program's creator cannot be removed from its PROGRAMMERS set");
        }
        program.getProgrammers().remove(userToRemove);
        programRepository.save(program);
    }

    @Transactional
    public void removeStaff(Long id, User user, String usernameToRemove) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        if (!program.getProgrammers().contains(user)) {
            throw new ForbiddenException("Unauthorized");
        }
        if (program.getState().ordinal() >= ProgramState.SUBMISSION.ordinal()) {
            throw new ConflictException("STAFF set is frozen after reaching SUBMISSION state");
        }
        User userToRemove = userRepository.findByUsername(usernameToRemove)
                .orElseThrow(() -> new NotFoundException("User not found"));
        program.getStaff().remove(userToRemove);
        programRepository.save(program);
    }

    @Transactional
    public void deleteProgram(Long id, User user) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        if (!program.getProgrammers().contains(user)) {
            throw new ForbiddenException("Unauthorized");
        }
        if (program.getState() != ProgramState.CREATED) {
            throw new ConflictException("Program can only be deleted in CREATED state");
        }
        programRepository.delete(program);
    }

    @Transactional
    public void updateProgramState(Long id, User user, ProgramState newState) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        if (!program.getProgrammers().contains(user)) {
            throw new ForbiddenException("Unauthorized");
        }
        
        ProgramState oldState = program.getState();
        // Validate transition
        validateTransition(oldState, newState);
        
        program.setState(newState);
        
        // Automatic rejection logic if moving to DECISION
        if (newState == ProgramState.DECISION) {
            for (Screening screening : program.getScreenings()) {
                boolean acceptedForDecision = screening.isFinallySubmitted()
                    && (screening.getState() == ScreeningState.APPROVED || screening.getState() == ScreeningState.FINAL_SUBMIT);
                if (screening.getState() != ScreeningState.REJECTED && !acceptedForDecision) {
                    screening.setState(ScreeningState.REJECTED);
                    screening.setRejectionReason("Automatically rejected: Final submission not received by DECISION state.");
                    auditLogger.logAction("AUTO_REJECT_SCREENING", "Screening ID: " + screening.getId(), "SYSTEM");
                }
            }
        }
        
        programRepository.save(program);
        auditLogger.logAction("UPDATE_PROGRAM_STATE", "Program ID: " + id + " from " + oldState + " to " + newState, user.getUsername());
    }

    private void validateTransition(ProgramState current, ProgramState next) {
        if (next.ordinal() <= current.ordinal()) {
            throw new ConflictException("States may not be rolled back");
        }
        // Specific sequence: CREATED -> SUBMISSION -> ASSIGNMENT -> REVIEW -> SCHEDULING -> FINAL_PUBLICATION -> DECISION -> ANNOUNCED
        if (next.ordinal() != current.ordinal() + 1) {
            throw new ConflictException("Invalid state transition");
        }
    }

    @Transactional(readOnly = true)
    public List<Program> searchPrograms(String name, String description, LocalDate date, String filmTitle, String auditorium, User user) {
        Specification<Program> spec = ProgramSpecification.search(name, description, date, filmTitle, auditorium);
        Comparator<Program> comparator = Comparator
                .comparing(Program::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Program::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

        return searchPipeline.execute(
                        () -> {
                            if (user != null && !rateLimiter.isAllowed(user.getUsername(), "SEARCH_PROGRAMS")) {
                                throw new BadRequestException("Rate limit exceeded for searches");
                            }
                        },
                        spec,
                        programRepository,
                        user,
                        this::isProgramVisibleToUser
                ).stream()
                .sorted(comparator)
                .toList();
    }

    private boolean isProgramVisibleToUser(Program program, User user) {
        return program.getState() == ProgramState.ANNOUNCED
                || (user != null && (program.getProgrammers().contains(user)
                || program.getStaff().contains(user)
                || program.getScreenings().stream().anyMatch(s -> s.getSubmitter().equals(user))));
    }

    @Transactional(readOnly = true)
    public Program getProgram(Long id, User user) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
        // Access check
        if (program.getState() != ProgramState.ANNOUNCED) {
            if (user == null || (!program.getProgrammers().contains(user) && 
                                 !program.getStaff().contains(user) && 
                                 program.getScreenings().stream().noneMatch(s -> s.getSubmitter().equals(user)))) {
                throw new ForbiddenException("This program is not yet announced or you do not have permission to view it.");
            }
        }
        return program;
    }
}
