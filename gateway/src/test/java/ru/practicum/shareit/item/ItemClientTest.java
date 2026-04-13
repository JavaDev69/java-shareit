package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ItemClientTest {
    private TestableItemClient itemClient;

    @BeforeEach
    void setUp() {
        itemClient = new TestableItemClient();
    }

    @Test
    void getItemsByTextShouldUseSearchPathAndTextParameter() {
        itemClient.getItems(8L, "drill");

        assertEquals("get", itemClient.lastCall);
        assertEquals("/search", itemClient.lastPath);
        assertEquals(8L, itemClient.lastUserId);
        assertEquals("drill", itemClient.lastParameters.get("text"));
    }

    @Test
    void getItemsShouldRequestRootPathForOwner() {
        itemClient.getItems(9L);

        assertEquals("get", itemClient.lastCall);
        assertEquals("", itemClient.lastPath);
        assertEquals(9L, itemClient.lastUserId);
        assertNull(itemClient.lastParameters);
    }

    @Test
    void getItemShouldRequestPathWithItemId() {
        itemClient.getItem(3L, 44L);

        assertEquals("get", itemClient.lastCall);
        assertEquals("/44", itemClient.lastPath);
        assertEquals(3L, itemClient.lastUserId);
    }

    @Test
    void saveItemShouldPostBody() {
        ItemDto request = new ItemDto();

        itemClient.saveItem(5L, request);

        assertEquals("post", itemClient.lastCall);
        assertEquals("", itemClient.lastPath);
        assertEquals(5L, itemClient.lastUserId);
        assertSame(request, itemClient.lastBody);
    }

    @Test
    void updateItemShouldPatchPathWithBody() {
        UpdateItemDto request = new UpdateItemDto();

        itemClient.updateItem(6L, 77L, request);

        assertEquals("patch", itemClient.lastCall);
        assertEquals("/77", itemClient.lastPath);
        assertEquals(6L, itemClient.lastUserId);
        assertSame(request, itemClient.lastBody);
    }

    @Test
    void saveCommentShouldPostToCommentEndpoint() {
        CommentDto request = new CommentDto("Nice one");

        itemClient.saveComment(12L, 4L, request);

        assertEquals("post", itemClient.lastCall);
        assertEquals("/4/comment", itemClient.lastPath);
        assertEquals(12L, itemClient.lastUserId);
        assertSame(request, itemClient.lastBody);
    }

    private static class TestableItemClient extends ItemClient {
        private String lastCall;
        private String lastPath;
        private Long lastUserId;
        private Map<String, Object> lastParameters;
        private Object lastBody;

        TestableItemClient() {
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
