package com.example.smarttask;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttask.data.model.Task;
import com.example.smarttask.ui.adapter.TaskAdapter;
import com.example.smarttask.ui.util.AlarmHelper;
import com.example.smarttask.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAgregar;
    private TaskViewModel taskViewModel;
    private TaskAdapter adapterActivas;
    private TaskAdapter adapterCompletadas;
    private RecyclerView recyclerViewTareas;
    private TextView textEmptyMessage;

    private boolean mostrandoActivas = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        setContentView(R.layout.activity_main);

        fabAgregar = findViewById(R.id.fabAgregar);
        recyclerViewTareas = findViewById(R.id.recyclerViewTareas);
        TextView btnActivas = findViewById(R.id.btnActivas);
        TextView btnCompletadas = findViewById(R.id.btnCompletadas);
        textEmptyMessage = findViewById(R.id.textEmptyMessage);

        recyclerViewTareas.setLayoutManager(new LinearLayoutManager(this));

        adapterActivas = new TaskAdapter();
        adapterCompletadas = new TaskAdapter();

        recyclerViewTareas.setAdapter(adapterActivas);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        taskViewModel.getTareasActivas().observe(this, tareas -> {
            adapterActivas.setTasks(tareas);
            if (mostrandoActivas) {
                recyclerViewTareas.setAdapter(adapterActivas);
                textEmptyMessage.setVisibility(tareas.isEmpty() ? View.VISIBLE : View.GONE);
                textEmptyMessage.setText("No hay tareas activas. ¡Crea una!");
            }
            for (Task tarea : tareas) {
                if (!tarea.isDone()) {
                    AlarmHelper.programarAlarma(this, tarea);
                }
            }
        });

        taskViewModel.getTareasCompletadas().observe(this, tareas -> {
            adapterCompletadas.setTasks(tareas);
            if (!mostrandoActivas) {
                recyclerViewTareas.setAdapter(adapterCompletadas);
                textEmptyMessage.setVisibility(tareas.isEmpty() ? View.VISIBLE : View.GONE);
                textEmptyMessage.setText("No hay tareas completadas. ¡Termina una!");
            }
        });

        btnActivas.setOnClickListener(v -> {
            mostrandoActivas = true;
            recyclerViewTareas.setAdapter(adapterActivas);
            taskViewModel.getTareasActivas().getValue(); // Para refrescar mensaje vacío
        });

        btnCompletadas.setOnClickListener(v -> {
            mostrandoActivas = false;
            recyclerViewTareas.setAdapter(adapterCompletadas);
            taskViewModel.getTareasCompletadas().getValue(); // Para refrescar mensaje vacío
        });

        fabAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        adapterActivas.setOnTaskUpdatedListener(task -> {
            taskViewModel.update(task);
            if (task.isDone()) {
                AlarmHelper.cancelarAlarmas(this, task);
            } else {
                AlarmHelper.cancelarAlarmas(this, task);
                AlarmHelper.programarAlarma(this, task);
            }
        });

        adapterCompletadas.setOnTaskUpdatedListener(task -> {
            new AlertDialog.Builder(this)
                    .setTitle("¿Marcar como no completada?")
                    .setMessage("¿Deseas mover esta tarea nuevamente a activas?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        task.setDone(false);
                        taskViewModel.update(task);
                        AlarmHelper.programarAlarma(this, task);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        task.setDone(true);
                        taskViewModel.update(task);
                    })
                    .setCancelable(false)
                    .show();
        });

        adapterCompletadas.setOnTaskClickListener(this::mostrarDialogoEditar);

        // Swipe para eliminar tareas activas
        ItemTouchHelper itemTouchHelperActivas = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task tarea = adapterActivas.getTaskAt(position);
                if (tarea != null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("¿Eliminar tarea?")
                            .setMessage("¿Estás seguro de que quieres eliminar esta tarea?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                AlarmHelper.cancelarAlarmas(MainActivity.this, tarea);
                                taskViewModel.delete(tarea);
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> adapterActivas.notifyItemChanged(position))
                            .setCancelable(false)
                            .show();
                } else {
                    adapterActivas.notifyItemChanged(position);
                }
            }
        });
        itemTouchHelperActivas.attachToRecyclerView(recyclerViewTareas);

        // Swipe para eliminar tareas completadas
        ItemTouchHelper itemTouchHelperCompletadas = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task tarea = adapterCompletadas.getTaskAt(position);
                if (tarea != null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("¿Eliminar tarea completada?")
                            .setMessage("¿Estás seguro de que quieres eliminar esta tarea?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                AlarmHelper.cancelarAlarmas(MainActivity.this, tarea);
                                taskViewModel.delete(tarea);
                                adapterCompletadas.notifyItemRemoved(position); // <-- AGREGADO PARA ACTUALIZAR UI AL ELIMINAR
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> adapterCompletadas.notifyItemChanged(position))
                            .setCancelable(false)
                            .show();
                } else {
                    adapterCompletadas.notifyItemChanged(position);
                }
            }
        });
        itemTouchHelperCompletadas.attachToRecyclerView(recyclerViewTareas);
    }

    private void mostrarDialogoAgregar() {
        View vistaFormulario = LayoutInflater.from(this).inflate(R.layout.dialog_formulario_tarea, null);

        EditText editTitulo = vistaFormulario.findViewById(R.id.editTitulo);
        EditText editDescripcion = vistaFormulario.findViewById(R.id.editDescripcion);
        EditText editFecha = vistaFormulario.findViewById(R.id.editFecha);
        EditText editHora = vistaFormulario.findViewById(R.id.editHora);

        editFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                editFecha.setText(year + "-" + (month + 1) + "-" + day);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        editHora.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                editHora.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        new AlertDialog.Builder(this)
                .setView(vistaFormulario)
                .setTitle("Nueva Tarea")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String titulo = editTitulo.getText().toString().trim();
                    String descripcion = editDescripcion.getText().toString().trim();
                    String fecha = editFecha.getText().toString().trim();
                    String hora = editHora.getText().toString().trim();

                    if (!titulo.isEmpty()) {
                        Task tarea = new Task(titulo, descripcion, false, fecha, hora);
                        taskViewModel.insert(tarea);
                        AlarmHelper.programarAlarma(this, tarea);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditar(Task tarea) {
        View vistaFormulario = LayoutInflater.from(this).inflate(R.layout.dialog_formulario_tarea, null);

        EditText editTitulo = vistaFormulario.findViewById(R.id.editTitulo);
        EditText editDescripcion = vistaFormulario.findViewById(R.id.editDescripcion);
        EditText editFecha = vistaFormulario.findViewById(R.id.editFecha);
        EditText editHora = vistaFormulario.findViewById(R.id.editHora);

        editTitulo.setText(tarea.getTitle());
        editDescripcion.setText(tarea.getDescription());
        editFecha.setText(tarea.getFecha());
        editHora.setText(tarea.getHora());

        editFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                editFecha.setText(year + "-" + (month + 1) + "-" + day);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        editHora.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                editHora.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Editar tarea")
                .setView(vistaFormulario)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    tarea.setTitle(editTitulo.getText().toString());
                    tarea.setDescription(editDescripcion.getText().toString());
                    tarea.setFecha(editFecha.getText().toString());
                    tarea.setHora(editHora.getText().toString());

                    taskViewModel.update(tarea);
                    AlarmHelper.cancelarAlarmas(this, tarea);
                    if (!tarea.isDone()) {
                        AlarmHelper.programarAlarma(this, tarea);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
