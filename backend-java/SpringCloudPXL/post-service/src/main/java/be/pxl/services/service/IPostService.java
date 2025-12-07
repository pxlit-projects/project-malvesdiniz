package be.pxl.services.service;

import be.pxl.services.domain.Post;
import be.pxl.services.domain.dto.FilterDtoRequest;
import be.pxl.services.domain.dto.PostRequest;
import be.pxl.services.domain.dto.PostResponse;

import java.util.List;
import java.util.UUID;

public interface IPostService {
    List<PostResponse> getAll();
    PostResponse create(PostRequest request, String user, String role);
    PostResponse updateDraft(UUID id, PostRequest request, String user, String role);
    PostResponse submitPost(UUID id, String user, String role);
    List<PostResponse> getAllPostsPublished(FilterDtoRequest filter);
    PostResponse approve(UUID id, String redact);
    PostResponse reject(UUID id, String redact,String comment);
    List<PostResponse> getSubmittedPosts();
    PostResponse getById(UUID id);
    void deletePost(UUID id, String user, String role);

}
