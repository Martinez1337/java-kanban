# Task Manager Project

## Overview
This project is a Java-based task management system that allows creating, updating, and managing tasks, epics, and subtasks. It includes an in-memory and file-backed storage, an HTTP server for API interactions, and comprehensive unit tests.

The system supports task prioritization, history tracking, and time-based conflict detection to prevent overlapping tasks.

### Key features
- **Task types:** Simple Tasks, Epics (grouping subtasks), and Subtasks  
- **Task statuses:** `NEW`, `IN_PROGRESS`, `DONE`  
- **Time management:** Tasks can have start times and durations; overlaps are detected and prevented  
- **Persistence:** In-memory or file-backed (CSV format)  
- **API:** RESTful HTTP endpoints for CRUD operations on tasks, subtasks, epics, plus history and prioritized views  
- **Testing:** Extensive JUnit tests for managers, tasks, and HTTP handlers, including error scenarios  

The project is built with **Java** and uses libraries like **Gson** for JSON handling and **JUnit** for testing.

---

## Installation

### Prerequisites
- Java JDK 11+ 

### Setup
Clone the repository:
```bash
git clone https://github.com/yourusername/task-manager-project.git
```
Navigate to the project directory:
```bash
cd task-manager-project
```
Compile the Java files:
```bash
javac -d bin -cp lib/* src/ru/yandex/javacourse/schedule/**/*.java
```
Run the main application:
```bash
java -cp bin ru.yandex.javacourse.schedule.Main
```
### HTTP Server

To start the HTTP server:
```bash
java -cp bin ru.yandex.javacourse.schedule.http.HttpTaskServer
```
The server runs on port 8080.

---
## Usage
### Core Functionality

- **Task Management**: Create tasks with names, descriptions, statuses, durations, and start times
- **Epics and Subtasks**: Epics aggregate subtasks; their status and time are calculated automatically
- **Prioritization**: Tasks are sorted by start time
- **History**: Tracks viewed tasks
- **File Backup**: Saves and loads tasks to/from CSV files

### Example (from Main.java)
```java
TaskManager manager = Managers.getDefault();

Task task = new Task("Task #1", "Description", TaskStatus.NEW, null, null);
manager.addNewTask(task);

Epic epic = new Epic("Epic #1", "Epic description");
int epicId = manager.addNewEpic(epic);

Subtask subtask = new Subtask("Subtask #1", "Sub description", TaskStatus.NEW, null, null, epicId);
manager.addNewSubtask(subtask);
```

## HTTP API
The HTTP server exposes RESTful endpoints on http://localhost:8080.
### Endpoints

#### Tasks:
| Method | Endpoint      | Description           | Responses                               |
| ------ | ------------- | --------------------- | --------------------------------------- |
| GET    | `/tasks`      | Get all tasks         | 200 OK                                  |
| GET    | `/tasks/{id}` | Get task by ID        | 200 OK, 404 Not Found                   |
| POST   | `/tasks`      | Create or update task | 201 Created, 200 OK, 406 Not Acceptable |
| DELETE | `/tasks/{id}` | Delete task by ID     | 200 OK                                  |
| DELETE | `/tasks`      | Delete all tasks      | 200 OK                                  |


#### Subtasks:
| Method | Endpoint         | Description              | Responses                               |
| ------ | ---------------- | ------------------------ | --------------------------------------- |
| GET    | `/subtasks`      | Get all subtasks         | 200 OK                                  |
| GET    | `/subtasks/{id}` | Get subtask by ID        | 200 OK, 404 Not Found                   |
| POST   | `/subtasks`      | Create or update subtask | 201 Created, 200 OK, 406 Not Acceptable |
| DELETE | `/subtasks/{id}` | Delete subtask by ID     | 200 OK                                  |
| DELETE | `/subtasks`      | Delete all subtasks      | 200 OK                                  |


#### Epics:
| Method | Endpoint               | Description           | Responses             |
| ------ | ---------------------- | --------------------- | --------------------- |
| GET    | `/epics`               | Get all epics         | 200 OK                |
| GET    | `/epics/{id}`          | Get epic by ID        | 200 OK, 404 Not Found |
| GET    | `/epics/{id}/subtasks` | Get epic subtasks     | 200 OK, 404 Not Found |
| POST   | `/epics`               | Create or update epic | 201 Created, 200 OK   |
| DELETE | `/epics/{id}`          | Delete epic by ID     | 200 OK                |
| DELETE | `/epics`               | Delete all epics      | 200 OK                |

#### History:
| Method | Endpoint   | Description      | Responses |
| ------ | ---------- | ---------------- | --------- |
| GET    | `/history` | Get task history | 200 OK    |


#### Prioritized:
| Method | Endpoint       | Description           | Responses |
| ------ | -------------- | --------------------- | --------- |
| GET    | `/prioritized` | Get prioritized tasks | 200 OK    |

#### Example request (using curl):
```bash
textcurl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"name":"New Task","description":"Desc","status":"NEW"}'
```
