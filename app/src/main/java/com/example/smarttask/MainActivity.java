package com.example.smarttask;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAgregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Este es el layout correcto

        fabAgregar = findViewById(R.id.fabAgregar);

        fabAgregar.setOnClickListener(v -> mostrarDialogoAgregar());
    }

    private void mostrarDialogoAgregar() {
        // Aquí inflamos el layout del formulario
        LayoutInflater inflater = LayoutInflater.from(this);
        View vistaFormulario = inflater.inflate(R.layout.dialog_formulario_tarea, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(vistaFormulario)
                .setTitle("Nueva Tarea")
                .setPositiveButton("Guardar", (dialog, which) -> {
                    // Aquí luego obtendrás los datos y guardarás
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
