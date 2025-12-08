package be.pxl.services.controller;

import be.pxl.services.domain.Post;
import be.pxl.services.domain.PostStatus;
import be.pxl.services.domain.dto.PostRequest;
import be.pxl.services.domain.dto.ReviewDecisionRequest;
import be.pxl.services.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PostRepository postRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeEach
    void setup() {
        postRepository.deleteAll();
    }

    @Test
    void testCreatePost_ShouldSucceed_WhenRoleIsRedact() throws Exception {

        PostRequest request = new PostRequest("Title", "Content");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("USER", "maria")
                        .header("ROLE", "redact")
                        .content(json)
        ).andExpect(status().isCreated());

        assertEquals(1, postRepository.findAll().size());
    }



    @Test
    void testUpdateDraft_ShouldSucceed() throws Exception {

        Post post = new Post(
                UUID.randomUUID(),
                "Old Title",
                "Old Content",
                "maria",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );
        postRepository.save(post);

        PostRequest update = new PostRequest("New Title", "New Content");
        String json = objectMapper.writeValueAsString(update);

        mockMvc.perform(
                put("/api/post/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("USER", "maria")
                        .header("ROLE", "redact")
                        .content(json)
        ).andExpect(status().isOk());

        Post updated = postRepository.findById(post.getId()).orElseThrow();
        assertEquals("New Title", updated.getTitle());
    }



    @Test
    void testSubmitPost_ShouldSucceed() throws Exception {

        Post post = new Post(
                UUID.randomUUID(),
                "T",
                "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );
        postRepository.save(post);

        mockMvc.perform(
                put("/api/post/" + post.getId() + "/submit")
                        .header("USER", "maria")
                        .header("ROLE", "redact")
        ).andExpect(status().isOk());

        assertEquals(PostStatus.SUBMITTED,
                postRepository.findById(post.getId()).get().getStatus());
    }


    @Test
    void testApprovePost_ShouldSucceed() throws Exception {

        Post post = new Post(
                UUID.randomUUID(),
                "T",
                "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.SUBMITTED,
                null
        );
        postRepository.save(post);

        mockMvc.perform(
                put("/api/post/" + post.getId() + "/approve")
                        .header("USER", "redact")
                        .header("ROLE", "redact")
        ).andExpect(status().isOk());

        assertEquals(PostStatus.APPROVED,
                postRepository.findById(post.getId()).get().getStatus());
    }

    @Test
    void testRejectPost_ShouldSucceed() throws Exception {

        Post post = new Post(
                UUID.randomUUID(),
                "T",
                "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.SUBMITTED,
                null
        );
        postRepository.save(post);

        ReviewDecisionRequest req = new ReviewDecisionRequest("Not good");
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(
                put("/api/post/" + post.getId() + "/reject")
                        .header("USER", "redact")
                        .header("ROLE", "redact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());

        Post updated = postRepository.findById(post.getId()).get();
        assertEquals(PostStatus.REJECTED, updated.getStatus());
        assertEquals("Not good", updated.getReviewComment());
    }


}

