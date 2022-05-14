package com.example.redsocial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.redsocial.adapters.AdapterChat;
import com.example.redsocial.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatActivity extends AppCompatActivity {

    //vistas del xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv /*status*/;
    EditText messageText;
    ImageButton sendBtn;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //iniciamos las vistas
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        messageText = findViewById(R.id.messageText);
        sendBtn = findViewById(R.id.sendBtn);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("USUARIOS_APP");

        //buscar usuario para obtener su información
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        //Obtener la foto del usuario y nombre
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    //obtenemos data
                    String name =""+ ds.child("nombres").getValue();
                    hisImage =""+ ds.child("Imagen").getValue();

                    //set data
                    nameTv.setText(name);
                    try{
                        //imagen recibida, la metemos en imageview en toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img).into(profileIv);

                    }
                    catch (Exception e){
                        //enseñamos la imagen por defecto
                        Picasso.get().load(R.drawable.ic_default_img).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Boton de enviar mensaje
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Cogemos el texto del edit text
                String message = messageText.getText().toString().trim();
                //Comprobamos si está vacio o no
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this, "No se puede mandar un mensaje vacio", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendMessage(message);
                }
            }
        });

        readMessages();
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set data al recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();



        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        databaseReference.child("Chats").push().setValue(hashMap);

        //reseteamos el edit text al mandar el mensaje
        messageText.setText("");
    }


    private void VerificacionInicioSesion(){
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            myUid = firebaseUser.getUid();

        }
        //Caso contrario nos dirige al main activity
        else{
            startActivity((new Intent(this,MainActivity.class)));
            finish();
        }
    }

    @Override
    protected void onStart() {
        VerificacionInicioSesion();
        super.onStart();
    }

    protected void onPause() {
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //escondemos el simbolo de buscar ya que no lo necesitamos
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.CerrarSesion){
            firebaseAuth.signOut();
            VerificacionInicioSesion();
        }
        return super.onOptionsItemSelected(item);
    }
}