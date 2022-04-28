package com.example.redsocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Registro extends AppCompatActivity {
    EditText Correo,Password,Nombres,Apellidos,Telefono;
    Button RegistrarUsuario;

    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null; //Afirmamos que el titulo no es nulo
        actionBar.setTitle("Resgistro");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Correo = findViewById(R.id.Correo);
        Password = findViewById(R.id.Password);
        Nombres = findViewById(R.id.Nombres);
        Apellidos = findViewById(R.id.Apellidos);
        Telefono = findViewById(R.id.Telefono);

        RegistrarUsuario = findViewById(R.id.RegistrarBtn);

        firebaseAuth = FirebaseAuth.getInstance();

        RegistrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = Correo.getText().toString();
                String pass = Password.getText().toString();

                //validamos
                if(!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
                    Correo.setError("Correo no valido");
                    Correo.setFocusable(true);
                }else if (pass.length()<6){
                    Password.setError("La contraseña debe contener mas de 6 caracteres");
                    Password.setFocusable(true);
                }else{
                    Registrar(correo,pass);
                }
            }
        });
    }

    //REGISTRAR USUARIO
    private void Registrar(String correo, String pass){
        firebaseAuth.createUserWithEmailAndPassword(correo,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            //UID
                            assert user !=null;
                            String uid = user.getUid();

                            String correo = Correo.getText().toString();
                            String pass = Password.getText().toString();
                            String nombres = Nombres.getText().toString();
                            String apellidos = Apellidos.getText().toString();
                            String telefono = Telefono.getText().toString();


                            //CREAMOS UN HASHMAP PARA MANDAR LOS DATOS A FIREBASE
                            HashMap<Object,String> DatosUsuario = new HashMap<>();
                            DatosUsuario.put("uid",uid);
                            DatosUsuario.put("correo",correo);
                            DatosUsuario.put("pass",pass);
                            DatosUsuario.put("nombres",nombres);
                            DatosUsuario.put("apellidos",apellidos);
                            DatosUsuario.put("telefono",telefono);
                            DatosUsuario.put("Imagen","");

                            //INICIALIZAMOS LA INSTANCIA A LA BASE DE DATOS
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("USUARIOS_APP");

                            reference.child(uid).setValue(DatosUsuario);
                            Toast.makeText(Registro.this, "SE REGISTRÓ EXITOSAMENTE", Toast.LENGTH_SHORT).show();
                            //UNA VEZ REGISTRADO NOS MANDA A INICIO
                            startActivity(new Intent(Registro.this,Inicio.class));
                        }else{
                            Toast.makeText(Registro.this, "Algo ha salido mal", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Registro.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*//HABILITAMOS LA ACCION PARA RETROCEDER
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }*/
}