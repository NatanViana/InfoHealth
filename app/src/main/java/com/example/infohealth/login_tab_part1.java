package com.example.infohealth;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;


public class login_tab_part1 extends Fragment {
    TextView email_text;
    TextView password_text;

    TextView esqueceusuasenha;
    LottieAnimationView lottie_animation;
    Button login;
    float v = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.login_tab_part1, container, false);

        lottie_animation = root.findViewById(R.id.lottie);
        lottie_animation.playAnimation();
        lottie_animation.loop(true);

        email_text = root.findViewById(R.id.email);
        password_text = root.findViewById(R.id.password);
        esqueceusuasenha = root.findViewById(R.id.esqueciminhasenha);
        login = root.findViewById(R.id.login_button);

        email_text.setTranslationX(800);
        password_text.setTranslationX(800);
        esqueceusuasenha.setTranslationX(800);
        login.setTranslationX(800);

        email_text.setAlpha(v);
        password_text.setAlpha(v);
        esqueceusuasenha.setAlpha(v);
        login.setAlpha(v);

        email_text.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(200).start();
        password_text.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        esqueceusuasenha.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(600).start();
        login.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();

        esqueceusuasenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(email_text.getText().toString().isEmpty() || email_text.getText().toString()==null){
                    exibirSnackbar(root,"Preencha um email válido antes de clicar para recuperar a sua senha!", 0);
                }
                else{
                    boolean isValid = isValidEmail(email_text.getText().toString());
                    if(isValid){

                        FirebaseAuth.getInstance().sendPasswordResetEmail(email_text.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            exibirSnackbar(root, "Envio de recuperação de senha enviado com sucesso para seu email! Por favor check seu email"+email_text.getText().toString(),1);
                                        } else {
                                            exibirSnackbar(root, "Errom de envio de recuperação de senha para seu email! ",2);

                                        }
                                    }
                                });
                    }
                    else{

                        exibirSnackbar(root,"Preencha um email válido antes de clicar para recuperar a sua senha!", 0);
                    }

                }



            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviar();
            }
        });
        return root;
    }

    public boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();


    }

    public void exibirSnackbar(View rootView, String mensagem, int cor) {
        Snackbar snackbar = Snackbar.make(rootView, mensagem, Snackbar.LENGTH_SHORT);

        // Definir cor de fundo personalizada
        if(cor==0){
            int backgroundColor = getResources().getColor(R.color.roxo_app);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));

        }
        else if(cor==1){
            int backgroundColor = getResources().getColor(R.color.green);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
            int textColor = getResources().getColor(R.color.roxo_app);
            snackbar.setTextColor(textColor);

        }
        else{
            int backgroundColor = getResources().getColor(R.color.pink_vivo);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));

        }


        snackbar.show();
    }

    public void enviar(){

        String email = email_text.getText().toString();
        String password = password_text.getText().toString();

        Intent intentEnviadora = new Intent(getContext(),login.class);
        Bundle parametros = new Bundle();

        parametros.putString("email",email);
        parametros.putString("password",password);
        parametros.putString("option","sign_in");
        intentEnviadora.putExtras(parametros);

        startActivity(intentEnviadora);

    }


}
