package com.mindsoft.abu.abu_mvp.Entidades;

import java.util.Date;


public class BeaconSQLITE {

    private String title;
    private String id;
    private Date fechaLectura;
    private int Usuario;

    public BeaconSQLITE(String title, String id,Date fecha,int usu) {
        this.setTitle(title);
        this.setId(id);
        this.setFechaLectura(fecha);
        this.setUsuario(usu);

    }
    public BeaconSQLITE() {

    }

    public String getTitle() {
        return title;
    }

    public String getID() {
        return id;
    }

    public Date getFecha() {
        return fechaLectura;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFechaLectura(Date fechaLectura) {
        this.fechaLectura = fechaLectura;
    }

    public int getUsuario() {
        return Usuario;
    }

    public void setUsuario(int usuario) {
        Usuario = usuario;
    }
}