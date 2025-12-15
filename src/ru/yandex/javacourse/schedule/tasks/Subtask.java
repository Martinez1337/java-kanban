package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    protected int epicId;

    public Subtask(
			int id, String name, String description, TaskStatus status,
			Duration duration, LocalDateTime startTime, int epicId
	) {
        super(id, name, description, status, duration, startTime);
        if (id == epicId) {
            throw new IllegalArgumentException("Subtask id must not be equal to epic id");
        }
        this.epicId = epicId;
    }

    public Subtask(
			String name, String description, TaskStatus status,
			Duration duration, LocalDateTime startTime, int epicId
	) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(Subtask other) {
        super(other);
        this.epicId = other.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", epicId=" + epicId +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", duration='" + duration + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }
}
