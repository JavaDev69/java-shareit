package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.mapper.DateTimeMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.RequestHeaders.USER_ID_HEADER;

/**
 * @author Andrew Vilkov
 * @created 28.03.2026 - 13:03
 * @project java-shareit
 */
@WebMvcTest(BookingController.class)
@Import({BookingMapperImpl.class, DateTimeMapperImpl.class})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveShouldCreateBooking() throws Exception {
        Long userId = 1L;

        BookingDto request = new BookingDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        Item item = Item.builder().id(1L).available(true).build();
        User user = User.builder().id(userId).build();

        Booking saved = Booking.builder()
                .id(1L)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        when(itemService.getItem(1L)).thenReturn(item);
        when(userService.getUser(userId)).thenReturn(user);

        when(bookingService.save(any(Booking.class))).thenReturn(saved);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).save(argThat(booking ->
                booking.getItem().getId().equals(1L) &&
                        booking.getBooker().getId().equals(userId) &&
                        booking.getStatus() == BookingStatus.WAITING
        ));
    }

    @Test
    void updateShouldApproveBooking() throws Exception {
        Long userId = 1L;

        Booking updated = Booking.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.setApproveStatus(userId, 1L, true)).thenReturn(updated);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void findOneShouldReturnBooking() throws Exception {
        Long userId = 1L;

        Item item = Item.builder().id(1L).build();
        User user = User.builder().id(userId).build();

        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .start(Instant.now())
                .end(Instant.now().plusSeconds(3600))
                .build();

        when(bookingService.findById(1L, userId)).thenReturn(booking);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void findAllForBookerShouldReturnBookings() throws Exception {
        Long userId = 1L;

        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .start(Instant.now())
                .end(Instant.now().plusSeconds(3600))
                .build();

        when(bookingService.findByUserIdAndState(userId, BookingState.ALL))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }


    @Test
    void findAllForBookerShouldReturnBookingsWithDefaultState() throws Exception {
        Long userId = 1L;

        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.findByUserIdAndState(userId, BookingState.ALL))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findAllForBookerShouldReturnBookingsWithState() throws Exception {
        Long userId = 1L;

        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.findByUserIdAndState(userId, BookingState.PAST))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", BookingState.PAST.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void findAllForOwnerShouldReturnBookings() throws Exception {
        Long userId = 1L;

        Booking booking = Booking.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.findByOwnerIdAndState(userId, BookingState.ALL))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}