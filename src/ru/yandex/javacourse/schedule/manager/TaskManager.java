package ru.yandex.javacourse.schedule.manager;

import java.util.List;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

public interface TaskManager {
	List<Task> getTasks();

	List<Subtask> getSubtasks();

	List<Epic> getEpics();

	List<Subtask> getEpicSubtasks(int epicId);

	List<Task> getPrioritizedTasks();

	Task getTask(int id);

	Subtask getSubtask(int id);

	Epic getEpic(int id);

	Integer createTask(Task task);

	Integer createEpic(Epic epic);

	Integer createSubtask(Subtask subtask);

	void updateTask(Task task);

	void updateEpic(Epic epic);

	void updateSubtask(Subtask subtask);

	void deleteTask(int id);

	void deleteEpic(int id);

	void deleteSubtask(int id);

	void deleteTasks();

	void deleteSubtasks();

	void deleteEpics();

	List<Task> getHistory();
}
