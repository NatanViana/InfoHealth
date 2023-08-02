package com.tccnatan.infohealth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tcc.infohealth.R;


import java.util.Calendar;
import java.text.SimpleDateFormat;



public class login extends AppCompatActivity {
    private static final String TAG ="login" ;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG1 ="GOOGLEAUTH";

    GoogleSignInClient mGoogleSignInClient;
    Dialog dialog;

    TabLayout tabLayout;
    ViewPager viewPager;
    FloatingActionButton facebook;
    FloatingActionButton google;
    FloatingActionButton twitter;
    LoginAdapter adapter;
    String email;
    String password;
    String option;
    String nome;

    private FirebaseAuth mAuth;

    private FirebaseDatabase database;

    float v=0;

    int alert = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        //facebook = findViewById(R.id.login_facebook);
        google = findViewById(R.id.login_google);
        //twitter = findViewById(R.id.login_twitter);



        tabLayout.addTab(tabLayout.newTab().setText("Login"));
        tabLayout.addTab(tabLayout.newTab().setText("Signup"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        adapter = new LoginAdapter(getSupportFragmentManager(), this, tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tabLayout.getSelectedTabPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //facebook.setTranslationY(300);
        google.setTranslationY(300);
        //twitter.setTranslationY(300);
        tabLayout.setTranslationY(300);

        //facebook.setAlpha(v);
        google.setAlpha(v);
        //twitter.setAlpha(v);
        tabLayout.setAlpha(v);

        //facebook.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(400).start();
        google.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(600).start();
        //twitter.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(800).start();
        tabLayout.animate().translationY(0).alpha(1).setDuration(1000).setStartDelay(100).start();

        Intent intentRecebedora = getIntent();
        Bundle parametros = intentRecebedora.getExtras();
        if(parametros!=null){
            email = parametros.getString("email");
            password = parametros.getString("password");
            option = parametros.getString("option");
            //Toast.makeText(getApplicationContext(),"Email"+ email +"\n password"+password+"\n option"+option,Toast.LENGTH_LONG).show();
            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ){
                Toast.makeText(getApplicationContext(),"Enter email and password",Toast.LENGTH_LONG).show();
                return;
            }
            if(option.equals("sign_in")){

               SignIn();
            }
            else{
                nome = parametros.getString("nome");
                SignUp(nome);

            }

        }
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/user.birthday.read"))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        //Getting the button click
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GSignIn();

            }
        });

    }

    private void GSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        google.setEnabled(false);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            //dialog.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                //Toast.makeText(login.this,"firebaseAuthWithGoogle:" + account.getId(),Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken(), account.getDisplayName(),account.getEmail());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                //dialog.dismiss();
                // ...
            }
            google.setEnabled(true);
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String accountname, String accountemail) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(login.this,"signInWithCredential:success",Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference myRef = database.getReference();
                            nome = accountname;
                            email =accountemail;


                            if(user!=null){
                                myRef.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            updateUI(user,user.getDisplayName());

                                        }
                                        else{
                                            myRef.child("Users").child(user.getUid()).child("nome").setValue(nome);
                                            myRef.child("Users").child(user.getUid()).child("email").setValue(email);
                                            myRef.child("Users").child(user.getUid()).child("password").setValue(password);
                                            myRef.child("Users").child(user.getUid()).child("Nº notificações").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Respondidas").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Ignoradas").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Alert").child("alert").setValue("None");
                                            myRef.child("Users").child(user.getUid()).child("Alert").child("Flag").setValue(true);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Alert").setValue(-1);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Rua").setValue("");
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Nº").setValue("");
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Rua").setValue("");
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Nº").setValue("");
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Latitude").setValue(-1);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Longitude").setValue(-1);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Latitude").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Longitude").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Latitude").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Longitude").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("País").setValue("");
                                            myRef.child("Users").child(user.getUid()).child("Localização").child("Cidade").setValue("");

                                            myRef.child("Users").child(user.getUid()).child("Analises").child("SEMANA").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Analises").child("Gravar Semana").setValue(false);
                                            myRef.child("Users").child(user.getUid()).child("Estado Atual").setValue(-1);
                                            myRef.child("Users").child(user.getUid()).child("Flag Resposta").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Flag Resposta no Dia").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Ignoradas_week").setValue(0);
                                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Respondidas_week").setValue(0);


                                            // Obtendo a data atual
                                            Calendar calendar = Calendar.getInstance();
                                            // Definindo o formato da data desejado
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                            // Obtendo a data formatada como uma string
                                            String dataFormatada = dateFormat.format(calendar.getTime());

                                            myRef.child("Users").child(user.getUid()).child("Data de Acesso").setValue(dataFormatada);
                                            myRef.child("Users").child(user.getUid()).child("Data de Referencia").setValue(dataFormatada);
                                            myRef.child("Users").child(user.getUid()).child("Ofensiva").setValue("0");
                                            myRef.child("Users").child(user.getUid()).child("Comorbidade").setValue("");
                                            myRef.child("Users").child(user.getUid()).child("Idade").setValue("");

                                            myRef.child("Users").child(user.getUid()).child("Autenticação").setValue("Google");

                                            for (int i = 0; i < 504 ; i++) {
                                                for (int j = 0; j < 2; j++) {
                                                    myRef.child("Users").child(user.getUid()).child("Qtable").child(""+i+"_"+j).setValue(0);
                                                }

                                            }
                                            myRef.child("Users").child(user.getUid()).child("Recompensa Total").setValue(0);

                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(nome).build();
                                            user.updateProfile(profileUpdates);
                                            updateUI(user,user.getDisplayName());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            //  Log.w(TAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(login.this,"Login failed",Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }


    private void SignIn(){

        //Toast.makeText(getApplicationContext(),"Sign In mode",Toast.LENGTH_LONG).show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            updateUI(user,user.getDisplayName());


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(login.this, "Authentication failed, write a valid email and password.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });


    }
    private void SignUp(String nome){


        //Toast.makeText(getApplicationContext(),"Sign up mode",Toast.LENGTH_LONG).show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference myRef = database.getReference();

                            myRef.child("Users").child(user.getUid()).child("nome").setValue(nome);
                            myRef.child("Users").child(user.getUid()).child("email").setValue(email);
                            myRef.child("Users").child(user.getUid()).child("password").setValue(password);
                            myRef.child("Users").child(user.getUid()).child("Nº notificações").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Respondidas").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Ignoradas").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Alert").child("alert").setValue("None");
                            myRef.child("Users").child(user.getUid()).child("Alert").child("Flag").setValue(true);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Alert").setValue(-1);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Rua").setValue("");
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Nº").setValue("");
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Rua").setValue("");
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Nº").setValue("");
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Latitude").setValue(-1);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Longitude").setValue(-1);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Latitude").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Longitude").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Latitude").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Longitude").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Localização").child("País").setValue("");
                            myRef.child("Users").child(user.getUid()).child("Localização").child("Cidade").setValue("");

                            myRef.child("Users").child(user.getUid()).child("Analises").child("SEMANA").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Analises").child("Gravar Semana").setValue(false);
                            myRef.child("Users").child(user.getUid()).child("Estado Atual").setValue(-1);
                            myRef.child("Users").child(user.getUid()).child("Flag Resposta").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Flag Resposta no Dia").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Ignoradas_week").setValue(0);
                            myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Respondidas_week").setValue(0);

                            // Obtendo a data atual
                            Calendar calendar = Calendar.getInstance();
                            // Definindo o formato da data desejado
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            // Obtendo a data formatada como uma string
                            String dataFormatada = dateFormat.format(calendar.getTime());

                            myRef.child("Users").child(user.getUid()).child("Data de Acesso").setValue(dataFormatada);
                            myRef.child("Users").child(user.getUid()).child("Data de Referencia").setValue(dataFormatada);
                            myRef.child("Users").child(user.getUid()).child("Ofensiva").setValue("0");
                            myRef.child("Users").child(user.getUid()).child("Comorbidade").setValue("");
                            myRef.child("Users").child(user.getUid()).child("Idade").setValue("");

                            myRef.child("Users").child(user.getUid()).child("Autenticação").setValue("Normal");

                            for (int i = 0; i < 504 ; i++) {
                                for (int j = 0; j < 2; j++) {
                                    myRef.child("Users").child(user.getUid()).child("Qtable").child(""+i+"_"+j).setValue(0);
                                }

                            }

                            myRef.child("Users").child(user.getUid()).child("Recompensa Total").setValue(0);


                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(nome).build();
                            user.updateProfile(profileUpdates);
                            updateUI(user,nome);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(login.this, "Authentication failed, please write a password with more than 6 characters or a valid email.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }




    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        System.out.println("CurrentUser: "+currentUser);
        if(currentUser != null){
            currentUser.reload();
            updateUI(currentUser,currentUser.getDisplayName());
        }
    }

    private void updateUI(FirebaseUser user,String nome_usuario) {
        DatabaseReference myRef = database.getReference();

        alert=0;
        System.out.println("Indo para MainActivity");

        myRef.child("Users").child(user.getUid()).child("Localização").child("Alert").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                alert = Integer.parseInt(""+ dataSnapshot.getValue());

                if(alert == -1){
                    // pedir localização trabalho e casa e doença pela 1 vez
                    //ir para atividade extra

                    atividadeinfo();

                }
                else{

                    atividadeprincipal();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }

        });


    }

    private void atividadeprincipal() {
        //Realizar mainactivity sem validar localização
        Intent pagina_usuario = new Intent(this,MainActivity.class);
        startActivity(pagina_usuario);
        finish();
    }

    private void atividadeinfo() {
        Intent pagina_info= new Intent(this,formulario_inicial.class);
        startActivity(pagina_info);
        finish();
    }




}