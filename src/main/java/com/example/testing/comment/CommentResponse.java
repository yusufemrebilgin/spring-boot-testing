package com.example.testing.comment;

public record CommentResponse(
        Long id,
        String name,
        String email,
        String body
) {}
