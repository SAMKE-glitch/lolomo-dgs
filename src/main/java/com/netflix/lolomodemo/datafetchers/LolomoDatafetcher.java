package com.netflix.lolomodemo.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.lolomodemo.codegen.types.ShowCategory;

import java.util.List;

@DgsComponent // registers this class as a GraphQL component in Spring
public class LolomoDatafetcher {

    @DgsQuery // maps this method to the lolomo query in schema
    public List<ShowCategory> lolomo() {
        return List.of(
                ShowCategory.newBuilder().id(1).name("Top 10").shows().build(),
                ShowCategory.newBuilder().id(2).name("Continue Watching").build()
        );
    }
}
