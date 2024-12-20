package com.example.testing.comment;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper mapper;
    private final CommentRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private Comment findCommentById(Long commentId) {
        return repository.findById(commentId).orElseThrow(() -> {
            logger.error("Comment not found with id {}", commentId);
            return new CommentNotFoundException(commentId);
        });
    }

    public CommentResponse getCommentById(Long commentId) {
        return mapper.toResponse(findCommentById(commentId));
    }

    public Page<CommentResponse> getAllComments(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public CommentResponse createComment(CommentRequest request) {
        logger.info("Creating a new comment {}", request);
        Comment commentToBeSaved = mapper.toEntity(request);
        commentToBeSaved = repository.save(commentToBeSaved);

        logger.info("Successfully created comment with id {}", commentToBeSaved.getId());
        return mapper.toResponse(commentToBeSaved);
    }

    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        logger.info("Updating comment with id {}", commentId);
        Comment commentToBeUpdated = findCommentById(commentId);
        // updating fields
        commentToBeUpdated.setName(request.name());
        commentToBeUpdated.setEmail(request.email());
        commentToBeUpdated.setBody(request.body());

        logger.info("Successfully updated comment with id {}", commentId);
        return mapper.toResponse(repository.save(commentToBeUpdated));
    }

    public void deleteComment(Long commentId) {
        logger.info("Deleting comment with id {}", commentId);
        Comment commentToBeDeleted = findCommentById(commentId);

        repository.delete(commentToBeDeleted);
        logger.info("Successfully deleted comment with id {}", commentId);
    }

}
