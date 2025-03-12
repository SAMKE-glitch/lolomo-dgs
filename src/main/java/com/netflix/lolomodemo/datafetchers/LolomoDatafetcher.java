package com.netflix.lolomodemo.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.lolomodemo.ArtworkService;
import com.netflix.lolomodemo.ShowsRepository;
import com.netflix.lolomodemo.codegen.types.Show;
import com.netflix.lolomodemo.codegen.types.ShowCategory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@DgsComponent // registers this class as a GraphQL component in Spring
public class LolomoDatafetcher {
    private final ShowsRepository showsRepository;
    private final ArtworkService artworkService;

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
    @DgsData(parentType = "Show")
    public CompletableFuture<String> artworkUrl(DgsDataFetchingEnvironment dfe) {
        return CompletableFuture.supplyAsync(() -> {
            Show show = dfe.getSourceOrThrow();
            return artworkService.generateForTitle(show.getTitle());
        }).exceptionally(ex -> "default_artwork_url");
    }
}