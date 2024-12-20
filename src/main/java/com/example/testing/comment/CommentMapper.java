package com.example.testing.comment;

import org.springframework.stereotype.Component;

@Component
public final class CommentMapper {

    public Comment toEntity(CommentRequest commentRequest) {
        return new Comment(
                null,
                commentRequest.name(),
                commentRequest.email(),
                commentRequest.body(),
                null
        );
    }

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getName(),
                comment.getEmail(),
                comment.getBody()
        );
    }

}
