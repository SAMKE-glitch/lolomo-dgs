package com.netflix.lolomodemo.datafetchers;

import com.netflix.graphql.dgs.*;
import com.netflix.lolomodemo.ArtworkService;
import com.netflix.lolomodemo.ShowsRepository;
import com.netflix.lolomodemo.codegen.types.Show;
import com.netflix.lolomodemo.codegen.types.SearchFilter;
import com.netflix.lolomodemo.codegen.types.ShowCategory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@DgsComponent // registers this class as a GraphQL component in Spring
public class LolomoDatafetcher {
    private final ShowsRepository showsRepository;
    private final ArtworkService artworkService;

    // Enterprise Consideration: Using a custom thread pool to fine-tune performance
    private final Executor customExecutor = Executors.newFixedThreadPool(10);

    public LolomoDatafetcher(ShowsRepository showsRepository, ArtworkService artworkService) {
        this.showsRepository = showsRepository;
        this.artworkService = artworkService;
    }

    @DgsQuery // maps this method to the lolomo query in schema
    public List<ShowCategory> lolomo() {
        return List.of(
                ShowCategory.newBuilder().id(1).name("Top 10").shows(showsRepository.showsForCategory(1)).build(),
                ShowCategory.newBuilder().id(2).name("Continue Watching").shows(showsRepository.showsForCategory(2)).build()
        );
    }
    // artworkUrl method with VirtualThreads
    // with application.properties under resources set to dgs.graphql.virtualthreads.enabled=true
    /**@DgsData(parentType = "Show")
    public String artworkUrl(DgsDataFetchingEnvironment dfe) {
        Show show = dfe.getSourceOrThrow();
        return artworkService.generateForTitle(show.getTitle());

    }**/

    // We specify this datafetcher for this particular field in Show object/list
   /* @DgsData(parentType = "Show")
    public CompletableFuture<String> artworkUrl(DgsDataFetchingEnvironment dfe) {
        return CompletableFuture.supplyAsync(() -> {
            Show show = dfe.getSourceOrThrow();
            return artworkService.generateForTitle(show.getTitle());
        }).exceptionally(ex -> "default_artwork_url");
    }**/

    /*
     * Asynchronous data fetcher for generating artwork URLs.
     *
     * This method uses CompletableFuture to run the task in a separate thread from a custom executor.
     * It retrieves a Show object, generates an artwork URL based on the show's title,
     * and handles any errors by returning a default artwork URL.
     *
     * Enterprise Considerations:
     * - Custom Thread Pools: Using 'customExecutor' to better manage and fine-tune thread usage.
     * - Monitoring and Logging: Placeholders are added (as comments) where logging can be implemented.
     * - Further Error Handling: More sophisticated error handling or retries can be added if needed.
     *
     * @param dfe Data fetching environment that holds the source Show object.
     * @return A CompletableFuture that will eventually provide the artwork URL as a String.
     */
    @DgsData(parentType = "Show")
    public CompletableFuture<String> artworkUrl(DgsDataFetchingEnvironment dfe) {
        // I use CompletableFuture with a custom thread pool for asynchronous execution
        return CompletableFuture.supplyAsync(() -> {
            // Retrieving the current Show object from the dfe
            Show show = dfe.getSourceOrThrow();


            String artworkUrl = artworkService.generateForTitle(show.getTitle());

            return artworkUrl;
        }, customExecutor).exceptionally(ex -> {
            return "default_artwork_url";});
    }

    @DgsQuery
    public List<Show>search(@InputArgument SearchFilter searchFilter) {
        return showsRepository.allShows().stream().filter(s -> s.getTitle().startsWith(searchFilter.getTitle())).toList();
    }
}