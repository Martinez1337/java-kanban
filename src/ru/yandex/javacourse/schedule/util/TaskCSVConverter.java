package ru.yandex.javacourse.schedule.util;

import ru.yandex.javacourse.schedule.tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class TaskCSVConverter {
    public static String fromTaskToString(Task task) {
        StringBuilder sb = new StringBuilder();

        long durationMinutes = Optional.ofNullable(task.getDuration())
                .map(Duration::toMinutes)
                .orElse(0L);

        String startTimeStr = Optional.ofNullable(task.getStartTime())
                .map(LocalDateTime::toString)
                .orElse("null");

        sb.append(task.getId()).append(',')
                .append(task.getType()).append(',')
                .append(task.getName()).append(',')
                .append(task.getStatus()).append(',')
                .append(task.getDescription()).append(',')
                .append(durationMinutes).append(',')
                .append(startTimeStr).append(',');

        return sb.toString();
    }

    public static String fromTaskToString(Subtask subtask) {
        String baseString = fromTaskToString((Task) subtask);
        return baseString + subtask.getEpicId();
    }

    public static Task fromStringToTask(String str) {
        String[] params = str.split(",", -1);

        int id = Integer.parseInt(params[0]);
        TaskType type = TaskType.valueOf(params[1].toUpperCase());
        String name = params[2];
        TaskStatus status = TaskStatus.valueOf(params[3]);
        String description = params[4];
        Duration duration = Duration.ofMinutes(Long.parseLong(params[5]));
        LocalDateTime startTime = null;

        if (!params[6].isEmpty() && !params[6].equals("null")) {
            startTime = LocalDateTime.parse(params[6]);
        }

        return switch (type) {
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> new Subtask(id, name, description, status, duration, startTime, Integer.parseInt(params[7]));
            case TASK -> new Task(id, name, description, status, duration, startTime);
        };
    }
}
