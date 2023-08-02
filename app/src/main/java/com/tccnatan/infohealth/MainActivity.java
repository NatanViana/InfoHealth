package com.tccnatan.infohealth;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.tcc.infohealth.R;

import java.io.IOException;

import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    Button sign_out;

    Intent serviceIntent;
    TextView textView;

    TextView saúde;
    TextView ofensiva;
    TextView idade;


    LottieAnimationView lottie_animation;
    Button responder_questionario;
    ImageView image_profile;

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseUser user;

    private DatabaseReference myRef;

    private ValueEventListener value_listener;
    private Uri imageURi;

    String respondidas;

    String alert;

    int res;
    private Bitmap bitmap;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final int PERMISSION_CODE = 123;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.home:
            {
                Toast.makeText(MainActivity.this, "Home selected", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.profile:
            {
                Toast.makeText(MainActivity.this, "Profile selected", Toast.LENGTH_SHORT).show();
                Intent pagina_profile = new Intent(this,profile.class);
                startActivity(pagina_profile);
                break;
            }
            case R.id.logout:
            {
                Toast.makeText(MainActivity.this, "Logout selected", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                onLogout();
                login();
                finish();
                break;
            }

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        sign_out = findViewById(R.id.sign_out);
        textView = findViewById(R.id.nome);
        saúde = findViewById(R.id.disease_text);
        idade = findViewById(R.id.age_text);
        ofensiva = findViewById(R.id.streak_text);


        lottie_animation = findViewById(R.id.lottie);
        lottie_animation.playAnimation();
        lottie_animation.loop(true);
        image_profile = findViewById(R.id.profile_image);
        responder_questionario = findViewById(R.id.button_responder_questionario);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        user = mAuth.getCurrentUser();


        // Simulando um tempo de carregamento
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Carregamento concluído, torna a barra de progresso invisível
                //progressBar.setVisibility(View.INVISIBLE);
                // Ou, se preferir remover completamente a barra de progresso
                progressBar.setVisibility(View.GONE);
            }
        }, 3000); // Tempo de simulação de 3 segundos (substitua pelo tempo real de carregamento)



        if(user!=null){
            myRef = database.getReference();
            textView.setText(user.getDisplayName());
            ler_dados(myRef, "Alert");
            ler_dados(myRef, "Comorbidade");
            ler_dados(myRef, "Ofensiva");
            ler_dados(myRef, "Idade");
            ler_dados(myRef, "Nº notificações");
            ler_dados(myRef,"Localização");

        }
        else{
            login();
        }


        // Verificar se a permissão já foi concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permissão ao usuário
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_CODE);
            } else {

            }
        }


        // Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        }
        else{
            String[] permissions = {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION);

        }


        if(!foregroundServiceRunning()){
            // Inicie o serviço de foreground
            serviceIntent = new Intent(this,MyForegroundService.class);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this,serviceIntent);
            }
        }


        responder_questionario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textView.setText(respondidas);
                //Sortear o PROM com P1 P2 P3  em novas versões..
                Quiz("P1",respondidas);


            }
        });



        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                onLogout();
                login();
                finish();

            }
        });



        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChoosePicture();

            }
        });



    }



    public void onLogout() {

        if (foregroundServiceRunning()) {
            Intent serviceIntentstop = new Intent(this, MyForegroundService.class);
            serviceIntentstop.setAction("STOP_SERVICE");
            startService(serviceIntentstop);
        }
    }

    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager)  getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){

            if(MyForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return  false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void login() {
        Intent pagina_login = new Intent(this,login.class);
        startActivity(pagina_login);
    }

    private void ChoosePicture(){
        Intent gallery_phone = new Intent();
        gallery_phone.setType("image/*");
        gallery_phone.setAction(Intent.ACTION_PICK);
        startActivityForResult(gallery_phone,1);


    }


    public void ler_dados(DatabaseReference myRef,String variavel) {

        if (value_listener != null) {
            myRef.child("Proms").child(user.getUid()).child(variavel).removeEventListener(value_listener);
        }

        myRef.child("Users").child(user.getUid()).child(variavel).addValueEventListener(value_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                if(variavel=="Alert"){
                    alert = ""+ dataSnapshot.child("alert").getValue();
                    // alerta flag para não mandar se já tiver mandado

                    if(alert.equals("None")){
                        responder_questionario.setBackgroundResource(R.drawable.text_shape_icon);
                        responder_questionario.setAlpha(0.5F);
                        responder_questionario.setClickable(false);
                        responder_questionario.setText("Nenhum Questionário Pendente");
                        lottie_animation.setAnimation(R.raw.healthcheck);

                    }
                    else{
                        responder_questionario.setBackgroundResource(R.drawable.text_shape_icon);
                        responder_questionario.setText("Questionário Pendente "+ "⚠️");
                        lottie_animation.setAnimation(R.raw.healthalert);
                        responder_questionario.setAlpha(1);
                        responder_questionario.setClickable(true);

                    }

                }

                if(variavel=="Nº notificações"){
                    respondidas = "" + String.valueOf(dataSnapshot.child("Respondidas").getValue());
                    res = Integer.parseInt(respondidas);
                    res = res + 1;
                    respondidas = String.valueOf(res);


                }
                if(variavel=="Idade"){
                    String idade_text = ""+ dataSnapshot.getValue();
                    if(idade_text!=""){
                        idade.setText(idade_text);
                    }


                }
                if(variavel=="Ofensiva"){
                    String ofensiva_text = ""+ dataSnapshot.getValue();
                    ofensiva.setText(ofensiva_text);

                }
                if(variavel=="Comorbidade"){
                    String comorbidade_text = ""+ dataSnapshot.getValue();
                    saúde.setText(comorbidade_text);

                }
                if(variavel=="Localização"){
                    String endereço_casa = ""+ dataSnapshot.child("Casa").child("Rua").getValue()+", "+ dataSnapshot.child("Casa").child("Nº").getValue();
                    //+", "+ dataSnapshot.child("Cidade").getValue()+", "+ dataSnapshot.child("País").getValue()

                    String endereço_trabalho = ""+ dataSnapshot.child("Trabalho").child("Rua").getValue()+", "+dataSnapshot.child("Trabalho").child("Nº").getValue() ;
                    //+", "+ dataSnapshot.child("Cidade").getValue()+", "+ dataSnapshot.child("País").getValue()

                    LocalizationConvertion(endereço_casa,"casa",myRef);
                    LocalizationConvertion(endereço_trabalho,"trabalho",myRef);



                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }

        });

    }



    private void LocalizationConvertion(String endereco,String tipo,DatabaseReference myRef ){



        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            //System.out.println("Endereço:"+endereco);
            List<Address> addresses = geocoder.getFromLocationName(endereco, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();


                if(tipo=="casa"){
                    //System.out.println(latitude);
                    //System.out.println(longitude);

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Latitude").setValue(latitude);
                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Longitude").setValue(longitude);


                }
                else{

                    if(tipo=="trabalho"){
                        //System.out.println(latitude);
                        //System.out.println(longitude);

                        myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Latitude").setValue(latitude);
                        myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Longitude").setValue(longitude);

                    }

                }

            } else {
                //System.out.println("Nenhum endereço encontrado");
                if(tipo=="casa"){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Latitude").setValue(-1);
                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Longitude").setValue(-1);


                }
                else{


                    if(tipo=="trabalho"){

                        myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Latitude").setValue(-1);
                        myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Longitude").setValue(-1);

                    }

                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imageURi = data.getData();
            //image_profile.setImageURI(imageURi);
            UploadPicture();

        }
    }

    private void UploadPicture() {
        FirebaseUser user = mAuth.getCurrentUser();
        StorageReference uploadRef = storageReference.child("Images/"+"Users/"+user.getUid()+"/profile_picture");
        //Toast.makeText(getApplicationContext(),"Entrei",Toast.LENGTH_LONG).show();
        bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageURi);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        uploadRef.putFile(imageURi).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(),"Image Uploaded.",Toast.LENGTH_LONG).show();
                        uploadRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //image_profile.setImageBitmap(bitmap);
                                Picasso.get().load(uri).into(image_profile);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to Upload.",Toast.LENGTH_LONG).show();

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            textView.setText(currentUser.getDisplayName());
            user = currentUser;
            StorageReference profileReference = storageReference.child("Images/"+"Users/"+currentUser.getUid()+"/profile_picture");
            currentUser.reload();
            profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //image_profile.setImageBitmap(bitmap);
                    Picasso.get().load(uri).into(image_profile);
                }
            });
        }
        else{
            login();
        }


    }

    private void Quiz(String prom, String respondidas) {
        Intent pagina_quiz = new Intent(this,Quiz.class);
        Bundle parametros_envio = new Bundle();
        parametros_envio.putString("prom",prom);
        parametros_envio.putString("res",respondidas);
        pagina_quiz.putExtras(parametros_envio);
        startActivity(pagina_quiz);
    }



}