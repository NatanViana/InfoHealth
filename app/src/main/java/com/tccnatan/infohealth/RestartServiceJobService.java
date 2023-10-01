package com.tccnatan.infohealth;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class RestartServiceJobService extends JobService {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseUser user;

    @Override
    public boolean onStartJob(JobParameters params) {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Log.d("RestartServiceJob", "Job started");


        // Verifique se o serviço já está em execução
        if (!isServiceRunning(this, MyForegroundService.class) && (user!=null)) {
            // Inicie o serviço novamente
            System.out.println("Reiniciando o serviço...");
            Intent serviceIntent = new Intent(this, MyForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this,serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        // Retorne false para indicar que a tarefa foi concluída
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("RestartServiceJob", "Job stopped");
        return false;
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
