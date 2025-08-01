package com.example.smarttask.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.smarttask.data.database.TaskDatabase;
import com.example.smarttask.data.model.Task;
import com.example.smarttask.ui.util.AlarmHelper;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Executors.newSingleThreadExecutor().execute(() -> {
                List<Task> tareas = TaskDatabase.getInstance(context).taskDao().getAllTasksSync();
                for (Task tarea : tareas) {
                    if (!tarea.isDone()) {
                        AlarmHelper.programarAlarma(context, tarea);
                    }
                }
            });
        }
    }
}
