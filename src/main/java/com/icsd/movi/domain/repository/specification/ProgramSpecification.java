package com.icsd.movi.domain.repository.specification;

import com.icsd.movi.domain.model.Film;
import com.icsd.movi.domain.model.Program;
import com.icsd.movi.domain.model.Screening;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProgramSpecification {

    public static Specification<Program> search(String name, String description, LocalDate date, String filmTitle, String auditorium) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(containsAllWords(cb, root.get("name"), name));
            }
            if (description != null && !description.trim().isEmpty()) {
                predicates.add(containsAllWords(cb, root.get("description"), description));
            }
            if (date != null) {
                predicates.add(cb.and(
                        cb.lessThanOrEqualTo(root.get("startDate"), date),
                        cb.greaterThanOrEqualTo(root.get("endDate"), date)
                ));
            }
            
            if (filmTitle != null && !filmTitle.trim().isEmpty()) {
                Join<Program, Screening> screeningJoin = root.join("screenings", JoinType.INNER);
                Join<Screening, Film> filmJoin = screeningJoin.join("film", JoinType.INNER);
                predicates.add(containsAllWords(cb, filmJoin.get("title"), filmTitle));
            }
            if (auditorium != null && !auditorium.trim().isEmpty()) {
                Join<Program, Screening> screeningJoin = root.join("screenings", JoinType.INNER);
                predicates.add(containsAllWords(cb, screeningJoin.get("auditoriumName"), auditorium));
            }

            query.distinct(true);
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate containsAllWords(jakarta.persistence.criteria.CriteriaBuilder cb, jakarta.persistence.criteria.Expression<String> field, String query) {
        String[] words = query.split("\\s+");
        List<Predicate> wordPredicates = new ArrayList<>();
        for (String word : words) {
            wordPredicates.add(cb.like(cb.lower(field), "%" + word.toLowerCase() + "%"));
        }
        return cb.and(wordPredicates.toArray(new Predicate[0]));
    }
}
