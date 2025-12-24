package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

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
