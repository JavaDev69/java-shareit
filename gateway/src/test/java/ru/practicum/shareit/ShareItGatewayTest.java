package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Andrew Vilkov
 * @created 13.04.2026 - 22:58
 * @project java-shareit
 */
@SpringBootTest
@ActiveProfiles("test")
class ShareItGatewayTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void shouldCreateGatewayControllersAndClientsBeans() {
        assertNotNull(applicationContext.getBean(UserController.class));
        assertNotNull(applicationContext.getBean(ItemController.class));
        assertNotNull(applicationContext.getBean(BookingController.class));
        assertNotNull(applicationContext.getBean(ItemRequestController.class));

        assertNotNull(applicationContext.getBean(UserClient.class));
        assertNotNull(applicationContext.getBean(ItemClient.class));
        assertNotNull(applicationContext.getBean(BookingClient.class));
        assertNotNull(applicationContext.getBean(ItemRequestClient.class));
    }
}
