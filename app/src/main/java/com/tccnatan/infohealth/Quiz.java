package com.tccnatan.infohealth;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class Quiz extends AppCompatActivity {


    Button R1;
    Button R2;
    Button R3;
    Button confirmar;

    TextView n_questao;
    TextView tipo;

    TextView text_questao;

    int questao;


    String resposta;
    String r1;
    String r2;
    String r3;
    String type;

    String respondidas;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseUser user;

    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        n_questao = findViewById(R.id.n_questao);
        tipo = findViewById(R.id.tipo);
        text_questao = findViewById(R.id.text_questao);


        R1 = findViewById(R.id.button_1);
        R2 = findViewById(R.id.button_2);
        R3 = findViewById(R.id.button_3);
        confirmar = findViewById(R.id.button_confirmar);



        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        user = mAuth.getCurrentUser();

        Intent intentRecebedora = getIntent();
        Bundle parametros = intentRecebedora.getExtras();

        questao = 1;
        respondidas = parametros.getString("res");
        int n = Integer.parseInt(respondidas);





        if(user!=null){
            myRef = database.getReference();
            ler_proms(myRef, parametros.getString("prom"),"Q"+questao);
        }
        else{
            login();
        }

        R1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                R2.setBackgroundResource(R.drawable.button_shape_blue);
                R2.setTextColor(R.color.roxo_app);

                R3.setBackgroundResource(R.drawable.button_shape_blue);
                R3.setTextColor(R.color.roxo_app);

                R1.setBackgroundResource(R.drawable.button_shape_green);
                R1.setTextColor(R.color.green_dark);

                confirmar.setClickable(true);
                confirmar.setAlpha(1);

                resposta = r1;



            }

        });

        R2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                R1.setBackgroundResource(R.drawable.button_shape_blue);
                R1.setTextColor(R.color.roxo_app);

                R3.setBackgroundResource(R.drawable.button_shape_blue);
                R3.setTextColor(R.color.roxo_app);

                R2.setBackgroundResource(R.drawable.button_shape_green);
                R2.setTextColor(R.color.green_dark);

                confirmar.setClickable(true);
                confirmar.setAlpha(1);

                resposta = r2;


            }

        });

        R3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {

                R1.setBackgroundResource(R.drawable.button_shape_blue);
                R1.setTextColor(R.color.roxo_app);

                R2.setBackgroundResource(R.drawable.button_shape_blue);
                R2.setTextColor(R.color.roxo_app);

                R3.setBackgroundResource(R.drawable.button_shape_green);
                R3.setTextColor(R.color.green_dark);

                confirmar.setClickable(true);
                confirmar.setAlpha(1);

                resposta = r3;



            }

        });



        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.child("Users").child(user.getUid()).child("Respostas").child(""+respondidas).child("Q"+questao).setValue(resposta);
                questao = questao+1;
                if(questao<=5){
                    ler_proms(myRef, parametros.getString("prom"),"Q"+questao);

                }
                else{
                    myRef.child("Users").child(user.getUid()).child("Nº notificações").child("Respondidas").setValue(n);
                    myRef.child("Users").child(user.getUid()).child("Alert").child("alert").setValue("None");
                    updateUI(user,user.getDisplayName());
                }


            }

        });


    }

    public void ler_proms(DatabaseReference myRef,String prom,String variavel) {

        myRef.child("Proms").child(prom).child(variavel).addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.


                r1 = ""+ dataSnapshot.child("R1").getValue();
                r2 = ""+ dataSnapshot.child("R2").getValue();
                r3 = ""+ dataSnapshot.child("R3").getValue();
                type = ""+ dataSnapshot.child("Tipo").getValue();


                R1.setText(r1);
                R2.setText(r2);
                R3.setText(r3);
                tipo.setText(type);
                n_questao.setText("Questão "+questao+" de 5");
                text_questao.setText("Escolha a opção que melhor descreve sua "+ type +" atualmente");

                confirmar.setClickable(false);
                confirmar.setAlpha(0.5F);


                R1.setBackgroundResource(R.drawable.button_shape_blue);
                R1.setTextColor(R.color.roxo_app);

                R2.setBackgroundResource(R.drawable.button_shape_blue);
                R2.setTextColor(R.color.roxo_app);

                R3.setBackgroundResource(R.drawable.button_shape_blue);
                R3.setTextColor(R.color.roxo_app);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }

        });
    }



    private void login() {
        Intent pagina_login = new Intent(this,login.class);
        startActivity(pagina_login);
    }

    private void updateUI(FirebaseUser user,String nome_usuario) {
        finish();
    }


}