package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemComment;
import ru.practicum.shareit.item.model.ItemForRequest;
import ru.practicum.shareit.item.model.ItemWithBookingDate;

import java.util.Collection;
import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 08.03.2026 - 14:32
 * @project java-shareit
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    String LAST_BOOKING_QUERY = "(SELECT b.start_date " +
            "FROM bookings AS b " +
            "WHERE b.item_id=it.id AND b.end_date < NOW() AND status='APPROVED' " +
            "ORDER BY b.end_date DESC " +
            "LIMIT 1)";
    String NEXT_BOOKING_QUERY = "(SELECT b.start_date " +
            "FROM bookings AS b " +
            "WHERE b.item_id=it.id AND b.start_date > NOW() AND status='APPROVED' " +
            "ORDER BY b.start_date ASC " +
            "LIMIT 1)";

    @NativeQuery(value = "SELECT " +
            "it.*, " +
            LAST_BOOKING_QUERY + " AS last_booking, " +
            NEXT_BOOKING_QUERY + " AS next_booking " +
            "FROM items AS it " +
            "WHERE it.owner_id=:userId " +
            "ORDER BY it.id ASC ")
    List<ItemWithBookingDate> findAllByOwnerId(@Param("userId") Long userId);

    @Query(value = "SELECT c.id, c.item.id AS itemId, c.text, c.author.name AS authorName, c.created " +
            "FROM Comment c " +
            "WHERE c.item.id IN :itemIds")
    List<ItemComment> findCommentsByItemIds(@Param("itemIds") List<Long> itemIds);

    @Query("select it from Item as it " +
            "where (lower(it.description) like lower(concat('%',:text,'%')) " +
            "or lower(it.name) like lower(concat('%',:text,'%')))" +
            "and it.available=true")
    List<Item> findAllByText(@Param("text") String text);

    List<ItemForRequest> findByRequestIdIn(Collection<Long> requestIds);
}
