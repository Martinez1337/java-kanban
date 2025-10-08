package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoryEditing() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.addTask(task1);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        historyManager.addTask(task2);
        assertEquals(2, historyManager.getHistory().size(), "historic task should be added");

        historyManager.remove(1);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be removed");
    }

    @Test
    public void testHistoricVersions() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        // При дабавлении task повторно в историю просмотра, старая запись о просмотре стирается, согласно условию
        // Поэтому getHistory().size() должен быть равен 1
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
    }

    @Test
    public void testHistoricVersionsByPointer() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.NEW, historyManager.getHistory().get(0).getStatus(), "historic task should not be changed");
    }
}
