package com.icsd.movi.service;

import com.icsd.movi.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchPipelineTest {

    private final SearchPipeline searchPipeline = new SearchPipeline();

    @Mock
    private Runnable rateLimitChecker;

    @Mock
    private Specification<TestItem> spec;

    @Mock
    private JpaSpecificationExecutor<TestItem> repository;

    @Test
    void execute_runsRateLimit_queriesBySpec_andAppliesVisibilityFilter() {
        // Happy path: Normal search execution with visibility filtering. Verifies rate limiting, spec usage, and visibility filtering are correctly applied.
        User user = User.builder().username("alice").build();
        List<TestItem> rawResults = List.of(
                new TestItem("public-item", true, "other"),
                new TestItem("owned-item", false, "alice"),
                new TestItem("hidden-item", false, "other")
        );
        when(repository.findAll(spec)).thenReturn(rawResults);

        BiPredicate<TestItem, User> visibilityFilter = (item, currentUser) ->
                item.publicItem || item.ownerUsername.equals(currentUser.getUsername());

        List<TestItem> visible = searchPipeline.execute(rateLimitChecker, spec, repository, user, visibilityFilter);

        verify(rateLimitChecker).run();
        verify(repository).findAll(spec);
        assertEquals(2, visible.size());
        assertEquals("public-item", visible.get(0).name());
        assertEquals("owned-item", visible.get(1).name());
    }

    @Test
    void execute_returnsUnfilteredResults_whenVisibilityFilterIsNull() {
        // Edge case: Search without visibility filter. Should return all raw results as there's no restriction.
        List<TestItem> rawResults = List.of(new TestItem("only-item", true, "owner"));
        when(repository.findAll(spec)).thenReturn(rawResults);

        List<TestItem> result = searchPipeline.execute(rateLimitChecker, spec, repository, null, null);

        verify(rateLimitChecker).run();
        verify(repository).findAll(spec);
        assertSame(rawResults, result);
    }

    @Test
    void execute_usesFindAll_whenSpecificationIsNull() {
        // Edge case: Search without a specification. Should perform a full find-all, ignoring spec-based filtering.
        List<TestItem> rawResults = List.of(new TestItem("first", true, "owner"));
        when(repository.findAll((Specification<TestItem>) null)).thenReturn(rawResults);

        List<TestItem> result = searchPipeline.execute(rateLimitChecker, null, repository, null, (item, user) -> true);

        verify(rateLimitChecker).run();
        verify(repository).findAll((Specification<TestItem>) null);
        assertEquals(1, result.size());
        assertEquals("first", result.getFirst().name());
    }

    private record TestItem(String name, boolean publicItem, String ownerUsername) {
    }
}



