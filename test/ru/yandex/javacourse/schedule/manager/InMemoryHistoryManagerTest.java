package ru.yandex.javacourse.schedule.manager;

public class InMemoryHistoryManagerTest extends HistoryManagerTest {

    @Override
    protected void initHistoryManager() {
        historyManager = new InMemoryHistoryManager();
    }
}
