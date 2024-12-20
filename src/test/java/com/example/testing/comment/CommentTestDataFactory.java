package com.example.testing.comment;

public class CommentTestDataFactory {

    public static Comment comment(Long id, String name) {
        return comment(id, name, "email@example.com", "default-comment-body");
    }

    public static Comment comment(Long id, String name, String email, String body) {
        return new Comment(id, name, email, body);
    }

    public static CommentRequest request(Comment c) {
        return new CommentRequest(c.getName(), c.getEmail(), c.getBody());
    }

    public static CommentResponse response(Comment c) {
        return new CommentResponse(c.getId(), c.getName(), c.getEmail(), c.getBody());
    }

}