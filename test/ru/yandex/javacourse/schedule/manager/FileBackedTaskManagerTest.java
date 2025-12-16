package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest {
    private Path tempFile;

    @Override
    protected void initManager() {
        try {
            tempFile = Files.createTempFile("taskmanager", ".csv");
            manager = new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file for test", e);
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void loadFromFile_emptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    @Test
    void loadFromFile_multipleTasks() throws IOException {
        // Создаём файл вручную
        String csvContent = """
                id,type,name,status,description,duration,startTime,epic
                1,TASK,Task1,NEW,Desc1,10,null,
                2,EPIC,Epic1,NEW,DescEpic,10,null,
                3,SUBTASK,Sub1,DONE,DescSub,10,null,2
                """;
        Files.writeString(tempFile, csvContent);

        // Загружаем
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем данные
        assertEquals(1, loaded.getTasks().size());
        assertEquals("Task1", loaded.getTasks().get(0).getName());
        assertEquals(1, loaded.getEpics().size());
        assertEquals("Epic1", loaded.getEpics().get(0).getName());
        assertEquals(TaskStatus.DONE, loaded.getEpics().get(0).getStatus());
        assertEquals(1, loaded.getSubtasks().size());
        assertEquals("Sub1", loaded.getSubtasks().get(0).getName());
        assertEquals(2, loaded.getSubtasks().get(0).getEpicId());

        // Проверяем generatorId (max 3 + 1 = 4)
        Task newTask = new Task("NewTask", "Desc", TaskStatus.NEW, null, null);
        int newId = loaded.addNewTask(newTask);
        assertEquals(4, newId);
    }

    @Test
    void loadFromFile_throwException_missingEpic() {
        // Файл с сабтаском без эпика
        String csvContent = "id,type,name,status,description,duration,startTime,epic\n" +
                "3,SUBTASK,Sub1,NEW,DescSub,10,null,999\n";
        try {
            Files.writeString(tempFile, csvContent);
            assertThrows(IllegalStateException.class, () -> FileBackedTaskManager.loadFromFile(tempFile));
        } catch (IOException e) {
            fail("Failed to write file");
        }
    }

    @Test
    void save_multipleTasks() {
        // Добавляем задачи
        Task task = new Task("Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(10), null);
        manager.addNewTask(task);
        Epic epic = new Epic("Epic1", "DescEpic");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Sub1", "DescSub", TaskStatus.NEW, Duration.ofMinutes(10), null, epicId);
        subtask.setStatus(TaskStatus.DONE);
        manager.addNewSubtask(subtask);

        // Проверяем содержимое файла сохранения
        try {
            String content = Files.readString(tempFile);
            assertTrue(content.contains("1,TASK,Task1,NEW,Desc1,10,null,\n"));
            assertTrue(content.contains("2,EPIC,Epic1,DONE,DescEpic,10,null,\n"));
            assertTrue(content.contains("3,SUBTASK,Sub1,DONE,DescSub,10,null,2\n"));
        } catch (IOException e) {
            fail("Failed to read file");
        }
    }
}