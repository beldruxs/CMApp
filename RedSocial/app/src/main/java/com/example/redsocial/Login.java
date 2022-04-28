package com.example.redsocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    EditText CorreoLogin, Passwordlogin;
    Button INGRESAR;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null; //Afirmamos que el titulo no es nulo
        actionBar.setTitle("Login");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        CorreoLogin = findViewById(R.id.CorreoLogin);
        Passwordlogin = findViewById(R.id.Passwordlogin);
        INGRESAR = findViewById(R.id.INGRESAR);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        //ASIGNAMOS UN EVENTO AL BOTON INGRESAR
        INGRESAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CONVERTIMOS A STRING EL CORREO Y CONTRASEÑA
                String correo = CorreoLogin.getText().toString();
                String pass = Passwordlogin.getText().toString();

                if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
                    CorreoLogin.setError("Correo inválido");
                    CorreoLogin.setFocusable(true); // Activar el modo de enfoque de la vista
                }else if(pass.length()<6){
                    Passwordlogin.setError("La contraseña debe tener más de 6 caracteres");
                    Passwordlogin.setFocusable(true);
                }else{
                    LOGINUSUARIO(correo,pass);
                }
            }
        });

    }

    private void LOGINUSUARIO(String correo, String pass) {
        progressDialog.setCancelable(false);
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(correo,pass)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss(); // El progress se cierra
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    //Cuando iniciemos sesion nos manda al Inicio

                    startActivity(new Intent(Login.this,Inicio.class));
                    assert user != null; //Afirmamos que el usuario no es nulo,obtenemos su correo electrónico
                    Toast.makeText(Login.this, "Hola! Bienvenid@" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    finish();

                }else{
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "Algo ha salido mal", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //CREAMOS UN MENSAJE PERSONALIZADO

    /*//HABILITAMOS LA ACCION PARA RETROCEDER
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }*/
}