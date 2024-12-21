package com.example.testing.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentControllerTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:latest");

    @LocalServerPort
    Integer port;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommentRepository commentRepository;

    private String baseUrl;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/comments";
        commentRepository.deleteAll();
    }

    @Test
    void givenPaginationParameter_whenGetAllComments_thenReturnPageOfComments() throws Exception {
        // given
        Comment c1 = CommentTestDataFactory.comment("comment-1");
        Comment c2 = CommentTestDataFactory.comment("comment-2");
        saveCommentsToDB(c1, c2);

        // when & then
        mvc.perform(get(baseUrl)
                        .param("page", "0")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("comment-1")))
                .andExpect(jsonPath("$.content[1].name", is("comment-2")));
    }

    @Test
    void givenCommentId_whenGetCommentById_thenReturnComment() throws Exception {
        // given
        Comment existingComment = CommentTestDataFactory.comment("existing-comment");
        existingComment = saveCommentToDB(existingComment);

        // when & then
        mvc.perform(get(baseUrl + "/{id}", existingComment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingComment.getId().intValue())))
                .andExpect(jsonPath("$.name", is(existingComment.getName())))
                .andExpect(jsonPath("$.body", is(existingComment.getBody())));
    }

    @Test
    void givenNonExistingCommentId_whenGetCommentById_thenReturn404NotFoundStatus() throws Exception {
        // given
        int commentId = 999_999;

        // when & then
        mvc.perform(get(baseUrl + "/{id}", commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenCommentRequest_whenCreateComment_thenReturnCreatedComment() throws Exception {
        // given
        CommentRequest request = CommentTestDataFactory.request("new-comment");

        // when & then
        mvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(request.name())));
    }

    @Test
    void givenCommentIdAndCommentRequest_whenUpdateComment_thenReturnUpdatedComment() throws Exception {
        // given
        Comment existingComment = CommentTestDataFactory.comment("existing-comment");
        existingComment = saveCommentToDB(existingComment);

        CommentRequest updateRequest = CommentTestDataFactory.request("updated-comment");

        // when & then
        mvc.perform(put(baseUrl + "/{id}", existingComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingComment.getId().intValue())))
                .andExpect(jsonPath("$.name", is(updateRequest.name())))
                .andExpect(jsonPath("$.email", is(updateRequest.email())))
                .andExpect(jsonPath("$.body", is(updateRequest.body())));
    }

    @Test
    void givenCommentId_whenDeleteComment_thenReturnNoContent() throws Exception {
        // given
        Comment commentToBeDeleted = CommentTestDataFactory.comment("test-comment");
        commentToBeDeleted = saveCommentToDB(commentToBeDeleted);

        // when & then
        mvc.perform(delete(baseUrl + "/{id}", commentToBeDeleted.getId()))
                .andExpect(status().isNoContent());
    }

    /*------------------------ Helper Methods ------------------------*/

    private Comment saveCommentToDB(Comment comment) {
        return commentRepository.save(comment);
    }

    private void saveCommentsToDB(Comment... comments) {
        commentRepository.saveAll(Arrays.asList(comments));
    }

}