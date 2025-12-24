package ru.yandex.javacourse.schedule.manager;

public class InMemoryTaskManagerTest extends TaskManagerTest {

    @Override
    protected void initManager() {
        manager = new InMemoryTaskManager();
    }
}