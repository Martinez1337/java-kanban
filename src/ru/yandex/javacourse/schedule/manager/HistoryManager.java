package ru.yandex.javacourse.schedule.manager;

import java.util.List;

import ru.yandex.javacourse.schedule.tasks.Task;

public interface HistoryManager {
	List<Task> getHistory();

	void addTask(Task task);

	void remove(int id);
}
