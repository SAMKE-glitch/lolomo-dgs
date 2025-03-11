package com.netflix.lolomodemo;

import com.netflix.lolomodemo.codegen.types.Show;
import org.springframework.stereotype.Component;

import java.util.List;

@Component //
public class ShowsRepository {

    /**
     * class for shows
     */
    private final static List<Show> shows = List.of(
            Show.newBuilder().title("The Witcher").build(),
            Show.newBuilder().title("Wednesday").build(),
            Show.newBuilder().title("Sweet Tooth").build(),
            Show.newBuilder().title("Black Mirror").build(),
            Show.newBuilder().title("Sex Education").build(),
            Show.newBuilder().title("Manifest").build(),
            Show.newBuilder().title("Love is Blind").build(),
            Show.newBuilder().title("You").build(),
            Show.newBuilder().title("Receiver").build(),
            Show.newBuilder().title("The Last Dance").build()
    );

    public List<Show> showsForCategory(int category) {
        return switch (category) {
            case 1 -> shows;
            case 2 -> List.of(shows.get(9), shows.get(7), shows.get(0));
            default -> List.of();
        };
    }

    public List<Show> allShows() { return shows; }
}
