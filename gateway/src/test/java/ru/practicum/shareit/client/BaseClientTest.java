package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.common.RequestHeaders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {
    @Mock
    private RestTemplate restTemplate;

    private TestClient baseClient;

    @BeforeEach
    void setUp() {
        baseClient = new TestClient(restTemplate);
    }

    @Test
    void getWithUserAndParametersShouldSetHeadersAndCallExchangeWithParameters() {
        Map<String, Object> parameters = Map.of("state", "ALL");
        when(restTemplate.exchange(eq("/bookings?state={state}"), eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(), eq(Object.class), same(parameters)))
                .thenReturn(ResponseEntity.ok(Map.of("ok", true)));

        ResponseEntity<Object> response = baseClient.callGet("/bookings?state={state}", 15L, parameters);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("ok", true), response.getBody());

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/bookings?state={state}"), eq(HttpMethod.GET), entityCaptor.capture(),
                eq(Object.class), same(parameters));

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(List.of(MediaType.APPLICATION_JSON), headers.getAccept());
        assertEquals("15", headers.getFirst(RequestHeaders.USER_ID_HEADER));
        assertNull(entityCaptor.getValue().getBody());
    }

    @Test
    void postWithoutUserShouldNotSetUserHeaderAndPassBody() {
        Map<String, Object> body = Map.of("name", "John");
        when(restTemplate.exchange(eq("/users"), eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1L)));

        ResponseEntity<Object> response = baseClient.callPost("/users", body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Map.of("id", 1L), response.getBody());

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/users"), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class));

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertFalse(headers.containsKey(RequestHeaders.USER_ID_HEADER));
        assertEquals(body, entityCaptor.getValue().getBody());
    }

    @Test
    void patchWithOnlyUserIdShouldSendNullBody() {
        when(restTemplate.exchange(eq("/bookings/1"), eq(HttpMethod.PATCH),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = baseClient.callPatch("/bookings/1", 88L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/bookings/1"), eq(HttpMethod.PATCH), entityCaptor.capture(), eq(Object.class));

        assertEquals("88", entityCaptor.getValue().getHeaders().getFirst(RequestHeaders.USER_ID_HEADER));
        assertNull(entityCaptor.getValue().getBody());
    }

    @Test
    void deleteWithParametersShouldCallExchangeWithDeleteMethod() {
        Map<String, Object> parameters = Map.of("force", true);
        when(restTemplate.exchange(eq("/items/1?force={force}"), eq(HttpMethod.DELETE),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(), eq(Object.class), same(parameters)))
                .thenReturn(ResponseEntity.noContent().build());

        ResponseEntity<Object> response = baseClient.callDelete("/items/1?force={force}", 20L, parameters);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/items/1?force={force}"), eq(HttpMethod.DELETE), entityCaptor.capture(),
                eq(Object.class), same(parameters));

        assertEquals("20", entityCaptor.getValue().getHeaders().getFirst(RequestHeaders.USER_ID_HEADER));
    }

    @Test
    void shouldReturnRawErrorResponseWhenRestTemplateThrowsHttpStatusCodeException() {
        byte[] responseBody = "validation error".getBytes(StandardCharsets.UTF_8);
        when(restTemplate.exchange(eq("/bookings"), eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(), eq(Object.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                        HttpHeaders.EMPTY, responseBody, StandardCharsets.UTF_8));

        ResponseEntity<Object> response = baseClient.callGet("/bookings");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof byte[]);
        assertArrayEquals(responseBody, (byte[]) response.getBody());
    }

    private static class TestClient extends BaseClient {

        TestClient(RestTemplate rest) {
            super(rest);
        }

        ResponseEntity<Object> callGet(String path) {
            return get(path);
        }

        ResponseEntity<Object> callGet(String path, Long userId, Map<String, Object> parameters) {
            return get(path, userId, parameters);
        }

        <T> ResponseEntity<Object> callPost(String path, T body) {
            return post(path, body);
        }

        <T> ResponseEntity<Object> callPatch(String path, long userId) {
            return patch(path, userId);
        }

        ResponseEntity<Object> callDelete(String path, Long userId, Map<String, Object> parameters) {
            return delete(path, userId, parameters);
        }
    }
}
