package ru.yandex.javacourse.schedule.manager;

import java.nio.file.Path;

public class Managers {
	public static TaskManager getDefault() {
		return new InMemoryTaskManager();
	}

	public static TaskManager getFileBacked(Path savePath) {
		return new FileBackedTaskManager(savePath);
	}

	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}
}
