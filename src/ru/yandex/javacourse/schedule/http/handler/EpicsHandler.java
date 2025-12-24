package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;

import java.io.IOException;

public class EpicsHandler extends CrudTaskHandler<Epic> {
    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected Class<Epic> getType() {
        return Epic.class;
    }

    @Override
    protected Object getAll() {
        return taskManager.getEpics();
    }

    @Override
    protected Epic getById(int id) {
        return taskManager.getEpic(id);
    }

    @Override
    protected void create(Epic epic) {
        taskManager.createEpic(epic);
    }

    @Override
    protected void update(Epic epic) {
        taskManager.updateEpic(epic);
    }

    @Override
    protected void deleteAll() {
        taskManager.deleteEpics();
    }

    @Override
    protected void deleteById(int id) {
        taskManager.deleteEpic(id);
    }

    @Override
    protected void handleCustomGet(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
            int id = Integer.parseInt(pathParts[2]);
            String dataJson = gson.toJson(taskManager.getEpicSubtasks(id));
            sendText(exchange, dataJson, 200);
        } else {
            super.handleCustomGet(exchange, pathParts);
        }
    }
}