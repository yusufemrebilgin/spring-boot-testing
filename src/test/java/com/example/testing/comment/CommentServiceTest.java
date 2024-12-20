package com.example.testing.comment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    CommentService commentService;

    @Mock
    CommentMapper commentMapper;

    @Mock
    CommentRepository commentRepository;

    @Test
    void givenExistingCommentId_whenGetCommentById_thenReturnCommentResponse() {
        // given
        Comment comment = CommentTestDataFactory.comment(1L, "test-comment");
        CommentResponse expected = CommentTestDataFactory.response(comment);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentMapper.toResponse(any(Comment.class))).thenReturn(expected);

        // when
        CommentResponse actual = commentService.getCommentById(comment.getId());

        // then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNonExistingCommentId_whenGetCommentById_thenThrowCommentNotFoundException() {
        // given
        Long commentId = 999L;
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        CommentNotFoundException ex = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.getCommentById(commentId)
        );

        // then
        assertThat(ex).isNotNull();
        assertThat(ex).hasMessageContaining(String.valueOf(commentId));
    }

    @Test
    void givenPageable_whenGetAllComments_thenReturnPaginatedResponse() {
        // given
        List<Comment> comments = List.of(
                CommentTestDataFactory.comment(1L, "comment-1"),
                CommentTestDataFactory.comment(2L, "comment-2")
        );

        List<CommentResponse> responses = List.of(
                CommentTestDataFactory.response(comments.get(0)),
                CommentTestDataFactory.response(comments.get(1))
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(commentRepository.findAll(pageable)).thenReturn(commentPage);
        for (int i = 0; i < comments.size(); i++) {
            when(commentMapper.toResponse(comments.get(i))).thenReturn(responses.get(i));
        }

        // when
        Page<CommentResponse> response = commentService.getAllComments(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(responses.size());
        for (int i = 0; i < comments.size(); i++) {
            assertThat(response.getContent().get(i)).isEqualTo(responses.get(i));
        }
    }

    @Test
    void givenCommentRequest_whenCreateComment_thenReturnCreatedCommentResponse() {
        // given
        Comment comment = CommentTestDataFactory.comment(1L, "new-comment");
        CommentRequest request = CommentTestDataFactory.request(comment);
        CommentResponse expected = CommentTestDataFactory.response(comment);

        when(commentMapper.toEntity(request)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(any(Comment.class))).thenReturn(expected);

        // when
        CommentResponse actual = commentService.createComment(request);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void givenCommentIdAndCommentRequest_whenUpdateComment_thenReturnUpdatedCommentResponse() {
        // given
        Comment existingComment = CommentTestDataFactory.comment(1L, "existing-comment");
        Comment updatedComment = CommentTestDataFactory.comment(existingComment.getId(), "updated-comment");

        CommentRequest request = CommentTestDataFactory.request(updatedComment);
        CommentResponse expected = CommentTestDataFactory.response(updatedComment);

        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(existingComment)).thenReturn(updatedComment);
        when(commentMapper.toResponse(updatedComment)).thenReturn(expected);

        // when
        CommentResponse actual = commentService.updateComment(existingComment.getId(), request);

        // then
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
        verify(commentRepository, times(1)).findById(existingComment.getId());
        verify(commentRepository, times(1)).save(updatedComment);
    }

    @Test
    void givenExistingCommentId_whenDeleteComment_thenReturnExistingComment() {
        // given
        Long commentId = 1L;
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(new Comment()));

        // when & then
        commentService.deleteComment(commentId);
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).delete(any(Comment.class));
    }

    @Test
    void givenNonExistingCommentId_whenDeleteComment_thenThrowCommentNotFoundException() {
        // given
        Long commentId = 999L;
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        CommentNotFoundException ex = assertThrows(
                CommentNotFoundException.class,
                () -> commentService.deleteComment(commentId)
        );

        assertThat(ex).isNotNull();
        assertThat(ex).hasMessageContaining(String.valueOf(commentId));
        verify(commentRepository, never()).delete(any(Comment.class));
    }

}