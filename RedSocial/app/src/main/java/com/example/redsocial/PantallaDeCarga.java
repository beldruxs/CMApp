package com.example.redsocial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class PantallaDeCarga extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_de_carga);

        //Esto se representa en segundos, que demorará la pantalla de carga
        final int Duracion = 2500;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Esto se ejecutará pasados los segundos
                Intent intent = new Intent(PantallaDeCarga.this, MainActivity.class);
                startActivity(intent);
                //Nos dirige de ésta actividad, al mainAvctivity
            }
        },Duracion);
    }
}