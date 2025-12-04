package ru.yandex.javacourse.schedule.util;

import ru.yandex.javacourse.schedule.tasks.*;

public class TaskCSVConverter {
    public static String fromTaskToString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(',')
                .append(task.getType()).append(',')
                .append(task.getName()).append(',')
                .append(task.getStatus()).append(',')
                .append(task.getDescription()).append(',');

        return sb.toString();
    }

    public static String fromTaskToString(Subtask subtask) {
        String baseString = fromTaskToString((Task) subtask);
        return baseString + subtask.getEpicId();
    }

    public static Task fromStringToTask(String str) {
        String[] params = str.split(",");
        return switch (TaskType.valueOf(params[1].toUpperCase())) {
            case EPIC -> new Epic(
                    Integer.parseInt(params[0]),
                    params[2],
                    params[4]);
            case SUBTASK -> new Subtask(
                    Integer.parseInt(params[0]),
                    params[2],
                    params[4],
                    TaskStatus.valueOf(params[3]),
                    Integer.parseInt(params[5]));
            case TASK -> new Task(
                    Integer.parseInt(params[0]),
                    params[2],
                    params[4],
                    TaskStatus.valueOf(params[3]));
        };
    }
}
