package com.icsd.movi.domain.repository.specification;

import com.icsd.movi.domain.model.Film;
import com.icsd.movi.domain.model.Screening;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScreeningSpecification {

    public static Specification<Screening> search(Long programId, String title, String cast, String genre, LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (programId != null) {
                predicates.add(cb.equal(root.get("program").get("id"), programId));
            }

            if (title != null && !title.trim().isEmpty()) {
                Join<Screening, Film> filmJoin = root.join("film", JoinType.INNER);
                predicates.add(containsAllWords(cb, filmJoin.get("title"), title));
            }
            if (cast != null && !cast.trim().isEmpty()) {
                Join<Screening, Film> filmJoin = root.join("film", JoinType.INNER);
                predicates.add(containsAllWords(cb, filmJoin.get("cast"), cast));
            }
            if (genre != null && !genre.trim().isEmpty()) {
                Join<Screening, Film> filmJoin = root.join("film", JoinType.INNER);
                predicates.add(containsAllWords(cb, filmJoin.get("genres"), genre));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), end));
            }

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
