package com.icsd.movi.service;

import com.icsd.movi.domain.model.Screening;
import com.icsd.movi.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface IScreeningService extends BaseService<Screening, Long> {
    Screening createScreening(User user, Long programId, String name, Long filmId, String auditoriumName);
    Screening updateScreening(Long id, User user, String name, Long filmId, String auditoriumName, LocalDateTime start, LocalDateTime end);
    void submitScreening(Long id, User user);
    void withdrawScreening(Long id, User user);
    void assignHandler(Long id, User user, String handlerUsername);
    void reviewScreening(Long id, User user, Integer score, String comments);
    void approveScreening(Long id, User user, String notes);
    void rejectScreening(Long id, User user, String reason);
    void finalSubmitScreening(Long id, User user);
    void acceptScreening(Long id, User user);
    List<Screening> searchScreenings(Long programId, String title, String cast, String genre, LocalDateTime start, LocalDateTime end, User user, boolean sortByTime);
    Screening getScreening(Long id, User user);
}
