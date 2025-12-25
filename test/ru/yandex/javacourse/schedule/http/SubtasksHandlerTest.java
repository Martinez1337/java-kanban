package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
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

public class SubtasksHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public SubtasksHandlerTest() throws IOException {
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
    public void createSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Sub", "Testing sub", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epicId);
        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Test Sub", subtasksFromManager.get(0).getName());
    }

    @Test
    public void createSubtask_timeConflict() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        LocalDateTime start = LocalDateTime.now();
        Subtask existingSubtask = new Subtask("Existing", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), start, epicId);
        manager.createSubtask(existingSubtask);

        Subtask conflictingSubtask = new Subtask("Conflict", "Desc", TaskStatus.NEW, Duration.ofMinutes(30), start.plusMinutes(10), epicId);
        String json = gson.toJson(conflictingSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается с уже существующими", response.body());
    }

    @Test
    public void updateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Sub", "Testing sub", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epicId);
        int id = manager.createSubtask(subtask);

        subtask.setName("Updated Sub");
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask updatedSubtask = manager.getSubtask(id);
        assertEquals("Updated Sub", updatedSubtask.getName());
        assertEquals(TaskStatus.IN_PROGRESS, updatedSubtask.getStatus());
    }

    @Test
    public void updateSubtask_timeConflict() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        LocalDateTime start1 = LocalDateTime.now();
        Subtask sub1 = new Subtask("Sub1", "Desc1", TaskStatus.NEW, Duration.ofMinutes(30), start1, epicId);
        int id1 = manager.createSubtask(sub1);

        LocalDateTime start2 = start1.plusHours(1);
        Subtask sub2 = new Subtask("Sub2", "Desc2", TaskStatus.NEW, Duration.ofMinutes(30), start2, epicId);
        int id2 = manager.createSubtask(sub2);

        sub2.setStartTime(start1.plusMinutes(10)); // Cause overlap with sub1
        String json = gson.toJson(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается с уже существующими", response.body());
    }

    @Test
    public void getAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Sub1 desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epicId);
        Subtask sub2 = new Subtask("Sub2", "Sub2 desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1), epicId);
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type type = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> fromJson = gson.fromJson(response.body(), type);
        assertEquals(2, fromJson.size());
    }

    @Test
    public void getSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Sub", "Testing sub", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epicId);
        int id = manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask fromJson = gson.fromJson(response.body(), Subtask.class);
        assertEquals("Test Sub", fromJson.getName());
    }

    @Test
    public void subtaskById_notFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Объект не найден", response.body());
    }

    @Test
    public void deleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Sub", "Testing sub", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epicId);
        int id = manager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertThrows(NotFoundException.class, () -> manager.getSubtask(id));
    }

    @Test
    public void deleteAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Sub1 desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epicId);
        Subtask sub2 = new Subtask("Sub2", "Sub2 desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1), epicId);
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getSubtasks().isEmpty());
    }
}