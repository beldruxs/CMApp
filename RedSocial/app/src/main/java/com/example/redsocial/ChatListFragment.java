package com.example.redsocial;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatListFragment newInstance(String param1, String param2) {
        ChatListFragment fragment = new ChatListFragment();
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
        View view  = inflater.inflate(R.layout.fragment_chat_list, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        return view;
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