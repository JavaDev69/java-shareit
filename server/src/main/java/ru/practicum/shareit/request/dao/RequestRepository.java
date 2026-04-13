package ru.practicum.shareit.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 14:30
 * @project java-shareit
 */
@Repository
public interface RequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestor(User requestor);

    List<ItemRequest> findByItemsEmpty();
}
