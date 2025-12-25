package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

public class SubtasksHandler extends CrudTaskHandler<Subtask> {

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected Class<Subtask> getType() {
        return Subtask.class;
    }

    @Override
    protected Object getAll() {
        return taskManager.getSubtasks();
    }

    @Override
    protected Subtask getById(int id) {
        return taskManager.getSubtask(id);
    }

    @Override
    protected void create(Subtask subtask) {
        taskManager.createSubtask(subtask);
    }

    @Override
    protected void update(Subtask subtask) {
        taskManager.updateSubtask(subtask);
    }

    @Override
    protected void deleteAll() {
        taskManager.deleteSubtasks();
    }

    @Override
    protected void deleteById(int id) {
        taskManager.deleteSubtask(id);
    }
}