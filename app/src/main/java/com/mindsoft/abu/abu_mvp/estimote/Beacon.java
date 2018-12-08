package com.mindsoft.abu.abu_mvp.estimote;

import java.util.Date;
import java.util.Map;

public class Beacon {

    private String title;
    private String id;
    private Date fechaLectura;
    private Map<String,String> attachments;

    public Beacon(String title, String id,Date fecha,Map<String,String> attachment) {
        this.title = title;
        this.id = id;
        this.fechaLectura=fecha;
        this.attachments=attachment;
    }
   public Beacon() {

    }

    public String getTitle() {
        return title;
    }

    public String getID(){ return id;}

    public Date getFecha() {
        return fechaLectura;
    }

    public Map<String,String> getAttachments(){return attachments;}
}
