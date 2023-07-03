package com.tccnatan.infohealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.tcc.infohealth.R;

public class signup_tab_part2 extends Fragment {
    TextView nome_text;
    TextView email_text;
    LottieAnimationView lottie_animation;
    TextView password_text;
    Button signup;

    float v = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.signup_tab_part2, container, false);


        lottie_animation = root.findViewById(R.id.lottie);
        lottie_animation.playAnimation();
        lottie_animation.loop(true);

        nome_text = root.findViewById(R.id.nome);
        email_text = root.findViewById(R.id.email);
        password_text = root.findViewById(R.id.password);
        signup = root.findViewById(R.id.signup_button);



        nome_text.setTranslationX(800);
        email_text.setTranslationX(800);
        password_text.setTranslationX(800);
        signup.setTranslationX(800);

        nome_text.setAlpha(v);
        email_text.setAlpha(v);
        password_text.setAlpha(v);
        signup.setAlpha(v);


        nome_text.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(200).start();
        email_text.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        password_text.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        signup.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviar();
            }
        });
        return root;
    }
    public void enviar(){

        String email = email_text.getText().toString();
        String password = password_text.getText().toString();
        String nome = nome_text.getText().toString();


        Intent intentEnviadora = new Intent(getContext(),login.class);
        Bundle parametros = new Bundle();

        parametros.putString("email",email);
        parametros.putString("password",password);
        parametros.putString("option","sign_up");
        parametros.putString("nome",nome);

        intentEnviadora.putExtras(parametros);

        startActivity(intentEnviadora);

    }
}
