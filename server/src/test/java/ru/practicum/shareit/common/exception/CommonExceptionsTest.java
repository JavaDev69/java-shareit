package ru.practicum.shareit.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class CommonExceptionsTest {
    @Test
    void shareItExceptionShouldKeepMessageAndHaveInternalServerErrorStatus() {
        ShareItException exception = new ShareItException("internal error");

        assertThat(exception).hasMessage("internal error");
        assertResponseStatus(ShareItException.class, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void forbiddenExceptionShouldSupportDirectAndFormattedMessages() {
        ForbiddenException directException = new ForbiddenException("forbidden");
        ForbiddenException formattedException = new ForbiddenException("%s: %s", "access", "denied");

        assertThat(directException).hasMessage("forbidden");
        assertThat(formattedException).hasMessage("access: denied");
        assertThat(formattedException).isInstanceOf(ShareItException.class);
        assertResponseStatus(ForbiddenException.class, HttpStatus.FORBIDDEN);
    }

    @Test
    void notFoundExceptionShouldSupportDirectAndFormattedMessages() {
        NotFoundException directException = new NotFoundException("not found");
        NotFoundException formattedException = new NotFoundException("id=%d not found", 42);

        assertThat(directException).hasMessage("not found");
        assertThat(formattedException).hasMessage("id=42 not found");
        assertThat(formattedException).isInstanceOf(ShareItException.class);
        assertResponseStatus(NotFoundException.class, HttpStatus.NOT_FOUND);
    }

    @Test
    void saveCommentExceptionShouldSupportDirectAndFormattedMessages() {
        SaveCommentException directException = new SaveCommentException("cannot save");
        SaveCommentException formattedException = new SaveCommentException("%s %s", "cannot", "save");

        assertThat(directException).hasMessage("cannot save");
        assertThat(formattedException).hasMessage("cannot save");
        assertThat(formattedException).isInstanceOf(ShareItException.class);
        assertResponseStatus(SaveCommentException.class, HttpStatus.BAD_REQUEST);
    }

    @Test
    void emailAlreadyExistExceptionShouldContainEmailAndConflictStatus() {
        EmailAlreadyExistException exception = new EmailAlreadyExistException("mail@test.local");

        assertThat(exception).hasMessage("Данный email уже занят: mail@test.local");
        assertThat(exception).isInstanceOf(ShareItException.class);
        assertResponseStatus(EmailAlreadyExistException.class, HttpStatus.CONFLICT);
    }

    @Test
    void itemNotAvailableExceptionShouldHaveDefaultMessageAndBadRequestStatus() {
        ItemNotAvailableException exception = new ItemNotAvailableException();

        assertThat(exception).hasMessage("Вещь недоступна для бронирования.");
        assertThat(exception).isInstanceOf(ShareItException.class);
        assertResponseStatus(ItemNotAvailableException.class, HttpStatus.BAD_REQUEST);
    }

    @Test
    void userNotSpecifiedExceptionShouldHaveDefaultMessageAndBadRequestStatus() {
        UserNotSpecifiedException exception = new UserNotSpecifiedException();

        assertThat(exception).hasMessage("Пользователь не задан.");
        assertThat(exception).isInstanceOf(ShareItException.class);
        assertResponseStatus(UserNotSpecifiedException.class, HttpStatus.BAD_REQUEST);
    }

    private void assertResponseStatus(Class<?> exceptionClass, HttpStatus expectedStatus) {
        ResponseStatus responseStatus = exceptionClass.getAnnotation(ResponseStatus.class);
        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(expectedStatus);
    }
}
