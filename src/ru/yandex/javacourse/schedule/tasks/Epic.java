package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
	private final ArrayList<Integer> subtaskIds;
	private LocalDateTime endTime;

	//  Необходим для корректной работы Gson
	// https://stackoverflow.com/questions/18645050/is-default-no-args-constructor-mandatory-for-gson
	private Epic() {
		super("", "", NEW, Duration.ZERO, null);
		this.subtaskIds = new ArrayList<>();
	}

	public Epic(int id, String name, String description) {
		super(id, name, description, NEW, Duration.ZERO, null);
		subtaskIds = new ArrayList<>();
	}

	public Epic(String name, String description) {
		super(name, description, NEW, Duration.ZERO, null);
		this.subtaskIds = new ArrayList<>();
	}

	public Epic(Epic other) {
		super(other);
		this.subtaskIds = new ArrayList<>();
		if (other.subtaskIds != null) {
			this.subtaskIds.addAll(other.subtaskIds);
		}
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
