package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TasksHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public TasksHandlerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void createTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void createTask_timeConflict() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.now();
        Task existingTask = new Task("Existing", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), start);
        manager.createTask(existingTask);

        Task conflictingTask = new Task("Conflict", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), start.plusMinutes(10));
        String taskJson = gson.toJson(conflictingTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается с уже существующими", response.body());
    }

    @Test
    public void updateTask() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        int id = manager.createTask(task);

        task.setName("Updated Test");
        task.setStatus(TaskStatus.IN_PROGRESS);
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task updatedTask = manager.getTask(id);
        assertEquals("Updated Test", updatedTask.getName());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    public void updateTask_timeConflict() throws IOException, InterruptedException {
        LocalDateTime start1 = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), start1);
        int id1 = manager.createTask(task1);

        LocalDateTime start2 = start1.plusHours(1);
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(30), start2);
        int id2 = manager.createTask(task2);

        task2.setStartTime(start1.plusMinutes(10));
        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается с уже существующими", response.body());
    }

    @Test
    public void getAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Test1", "Testing1", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task("Test2", "Testing2", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1));
        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type taskType = new TypeToken<List<Task>>() {}.getType();
        List<Task> tasksFromJson = gson.fromJson(response.body(), taskType);
        assertEquals(2, tasksFromJson.size());
    }

    @Test
    public void getTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        int id = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task taskFromJson = gson.fromJson(response.body(), Task.class);
        assertEquals("Test", taskFromJson.getName());
    }

    @Test
    public void getTaskById_notFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Объект не найден", response.body());
    }

    @Test
    public void deleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        int id = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertThrows(NotFoundException.class, () -> manager.getTask(id));
    }

    @Test
    public void deleteAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Test1", "Testing1", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        Task task2 = new Task("Test2", "Testing2", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1));
        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getTasks().isEmpty());
    }
}