package ru.yandex.javacourse.schedule.http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(final JsonWriter out, final Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toMinutes());
        }
    }

    @Override
    public Duration read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return Duration.ofMinutes(in.nextLong());
    }
}