package com.tccnatan.infohealth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tcc.infohealth.R;

public class formulario_inicial extends AppCompatActivity {

    TextView pais;
    TextView cidade;
    TextView idade;
    Spinner spinner_comorbidade;
    TextView endereçocasa;
    TextView endereçotrabalho;
    TextView ncasa;
    TextView ntrabalho;
    Button confirmar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseUser user;

    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_inicial);

        pais = findViewById(R.id.pais);
        cidade= findViewById(R.id.cidade);
        idade = findViewById(R.id.idade);
        endereçocasa = findViewById(R.id.EndereçoResidencial);
        endereçotrabalho =  findViewById(R.id.EndereçoTrabalho);
        ncasa = findViewById(R.id.NResidencial);
        ntrabalho = findViewById(R.id.NTrabalho);
        confirmar = findViewById(R.id.button_enviar);
        spinner_comorbidade = findViewById(R.id.disease);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        user = mAuth.getCurrentUser();

        if(user!=null){
            myRef = database.getReference();

        }
        else{
            login();
        }


        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pais.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("País").setValue(pais.getText().toString());

                }
                if(cidade.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Cidade").setValue(cidade.getText().toString());

                }

                if(idade.getText().toString()!=null & !idade.getText().toString().isEmpty()){

                    myRef.child("Users").child(user.getUid()).child("Idade").setValue(idade.getText().toString());

                }
                if(endereçocasa.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Rua").setValue(endereçocasa.getText().toString());

                }
                if(endereçotrabalho.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Rua").setValue(endereçotrabalho.getText().toString());

                }
                if(ncasa.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Casa").child("Nº").setValue(ncasa.getText().toString());

                }
                if(ntrabalho.getText().toString()!=null){

                    myRef.child("Users").child(user.getUid()).child("Localização").child("Trabalho").child("Nº").setValue(ntrabalho.getText().toString());

                }
                if(spinner_comorbidade.getSelectedItem().toString()!= "🤍 Doença"){

                    myRef.child("Users").child(user.getUid()).child("Comorbidade").setValue(spinner_comorbidade.getSelectedItem().toString());

                }

                myRef.child("Users").child(user.getUid()).child("Localização").child("Alert").setValue(1);
                atividadeprincipal();
            }

        });


    }

    private void login() {
        Intent pagina_login = new Intent(this,login.class);
        startActivity(pagina_login);
        finish();
    }

    private void atividadeprincipal() {
        Intent pagina_usuario = new Intent(this,MainActivity.class);
        startActivity(pagina_usuario);
        finish();
    }

}