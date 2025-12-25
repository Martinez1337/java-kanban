package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.manager.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TimeConflictException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

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
        sendError(exchange, "Operation is not supported", 404);
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        sendError(exchange, "Operation is not supported", 404);
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        sendError(exchange, "Operation is not supported", 404);
    }

    protected void sendText(HttpExchange h, String text, int rCode) throws IOException {
        byte[] resp = text.getBytes(DEFAULT_CHARSET);

        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(rCode, resp.length);

        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    protected void sendError(HttpExchange h, String errorMessage, int rCode) throws IOException {
        sendText(h, errorMessage, rCode);
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendError(h, "Объект не найден", 404);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        sendError(h, "Задача пересекается с уже существующими", 406);
    }
}
