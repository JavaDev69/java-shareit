package ru.practicum.shareit.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestHeaders {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";
}
