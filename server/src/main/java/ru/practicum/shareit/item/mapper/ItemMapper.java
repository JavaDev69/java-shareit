package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndDateDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.ResponseItemWithCommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemWithBookingDate;
import ru.practicum.shareit.request.dao.RequestRepository;

import java.time.ZoneId;

/**
 * @author Andrew Vilkov
 * @created 04.03.2026 - 17:31
 * @project java-shareit
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {
                CommentMapper.class,
                DateTimeMapper.class
        },
        imports = {
                ZoneId.class
        })
public abstract class ItemMapper {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    RequestRepository requestRepository;

    public abstract ResponseItemDto map(Item item);

    public abstract ResponseItemWithCommentDto map(ItemWithCommentsAndDateDto item);

    @Mapping(target = "available", source = "isAvailable")
    public abstract ResponseItemDto map(ItemWithBookingDate item);

    @Mapping(
            target = "lastBooking",
            expression = "java(dateTimeMapper.map(bookingRepository.getLastBookingByItemId(item.getId())))"
    )
    @Mapping(
            target = "nextBooking",
            expression = "java(dateTimeMapper.map(bookingRepository.getNextBookingByItemId(item.getId())))"
    )
    public abstract ResponseItemWithCommentDto mapWithComment(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "request",
            expression = "java(item.getRequestId()==null? null : requestRepository.findById(item.getRequestId()).get())"
    )
    public abstract Item map(ItemDto item);

}
