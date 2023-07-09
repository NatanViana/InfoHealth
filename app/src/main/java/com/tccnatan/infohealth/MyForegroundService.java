package com.tccnatan.infohealth;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tcc.infohealth.R;

import java.time.Instant;
import java.util.Calendar;

import android.Manifest;
import android.widget.Toast;


public class MyForegroundService  extends Service {

    Instant startTime;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseUser user;

    private DatabaseReference myRef;

    int numStates = 504;
    int numActions = 2;
    double learningRate = 0.1;
    double discountFactor = 0.9;
    double epsilon = 0.9;

    int currentState = -1;

    int nextState = -1;

    double reward_total = 0;
    int flag =-1;

    int semanas = 0;

    QLearning qLearning;

    int action = 1;
    double reward= 0;

    private LocationManager locationManager;
    private LocationListener locationListener;

    double latitude;
    double longitude;

    double latitude_casa;
    double longitude_casa;

    double latitude_trabalho;
    double longitude_trabalho;

    double raio = 0.5;

    int local = 2;

    int ofensiva =0;

    int notification_weeek = 0;
    int not_ignoradas_week = 0;
    int not_respondidas_week = 0;

    int notificações_respondidas = 0;

    int notificações_ignoradas = 0;

    int referencia_resposta = 0;

    int referencia_dia = 0;

    int flag_resposta = 0;

    int flag_respordia = 0;

   double[][] qtable_firebase = new double[numStates][numActions];

   boolean servicerunning = true;

    @Override
    public void onCreate() {

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        user = mAuth.getCurrentUser();

        myRef = database.getReference();

        Calendar calendario = Calendar.getInstance();

        referencia_dia = calendario.get(Calendar.DAY_OF_WEEK);

        switch (referencia_dia) {
            case Calendar.SUNDAY:
                referencia_dia = 0;
                break;
            case Calendar.MONDAY:
                referencia_dia = 1;
                break;
            case Calendar.TUESDAY:
                referencia_dia = 2;
                break;
            case Calendar.WEDNESDAY:
                referencia_dia = 3;
                break;
            case Calendar.THURSDAY:
                referencia_dia = 4;
                break;
            case Calendar.FRIDAY:
                referencia_dia = 5;
                break;
            case Calendar.SATURDAY:
                referencia_dia= 6;
                break;
        }


        qLearning = new QLearning(numStates, numActions, learningRate, discountFactor, epsilon,referencia_dia,user,myRef,qtable_firebase);



        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Verifica o comando recebido
        if (intent != null) {
            String act = intent.getAction();
            if (act != null) {
                if (act.equals("STOP_SERVICE")) {
                    // Para o serviço e remove-o do primeiro plano
                    System.out.println("Encerrando");
                    servicerunning = false;
                    stopForeground(true);
                    stopSelf();
                }
            }
        }

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startTime = Instant.now();
                        }
                        if(servicerunning){
                            System.out.println("Iniciando Novamente");
                        }

                        // QLearning
                        ler_dados_single(myRef,"Qtable");
                        ler_dados_single(myRef,"Recompensa Total");
                        ler_dados_single(myRef,"Analises");
                        ler_dados(myRef,"Localização");
                        ler_dados_single(myRef,"Ofensiva");


