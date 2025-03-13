package com.netflix.lolomodemo.datafetchers;

import com.netflix.lolomodemo.ShowsRepository;
import com.netflix.lolomodemo.codegen.types.SearchFilter;
import com.netflix.lolomodemo.codegen.types.Show;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LolomoDatafetcherTest {
    @Mock
    private ShowsRepository showsRepository;

    @InjectMocks
    private LolomoDatafetcher lolomoDatafetcher;

    @Test
    void search_ShouldReturnShowsStartingWithTitle() {
        // ARRANGE
        SearchFilter filter = SearchFilter.newBuilder().title("The").build();

        // Mock ShowsRepository to return a list of shows
        List<Show> mockShows = List.of(
                Show.newBuilder().title("The Witcher").build(),
                Show.newBuilder().title("Wednesday").build(),
                Show.newBuilder().title("The Witcher").build()
        );
        when(showsRepository.allShows()).thenReturn(mockShows);

        // ACT
        List<Show> results = lolomoDatafetcher.search(filter);

        // ASSERT
        // Expect the "The Witcher" and "The Last Dance"
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(show -> show.getTitle().startsWith("The")));
        // Verify repository interaction
        verify(showsRepository, times(1)).allShows();
    }
}