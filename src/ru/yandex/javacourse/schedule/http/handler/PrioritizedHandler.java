package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                String dataJson = gson.toJson(taskManager.getPrioritizedTasks());
                sendText(exchange, dataJson, 200);
            } else {
                sendError(exchange, "Method Not Allowed", 405);
            }
        } catch (Exception e) {
            sendError(exchange, "Internal Server Error", 500);
        } finally {
            exchange.close();
        }
    }
}