                        try {
                            Thread.sleep(5000); // Atraso de 5 segundos
                            // Código a ser executado após o atraso de 5 segundos
                            qLearning.qTable = qtable_firebase;
                            referencia_resposta = notificações_respondidas +1 ;

                            //qLearning.printQValues();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        while(servicerunning){

                            //System.out.println("Foreground Service is running...");
                            //int flag_estado = printMensagemAposUmMinuto();


                            //QLearning
                            if ( currentState != flag ){

                                action = qLearning.chooseAction(currentState);
                                //System.out.println("Action:"+action+"---- State: "+nextState);

                                if(action == 0){
                                    myRef.child("Users").child(user.getUid()).child("Alert").child("alert").setValue("P1");
                                    notification_weeek = notification_weeek +1;
                                }

                                flag = currentState;

                            }


                            //Esperar mudança do estado
                            local = 2;
                            if(estaNoLocal(latitude,longitude,latitude_casa,longitude_casa,raio) && estaNoLocal(latitude,longitude,latitude_trabalho,longitude_trabalho,raio)){
                                if(calcularDistancia(latitude,longitude,latitude_casa,longitude_casa) <= calcularDistancia(latitude,longitude,latitude_trabalho,longitude_trabalho)){
                                    local = 0;
                                }
                                else{
                                    local = 1;
                                }

                            }
                            else if(estaNoLocal(latitude,longitude,latitude_casa,longitude_casa,raio)){

                                local = 0;

                            }
                            else if(estaNoLocal(latitude,longitude,latitude_trabalho,longitude_trabalho,raio)){

                                local = 1;
                            }

                            //System.out.println("LOCAL: "+local +"latitude: "+latitude+ "longitude: "+longitude );
                            //System.out.println("LOCAL: "+local +"latitude_t: "+latitude_casa+ "longitude_t: "+longitude_casa );
                            //System.out.println("LOCAL: "+local +"latitude_t: "+latitude_trabalho+ "longitude_t: "+longitude_trabalho );

                            nextState = qLearning.getNextState(currentState, action, numStates, local);  // Função de próximo estado
                            //System.out.println("Next State: "+nextState);


                            //System.out.println("N Respondidas: "+notificações_respondidas);



                            if(nextState != currentState){

                                // impedir erro com currentState no inicio ao ser -1.
                                if(currentState != -1){

                                    System.out.println("Dia da Semana: "+qLearning.diadaSemana+ "NextState = "+ nextState);
                                    //System.out.println( "refrencia ="+referencia_dia + "ref resposta: "+referencia_resposta + "not res: "+ notificações_respondidas);

                                    if(notificações_respondidas == referencia_resposta){
                                        System.out.println("respondeu");
                                        referencia_resposta = notificações_respondidas +1;

                                        // incremento de ofensiva 1 vez no dia em caso de resposta
                                        if(flag_respordia==0 && (referencia_dia == qLearning.diadaSemana)){
                                            System.out.println("aumentar ofensiva");
                                            ofensiva = ofensiva+1;
                                            myRef.child("Users").child(user.getUid()).child("Ofensiva").setValue(ofensiva);
                                            flag_respordia = 1;
                                        }

                                        flag_resposta = 1;
                                        not_respondidas_week = not_respondidas_week +1;


                                    }

                                    if(action == 0 && flag_resposta == 0){
                                        myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Ignoradas").setValue(notificações_ignoradas+1);

                                        not_ignoradas_week = not_ignoradas_week +1;
                                    }

                                    // verificar se zera ou não a ofensiva em caso de não resposta no dia
                                    if(referencia_dia == qLearning.diadaSemana-1){
                                        System.out.println("mudar dia");

                                        if(flag_respordia==0){

                                            System.out.println("zerando ofensiva");
                                            ofensiva = 0;
                                            myRef.child("Users").child(user.getUid()).child("Ofensiva").setValue(ofensiva);
                                        }

                                        referencia_dia = qLearning.diadaSemana;
                                        flag_respordia = 0;
                                    }



                                    // Simule tomar a ação escolhida e observe a recompensa e o próximo estado
                                    reward = qLearning.getReward(currentState, action, flag_resposta);  // Função de recompensa

                                    qLearning.updateQValue(currentState, action, reward, nextState);

                                    //qLearning.printQValues();

                                    reward_total = reward_total + reward;

                                    myRef.child("Users").child(user.getUid()).child("Recompensa Total").setValue(reward_total);

                                    //System.out.println("Reward Total: "+reward_total);



                                }

                                currentState = nextState;

                                if(0<=currentState && currentState<=2){
                                    System.out.println("Reward Total: "+reward_total);
                                    semanas = semanas + 1;
                                    System.out.println("Semanas Passadas: "+ semanas);
                                    System.out.println("Notificações da Semana: " + notification_weeek );
                                    //para analíses
                                    myRef.child("Users").child(user.getUid()).child("Analises").child("SEMANA").setValue(semanas);
                                    myRef.child("Users").child(user.getUid()).child("Analises").child("Semanas").child(""+semanas).child("Notificações").setValue(notification_weeek);
                                    myRef.child("Users").child(user.getUid()).child("Analises").child("Semanas").child(""+semanas).child("Recompensa_Total").setValue(reward_total);
                                    myRef.child("Users").child(user.getUid()).child("Analises").child("Semanas").child(""+semanas).child("Not_Ignoradas").setValue(not_ignoradas_week);
                                    myRef.child("Users").child(user.getUid()).child("Analises").child("Semanas").child(""+semanas).child("Not_Respondidas").setValue(not_respondidas_week);
                                    notification_weeek = 0;
                                    not_ignoradas_week = 0;
                                    not_respondidas_week = 0;
                                }

                                flag = -1;
                                flag_resposta = 0;

                                // Flag no firebase para mandar notificação -> caso em false o app tem permissão novamente para mandar.
                                myRef.child("Users").child(user.getUid()).child("Alert").child("Flag").setValue(false);
                                myRef.child("Users").child(user.getUid()).child("Alert").child("alert").setValue("None");



                            }

                        }
                    }
                }
        ).start();

        final String CHANNEL_ID = "Foreground Service";
        final long[] pattern ={100, 300, 300, 300};

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID,CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
        }
        NotificationManager manager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            manager = getSystemService(NotificationManager.class);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.logo_infohealth_app_round)
                    .setContentTitle("Foreground")
                    .setContentText("Q-learning is running")
                    .setAutoCancel(true)
                    .setVibrate(pattern);
        }

        startForeground(1001,builder.build());

        ler_dados(myRef,"Nº notificações");

        // Inicializar o LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Inicializar o LocationListener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Aqui você obtém as atualizações de localização do usuário
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                // Faça o que for necessário com as coordenadas de localização

                myRef.child("Users").child(user.getUid()).child("Localização").child("Latitude").setValue(latitude);
                myRef.child("Users").child(user.getUid()).child("Localização").child("Longitude").setValue(longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        // Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } catch (Exception e) {
                // Lidar com exceções relacionadas à solicitação de atualizações de localização
                e.printStackTrace();
            }
        }
        else {
                // Lidar com o caso em que as permissões não são concedidas
                latitude = -1;
                longitude = -1;
            }
        

        return super.onStartCommand(intent, flags, startId);
    }

    // Função para verificar se o usuário está dentro do range de latitude e longitude
    public static boolean estaNoLocal(double latitudeUsuario, double longitudeUsuario, double latitudeLocal, double longitudeLocal, double raio) {
        double distancia = calcularDistancia(latitudeUsuario, longitudeUsuario, latitudeLocal, longitudeLocal);
        return distancia <= raio;
    }

    // Função para calcular a distância entre duas coordenadas usando a fórmula de Haversine
    public static double calcularDistancia(double latitude1, double longitude1, double latitude2, double longitude2) {
        final int RAIO_TERRA = 6371; // Raio médio da Terra em quilômetros

        double dLatitude = Math.toRadians(latitude2 - latitude1);
        double dLongitude = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(dLatitude / 2) * Math.sin(dLatitude / 2) +
                Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) *
                        Math.sin(dLongitude / 2) * Math.sin(dLongitude / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distancia = RAIO_TERRA * c;
        return distancia;
    }

    public void ler_dados(DatabaseReference myRef,String variavel) {

        myRef.child("Users").child(user.getUid()).child(variavel).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                if (variavel == "Localização") {
                    String latc = "" + String.valueOf(dataSnapshot.child("Casa").child("Latitude").getValue());
                    String longc = "" + String.valueOf(dataSnapshot.child("Casa").child("Longitude").getValue());

                    String latt = "" + String.valueOf(dataSnapshot.child("Trabalho").child("Latitude").getValue());
                    String longt = "" + String.valueOf(dataSnapshot.child("Trabalho").child("Longitude").getValue());


                    latitude_casa = Double.parseDouble(latc);
                    longitude_casa = Double.parseDouble(longc);
                    latitude_trabalho = Double.parseDouble(latt);
                    longitude_trabalho = Double.parseDouble(longt);


                }
                if(variavel == "Nº notificações"){

                    String respondidas = "" + String.valueOf(dataSnapshot.child("Respondidas").getValue());
                    notificações_respondidas = Integer.parseInt(respondidas);
                    String ignoradas = "" + String.valueOf(dataSnapshot.child("Ignoradas").getValue());
                    notificações_ignoradas = Integer.parseInt(ignoradas);

                }



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }

        });
    }

    public void ler_dados_single(DatabaseReference myRef,String variavel) {

        myRef.child("Users").child(user.getUid()).child(variavel).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                if(variavel=="Qtable"){

                    for (int i = 0; i < 504; i++) {
                        for (int j = 0; j < 2; j++) {
                            String val = "" + String.valueOf(dataSnapshot.child(""+i+"_"+j).getValue());
                            qtable_firebase [i][j] = Double.parseDouble(val);
                        }

                    }



                }

                if(variavel=="Recompensa Total"){

                    String val = "" + String.valueOf(dataSnapshot.getValue());
                    reward_total = Integer.parseInt(val);

                }
                if(variavel == "Ofensiva"){

                    String val = "" + String.valueOf(dataSnapshot.getValue());
                    ofensiva = Integer.parseInt(val);


                }
                if(variavel == "Analises"){

                    String val = "" + String.valueOf(dataSnapshot.child("SEMANA").getValue());
                    semanas = Integer.parseInt(val);


                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
