package ru.yandex.javacourse.schedule.manager;

public class TimeConflictException extends RuntimeException {
    public TimeConflictException(String message) {
        super(message);
    }
}