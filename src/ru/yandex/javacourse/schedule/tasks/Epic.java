package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
	protected ArrayList<Integer> subtaskIds = new ArrayList<>();
	private LocalDateTime endTime = null;

	public Epic(int id, String name, String description) {
		super(id, name, description, NEW, Duration.ZERO, null);
	}

	public Epic(String name, String description) {
		super(name, description, NEW, Duration.ZERO, null);
	}

	public Epic(Epic other) {
		super(other);
		this.subtaskIds.addAll(other.subtaskIds);
		this.endTime = other.endTime;
	}

	public void addSubtaskId(int id) {
		if (!subtaskIds.contains(id) && this.id != id) {
			subtaskIds.add(id);
		}
	}

	public List<Integer> getSubtaskIds() {
		return subtaskIds;
	}

	public void cleanSubtaskIds() {
		subtaskIds.clear();
		this.duration = Duration.ZERO;
		this.startTime = null;
		this.endTime = null;
	}

	public void removeSubtask(int id) {
		subtaskIds.remove(Integer.valueOf(id));
	}

	@Override
	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public TaskType getType() {
		return TaskType.EPIC;
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", duration=" + duration +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", subtaskIds=" + subtaskIds +
				'}';
	}
}
