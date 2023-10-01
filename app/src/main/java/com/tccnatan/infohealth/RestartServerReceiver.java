package com.tccnatan.infohealth;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RestartServerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("com.tccnatan.infohealth.START_FOREGROUND_SERVICE")) {
            // Verifique se o usuário está online
            if (isUserOnline()) {
                // Verifique se o serviço não está em execução
                if (!isServiceRunning(context, MyForegroundService.class)) {
                    // Inicie o serviço de foreground
                    System.out.println("Reiniciando o serviço pelo alarme...");
                    Intent serviceIntent = new Intent(context, MyForegroundService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startService(serviceIntent);
                    } else {
                        context.startService(serviceIntent);
                    }
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
}
