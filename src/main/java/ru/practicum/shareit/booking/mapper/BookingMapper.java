package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.mapper.DateTimeMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.ZoneId;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                DateTimeMapper.class
        },
        imports = {
                BookingStatus.class,
                ZoneId.class
        })
public abstract class BookingMapper {
    @Autowired
    protected ItemService itemService;
    @Autowired
    protected UserService userService;

    @Mapping(target = "status", expression = "java(BookingStatus.WAITING)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", expression = "java(itemService.getItem(bookingDto.getItemId()))")
    @Mapping(target = "booker", expression = "java(userService.getUser(bookingDto.getBookerId()))")
    public abstract Booking toEntity(BookingDto bookingDto);

    public abstract ResponseBookingDto toDto(Booking booking);
}