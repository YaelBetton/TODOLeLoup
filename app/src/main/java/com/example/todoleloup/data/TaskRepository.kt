package com.example.todoleloup.data

class TaskRepository {
    private val tasks: MutableList<Task> = mutableListOf()

    fun addTask(task: Task) {
        tasks.add(task)
    }

    fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            tasks[index] = task
        }
    }

    fun deleteTask(task: Task) {
        tasks.removeAll { it.id == task.id }
    }

    fun getTasksByStatus(status: TaskStatus): List<Task> {
        return tasks.filter { it.status == status }
    }

    fun getTasksByPriority(priority: Priority): List<Task> {
        return tasks.filter { it.priority == priority }
    }

    fun purgeDoneTasks() {
        tasks.removeAll { it.status == TaskStatus.DONE }
    }

    fun getAllTasks(): List<Task> {
        return tasks.toList()
    }
}

