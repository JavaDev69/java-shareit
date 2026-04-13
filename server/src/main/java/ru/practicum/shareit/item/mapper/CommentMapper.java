package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ResponseCommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.Instant;
import java.time.ZoneId;

/**
 * @author Andrew Vilkov
 * @created 04.03.2026 - 17:31
 * @project java-shareit
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {
                DateTimeMapper.class
        },
        imports = {
                ZoneId.class,
                Instant.class
        })
public interface CommentMapper {
    @Mapping(target = "created", expression = "java(Instant.now())")
    Comment map(CommentDto dto);

    @Mapping(target = "authorName", source = "author.name")
    ResponseCommentDto map(Comment comment);
}
