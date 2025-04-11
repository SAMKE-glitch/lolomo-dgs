package com.netflix.lolomodemo.datafetchers;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.lolomodemo.ArtworkService;
import com.netflix.lolomodemo.ShowsRepository;
import com.netflix.lolomodemo.codegen.types.SearchFilter;
import com.netflix.lolomodemo.codegen.types.Show;
import com.netflix.lolomodemo.codegen.types.ShowCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LolomoDatafetcherTest {
    @Mock
    private ShowsRepository showsRepository;

    @Mock
    private ArtworkService artworkService;

    @InjectMocks
    private LolomoDatafetcher lolomoDatafetcher;

    /**
     * Overrides the customExecutor field of LolomoDatafetcher with a direct executor.
     */
    private void overrideExecutor(LolomoDatafetcher datafetcher, Executor executor) {
        try {
            Field executorField = LolomoDatafetcher.class.getDeclaredField("customExecutor");
            executorField.setAccessible(true);
            executorField.set(datafetcher, executor);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to override customExecutor", e);
        }
    }


    @Test
    void lolomo_ShouldReturnExpectedShowCategories() {
        // ARRANGE: Prepare the expected lists of shows for each category.
        List<Show> category1Shows = List.of(
                Show.newBuilder().title("Show A").artworkUrl("urlA").build(),
                Show.newBuilder().title("Show B").artworkUrl("urlB").build()
        );
        List<Show> category2Shows = List.of(
                Show.newBuilder().title("Show C").artworkUrl("urlC").build(),
                Show.newBuilder().title("Show D").artworkUrl("urlD").build()
        );

        // Stub the repository methods for each category.
        when(showsRepository.showsForCategory(1)).thenReturn(category1Shows);
        when(showsRepository.showsForCategory(2)).thenReturn(category2Shows);

        // ACT: Invoke the method under test.
        List<ShowCategory> result = lolomoDatafetcher.lolomo();

        // ASSERT: Validate the size and structure of the returned list.
        assertNotNull(result, "The result should not be null");
        assertEquals(2, result.size(), "There should be exactly two ShowCategory entries");

        // Validate first category: id = 1, name = "Top 10", and correct shows.
        ShowCategory category1 = result.get(0);
        assertEquals(1, category1.getId(), "Category 1 should have id 1");
        assertEquals("Top 10", category1.getName(), "Category 1 should be named 'Top 10'");
        assertEquals(category1Shows, category1.getShows(), "Category 1 shows should match the expected list");

        // Validate second category: id = 2, name = "Continue Watching", and correct shows.
        ShowCategory category2 = result.get(1);
        assertEquals(2, category2.getId(), "Category 2 should have id 2");
        assertEquals("Continue Watching", category2.getName(), "Category 2 should be named 'Continue Watching'");
        assertEquals(category2Shows, category2.getShows(), "Category 2 shows should match the expected list");

        // Verify that the repository methods were invoked exactly once for each category.
        verify(showsRepository, times(1)).showsForCategory(1);
        verify(showsRepository, times(1)).showsForCategory(2);
    }

    @Test
    void lolomo_ShouldReturnEmptyCategoriesWhenNoShows() {
        // ARRANGE: Stub repository to return empty lists.
        when(showsRepository.showsForCategory(1)).thenReturn(List.of());
        when(showsRepository.showsForCategory(2)).thenReturn(List.of());

        // ACT:
        List<ShowCategory> result = lolomoDatafetcher.lolomo();

        // ASSERT:
        assertNotNull(result, "The result should not be null");
        assertEquals(2, result.size(), "There should be two ShowCategory entries");

        // Category 1 validations.
        ShowCategory category1 = result.get(0);
        assertEquals(1, category1.getId());
        assertEquals("Top 10", category1.getName());
        assertNotNull(category1.getShows(), "Shows list should not be null");
        assertTrue(category1.getShows().isEmpty(), "Shows list should be empty");

        // Category 2 validations.
        ShowCategory category2 = result.get(1);
        assertEquals(2, category2.getId());
        assertEquals("Continue Watching", category2.getName());
        assertNotNull(category2.getShows(), "Shows list should not be null");
        assertTrue(category2.getShows().isEmpty(), "Shows list should be empty");

        verify(showsRepository, times(1)).showsForCategory(1);
        verify(showsRepository, times(1)).showsForCategory(2);
    }


    @Test
    void lolomo_WhenRepositoryThrowsException_ShouldPropagateError() {
        // ARRANGE: Stub repository for category 1 to throw an exception.
        when(showsRepository.showsForCategory(1)).thenThrow(new RuntimeException("Error fetching category 1"));

        // ACT & ASSERT:
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lolomoDatafetcher.lolomo();
        });
        assertEquals("Error fetching category 1", exception.getMessage());

        // Verify that only the first repository call was made.
        verify(showsRepository, times(1)).showsForCategory(1);
        // Depending on the implementation, category 2 might not be invoked if the first call fails.
        verify(showsRepository, never()).showsForCategory(2);
    }

    @Test
    void lolomo_WhenRepositoryReturnsNull_ShouldHandleGracefully() {
        // ARRANGE: For category 1, return a valid list; for category 2, return null.
        List<Show> category1Shows = List.of(
                Show.newBuilder().title("Show A").artworkUrl("urlA").build()
        );
        when(showsRepository.showsForCategory(1)).thenReturn(category1Shows);
        when(showsRepository.showsForCategory(2)).thenReturn(null);

        // ACT:
        List<ShowCategory> result = lolomoDatafetcher.lolomo();

        // ASSERT:
        assertNotNull(result, "The result should not be null");
        assertEquals(2, result.size(), "There should be two ShowCategory entries");

        // Category 1 should have the valid list.
        ShowCategory category1 = result.get(0);
        assertEquals(category1Shows, category1.getShows());

        // Category 2: Depending on your business logic, you might expect null or a default empty list.
        // Here we assert that it is null.
        ShowCategory category2 = result.get(1);
        assertNull(category2.getShows(), "Expected null for category 2 shows");

        verify(showsRepository, times(1)).showsForCategory(1);
        verify(showsRepository, times(1)).showsForCategory(2);
    }

    @Test
    void artworkUrl_ShouldReturnGeneratedUrl() throws Exception {
        // ARRANGE
        // Use a direct executor for synchronous execution during tests.
        Executor directExecutor = Runnable::run;
        overrideExecutor(lolomoDatafetcher, directExecutor);

        String title = "Test Show";
        String generatedUrl = "generated-url";
        Show show = Show.newBuilder().title(title).build();

        // Create a mock environment returning our show.
        DgsDataFetchingEnvironment env = mock(DgsDataFetchingEnvironment.class);
        when(env.getSourceOrThrow()).thenReturn(show);
        // Stub the artwork service to return the generated URL.
        when(artworkService.generateForTitle(title)).thenReturn(generatedUrl);

        // ACT
        CompletableFuture<String> future = lolomoDatafetcher.artworkUrl(env);
        String result = future.get(); // Waits for completion.

        // ASSERT
        assertEquals(generatedUrl, result, "The returned artwork URL should match the generated URL.");
        verify(artworkService, times(1)).generateForTitle(title);
    }

    @Test
    void artworkUrl_WhenArtworkServiceThrowsException_ShouldReturnDefaultUrl() throws Exception {
        // ARRANGE
        Executor directExecutor = Runnable::run;
        overrideExecutor(lolomoDatafetcher, directExecutor);

        String title = "Test Show";
        Show show = Show.newBuilder().title(title).build();

        // Create a mock environment returning our show.
        DgsDataFetchingEnvironment env = mock(DgsDataFetchingEnvironment.class);
        when(env.getSourceOrThrow()).thenReturn(show);
        // Stub the artwork service to throw an exception.
        when(artworkService.generateForTitle(title)).thenThrow(new RuntimeException("Failure"));

        // ACT
        CompletableFuture<String> future = lolomoDatafetcher.artworkUrl(env);
        String result = future.get(); // Waits for completion.

        // ASSERT
        assertEquals("default_artwork_url", result, "On error, the default artwork URL should be returned.");
    }


    @Test
    void search_ShouldReturnShowsStartingWithTitle() {
        // ARRANGE
        SearchFilter filter = SearchFilter.newBuilder().title("The").build();

        // Mock ShowsRepository to return a list of shows
        List<Show> mockShows = List.of(
                Show.newBuilder().title("The Witcher").build(),
                Show.newBuilder().title("Wednesday").build(),
                Show.newBuilder().title("The Last Dance").build()
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

    @Test
    void search_ShouldReturnAllShowsWhenTitleIsEmpty() {
        // Arrange
        SearchFilter filter = SearchFilter.newBuilder().title("").build(); // Empty title
        List<Show> mockShows = List.of(
                Show.newBuilder().title("The Witcher").build(),
                Show.newBuilder().title("Wednesday").build()
        );
        when(showsRepository.allShows()).thenReturn(mockShows);

        // Act
        List<Show> results = lolomoDatafetcher.search(filter);

        // Assert
        assertEquals(2, results.size());
        verify(showsRepository).allShows();
    }
    @Test
    void search_ShouldReturnEmptyListWhenNoMatches() {
        // Arrange
        SearchFilter filter = SearchFilter.newBuilder().title("XYZ").build();
        when(showsRepository.allShows()).thenReturn(List.of()); // Empty list

        // Act
        List<Show> results = lolomoDatafetcher.search(filter);

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void search_ShouldCallAllShowsOnce() {
        // Arrange
        SearchFilter filter = SearchFilter.newBuilder().title("The").build();
        when(showsRepository.allShows()).thenReturn(List.of());

        // Act
        lolomoDatafetcher.search(filter);

        // Assert
        verify(showsRepository, times(1)).allShows();
        verifyNoMoreInteractions(showsRepository); // No other methods called
    }
}
/**
@Test
@DisplayName("getTerritory(): returns null when territoryId is null")
void shouldReturnNullWhenTerritoryIdIsNull() {
    // ARRANGE
    DeliveryNote deliveryNote = new DeliveryNote(); // territoryId not set (remains null)
    when(dfe.getSource()).thenReturn(deliveryNote);

    // ACT
    CompletableFuture<Territory> result = graphQLHandler.getTerritory(dfe);

    // ASSERT
    assertNull(result, "The result should be null if territoryId is null");
}**/