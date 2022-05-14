package com.example.redsocial.models;

public class ModellUser {
    //usamos mismo nombre que en firebase
    String nombres,correo,search,telefono,Imagen,cover,uid;

    public ModellUser(String nombres, String correo, String telefono, String imagen, String cover, String uid, String search) {
        this.nombres = nombres;
        this.correo = correo;
        this.telefono = telefono;
        Imagen = imagen;
        this.cover = cover;
        this.uid = uid;
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String imagen) {
        Imagen = imagen;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    //Sin esto crashea, porqué?
    public ModellUser(){}
}