package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ResponseCommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {
    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() {
        DateTimeMapper dtMapper = Mappers.getMapper(DateTimeMapper.class);
        commentMapper = Mappers.getMapper(CommentMapper.class);
        ReflectionTestUtils.setField(commentMapper, "dateTimeMapper", dtMapper);
    }

    @Test
    @DisplayName("Проверка маппинга CommentDto в Comment с текущей датой создания")
    void testMapToComment() {
        CommentDto commentDto = new CommentDto("Текст комментария");

        Comment comment = commentMapper.map(commentDto);

        assertThat(comment.getText()).isEqualTo(commentDto.text());
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    @DisplayName("Проверка маппинга Comment в ResponseCommentDto с именем автора")
    void testMapToResponseCommentDto() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setCreated(Instant.now());
        comment.setText("Текст комментария");
        comment.setAuthor(new User(1L, "Автор", "author@mail.ru"));

        LocalDateTime expected = LocalDateTime.ofInstant(comment.getCreated(), ZoneOffset.UTC);

        ResponseCommentDto responseCommentDto = commentMapper.map(comment);

        assertThat(responseCommentDto.id()).isEqualTo(comment.getId());
        assertThat(responseCommentDto.text()).isEqualTo(comment.getText());
        assertThat(responseCommentDto.authorName()).isEqualTo(comment.getAuthor().getName());
        assertThat(responseCommentDto.created()).isEqualTo(expected);
    }
}
