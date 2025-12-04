package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;

public class InMemoryTaskManagerTest extends TaskManagerTest {

    @Override
    protected void initManager() {
        manager = new InMemoryTaskManager();
    }
}