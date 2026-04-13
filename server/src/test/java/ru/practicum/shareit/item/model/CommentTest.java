package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {
    @Test
    void equalsShouldReturnTrueForSameInstance() {
        Comment comment = Comment.builder().id(1L).text("text").build();

        assertThat(comment).isEqualTo(comment);
    }

    @Test
    void equalsShouldReturnFalseForNull() {
        Comment comment = Comment.builder().id(1L).text("text").build();

        assertThat(comment.equals(null)).isFalse();
    }

    @Test
    void equalsShouldReturnFalseForDifferentClass() {
        Comment comment = Comment.builder().id(1L).text("text").build();

        assertThat(comment.equals("not-a-comment")).isFalse();
    }

    @Test
    void equalsShouldReturnFalseWhenIdsAreNull() {
        Comment first = Comment.builder().text("first").build();
        Comment second = Comment.builder().text("second").build();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void equalsShouldReturnTrueWhenIdsAreEqual() {
        Comment first = Comment.builder().id(10L).text("first").build();
        Comment second = Comment.builder().id(10L).text("second").build();

        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(first);
    }

    @Test
    void equalsShouldReturnFalseWhenIdsAreDifferent() {
        Comment first = Comment.builder().id(10L).text("first").build();
        Comment second = Comment.builder().id(11L).text("second").build();

        assertThat(first).isNotEqualTo(second);
        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void hashCodeShouldBeEqualForObjectsWithSameId() {
        Comment first = Comment.builder().id(10L).text("first").build();
        Comment second = Comment.builder().id(10L).text("second").build();

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
