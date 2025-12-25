package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

public class TasksHandler extends CrudTaskHandler<Task> {

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected Class<Task> getType() {
        return Task.class;
    }

    @Override
    protected Object getAll() {
        return taskManager.getTasks();
    }

    @Override
    protected Task getById(int id) {
        return taskManager.getTask(id);
    }

    @Override
    protected void create(Task task) {
        taskManager.createTask(task);
    }

    @Override
    protected void update(Task task) {
        taskManager.updateTask(task);
    }

    @Override
    protected void deleteAll() {
        taskManager.deleteTasks();
    }

    @Override
    protected void deleteById(int id) {
        taskManager.deleteTask(id);
    }
}