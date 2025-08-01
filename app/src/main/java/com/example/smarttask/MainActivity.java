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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttask.data.model.Task;
import com.example.smarttask.ui.adapter.TaskAdapter;
import com.example.smarttask.ui.util.AlarmHelper; // Importa tu helper de alarmas
import com.example.smarttask.viewmodel.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAgregar;
    private TaskViewModel taskViewModel;
    private TaskAdapter adapterActivas;
    private TaskAdapter adapterCompletadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Si no tiene permiso para alarmas exactas, abrir configuración para activarlo
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
        setContentView(R.layout.activity_main);

        fabAgregar = findViewById(R.id.fabAgregar);
        RecyclerView recyclerViewActivas = findViewById(R.id.recyclerViewTareasActivas);
        RecyclerView recyclerViewCompletadas = findViewById(R.id.recyclerViewTareasCompletadas);
        TextView textToggleCompletadas = findViewById(R.id.textToggleCompletadas);

        recyclerViewActivas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCompletadas.setLayoutManager(new LinearLayoutManager(this));

        adapterActivas = new TaskAdapter();
        adapterCompletadas = new TaskAdapter();

        recyclerViewActivas.setAdapter(adapterActivas);
        recyclerViewCompletadas.setAdapter(adapterCompletadas);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        taskViewModel.getTareasActivas().observe(this, tareas -> {
            adapterActivas.setTasks(tareas);
            // Cada vez que carga tareas activas, programa las alarmas para estas
            if (tareas != null) {
                for (Task tarea : tareas) {
                    if (!tarea.isDone()) {
                        AlarmHelper.programarAlarma(this, tarea);
                    }
                }
            }
        });

        taskViewModel.getTareasCompletadas().observe(this, tareas -> {
            adapterCompletadas.setTasks(tareas);

            if ((tareas == null || tareas.isEmpty()) && recyclerViewCompletadas.getVisibility() == View.VISIBLE) {
                recyclerViewCompletadas.setVisibility(View.GONE);
                textToggleCompletadas.setText("Mostrar tareas completadas");
                Toast.makeText(MainActivity.this, "No hay tareas completadas", Toast.LENGTH_SHORT).show();
            }
        });

        textToggleCompletadas.setOnClickListener(v -> {
            if (recyclerViewCompletadas.getVisibility() == View.GONE) {
                recyclerViewCompletadas.setVisibility(View.VISIBLE);
                textToggleCompletadas.setText("Ocultar tareas completadas");
            } else {
                recyclerViewCompletadas.setVisibility(View.GONE);
                textToggleCompletadas.setText("Mostrar tareas completadas");
            }
        });

        fabAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        adapterActivas.setOnTaskUpdatedListener(task -> {
            // Actualiza estado a completada y actualiza BD
            task.setDone(true);
            taskViewModel.update(task);

            // Cancelar alarmas porque está completada
            AlarmHelper.cancelarAlarmas(this, task);
        });

        adapterCompletadas.setOnTaskUpdatedListener(task -> {
            // Confirmación al desmarcar una tarea completada
            new AlertDialog.Builder(this)
                    .setTitle("¿Marcar como no completada?")
                    .setMessage("¿Deseas mover esta tarea nuevamente a activas?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        task.setDone(false);
                        taskViewModel.update(task);

                        // Programa alarma porque ahora está activa
                        AlarmHelper.programarAlarma(this, task);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        task.setDone(true);
                        taskViewModel.update(task);
                    })
                    .setCancelable(false)
                    .show();
        });

        // Swipe para eliminar en tareas activas
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task tareaAEliminar = adapterActivas.getTaskAt(position);

                if (tareaAEliminar != null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("¿Eliminar tarea?")
                            .setMessage("¿Estás seguro de que quieres eliminar esta tarea?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                // Cancelar alarmas antes de borrar
                                AlarmHelper.cancelarAlarmas(MainActivity.this, tareaAEliminar);
                                taskViewModel.delete(tareaAEliminar);
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> adapterActivas.notifyItemChanged(position))
                            .setCancelable(false)
                            .show();
                } else {
                    adapterActivas.notifyItemChanged(position);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerViewActivas);
    }

    private void mostrarDialogoAgregar()    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View vistaFormulario = inflater.inflate(R.layout.dialog_formulario_tarea, null);

        EditText editTitulo = vistaFormulario.findViewById(R.id.editTitulo);
        EditText editDescripcion = vistaFormulario.findViewById(R.id.editDescripcion);
        EditText editFecha = vistaFormulario.findViewById(R.id.editFecha);
        EditText editHora = vistaFormulario.findViewById(R.id.editHora);

        editFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        String fecha = year + "-" + (month + 1) + "-" + dayOfMonth;
                        editFecha.setText(fecha);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        editHora.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (TimePicker view, int hourOfDay, int minute) -> {
                        String hora = String.format("%02d:%02d", hourOfDay, minute);
                        editHora.setText(hora);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
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

                        // Programar alarma para la nueva tarea
                        AlarmHelper.programarAlarma(this, tarea);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
