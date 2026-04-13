package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

/**
 * @author Andrew Vilkov
 * @created 07.04.2026 - 20:51
 * @project java-shareit
 */
@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";
    private static final String ALL_REQUEST_PREFIX = "/all";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> getAllRequests(long userId) {
        return get(ALL_REQUEST_PREFIX, userId);
    }


    public ResponseEntity<Object> getOwnRequests(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getRequest(long userId, Long requestId) {
        return get("/" + requestId, userId);
    }

    public ResponseEntity<Object> saveRequest(long userId, ItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

}
