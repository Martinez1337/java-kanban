package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.manager.TimeConflictException;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class CrudTaskHandler<T extends Task> extends BaseHttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public CrudTaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    protected abstract Class<T> getType();
    protected abstract Object getAll();
    protected abstract T getById(int id);
    protected abstract void create(T item);
    protected abstract void update(T item);
    protected abstract void deleteAll();
    protected abstract void deleteById(int id);

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange);
                default -> sendError(exchange, "Method Not Allowed", 405);
            }
        } catch (JsonParseException e) {
            sendError(exchange, "Invalid JSON", 400);
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid ID", 400);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (TimeConflictException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendError(exchange, "Internal Server Error", 500);
        } finally {
            exchange.close();
        }
    }

    protected void handleGet(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length == 2) {
            sendText(exchange, gson.toJson(getAll()), 200);
        } else if (pathParts.length == 3) {
            int id = Integer.parseInt(pathParts[2]);
            T item = getById(id);
            sendText(exchange, gson.toJson(item), 200);
        } else {
            handleCustomGet(exchange, pathParts);
        }
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length == 2) {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            T item = gson.fromJson(body, getType());
            Integer id = item.getId();
            if (id == null || id == 0) {
                create(item);
                sendText(exchange, gson.toJson(item), 201);
            } else {
                update(item);
                sendText(exchange, gson.toJson(item), 200);
            }
        } else {
            sendError(exchange, "Bad Request", 400);
        }
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length == 2) {
            deleteAll();
            exchange.sendResponseHeaders(200, -1);
        } else if (pathParts.length == 3) {
            int id = Integer.parseInt(pathParts[2]);
            deleteById(id);
            exchange.sendResponseHeaders(200, -1);
        } else {
            sendError(exchange, "Bad Request", 400);
        }
    }

    protected void handleCustomGet(HttpExchange exchange, String[] pathParts) throws IOException {
        sendError(exchange, "Bad Request", 400);
    }
}
