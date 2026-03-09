package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.user.UserController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShareItTests {
    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    @Test
    void contextLoads() {
        assertThat(userController).as("User controller").isNotNull();
        assertThat(itemController).as("Item controller").isNotNull();
    }

}
