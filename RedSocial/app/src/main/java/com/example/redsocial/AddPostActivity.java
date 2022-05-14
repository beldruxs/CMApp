package com.example.redsocial;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar;

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    FirebaseUser firebaseUser;

    //Permisos
    private static final int CAMARA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //imagen pick constantes
    private static final int IMAGE_PICK_CAMARA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //Permisos arrays
    String[] cameraPermissions;
    String[] storagePermissions;

    //vistas
    EditText titleEt,descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    //informacion usuario
    String name,email,uid,dp;

    //Imagen escogida
    Uri image_uri = null;

    //progreso
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Añadir Nueva Publicación");
        //para volver atrás
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //iniciamos los permisos array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        VerificacionInicioSesion();

        actionBar.setSubtitle(email);

        //obtenemos informacion del usuario para incluirlo en el post
        userDbRef = FirebaseDatabase.getInstance().getReference("USUARIOS_APP");
        Query query = userDbRef.orderByChild("correo").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    name = "" + ds.child("nombres").getValue();
                    email = "" + ds.child("correo").getValue();
                    dp = "" + ds.child("Imagen").getValue();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //inicializamos vistas
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);

        //obtenemos la imagen de la galeria o camara(se intenta)

        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        //Listener boton upload

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //obtenemos data del editText
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Escribe un titulo", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Escribe una descripción", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(image_uri==null){
                    //publicar sin imagen
                    uploadData(title,description,"noImage");
                }else{
                    uploadData(title,description,String.valueOf(image_uri));
                }

            }
        });
    }

    private void uploadData(String title, String description, String uri) {
        pd.setMessage("Publicando...");
        pd.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timeStamp;
        if(!uri.equals("noImage")){
            //colgamos la foto con imagen
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri))
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //se ha subido la imagen a la base de datos
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());

                    String downloadUri = uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        //url se ha recibido, la subimos a la base de datos
                        HashMap<Object,String> hashMap = new HashMap<>();
                        //put post info
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uDp", dp);
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title);
                        hashMap.put("pDescripcion", description);
                        hashMap.put("pImage", downloadUri);

                        hashMap.put("pTime", timeStamp);
                        //ruta
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        //ponemos los datos en la referencia
                        ref.child(timeStamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //añadida a la base de datos
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, "Publicado", Toast.LENGTH_SHORT).show();
                                        //reseteamos las vistas
                                        titleEt.setText("");
                                        descriptionEt.setText("");
                                        imageIv.setImageURI(null);
                                        image_uri = null;
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //fallo añadiendo la publicacion en la base de datos
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            HashMap<Object,String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescripcion", description);
            hashMap.put("pImage", "noImage");

            hashMap.put("pTime", timeStamp);
            //ruta
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //ponemos los datos en la referencia
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //añadida a la base de datos
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Publicado", Toast.LENGTH_SHORT).show();
                            //reseteamos las vistas
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_uri = null;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //fallo añadiendo la publicacion en la base de datos
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }
    }

    private void showImagePickDialog() {
        String[] options = {"Camara","Galeria"};

        //dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escoge Imagen de");
        //aplicamos las opciones al dialogo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // el click de los items
                if(i == 0){
                    //Camara se ha clicado
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }

                }if(i==1){
                    //galeria
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }

                }
            }
        });
        //creamos y enseñamos el dialogo
        builder.create().show();
    }


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    String title = titleEt.getText().toString().trim();
                    String description = descriptionEt.getText().toString().trim();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {

                            // There are no request codes
                            Intent data = result.getData();
                            image_uri = data.getData();
                            Picasso.get().load(image_uri).into(imageIv);
                        }catch (Exception e){

                        }

                    }
                }
            });


    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        //someActivityResultLauncher.launch(galleryIntent);
        someActivityResultLauncher.launch(galleryIntent);


    }

    private void pickFromCamera() {

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Elección temporal");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Descripción temporal");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMARA_CODE);


    }

    private boolean checkStoragePermission(){
        //comprobamos si el permiso está disponible o no
        //devolvemos true si lo está
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //solicitamos el permiso galeria
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);

    }


    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMARA_REQUEST_CODE);

    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //ir a la actividad anterior
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        VerificacionInicioSesion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificacionInicioSesion();
    }

    private void VerificacionInicioSesion(){
        firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            email = firebaseUser.getEmail();
            uid = firebaseUser.getUid();

        }
        //Caso contrario nos dirige al main activity
        else{
            startActivity((new Intent(this,MainActivity.class)));
            finish();
        }
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

    //controlamos los resultados de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMARA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //tenemos ambos permisos
                        pickFromCamera();
                    }else{
                        Toast.makeText(this, "Camara y Galeria necesitan permisos", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //tenemos permisos
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, "Galeria necesita permisos", Toast.LENGTH_SHORT).show();
                    }
                }else{

                }
            }
            break;
        }

    }




}