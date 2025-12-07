package be.pxl.services.client;


import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.domain.dto.ReviewRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "post-service")
public interface PostClient {

    @PutMapping("/api/post/{id}/approve")
    PostResponse approvePost(@PathVariable UUID id, @RequestHeader("USER") String reviewer);

    @PutMapping("/api/post/{id}/reject")
    PostResponse rejectPost(@PathVariable UUID id, @RequestHeader("USER") String reviewer, @RequestBody ReviewRequest request);

    @GetMapping("/api/post/submitted")
    Iterable<PostResponse> getSubmittedPosts();
}