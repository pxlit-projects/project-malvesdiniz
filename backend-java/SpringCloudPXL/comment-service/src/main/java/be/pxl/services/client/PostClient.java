package be.pxl.services.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "post-service")
public interface PostClient {
    @GetMapping("/api/post/{id}/status")
    String getStatus(@PathVariable UUID id);
}
