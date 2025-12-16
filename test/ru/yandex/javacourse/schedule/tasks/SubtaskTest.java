package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void equals_compareById() {
        Subtask s0 = new Subtask(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null, 2);
        Subtask s1 = new Subtask(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS, null, null, 3);
        assertEquals(s0, s1, "task entities should be compared by id");
    }

    @Test
    public void constructor_throwException_idEqualsEpicId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Subtask(1, "Subtask 1", "Testing subtask 1", TaskStatus.NEW, null, null, 1)
        );
        assertEquals("Subtask id must not be equal to epic id", exception.getMessage());
    }
}
