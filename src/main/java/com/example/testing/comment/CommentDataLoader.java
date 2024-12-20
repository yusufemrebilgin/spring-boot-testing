package com.example.testing.comment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class CommentDataLoader implements CommandLineRunner {

    private final ObjectMapper mapper;
    private final CommentRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(CommentDataLoader.class);

    public CommentDataLoader(ObjectMapper mapper, CommentRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() != 0) {
            logger.info("Comments already loaded into database");
            return;
        }

        final String filename = "/data/comments.json";
        logger.info("Loading comments into database from file: {}", filename);
        try (InputStream inputStream = TypeReference.class.getResourceAsStream(filename)) {
            List<Comment> comments = mapper.readValue(inputStream, new TypeReference<>() {});
            repository.saveAll(comments);
            logger.info("{} comments recorded to database", comments.size());
        } catch (IOException ex) {
            logger.error("Failed to parse JSON file {}", filename);
            throw new RuntimeException(ex);
        }
    }

}
