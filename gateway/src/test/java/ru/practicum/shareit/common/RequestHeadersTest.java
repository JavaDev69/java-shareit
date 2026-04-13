package ru.practicum.shareit.common;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class RequestHeadersTest {
    @Test
    void shouldExposeExpectedUserIdHeaderName() {
        assertThat(RequestHeaders.USER_ID_HEADER).isEqualTo("X-Sharer-User-Id");
    }

    @Test
    void constructorShouldBePrivate() throws Exception {
        Constructor<RequestHeaders> constructor = RequestHeaders.class.getDeclaredConstructor();

        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        assertThat(constructor.newInstance()).isNotNull();
    }
}
