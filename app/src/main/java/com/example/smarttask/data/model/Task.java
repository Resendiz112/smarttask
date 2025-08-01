package com.example.smarttask.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public boolean isDone;
    public String fecha;  // nuevo campo
    public String hora;   // nuevo campo

    public Task(String title, String description, boolean isDone, String fecha, String hora) {
        this.title = title;
        this.description = description;
        this.isDone = isDone;
        this.fecha = fecha;
        this.hora = hora;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
}
