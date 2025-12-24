package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String dataJson = gson.toJson(taskManager.getHistory());
            if ("GET".equals(exchange.getRequestMethod())) {
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