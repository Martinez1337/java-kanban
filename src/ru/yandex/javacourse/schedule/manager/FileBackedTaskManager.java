package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.*;
import ru.yandex.javacourse.schedule.util.TaskCSVConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path saveFile;

    public FileBackedTaskManager(Path savePath) {
        this.saveFile = savePath;
    }

    private void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(saveFile)) {
            bw.write("id,type,name,status,description,duration,startTime,epic");
            bw.newLine();
            for (Task task : getTasks()) {
                bw.write(TaskCSVConverter.fromTaskToString(task));
                bw.newLine();
            }
            for (Epic epic : getEpics()) {
                bw.write(TaskCSVConverter.fromTaskToString(epic));
                bw.newLine();
            }
            for (Subtask subtask : getSubtasks()) {
                bw.write(TaskCSVConverter.fromTaskToString(subtask));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to save data to file: " + saveFile, e);
        }
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!Files.exists(file)) {
            return manager;
        }
        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            int maxId = 0;
            List<Subtask> allSubtasks = new ArrayList<>();
            br.readLine(); // Пропускаем заголовок
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Task task = TaskCSVConverter.fromStringToTask(line);
                maxId = Math.max(maxId, task.getId());
                switch (task.getType()) {
                    case TASK -> manager.tasks.put(task.getId(), task);
                    case EPIC -> manager.epics.put(task.getId(), (Epic) task);
                    case SUBTASK -> allSubtasks.add((Subtask) task);
                }
            }
            // Обрабатываем подзадачи
            for (Subtask subtask : allSubtasks) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic == null) {
                    throw new IllegalStateException("Epic not found for subtask ID " + subtask.getId());
                }
                manager.subtasks.put(subtask.getId(), subtask);
                epic.addSubtaskId(subtask.getId());
            }
            // Обновляем статусы и время эпиков
            for (Epic epic : manager.getEpics()) {
                manager.updateEpicParams(epic);
            }
            // Обновляем generatorId
            manager.generatorId = maxId;
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load data from file: " + file, e);
        }
        return manager;
    }


    @Override
    public Integer createTask(Task task) {
        Integer taskId = super.createTask(task);
        save();
        return taskId;
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        Integer subtaskId = super.createSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public Integer createEpic(Epic epic) {
        Integer epicId = super.createEpic(epic);
        save();
        return epicId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }
}
