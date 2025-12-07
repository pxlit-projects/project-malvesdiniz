package be.pxl.services.service;

import be.pxl.services.config.RabbitConfig;
import be.pxl.services.domain.dto.PostRequest;
import be.pxl.services.domain.dto.FilterDtoRequest;
import be.pxl.services.domain.Post;
import be.pxl.services.domain.PostStatus;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService implements IPostService {

    private final PostRepository postRepository;

    @Override
    public List<PostResponse> getAll() {
        return postRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public PostResponse create(PostRequest request, String user, String role) {
        if(!role.equals("redact")){
            throw new RuntimeException("Only redact can make posts");
        }
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCreatedAt(LocalDateTime.now());
        post.setStatus(PostStatus.DRAFT);
        post.setAuteur(user);
        post.setReviewComment(null);

        return this.toDTO(postRepository.save(post));
    }

    public PostResponse updateDraft(UUID id, PostRequest request, String user, String role){
        if(!role.equals("redact")){
            throw new RuntimeException("Only redact can update drafts");
        }
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if(post.getStatus() != PostStatus.DRAFT){
            throw new RuntimeException("Only drafts can be edited");
        }
        if(!post.getAuteur().equals(user)){
            throw new RuntimeException("Only the auteur can make changes on the draft");
        }
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        return this.toDTO(postRepository.save(post));
    }

    @Override
    public PostResponse submitPost(UUID id, String user, String role) {
        if(!role.equals("redact")){
            throw new RuntimeException("Only redact can submit drafts");
        }
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post does not exist"));

        if (post.getStatus() != PostStatus.DRAFT) {
            throw new RuntimeException("Only drafts can be submitted");
        }

        if(!post.getAuteur().equals(user)){
            throw new RuntimeException("Only the auteur can submit the draft");
        }

        post.setStatus(PostStatus.SUBMITTED);
        return this.toDTO(postRepository.save(post));
    }

    @Override
    public List<PostResponse> getAllPostsPublished(FilterDtoRequest filter) {
        Specification<Post> spec = createPostSpecification(filter);
        return postRepository.findAll(spec)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public PostResponse approve(UUID id, String redact) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getStatus() != PostStatus.SUBMITTED) {
            throw new RuntimeException("Only submitted posts can be approved.");
        }
        if (post.getAuteur().equals(redact)) {
            throw new RuntimeException("Redact can not review their own post.");
        }
        post.setStatus(PostStatus.APPROVED);
        post.setReviewComment(null);

        log.info("EVENT RECEIVED: Post {} status updated to APPROVED at {}",
                id, LocalDateTime.now());

        return toDTO(postRepository.save(post));
    }

    @Override
    public PostResponse reject(UUID id, String redact, String comment) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getStatus() != PostStatus.SUBMITTED) {
            throw new RuntimeException("Only submitted posts can be rejected.");
        }

        post.setStatus(PostStatus.REJECTED);
        post.setReviewComment(comment);

        log.info("EVENT RECEIVED: Post {} status updated to REJECTED at {} with comment '{}'",
                id, LocalDateTime.now(), comment);

        return toDTO(postRepository.save(post));
    }

    private Specification<Post> createPostSpecification(FilterDtoRequest filter) {
        return (root, query, builder) -> {

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("status"), PostStatus.APPROVED));

            if (filter.auteur() != null && !filter.auteur().isBlank()) {
                predicates.add(
                        builder.like(
                                builder.lower(root.get("auteur")),
                                "%" + filter.auteur().toLowerCase() + "%"
                        )
                );
            }

            if (filter.text() != null && !filter.text().isBlank()) {
                String value = "%" + filter.text().toLowerCase() + "%";

                predicates.add(
                        builder.or(
                                builder.like(builder.lower(root.get("title")), value),
                                builder.like(builder.lower(root.get("content")), value)
                        )
                );
            }

            if (filter.date() != null) {
                predicates.add(
                        builder.equal(
                                builder.function("date", LocalDate.class, root.get("createdAt")),
                                filter.date()
                        )
                );
            }

            return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private PostResponse toDTO(Post post){
        PostResponse postResponse = new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuteur(),
                post.getCreatedAt(),
                post.getStatus(),
                post.getReviewComment());
        return postResponse;
    }

    @Override
    public List<PostResponse> getSubmittedPosts() {
        return postRepository.findAllByStatus(PostStatus.SUBMITTED)
                .stream()
                .map(this::toDTO)
                .toList();
    }
    @Override
    public PostResponse getById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return toDTO(post);
    }

    public void deletePost(UUID id, String user, String role) {
        if (!"redact".equals(role)) {
            throw new RuntimeException("Only redact can delete posts");
        }
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        postRepository.delete(post);
    }

    @RabbitListener(queues = RabbitConfig.APPROVED_QUEUE)
    public void handleApproved(String postId) {
        log.info("EVENT RECEIVED FROM RABBIT: Post {} approved", postId);
    }

    @RabbitListener(queues = RabbitConfig.REJECTED_QUEUE)
    public void handleRejected(String payload) {
        log.info("EVENT RECEIVED FROM RABBIT: {}", payload);
    }

}
