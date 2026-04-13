package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingClient bookingClient;

    @Test
    void getBookingsShouldUseDefaultParams() throws Exception {
        when(bookingClient.getBookings(1L, BookingState.ALL, 0, 10))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1L, "status", "WAITING"))));

        mockMvc.perform(get("/bookings").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getOwnBookingsShouldReturnBookings() throws Exception {
        when(bookingClient.getOwnBookings(1L, BookingState.PAST, 5, 20))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 2L, "status", "APPROVED"))));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "PAST")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void bookItemShouldCreateBooking() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusHours(1));
        request.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingClient.bookItem(eq(1L), any(BookingRequestDto.class)))
                .thenReturn(ResponseEntity.status(201).body(Map.of("id", 1L, "status", "WAITING")));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getBookingShouldReturnBooking() throws Exception {
        when(bookingClient.getBooking(1L, 4L)).thenReturn(ResponseEntity.ok(
                Map.of("id", 4L, "status", "APPROVED")
        ));

        mockMvc.perform(get("/bookings/4").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateShouldReturnUpdatedBooking() throws Exception {
        when(bookingClient.updateBooking(1L, 7L, true)).thenReturn(ResponseEntity.ok(
                Map.of("id", 7L, "status", "APPROVED")
        ));

        mockMvc.perform(patch("/bookings/7")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingsShouldReturn400WhenFromIsNegative() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong(), any(BookingState.class), any(Integer.class), any(Integer.class));
    }

    @Test
    void bookItemShouldReturn400WhenStartAfterEnd() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusHours(3));
        request.setEnd(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any(BookingRequestDto.class));
    }
}
