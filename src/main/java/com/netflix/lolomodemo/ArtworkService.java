package com.netflix.lolomodemo;

import com.netflix.graphql.dgs.DgsComponent;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ArtworkService {
    // Logging statements
    private final static Logger LOGGER = LoggerFactory.getLogger(ArtworkService.class);

    public String generateForTitle(String title){
        LOGGER.info("Generating for {}", title);

        // Want to introduce latency
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return UUID.randomUUID() + "-" + title.toLowerCase().replaceAll(" ", "-");
    }
}
