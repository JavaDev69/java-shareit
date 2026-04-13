package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class BookingClientTest {
    private TestableBookingClient bookingClient;

    @BeforeEach
    void setUp() {
        bookingClient = new TestableBookingClient();
    }

    @Test
    void getBookingsShouldUseStateAndPagingInQuery() {
        bookingClient.getBookings(10L, BookingState.WAITING, 5, 20);

        assertEquals("get", bookingClient.lastCall);
        assertEquals("?state={state}&from={from}&size={size}", bookingClient.lastPath);
        assertEquals(10L, bookingClient.lastUserId);
        assertEquals("WAITING", bookingClient.lastParameters.get("state"));
        assertEquals(5, bookingClient.lastParameters.get("from"));
        assertEquals(20, bookingClient.lastParameters.get("size"));
    }

    @Test
    void getOwnBookingsShouldUseOwnerPrefixAndQuery() {
        bookingClient.getOwnBookings(15L, BookingState.ALL, 0, 10);

        assertEquals("get", bookingClient.lastCall);
        assertEquals("/owner?state={state}&from={from}&size={size}", bookingClient.lastPath);
        assertEquals(15L, bookingClient.lastUserId);
        assertEquals("ALL", bookingClient.lastParameters.get("state"));
        assertEquals(0, bookingClient.lastParameters.get("from"));
        assertEquals(10, bookingClient.lastParameters.get("size"));
    }

    @Test
    void bookItemShouldPostRequestBody() {
        BookingRequestDto requestDto = new BookingRequestDto();

        bookingClient.bookItem(2L, requestDto);

        assertEquals("post", bookingClient.lastCall);
        assertEquals("", bookingClient.lastPath);
        assertEquals(2L, bookingClient.lastUserId);
        assertSame(requestDto, bookingClient.lastBody);
    }

    @Test
    void getBookingShouldRequestPathWithBookingId() {
        bookingClient.getBooking(4L, 22L);

        assertEquals("get", bookingClient.lastCall);
        assertEquals("/22", bookingClient.lastPath);
        assertEquals(4L, bookingClient.lastUserId);
        assertNull(bookingClient.lastParameters);
    }

    @Test
    void updateBookingShouldPatchApprovalFlag() {
        bookingClient.updateBooking(11L, 33L, true);

        assertEquals("patch", bookingClient.lastCall);
        assertEquals("/33?approved={approved}", bookingClient.lastPath);
        assertEquals(11L, bookingClient.lastUserId);
        assertEquals(true, bookingClient.lastParameters.get("approved"));
        assertNull(bookingClient.lastBody);
    }

    private static class TestableBookingClient extends BookingClient {
        private String lastCall;
        private String lastPath;
        private Long lastUserId;
        private Map<String, Object> lastParameters;
        private Object lastBody;

        TestableBookingClient() {
            super("http://localhost:9090", new RestTemplateBuilder());
        }

        @Override
        protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
            this.lastCall = "get";
            this.lastPath = path;
            this.lastUserId = userId;
            this.lastParameters = parameters;
            this.lastBody = null;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> post(String path, Long userId, Map<String, Object> parameters, T body) {
            this.lastCall = "post";
            this.lastPath = path;
            this.lastUserId = userId;
            this.lastParameters = parameters;
            this.lastBody = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> patch(String path, Long userId, Map<String, Object> parameters, T body) {
            this.lastCall = "patch";
            this.lastPath = path;
            this.lastUserId = userId;
            this.lastParameters = parameters;
            this.lastBody = body;
            return ResponseEntity.ok().build();
        }
    }
}
