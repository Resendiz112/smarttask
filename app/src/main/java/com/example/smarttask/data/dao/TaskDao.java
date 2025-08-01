package com.example.smarttask.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smarttask.data.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY fecha, hora")
    LiveData<List<Task>> getTareasActivas();

    @Query("SELECT * FROM tasks WHERE isDone = 1")
    LiveData<List<Task>> getTareasCompletadas();

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasksSync();

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);
}
