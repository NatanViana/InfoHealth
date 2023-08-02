package com.tccnatan.infohealth;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Verifique se o usuário está online
            if (isUserOnline()) {
                // Verifique se o serviço não está em execução
                if (!isServiceRunning(context, MyForegroundService.class)) {
                    // Inicie o serviço de foreground
                    Intent serviceIntent = new Intent(context, MyForegroundService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent);
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

