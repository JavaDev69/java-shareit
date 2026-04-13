package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class UserClientTest {
    private TestableUserClient userClient;

    @BeforeEach
    void setUp() {
        userClient = new TestableUserClient();
    }

    @Test
    void getUsersShouldRequestRootPath() {
        userClient.getUsers();

        assertEquals("getNoUser", userClient.lastCall);
        assertEquals("", userClient.lastPath);
    }

    @Test
    void getUserShouldRequestPathWithUserId() {
        userClient.getUser(17L);

        assertEquals("getNoUser", userClient.lastCall);
        assertEquals("/17", userClient.lastPath);
    }

    @Test
    void saveUserShouldPostBody() {
        UserDto request = new UserDto();

        userClient.saveUser(request);

        assertEquals("postNoUser", userClient.lastCall);
        assertEquals("", userClient.lastPath);
        assertSame(request, userClient.lastBody);
    }

    @Test
    void updateUserShouldPatchUserPathWithBody() {
        UpdateUserDto request = new UpdateUserDto();

        userClient.updateUser(9L, request);

        assertEquals("patchNoUser", userClient.lastCall);
        assertEquals("/9", userClient.lastPath);
        assertSame(request, userClient.lastBody);
    }

    @Test
    void deleteUserShouldDeletePathWithUserId() {
        userClient.deleteUser(5L);

        assertEquals("deleteNoUser", userClient.lastCall);
        assertEquals("/5", userClient.lastPath);
        assertNull(userClient.lastBody);
    }

    private static class TestableUserClient extends UserClient {
        private String lastCall;
        private String lastPath;
        private Object lastBody;

        TestableUserClient() {
            super("http://localhost:9090", new RestTemplateBuilder());
        }

        @Override
        protected ResponseEntity<Object> get(String path) {
            this.lastCall = "getNoUser";
            this.lastPath = path;
            this.lastBody = null;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> post(String path, T body) {
            this.lastCall = "postNoUser";
            this.lastPath = path;
            this.lastBody = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> patch(String path, T body) {
            this.lastCall = "patchNoUser";
            this.lastPath = path;
            this.lastBody = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> delete(String path) {
            this.lastCall = "deleteNoUser";
            this.lastPath = path;
            this.lastBody = null;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
            throw new UnsupportedOperationException("Not expected in these tests");
        }
    }
}
