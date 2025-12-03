package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path saveFile;

    public FileBackedTaskManager(Path savePath) {
        this.saveFile = savePath;
    }

    public void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(saveFile)) {
            bw.write("id,type,name,status,description,epic");
            bw.newLine();
            for (Task task : getTasks()) {
                bw.write(fromTaskToString(task));
                bw.newLine();
            }
            for (Epic epic : getEpics()) {
                bw.write(fromTaskToString(epic));
                bw.newLine();
            }
            for (Subtask subtask : getSubtasks()) {
                bw.write(fromTaskToString(subtask));
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
            br.readLine(); // Пропускаем заголовок
            String line;
            int maxId = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Task task = fromStringToTask(line);
                maxId = Math.max(maxId, task.getId());
                switch (task.getType()) {
                    case TASK -> manager.tasks.put(task.getId(), task);
                    case EPIC -> manager.epics.put(task.getId(), (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic == null) {
                            throw new IllegalStateException("Epic not found for subtask ID " + subtask.getId());
                        }
                        manager.subtasks.put(subtask.getId(), subtask);
                        epic.addSubtaskId(subtask.getId());
                    }
                }
            }
            // Обновляем статусы эпиков
            for (Epic epic : manager.getEpics()) {
                manager.updateEpicStatus(epic.getId());
            }
            // Обновляем generatorId
            manager.generatorId = maxId;
        } catch (IOException e) {
            throw new ManagerSaveException("Failed to load data from file: " + file, e);
        }
        return manager;
    }

    private static String fromTaskToString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(',')
                .append(task.getType()).append(',')
                .append(task.getName()).append(',')
                .append(task.getStatus()).append(',')
                .append(task.getDescription()).append(',');

        if (task instanceof Subtask) {
            sb.append(((Subtask) task).getEpicId());
        }

        return sb.toString();
    }

    private static Task fromStringToTask(String str) {
        String[] params = str.split(",");
        return switch (TaskType.valueOf(params[1].toUpperCase())) {
            case EPIC -> new Epic(
                    Integer.parseInt(params[0]),
                    params[2],
                    params[4]);
            case SUBTASK -> new Subtask(
                    Integer.parseInt(params[0]),
                    params[2],
                    params[4],
                    TaskStatus.valueOf(params[3]),
                    Integer.parseInt(params[5]));
            case TASK -> new Task(
                    Integer.parseInt(params[0]),
                    params[2],
                    params[4],
                    TaskStatus.valueOf(params[3]));
        };
    }

    @Override
    public Integer addNewTask(Task task) {
        Integer taskId = super.addNewTask(task);
        save();
        return taskId;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer subtaskId = super.addNewSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public Integer addNewEpic(Epic epic) {
        Integer epicId = super.addNewEpic(epic);
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
