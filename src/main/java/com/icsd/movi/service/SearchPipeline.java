package com.icsd.movi.service;

import com.icsd.movi.domain.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiPredicate;

@Component
public class SearchPipeline {
    /* Both services use basically the same Search Pipeline
    This Pipeline is used to standardize the search process for both Program And Screening */

    /**
     * Executes a standardized search pipeline of
     * Rate Limit 
     * DB Search
     * Visibility Filtering.
     */
    public <T> List<T> execute(
            Runnable rateLimitChecker,
            Specification<T> spec,
            JpaSpecificationExecutor<T> repository,
            User currentUser,
            BiPredicate<T, User> visibilityFilter
    ) {
       
        rateLimitChecker.run();

       
        List<T> rawResults = repository.findAll(spec);

       
        if (visibilityFilter == null) {
            return rawResults;
        }

        return rawResults.stream()
                .filter(item -> visibilityFilter.test(item, currentUser))
                .toList();
    }
}


