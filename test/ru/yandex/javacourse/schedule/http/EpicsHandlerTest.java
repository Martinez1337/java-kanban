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

public class EpicsHandlerTest {
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public EpicsHandlerTest() throws IOException {
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
    public void createEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        String json = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(1, epicsFromManager.size());
        assertEquals("Test Epic", epicsFromManager.get(0).getName());
    }

    @Test
    public void updateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        int id = manager.createEpic(epic);

        epic.setName("Updated Epic");
        String json = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic updatedEpic = manager.getEpic(id);
        assertEquals("Updated Epic", updatedEpic.getName());
    }

    @Test
    public void getAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic1", "Epic1 desc");
        Epic epic2 = new Epic("Epic2", "Epic2 desc");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type type = new TypeToken<List<Epic>>() {}.getType();
        List<Epic> fromJson = gson.fromJson(response.body(), type);
        assertEquals(2, fromJson.size());
    }

    @Test
    public void getEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        int id = manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic fromJson = gson.fromJson(response.body(), Epic.class);
        assertEquals("Test Epic", fromJson.getName());
    }

    @Test
    public void getEpicById_notFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Объект не найден", response.body());
    }

    @Test
    public void getEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic desc");
        int epicId = manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Sub1 desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now(), epicId);
        Subtask sub2 = new Subtask("Sub2", "Sub2 desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1), epicId);
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type type = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> fromJson = gson.fromJson(response.body(), type);
        assertEquals(2, fromJson.size());
    }

    @Test
    public void getEpicSubtasks_epicNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Объект не найден", response.body());
    }

    @Test
    public void deleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        int id = manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertThrows(NotFoundException.class, () -> manager.getEpic(id));
    }

    @Test
    public void deleteAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic1", "Epic1 desc");
        Epic epic2 = new Epic("Epic2", "Epic2 desc");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getEpics().isEmpty());
    }
}