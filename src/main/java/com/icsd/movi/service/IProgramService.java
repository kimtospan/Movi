package com.icsd.movi.service;

import com.icsd.movi.domain.enums.ProgramState;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.User;

import java.time.LocalDate;
import java.util.List;

public interface IProgramService extends BaseService<Program, Long> {
    Program createProgram(User creator, String name, String description, LocalDate startDate, LocalDate endDate);
    Program updateProgram(Long id, User user, String name, String description, LocalDate startDate, LocalDate endDate);
    void addProgrammer(Long id, User user, String usernameToAdd);
    void addStaff(Long id, User user, String usernameToAdd);
    void removeProgrammer(Long id, User user, String usernameToRemove);
    void removeStaff(Long id, User user, String usernameToRemove);
    void deleteProgram(Long id, User user);
    void updateProgramState(Long id, User user, ProgramState newState);
    List<Program> searchPrograms(String name, String description, LocalDate date, String filmTitle, String auditorium, User user);
    Program getProgram(Long id, User user);
}
