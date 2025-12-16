package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {
	protected final Map<Integer, Task> tasks = new HashMap<>();
	protected final Map<Integer, Epic> epics = new HashMap<>();
	protected final Map<Integer, Subtask> subtasks = new HashMap<>();

	protected int generatorId = 0;

	private final HistoryManager historyManager = Managers.getDefaultHistory();

	private final TreeSet<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
		if (!task1.getStartTime().equals(task2.getStartTime())) {
			return task1.getStartTime().compareTo(task2.getStartTime());
		}
		return Integer.compare(task1.getId(), task2.getId());
	});

	private boolean isTasksOverlapping(Task newTask) {
		if (newTask.getStartTime() == null) {
			return false;
		}

		return prioritizedTasks.stream()
				.anyMatch(existingTask -> isTasksOverlapping(newTask, existingTask));
	}

	private boolean isTasksOverlapping(Task task1, Task task2) {
		if (task1 == task2 || Objects.equals(task1.getId(), task2.getId())
			|| (task1.getStartTime() == null || task2.getStartTime() == null)) {
			return false;
		}

		LocalDateTime start1 = task1.getStartTime();
		LocalDateTime end1 = task1.getEndTime();
		LocalDateTime start2 = task2.getStartTime();
		LocalDateTime end2 = task2.getEndTime();

		return start1.isBefore(end2) && end1.isAfter(start2);
	}

	@Override
	public ArrayList<Task> getTasks() {
		return new ArrayList<>(this.tasks.values());
	}

	@Override
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public ArrayList<Task> getPrioritizedTasks() {
		return new ArrayList<>(prioritizedTasks);
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		return epic.getSubtaskIds().stream()
				.map(subtasks::get)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		if (task == null) {
			return null;
		}
		historyManager.addTask(task);
		return new Task(task);
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		if (subtask == null) {
			return null;
		}
		historyManager.addTask(subtask);
		return new Subtask(subtask);
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		if (epic == null) {
			return null;
		}
		historyManager.addTask(epic);
		return new Epic(epic);
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}

	private int getNextId() {
		return ++generatorId;
	}

	@Override
	public Integer addNewTask(Task task) {
		if (isTasksOverlapping(task)) {
			throw new IllegalArgumentException("Task overlaps with an existing one");
		}
		if (task.getId() == null) {
			task.setId(getNextId());
		} else if (tasks.containsKey(task.getId())) {
			throw new IllegalArgumentException("Task with id " + task.getId() + " already exists");
		}

		tasks.put(task.getId(), task);
		addToPrioritized(task);
		return task.getId();
	}

	@Override
	public Integer addNewEpic(Epic epic) {
		if (epic.getId() == null) {
			epic.setId(getNextId());
		} else if (epics.containsKey(epic.getId())) {
			throw new IllegalArgumentException("Epic with id " + epic.getId() + " already exists");
		}

		epics.put(epic.getId(), epic);
		return epic.getId();
	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		Epic epic = epics.get(subtask.getEpicId());
		if (epic == null) {
			throw new IllegalArgumentException("Epic with id " + subtask.getEpicId() + " does not exist");
		}
		if (isTasksOverlapping(subtask)) {
			throw new IllegalArgumentException("Subtask overlaps with an existing one");
		}
		if (subtask.getId() == null) {
			subtask.setId(getNextId());
		} else if (subtasks.containsKey(subtask.getId())) {
			throw new IllegalArgumentException("Subtask with id " + subtask.getId() + " already exists");
		}

		subtasks.put(subtask.getId(), subtask);
		addToPrioritized(subtask);

		epic.addSubtaskId(subtask.getId());
		updateEpicParams(epic);

		return subtask.getId();
	}

	private void addToPrioritized(Task task) {
		if (task.getStartTime() != null) {
			prioritizedTasks.add(task);
		}
	}

	@Override
	public void updateTask(Task task) {
		final Task savedTask = tasks.get(task.getId());
		if (savedTask == null) {
			return;
		}
		if (isTasksOverlapping(task)) {
			throw new IllegalArgumentException("Task overlaps with an existing one.");
		}
		deleteFromPrioritized(savedTask);
		tasks.put(task.getId(), task);
		addToPrioritized(task);
	}

	@Override
	public void updateEpic(Epic epic) {
		final Epic savedEpic = epics.get(epic.getId());
		if (savedEpic != null) {
			savedEpic.setName(epic.getName());
			savedEpic.setDescription(epic.getDescription());
		}
	}

	protected void updateEpicParams(Epic epic) {
		updateEpicStatus(epic);
		updateEpicTimes(epic);
	}

	private void updateEpicStatus(Epic epic) {
		if (epic == null) return;

		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (status == null) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);
	}

	private void updateEpicTimes(Epic epic) {
		if (epic == null) return;

		List<Subtask> validSubtasks = epic.getSubtaskIds().stream()
				.map(subtasks::get)
				.filter(Objects::nonNull)
				.toList();

		if (validSubtasks.isEmpty()) {
			epic.setDuration(Duration.ZERO);
			epic.setStartTime(null);
			epic.setEndTime(null);
			return;
		}

		Duration totalDuration = validSubtasks.stream()
				.map(Subtask::getDuration)
				.filter(d -> d != null && !d.isNegative() && !d.isZero())
				.reduce(Duration.ZERO, Duration::plus);

		LocalDateTime earliestStart = validSubtasks.stream()
				.map(Subtask::getStartTime)
				.filter(Objects::nonNull)
				.min(LocalDateTime::compareTo)
				.orElse(null);

		LocalDateTime latestEnd = validSubtasks.stream()
				.map(Subtask::getEndTime)
				.filter(Objects::nonNull)
				.max(LocalDateTime::compareTo)
				.orElse(null);

		epic.setDuration(totalDuration);
		epic.setStartTime(earliestStart);
		epic.setEndTime(latestEnd);
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		final Subtask savedSubtask = subtasks.get(subtask.getId());
		if (savedSubtask == null) {
			throw new IllegalArgumentException("There is no subtask with id = " + subtask.getId());
		}
		if (savedSubtask.getEpicId() != subtask.getEpicId()) {
			throw new IllegalArgumentException("You cannot change epicId of an existent subtask");
		}
		final Epic epic = epics.get(subtask.getEpicId());
		if (epic == null) {
			throw new IllegalArgumentException("There is no epic with epicId = " + subtask.getEpicId());
		}
		if (isTasksOverlapping(subtask)) {
			throw new IllegalArgumentException("Subtask overlaps with an existing one.");
		}
		deleteFromPrioritized(savedSubtask);
		subtasks.put(subtask.getId(), subtask);
		addToPrioritized(subtask);

		updateEpicParams(epic);
	}

	@Override
	public void deleteTask(int id) {
		Task task = tasks.remove(id);
		if (task == null) {
			return;
		}
		historyManager.remove(id);
		deleteFromPrioritized(task);
	}

	private void deleteFromPrioritized(Task task) {
		if (task != null) {
			prioritizedTasks.remove(task);
		}
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
		if (epic == null) {
			return;
		}
		historyManager.remove(id);
		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.remove(subtaskId);
			historyManager.remove(subtaskId);
		}
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask subtask = subtasks.remove(id);
		if (subtask == null) {
			return;
		}
		historyManager.remove(id);
		deleteFromPrioritized(subtask);

		Epic epic = epics.get(subtask.getEpicId());
		epic.removeSubtask(id);
		updateEpicParams(epic);
	}

	@Override
	public void deleteTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
			deleteFromPrioritized(task);
        }
        tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
			deleteFromPrioritized(subtask);
        }

        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            updateEpicParams(epic);
        }
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
		epics.values().stream()
				.map(Task::getId)
				.forEach(historyManager::remove);
		epics.clear();

		subtasks.values().stream()
				.map(Task::getId)
				.forEach(historyManager::remove);
		subtasks.clear();
	}
}
