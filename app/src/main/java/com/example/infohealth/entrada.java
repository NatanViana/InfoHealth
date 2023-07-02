package com.example.infohealth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class entrada extends AppCompatActivity {

    Button iniciar_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);
        iniciar_button = findViewById(R.id.button_iniciar);


        iniciar_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();

            }
        });
    }

    private void login() {
        Intent pagina_login = new Intent(this,login.class);
        startActivity(pagina_login);
    }
}