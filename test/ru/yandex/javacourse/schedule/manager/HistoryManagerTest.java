package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class HistoryManagerTest {
    protected HistoryManager historyManager;

    protected abstract void initHistoryManager();

    @BeforeEach
    public void setUp() {
        initHistoryManager();
    }

    @Test
    public void addTask_addNewTaskInHistory() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        historyManager.addTask(task1);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, null, null);
        historyManager.addTask(task2);
        assertEquals(2, historyManager.getHistory().size(), "historic task should be added");
    }

    @Test
    public void addTask_deleteSameTaskFromPreviousHistory() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        // При дабавлении task повторно в историю просмотра, старая запись о просмотре стирается, согласно условию
        // Поэтому getHistory().size() должен быть равен 1
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
    }

    @Test
    public void addTask_createNewInstanceInHistory() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.NEW, historyManager.getHistory().get(0).getStatus(), "historic task should not be changed");
    }

    @Test
    public void addTask_removeOldAndAddNew_whenSameIdAddedAgain() {
        Task task1 = new Task(1, "Task 1", "Desc", TaskStatus.NEW, null, null);
        historyManager.addTask(task1);

        // Та же задача с изменениями
        Task task1Updated = new Task(1, "Task 1 Updated", "New Desc", TaskStatus.DONE, null, null);
        historyManager.addTask(task1Updated);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals("Task 1 Updated", history.get(0).getName());
        assertEquals(TaskStatus.DONE, history.get(0).getStatus());
    }

    @Test
    public void addTask_handleNullTask() {
        // Должно обрабатывать null без падения
        assertDoesNotThrow(() -> historyManager.addTask(null));
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void getHistory_returnEmptyList_noTasksAdded() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(0, history.size());
    }

    @Test
    public void getHistory_returnTasksInOrderOfAddition() {
        Task task1 = new Task(1, "Task 1", "Desc", TaskStatus.NEW, null, null);
        Task task2 = new Task(2, "Task 2", "Desc", TaskStatus.NEW, null, null);
        Task task3 = new Task(3, "Task 3", "Desc", TaskStatus.NEW, null, null);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
        assertEquals(3, history.get(2).getId());
    }

    @Test
    public void remove_deleteTaskFromHistory() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, null, null);
        historyManager.addTask(task1);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, null, null);
        historyManager.addTask(task2);

        historyManager.remove(1);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be removed");
    }

    @Test
    public void remove_deleteFromBeginning() {
        Task task1 = new Task(1, "Task 1", "Desc", TaskStatus.NEW, null, null);
        Task task2 = new Task(2, "Task 2", "Desc", TaskStatus.NEW, null, null);
        Task task3 = new Task(3, "Task 3", "Desc", TaskStatus.NEW, null, null);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    public void remove_deleteFromMiddle() {
        Task task1 = new Task(1, "Task 1", "Desc", TaskStatus.NEW, null, null);
        Task task2 = new Task(2, "Task 2", "Desc", TaskStatus.NEW, null, null);
        Task task3 = new Task(3, "Task 3", "Desc", TaskStatus.NEW, null, null);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(3, history.get(1).getId());
    }

    @Test
    public void remove_deleteFromEnd() {
        Task task1 = new Task(1, "Task 1", "Desc", TaskStatus.NEW, null, null);
        Task task2 = new Task(2, "Task 2", "Desc", TaskStatus.NEW, null, null);
        Task task3 = new Task(3, "Task 3", "Desc", TaskStatus.NEW, null, null);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
    }

    @Test
    public void remove_doNothing_whenTaskNotInHistory() {
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW, null, null);
        historyManager.addTask(task);

        // Удаляем несуществующий ID
        historyManager.remove(999);

        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    public void remove_fromSingleElementList() {
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW, null, null);
        historyManager.addTask(task);

        historyManager.remove(1);

        assertEquals(0, historyManager.getHistory().size());
    }
}
