package ru.yandex.javacourse.schedule.util.entities;

public class Node<T> {
    public T data;
    public Node<T> next;
    public Node<T> prev;

    public Node(T data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }

    public Node(T data, Node<T> prev, Node<T> next) {
        this.data = data;
        this.next = next;
        this.prev = prev;
    }
}
