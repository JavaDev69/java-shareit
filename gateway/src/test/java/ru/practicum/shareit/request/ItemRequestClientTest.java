package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ItemRequestClientTest {
    private TestableItemRequestClient itemRequestClient;

    @BeforeEach
    void setUp() {
        itemRequestClient = new TestableItemRequestClient();
    }

    @Test
    void getAllRequestsShouldUseAllPrefix() {
        itemRequestClient.getAllRequests(1L);

        assertEquals("get", itemRequestClient.lastCall);
        assertEquals("/all", itemRequestClient.lastPath);
        assertEquals(1L, itemRequestClient.lastUserId);
        assertNull(itemRequestClient.lastParameters);
    }

    @Test
    void getOwnRequestsShouldUseRootPath() {
        itemRequestClient.getOwnRequests(2L);

        assertEquals("get", itemRequestClient.lastCall);
        assertEquals("", itemRequestClient.lastPath);
        assertEquals(2L, itemRequestClient.lastUserId);
    }

    @Test
    void getRequestShouldUsePathWithRequestId() {
        itemRequestClient.getRequest(3L, 12L);

        assertEquals("get", itemRequestClient.lastCall);
        assertEquals("/12", itemRequestClient.lastPath);
        assertEquals(3L, itemRequestClient.lastUserId);
    }

    @Test
    void saveRequestShouldPostBodyToRootPath() {
        ItemRequestDto requestDto = new ItemRequestDto("Need a drill");

        itemRequestClient.saveRequest(4L, requestDto);

        assertEquals("post", itemRequestClient.lastCall);
        assertEquals("", itemRequestClient.lastPath);
        assertEquals(4L, itemRequestClient.lastUserId);
        assertSame(requestDto, itemRequestClient.lastBody);
    }

    private static class TestableItemRequestClient extends ItemRequestClient {
        private String lastCall;
        private String lastPath;
        private Long lastUserId;
        private Map<String, Object> lastParameters;
        private Object lastBody;

        TestableItemRequestClient() {
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
    }
}
