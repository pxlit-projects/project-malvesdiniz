package be.pxl.services.controller;

import be.pxl.services.client.PostClient;
import be.pxl.services.domain.dto.CommentRequest;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.repository.CommentRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class CommentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @MockBean
    private PostClient postClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void cleanDatabase() {
        commentRepository.deleteAll();
    }

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void setupProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    private PostResponse createFakeApprovedPost(UUID postId) {
        return new PostResponse(
                postId,
                "Title",
                "Content",
                "Author",
                LocalDateTime.now(),
                "APPROVED"
        );
    }

    @Test
    void testAddComment() throws Exception {

        UUID postId = UUID.randomUUID();

        when(postClient.getPost(postId)).thenReturn(createFakeApprovedPost(postId));

        CommentRequest request = new CommentRequest("Very good!");
        String json = mapper.writeValueAsString(request);

        mockMvc.perform(
                post("/api/comment/" + postId)
                        .header("USER", "Maria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());

        assertEquals(1, commentRepository.findAll().size());
    }


    @Test
    void testGetComments() throws Exception {

        UUID postId = UUID.randomUUID();

        when(postClient.getPost(postId)).thenReturn(createFakeApprovedPost(postId));

        mockMvc.perform(
                post("/api/comment/" + postId)
                        .header("USER", "john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CommentRequest("Nice!")))
        ).andExpect(status().isOk());

        mockMvc.perform(
                post("/api/comment/" + postId)
                        .header("USER", "anna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CommentRequest("I agree!")))
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/comment/" + postId)
        ).andExpect(status().isOk());

        assertEquals(2, commentRepository.findAll().size());
    }

    @Test
    void testEditComment() throws Exception {

        UUID postId = UUID.randomUUID();

        when(postClient.getPost(postId)).thenReturn(createFakeApprovedPost(postId));

        mockMvc.perform(
                post("/api/comment/" + postId)
                        .header("USER", "john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CommentRequest("old text")))
        ).andExpect(status().isOk());

        UUID commentId = commentRepository.findAll().get(0).getId();

        CommentRequest editReq = new CommentRequest("updated text");

        mockMvc.perform(
                put("/api/comment/edit/" + commentId)
                        .header("USER", "john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(editReq))
        ).andExpect(status().isOk());

        assertEquals("updated text", commentRepository.findAll().get(0).getText());
    }

    @Test
    void testDeleteComment() throws Exception {

        UUID postId = UUID.randomUUID();

        when(postClient.getPost(postId)).thenReturn(createFakeApprovedPost(postId));

        mockMvc.perform(
                post("/api/comment/" + postId)
                        .header("USER", "john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new CommentRequest("to delete")))
        ).andExpect(status().isOk());

        UUID commentId = commentRepository.findAll().get(0).getId();

        mockMvc.perform(
                delete("/api/comment/delete/" + commentId)
                        .header("USER", "john")
        ).andExpect(status().isNoContent());

        assertEquals(0, commentRepository.findAll().size());
    }
}
