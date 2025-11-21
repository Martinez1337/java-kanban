package ru.yandex.javacourse.schedule.manager;

import java.util.*;

import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.util.entities.Node;

public class InMemoryHistoryManager implements HistoryManager {
	private final Map<Integer, Node<Task>> taskMap = new HashMap<>();

	private Node<Task> head;
	private Node<Task> tail;

	private void linkLast(Task task) {
		if (task == null) {
			return;
		}

		remove(task.getId()); // Удаляем существующую задачу с таким же id, если такая существует

		Task taskCopy = new Task(task); // Создаем копию задачи чтобы избежать изменений извне
		final Node<Task> oldTail = tail;
		final Node<Task> newNode = new Node<>(taskCopy, oldTail, null);
		tail = newNode;

		if (oldTail == null) {
			head = newNode;
		} else {
			oldTail.setNext(newNode);
		}

		taskMap.put(task.getId(), newNode); // Добавляем в map для быстрого доступа
	}

	private void removeNode(Node<Task> node) {
		if (node == null) {
			return;
		}

		final Node<Task> next = node.getNext();
		final Node<Task> prev = node.getPrev();

		// Удаляем узел из списка
		if (prev == null) {
			head = next;
		} else {
			prev.setNext(next);
			node.setPrev(null);
		}

		if (next == null) {
			tail = prev;
		} else {
			next.setPrev(prev);
			node.setNext(null);
		}

        Task task = node.getData();
		taskMap.remove(task.getId()); // Удаляем указатель из taskMap
	}

	/**
	 * Собирает все задачи из списка в ArrayList в порядке от новой к старой
	 */
	private List<Task> getTasks() {
		List<Task> tasks = new ArrayList<>();
		Node<Task> current = head;

		while (current != null) {
			tasks.add(current.getData());
			current = current.getNext();
		}

		return tasks;
	}

	@Override
	public List<Task> getHistory() {
		return getTasks();
	}

	@Override
	public void addTask(Task task) {
		linkLast(task);
	}

	@Override
	public void remove(int id) {
		Node<Task> node = taskMap.get(id);
		if (node != null) {
			removeNode(node);
		}
	}
}
