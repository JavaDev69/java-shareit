package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndDateDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShareItTests {
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private RequestRepository requestRepository;

    @Test
    void userServiceGetAllUsersShouldReturnPersistedUsers() {
        User first = userRepository.save(newUser("first", "first@test.local"));
        User second = userRepository.save(newUser("second", "second@test.local"));

        List<User> actual = userService.getAllUsers();

        assertThat(actual)
                .extracting(User::getId)
                .contains(first.getId(), second.getId());
    }

    @Test
    void userServiceGetUserShouldReturnUserById() {
        User saved = userRepository.save(newUser("alex", "alex@test.local"));

        User actual = userService.getUser(saved.getId());

        assertThat(actual.getId()).isEqualTo(saved.getId());
        assertThat(actual.getEmail()).isEqualTo("alex@test.local");
    }

    @Test
    void userServiceSaveUserShouldPersistNewUser() {
        User newUser = newUser("save", "save@test.local");

        User saved = userService.saveUser(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("save");
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void userServiceUpdateUserShouldUpdateNameAndEmail() {
        User saved = userRepository.save(newUser("before", "before@test.local"));
        UpdateUserDto update = new UpdateUserDto();
        update.setName("after");
        update.setEmail("after@test.local");

        User updated = userService.updateUser(saved.getId(), update);

        assertThat(updated.getName()).isEqualTo("after");
        assertThat(updated.getEmail()).isEqualTo("after@test.local");
    }

    @Test
    void userServiceDeleteUserShouldRemoveUser() {
        User saved = userRepository.save(newUser("to-delete", "to-delete@test.local"));

        userService.deleteUser(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void itemServiceGetAllItemsShouldReturnOwnerItemsWithCommentsAndBookings() {
        User owner = userRepository.save(newUser("owner", "owner-items@test.local"));
        User booker = userRepository.save(newUser("booker", "booker-items@test.local"));

        Item item = itemRepository.save(newItem(owner, "drill", "desc", true));
        bookingRepository.save(newBooking(item, booker,
                Instant.now().minusSeconds(7_200),
                Instant.now().minusSeconds(3_600),
                BookingStatus.APPROVED));
        bookingRepository.save(newBooking(item, booker,
                Instant.now().plusSeconds(3_600),
                Instant.now().plusSeconds(7_200),
                BookingStatus.APPROVED));
        commentRepository.save(Comment.builder()
                .text("great")
                .item(item)
                .author(booker)
                .created(Instant.now().minusSeconds(100))
                .build());

        List<ItemWithCommentsAndDateDto> actual = itemService.getAllItems(owner.getId());

        assertThat(actual).hasSize(1);
        ItemWithCommentsAndDateDto dto = actual.getFirst();
        assertThat(dto.id()).isEqualTo(item.getId());
        assertThat(dto.lastBooking()).isNotNull();
        assertThat(dto.nextBooking()).isNotNull();
        assertThat(dto.comments()).hasSize(1);
    }

    @Test
    void itemServiceSaveItemShouldPersistItemWithOwner() {
        User owner = userRepository.save(newUser("owner-save-item", "owner-save-item@test.local"));
        Item item = Item.builder().name("bike").description("city").available(true).build();

        Item saved = itemService.saveItem(item, owner.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void itemServiceGetItemShouldReturnItemById() {
        User owner = userRepository.save(newUser("owner-get-item", "owner-get-item@test.local"));
        Item saved = itemRepository.save(newItem(owner, "tent", "2-person", true));

        Item actual = itemService.getItem(saved.getId());

        assertThat(actual.getId()).isEqualTo(saved.getId());
        assertThat(actual.getName()).isEqualTo("tent");
    }

    @Test
    void itemServiceGetItemsByTextShouldReturnOnlyMatchingAvailableItems() {
        User owner = userRepository.save(newUser("owner-search", "owner-search@test.local"));
        itemRepository.save(newItem(owner, "Power Drill", "for wood", true));
        itemRepository.save(newItem(owner, "Saw", "metal cutter", true));
        itemRepository.save(newItem(owner, "Old Drill", "broken", false));

        List<Item> actual = itemService.getItemsByText("drill");

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getName()).isEqualTo("Power Drill");
    }

    @Test
    void itemServiceUpdateItemShouldApplyProvidedFields() {
        User owner = userRepository.save(newUser("owner-update-item", "owner-update-item@test.local"));
        Item item = itemRepository.save(newItem(owner, "before", "before-desc", true));
        UpdateItemDto update = new UpdateItemDto();
        update.setName("after");
        update.setDescription("after-desc");
        update.setAvailable(false);

        Item updated = itemService.updateItem(item.getId(), update, owner.getId());

        assertThat(updated.getName()).isEqualTo("after");
        assertThat(updated.getDescription()).isEqualTo("after-desc");
        assertThat(updated.isAvailable()).isFalse();
    }

    @Test
    void itemServiceSaveCommentShouldPersistCommentForEligibleBooker() {
        User owner = userRepository.save(newUser("owner-comment", "owner-comment@test.local"));
        User booker = userRepository.save(newUser("booker-comment", "booker-comment@test.local"));
        Item item = itemRepository.save(newItem(owner, "camera", "dslr", true));

        bookingRepository.save(newBooking(item, booker,
                Instant.now().minusSeconds(7_200),
                Instant.now().minusSeconds(3_600),
                BookingStatus.APPROVED));

        Comment comment = Comment.builder().text("excellent").created(Instant.now()).build();

        Comment saved = itemService.saveComment(comment, item.getId(), booker.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItem().getId()).isEqualTo(item.getId());
        assertThat(saved.getAuthor().getId()).isEqualTo(booker.getId());
    }

    @Test
    void bookingServiceSaveShouldPersistBookingForAvailableItem() {
        User owner = userRepository.save(newUser("owner-booking-save", "owner-booking-save@test.local"));
        User booker = userRepository.save(newUser("booker-booking-save", "booker-booking-save@test.local"));
        Item item = itemRepository.save(newItem(owner, "board", "snowboard", true));
        Booking booking = newBooking(
                item,
                booker,
                Instant.now().plusSeconds(3_600),
                Instant.now().plusSeconds(7_200),
                BookingStatus.WAITING
        );

        Booking saved = bookingService.save(booking);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void bookingServiceFindByIdShouldReturnBookingForOwnerAccess() {
        User owner = userRepository.save(newUser("owner-find-booking", "owner-find-booking@test.local"));
        User booker = userRepository.save(newUser("booker-find-booking", "booker-find-booking@test.local"));
        Item item = itemRepository.save(newItem(owner, "kayak", "single", true));
        Booking booking = bookingRepository.save(newBooking(
                item,
                booker,
                Instant.now().plusSeconds(3_600),
                Instant.now().plusSeconds(7_200),
                BookingStatus.WAITING
        ));

        Booking actual = bookingService.findById(booking.getId(), owner.getId());

        assertThat(actual.getId()).isEqualTo(booking.getId());
    }

    @Test
    void bookingServiceFindByUserIdAndStateShouldReturnBookerBookings() {
        User owner = userRepository.save(newUser("owner-user-state", "owner-user-state@test.local"));
        User booker = userRepository.save(newUser("booker-user-state", "booker-user-state@test.local"));
        Item item = itemRepository.save(newItem(owner, "rollers", "inline", true));
        Booking booking = bookingRepository.save(newBooking(
                item,
                booker,
                Instant.now().plusSeconds(3_600),
                Instant.now().plusSeconds(7_200),
                BookingStatus.WAITING
        ));

        List<Booking> actual = bookingService.findByUserIdAndState(booker.getId(), BookingState.ALL);

        assertThat(actual)
                .extracting(Booking::getId)
                .contains(booking.getId());
    }

    @Test
    void bookingServiceFindByOwnerIdAndStateShouldReturnOwnerBookings() {
        User owner = userRepository.save(newUser("owner-owner-state", "owner-owner-state@test.local"));
        User booker = userRepository.save(newUser("booker-owner-state", "booker-owner-state@test.local"));
        Item item = itemRepository.save(newItem(owner, "skates", "hockey", true));
        Booking booking = bookingRepository.save(newBooking(
                item,
                booker,
                Instant.now().plusSeconds(3_600),
                Instant.now().plusSeconds(7_200),
                BookingStatus.WAITING
        ));

        List<Booking> actual = bookingService.findByOwnerIdAndState(owner.getId(), BookingState.ALL);

        assertThat(actual)
                .extracting(Booking::getId)
                .contains(booking.getId());
    }

    @Test
    void bookingServiceSetApproveStatusShouldApproveWaitingBooking() {
        User owner = userRepository.save(newUser("owner-approve", "owner-approve@test.local"));
        User booker = userRepository.save(newUser("booker-approve", "booker-approve@test.local"));
        Item item = itemRepository.save(newItem(owner, "projector", "hd", true));
        Booking booking = bookingRepository.save(newBooking(
                item,
                booker,
                Instant.now().plusSeconds(3_600),
                Instant.now().plusSeconds(7_200),
                BookingStatus.WAITING
        ));

        Booking approved = bookingService.setApproveStatus(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void itemRequestServiceSaveShouldCreateRequestForUser() {
        User requestor = userRepository.save(newUser("requestor-save", "requestor-save@test.local"));
        ItemRequestDto dto = new ItemRequestDto("Need a bike");

        ItemRequestResponseDto saved = itemRequestService.save(dto, requestor.getId());

        assertThat(saved.id()).isNotNull();
        assertThat(saved.description()).isEqualTo("Need a bike");
        assertThat(saved.created()).isNotNull();
    }

    @Test
    void itemRequestServiceGetRequestByIdShouldReturnExistingRequest() {
        User requestor = userRepository.save(newUser("requestor-get", "requestor-get@test.local"));
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need a tent")
                .requestor(requestor)
                .created(Instant.now())
                .build());

        ItemRequestWithItemResponseDto actual = itemRequestService.getRequestById(request.getId());

        assertThat(actual.id()).isEqualTo(request.getId());
        assertThat(actual.description()).isEqualTo("Need a tent");
    }

    @Test
    void itemRequestServiceGetOwnRequestsShouldReturnSortedRequests() {
        User requestor = userRepository.save(newUser("requestor-own", "requestor-own@test.local"));
        ItemRequest later = requestRepository.save(ItemRequest.builder()
                .description("later")
                .requestor(requestor)
                .created(Instant.now().plusSeconds(60))
                .build());
        ItemRequest earlier = requestRepository.save(ItemRequest.builder()
                .description("earlier")
                .requestor(requestor)
                .created(Instant.now().minusSeconds(60))
                .build());

        List<ItemRequestWithItemResponseDto> actual = itemRequestService.getOwnRequests(requestor.getId());

        List<Long> idsOrderedByCreated = actual.stream()
                .sorted(Comparator.comparing(ItemRequestWithItemResponseDto::created))
                .map(ItemRequestWithItemResponseDto::id)
                .toList();

        assertThat(idsOrderedByCreated).contains(earlier.getId(), later.getId());
        assertThat(actual.getFirst().id()).isEqualTo(earlier.getId());
    }

    @Test
    void itemRequestServiceGetAllRequestsShouldReturnOnlyRequestsWithoutItems() {
        User requestor = userRepository.save(newUser("requestor-all", "requestor-all@test.local"));
        ItemRequest withoutItems = requestRepository.save(ItemRequest.builder()
                .description("no-items")
                .requestor(requestor)
                .created(Instant.now())
                .build());
        ItemRequest withItems = requestRepository.save(ItemRequest.builder()
                .description("with-items")
                .requestor(requestor)
                .created(Instant.now())
                .build());
        User owner = userRepository.save(newUser("owner-request", "owner-request@test.local"));
        itemRepository.save(Item.builder()
                .name("for-request")
                .description("desc")
                .available(true)
                .owner(owner)
                .request(withItems)
                .build());

        List<ItemRequestResponseDto> actual = itemRequestService.getAllRequests();

        assertThat(actual)
                .extracting(ItemRequestResponseDto::id)
                .contains(withoutItems.getId())
                .doesNotContain(withItems.getId());
    }

    private User newUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private Item newItem(User owner, String name, String description, boolean available) {
        return Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .owner(owner)
                .build();
    }

    private Booking newBooking(Item item, User booker, Instant start, Instant end, BookingStatus status) {
        return Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status(status)
                .build();
    }
}
