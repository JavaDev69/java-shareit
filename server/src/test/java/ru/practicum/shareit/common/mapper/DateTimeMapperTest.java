package ru.practicum.shareit.common.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeMapperTest {
    private final DateTimeMapper mapper = Mappers.getMapper(DateTimeMapper.class);

    @Test
    void mapInstantToLocalDateTimeShouldConvertUsingUtc() {
        Instant instant = Instant.parse("2026-04-13T12:30:45Z");

        LocalDateTime actual = mapper.map(instant);

        assertThat(actual).isEqualTo(LocalDateTime.of(2026, 4, 13, 12, 30, 45));
    }

    @Test
    void mapInstantToLocalDateTimeShouldReturnNullForNullInput() {
        assertThat(mapper.map((Instant) null)).isNull();
    }

    @Test
    void mapLocalDateTimeToInstantShouldConvertUsingUtc() {
        LocalDateTime localDateTime = LocalDateTime.of(2026, 4, 13, 15, 0, 0);

        Instant actual = mapper.map(localDateTime);

        assertThat(actual).isEqualTo(localDateTime.toInstant(ZoneOffset.UTC));
    }

    @Test
    void mapLocalDateTimeToInstantShouldReturnNullForNullInput() {
        assertThat(mapper.map((LocalDateTime) null)).isNull();
    }
}
