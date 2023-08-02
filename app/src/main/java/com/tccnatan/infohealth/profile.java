package com.tccnatan.infohealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tcc.infohealth.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class profile extends AppCompatActivity {

    TextView nome;
    TextView senha;
    TextView email;
    TextView cidade;
    TextView pais;
    TextView endereço_casa;
    TextView n_casa;

    TextView endereço_trabalho;
    TextView n_trabalho;

    Button confirmar;

    private Uri imageURi;

    ImageView image_profile;

    private Bitmap bitmap;


    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseUser user;

    private DatabaseReference myRef;

    private ValueEventListener value_listener;

    String autenticacao;

    /*@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.home:
            {
                Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
                Intent pagina_usuario = new Intent(this,MainActivity.class);
                startActivity(pagina_usuario);
                break;
            }
            case R.id.profile:
            {
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.logout:
            {
                Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                if (foregroundServiceRunning()) {
                    Intent serviceIntent = new Intent(this, MyForegroundService.class);
                    stopService(serviceIntent);
                }
                login();
                break;
            }

        }
        return super.onOptionsItemSelected(item);

    }*/




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nome = findViewById(R.id.nome);
        senha = findViewById(R.id.senha);
        senha.setFocusable(false);
        senha.setFocusableInTouchMode(false);
        email = findViewById(R.id.email);
        cidade = findViewById(R.id.cidade);
        pais = findViewById(R.id.pais);

        endereço_casa = findViewById(R.id.casa_endereço);
        n_casa= findViewById(R.id.casa_n);
        endereço_trabalho = findViewById(R.id.trabalho_endereço);
        n_trabalho = findViewById(R.id.trabalho_n);
        confirmar = findViewById(R.id.confirmar);
        image_profile = findViewById(R.id.profile_image_2);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        user = mAuth.getCurrentUser();

        if(user!=null){
            myRef = database.getReference();
            nome.setText(user.getDisplayName());
            email.setText(user.getEmail());
            ler_dados(myRef, "Localização");
            ler_dados(myRef, "Autenticação");
        }
        else{
            login();
        }



        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(nome.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("nome").setValue(nome.getText().toString());

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nome.getText().toString())
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        System.out.println("Nome de usuário atualizado com sucesso");

                                    } else {
                                        System.out.println("Erro ao atualizar o nome de usuário");
                                    }
                                }
                            });

                }
                if(senha.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("password").setValue(senha.getText().toString());
                }

                if(email.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("email").setValue(email.getText().toString());
                    user.updateEmail(email.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        System.out.println("Nome de usuário atualizado com sucesso");
                                    } else {
                                        System.out.println("Erro ao atualizar o nome de usuário");
                                    }
                                }
                            });

                }

                if(pais.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("País").setValue(pais.getText().toString());

                }
                if(cidade.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Cidade").setValue(cidade.getText().toString());

                }
                if(endereço_casa.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Rua").setValue(endereço_casa.getText().toString());

                }
                if(endereço_trabalho.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Rua").setValue(endereço_trabalho.getText().toString());

                }
                if(n_casa.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Nº").setValue(n_casa.getText().toString());

                }
                if(n_trabalho.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Nº").setValue(n_trabalho.getText().toString());

                }

                Toast.makeText(getApplicationContext(),"Profile Updated",Toast.LENGTH_LONG).show();
                atividadeprincipal();

            }
        });




        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChoosePicture();

            }
        });

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }*/

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

                if(variavel=="Localização"){
                    cidade.setText(""+ dataSnapshot.child("Cidade").getValue());
                    pais.setText(""+ dataSnapshot.child("País").getValue());
                    endereço_casa.setText(""+ dataSnapshot.child("Casa").child("Rua").getValue());
                    n_casa.setText(""+ dataSnapshot.child("Casa").child("Nº").getValue());
                    endereço_trabalho.setText(""+ dataSnapshot.child("Trabalho").child("Rua").getValue());
                    n_trabalho.setText(""+ dataSnapshot.child("Trabalho").child("Nº").getValue());

                }
                if(variavel == "Autenticação"){

                    autenticacao=  ""+ dataSnapshot.getValue();

                    if(autenticacao.equals("Google")){

                        email.setFocusable(false);
                        email.setFocusableInTouchMode(false);

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }

        });
    }

    void atribuir_autenticacao(String valor){

        autenticacao = valor;

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

    private void atividadeprincipal() {
        finish();
    }
}