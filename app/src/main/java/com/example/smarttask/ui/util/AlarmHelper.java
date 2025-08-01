package com.example.smarttask.ui.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.smarttask.data.model.Task;
import com.example.smarttask.ui.receiver.NotificacionReceiver;

public class AlarmHelper {

    public static void programarAlarma(Context context, Task tarea) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Programar la notificación 24 horas antes
        long tiempoRecordatorio = calcularMillis(tarea.getFecha(), tarea.getHora()) - 24 * 60 * 60 * 1000L;
        if (tiempoRecordatorio > System.currentTimeMillis()) {
            PendingIntent pendingIntent = crearPendingIntent(context, tarea, 1, "Recordatorio: " + tarea.getTitle(), "Queda 1 día para finalizar la tarea");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoRecordatorio, pendingIntent);
        }

        // Programar la notificación de tarea vencida (justo en la hora límite)
        long tiempoVencido = calcularMillis(tarea.getFecha(), tarea.getHora());
        if (tiempoVencido > System.currentTimeMillis()) {
            PendingIntent pendingIntent = crearPendingIntent(context, tarea, 2, "Tarea vencida: " + tarea.getTitle(), "La tarea ha vencido");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoVencido, pendingIntent);
        }
    }

    public static void cancelarAlarmas(Context context, Task tarea) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent p1 = crearPendingIntent(context, tarea, 1, "", "");
        PendingIntent p2 = crearPendingIntent(context, tarea, 2, "", "");

        alarmManager.cancel(p1);
        alarmManager.cancel(p2);
    }

    private static PendingIntent crearPendingIntent(Context context, Task tarea, int id, String titulo, String mensaje) {
        Intent intent = new Intent(context, NotificacionReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("notificationId", tarea.getId() * 10 + id);

        return PendingIntent.getBroadcast(context, tarea.getId() * 10 + id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static long calcularMillis(String fecha, String hora) {
        // Convierte la fecha y hora (String) a milisegundos epoch
        // Fecha en formato "YYYY-MM-DD", hora en "HH:mm"
        try {
            String[] parts = fecha.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Mes base 0
            int day = Integer.parseInt(parts[2]);

            String[] partsHora = hora.split(":");
            int hour = Integer.parseInt(partsHora[0]);
            int minute = Integer.parseInt(partsHora[1]);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month, day, hour, minute, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
