package com.example.smarttask.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarttask.R;
import com.example.smarttask.data.model.Task;

import java.util.ArrayList;
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
        holder.checkboxDone.setChecked(tarea.isDone());

        holder.checkboxDone.setOnCheckedChangeListener(null); // ← MUY IMPORTANTE

        holder.checkboxDone.setChecked(tarea.isDone());
        holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tarea.setDone(isChecked);
            if (onTaskUpdatedListener != null) {
                onTaskUpdatedListener.onTaskUpdated(tarea);
            }
        });

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

            builder.setTitle("Editar tarea");
            builder.setPositiveButton("Guardar", (dialog, which) -> {
                tarea.setTitle(editTitulo.getText().toString());
                tarea.setDescription(editDescripcion.getText().toString());
                tarea.setFecha(editFecha.getText().toString());
                tarea.setHora(editHora.getText().toString());

                notifyItemChanged(position);

                if (onTaskUpdatedListener != null) {
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
