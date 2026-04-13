package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {
    @Test
    void equalsShouldReturnTrueForSameInstance() {
        Item item = Item.builder().id(1L).name("drill").build();

        assertThat(item).isEqualTo(item);
    }

    @Test
    void equalsShouldReturnFalseForNull() {
        Item item = Item.builder().id(1L).name("drill").build();

        assertThat(item.equals(null)).isFalse();
    }

    @Test
    void equalsShouldReturnFalseForDifferentClass() {
        Item item = Item.builder().id(1L).name("drill").build();

        assertThat(item.equals("not-an-item")).isFalse();
    }

    @Test
    void equalsShouldReturnFalseWhenIdsAreNull() {
        Item first = Item.builder().name("first").build();
        Item second = Item.builder().name("second").build();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void equalsShouldReturnTrueWhenIdsAreEqual() {
        Item first = Item.builder().id(10L).name("first").build();
        Item second = Item.builder().id(10L).name("second").build();

        assertThat(first).isEqualTo(second);
        assertThat(second).isEqualTo(first);
    }

    @Test
    void equalsShouldReturnFalseWhenIdsAreDifferent() {
        Item first = Item.builder().id(10L).name("first").build();
        Item second = Item.builder().id(11L).name("second").build();

        assertThat(first).isNotEqualTo(second);
        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void hashCodeShouldBeEqualForObjectsWithSameId() {
        Item first = Item.builder().id(10L).name("first").build();
        Item second = Item.builder().id(10L).name("second").build();

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
