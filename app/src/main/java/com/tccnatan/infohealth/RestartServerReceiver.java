package com.tccnatan.infohealth;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tcc.infohealth.R;

public class RestartServerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("com.tccnatan.infohealth.START_FOREGROUND_SERVICE")) {
            // Verifique se o usuário está online
            if (isUserOnline()) {
                // Verifique se o serviço não está em execução
                if (!isServiceRunning(context, MyForegroundService.class)) {
                    // chamada para ação!!
                    System.out.println("CHAMADA PARA AÇÃO..");
                    createNotificationChannel(context);
                    String message = "Entre no App para continuar a realizar o track da sua saúde!";
                    Intent pagina_notification = new Intent(context.getApplicationContext(),MainActivity.class);
                    pagina_notification.putExtra("nome","Notificação");
                    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(),1,pagina_notification,PendingIntent.FLAG_IMMUTABLE);
                    NotificationCompat.Builder builder = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        builder = new NotificationCompat.Builder(
                                context.getApplicationContext(), "My notification"
                        )
                                .setSmallIcon(R.drawable.logo_icon)
                                .setColor(Color.GREEN)
                                .setContentTitle("Nova Notificação")
                                .setContentText(message)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_SOUND)
                                //.setSound(alarmSound)
                                .setContentIntent(pendingIntent);
                    }

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1,builder.build());

                    // Inicie o serviço de foreground
                    /*System.out.println("Reiniciando o serviço pelo alarme...");
                    Intent serviceIntent = new Intent(context, MyForegroundService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }*/
                }
            }
        }
    }

    private boolean isUserOnline() {
        // Implemente sua lógica para verificar se o usuário está online
        // Pode ser usando Firebase Realtime Database, Firestore, ou qualquer outra forma de verificação de conexão
        // Retorne true se o usuário estiver online, caso contrário, retorne false
        // Exemplo:

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void createNotificationChannel(Context context) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My notification","My notification",NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(alarmSound, null);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }
}
