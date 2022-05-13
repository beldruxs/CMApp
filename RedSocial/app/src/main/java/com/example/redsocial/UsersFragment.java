package com.example.redsocial;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.example.redsocial.adapters.AdapterUsers;
import com.example.redsocial.models.ModellUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModellUser> userList;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    public UsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        //iniciar user list
        userList = new ArrayList<>();
        getAllUsers();
        return view;
    }

    private void getAllUsers() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("USUARIOS_APP");
        //Obtenemos todos los datos de la ruta
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModellUser modellUser = ds.getValue(ModellUser.class);
                    //Obtenemos todos los usuarios salvo el que está logeado
                    if(!modellUser.getUid().equals(firebaseUser.getUid())){
                        userList.add(modellUser);
                    }

                    adapterUsers = new AdapterUsers(getActivity(),userList);
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String s) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("USUARIOS_APP");
        //Obtenemos todos los datos de la ruta
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModellUser modellUser = ds.getValue(ModellUser.class);
                    //Obtenemos todos los usuarios de la busqueda salvo el que está logeado
                    if(!modellUser.getUid().equals(firebaseUser.getUid())){
                        if(modellUser.getNombres().toLowerCase().contains(s.toLowerCase())||
                                modellUser.getCorreo().toLowerCase().contains(s.toLowerCase())){
                            userList.add(modellUser);
                        }

                    }

                    adapterUsers = new AdapterUsers(getActivity(),userList);
                    //Refrescamos
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // Enseñar opciones
        super.onCreate(savedInstanceState);

    }

    private void VerificacionInicioSesion(){
        if(firebaseUser != null){


        }
        //Caso contrario nos dirige al main activity
        else{
            startActivity((new Intent(getActivity(),MainActivity.class)));
            getActivity().finish();
        }
    }

    //menu secundario
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        //escondemos icono addpost de este fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //Buscar
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();

        //Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //se llama cuando se presiona buscar, si no está vacio busca
                if (!TextUtils.isEmpty(s.trim())){
                    //bucamos
                    searchUsers(s);

                }else{
                    //Si está vacio, devuelve todos los usuarios
                    getAllUsers();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Se busca cuando se presiona cualquier letra
                if (!TextUtils.isEmpty(s.trim())){
                    //bucamos
                    searchUsers(s);

                }else{
                    //Si está vacio, devuelve todos los usuarios
                    getAllUsers();

                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
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

