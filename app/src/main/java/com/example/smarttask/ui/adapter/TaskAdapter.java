package com.example.smarttask.ui.adapter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttask.R;
import com.example.smarttask.data.model.Task;
import com.example.smarttask.ui.util.AlarmHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> listaTareas = new ArrayList<>();

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Task tarea);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new TaskViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task tarea = listaTareas.get(position);

        holder.textTitulo.setText(tarea.getTitle());
        holder.textDescripcion.setText(tarea.getDescription());
        holder.textFecha.setText(tarea.getFecha());
        holder.textHora.setText(tarea.getHora());

        // 1. Limpia listener anterior para evitar loops indeseados
        holder.checkboxDone.setOnCheckedChangeListener(null);

        // 2. Aplica el estado actual de la tarea
        holder.checkboxDone.setChecked(tarea.isDone());

        // 3. Asigna el nuevo listener
        holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tarea.setDone(isChecked);
            if (onTaskUpdatedListener != null) {
                onTaskUpdatedListener.onTaskUpdated(tarea);
            }
        });

        // 4. Click para editar tarea
        holder.itemView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_formulario_tarea, null);
            builder.setView(dialogView);

            EditText editTitulo = dialogView.findViewById(R.id.editTitulo);
            EditText editDescripcion = dialogView.findViewById(R.id.editDescripcion);
            EditText editFecha = dialogView.findViewById(R.id.editFecha);
            EditText editHora = dialogView.findViewById(R.id.editHora);

            editTitulo.setText(tarea.getTitle());
            editDescripcion.setText(tarea.getDescription());
            editFecha.setText(tarea.getFecha());
            editHora.setText(tarea.getHora());

            // Selector de fecha
            editFecha.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        v.getContext(),
                        (DatePicker view1, int year, int month, int dayOfMonth) -> {
                            String fecha = year + "-" + (month + 1) + "-" + dayOfMonth;
                            editFecha.setText(fecha);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            });

            // Selector de hora
            editHora.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        v.getContext(),
                        (TimePicker view1, int hourOfDay, int minute) -> {
                            String hora = String.format("%02d:%02d", hourOfDay, minute);
                            editHora.setText(hora);
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                );
                timePickerDialog.show();
            });

            builder.setTitle("Editar tarea");
            builder.setPositiveButton("Guardar", (dialog, which) -> {
                tarea.setTitle(editTitulo.getText().toString());
                tarea.setDescription(editDescripcion.getText().toString());
                tarea.setFecha(editFecha.getText().toString());
                tarea.setHora(editHora.getText().toString());

                notifyItemChanged(position);

                if (onTaskUpdatedListener != null) {
                    // Cancelar alarma previa
                    AlarmHelper.cancelarAlarmas(view.getContext(), tarea);

                    // Reprogramar nueva alarma
                    AlarmHelper.programarAlarma(view.getContext(), tarea);

                    // Actualizar en base de datos
                    onTaskUpdatedListener.onTaskUpdated(tarea);
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
    }


    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    public void setTasks(List<Task> tareas) {
        this.listaTareas = tareas;
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textDescripcion, textFecha, textHora;
        CheckBox checkboxDone;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            textFecha = itemView.findViewById(R.id.textFecha);
            textHora = itemView.findViewById(R.id.textHora);
            checkboxDone= itemView.findViewById(R.id.checkboxDone);
        }
    }

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(Task task);
    }

    private OnTaskUpdatedListener onTaskUpdatedListener;

    public void setOnTaskUpdatedListener(OnTaskUpdatedListener listener) {
        this.onTaskUpdatedListener = listener;
    }
    public Task getTaskAt(int position) {
        return listaTareas.get(position); // taskList es tu lista interna
    }

}
