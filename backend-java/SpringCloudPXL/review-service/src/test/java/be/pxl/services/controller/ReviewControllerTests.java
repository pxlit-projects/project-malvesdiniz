package be.pxl.services;

import be.pxl.services.client.PostClient;
import be.pxl.services.domain.dto.ReviewRequest;
import be.pxl.services.repository.ReviewRepository;
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

import java.util.UUID;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class ReviewControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @MockBean
    private PostClient postClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
    }

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }


    @Test
    void testRejectReview() throws Exception {

        UUID postId = UUID.randomUUID();
        ReviewRequest request = new ReviewRequest("Not good enough");
        String json = objectMapper.writeValueAsString(request);

        when(postClient.rejectPost(eq(postId), any())).thenReturn(null);

        mockMvc.perform(
                put("/api/review/" + postId + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("USER", "john")
                        .header("ROLE", "reviewer")
                        .content(json)
        ).andExpect(status().isOk());

        assertEquals(1, reviewRepository.findAll().size());
    }

    @Test
    void testApproveReview() throws Exception {

        UUID postId = UUID.randomUUID();

        when(postClient.approvePost(eq(postId))).thenReturn(null);

        mockMvc.perform(
                put("/api/review/" + postId + "/approve")
                        .header("USER", "john")
                        .header("ROLE", "reviewer")
        ).andExpect(status().isOk());

        assertEquals(1, reviewRepository.findAll().size());
    }

    @Test
    void testGetPendingPosts_WithReviewerRole() throws Exception {

        when(postClient.approvePost(any())).thenReturn(null);
        when(postClient.rejectPost(any(), any())).thenReturn(null);

        mockMvc.perform(
                put("/api/review/" + UUID.randomUUID() + "/approve")
                        .header("USER", "john").header("ROLE", "reviewer")
        ).andExpect(status().isOk());

        mockMvc.perform(
                put("/api/review/" + UUID.randomUUID() + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReviewRequest("bad")))
                        .header("USER", "john").header("ROLE", "reviewer")
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/review/pending")
                        .header("ROLE", "reviewer")
        ).andExpect(status().isOk());
    }
}
