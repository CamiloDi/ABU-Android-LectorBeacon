package com.mindsoft.abu.abu_mvp.SQLite;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;

import com.mindsoft.abu.abu_mvp.Base.Constantes;
import com.mindsoft.abu.abu_mvp.Entidades.BeaconSQLITE;
import com.mindsoft.abu.abu_mvp.estimote.Beacon;


import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsultasSQLite {
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static boolean eliminarTablaSQLITE(ConexionSQLiteHelper conn){
        try {

            SQLiteDatabase db = conn.getWritableDatabase();
            String[] datosEliminar = new String[0];

            db.delete(Constantes.TABLA_BEACON, "", null);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static BeaconSQLITE[] hayBeaconsSQLite(ConexionSQLiteHelper conn){

        SQLiteDatabase db = conn.getReadableDatabase();
        BeaconSQLITE[] beacons ;
        try{
            Cursor cursor = db.query(Constantes.TABLA_BEACON,null,null,null,null,null,null,null);;

            int cant = cursor.getCount();
            if(cant>0){
                cursor.moveToFirst();
                beacons= new BeaconSQLITE[cant];
                for (int i=0;i<cant;i++){
                    Date fechaBeacon=sdf.parse(cursor.getString(2));
                    beacons[i]=new BeaconSQLITE(cursor.getString(1),cursor.getString(0),fechaBeacon,cursor.getString(3));
                    cursor.moveToNext();
                }

                cursor.close();
                return beacons;

            }else{
                cursor.close();
                beacons=null ;
                return beacons;
            }
        }catch (Exception e){
            beacons=null ;
            return beacons;
        }

    }
    public static  boolean registrarBeacon(Beacon beaconARegistrar, ConexionSQLiteHelper conn ,String usuario){

        SQLiteDatabase db = conn.getWritableDatabase();
        long idResultante=0;
        boolean resultado=false;
        try{


            ContentValues values = new ContentValues();

            String beaconDate = sdf.format(beaconARegistrar.getFecha());

            values.put(Constantes.CAMPO_ID,beaconARegistrar.getID());
            values.put(Constantes.CAMPO_NOMBRE,beaconARegistrar.getTitle());
            values.put(Constantes.CAMPO_FECHA,beaconDate);
            values.put(Constantes.CAMPO_USUARIO,usuario);

            idResultante = db.insert(Constantes.TABLA_BEACON,Constantes.CAMPO_ID,values);
            if(idResultante!=0){
                resultado=true;
            }
        }catch (Exception e){
            String Error=e.getMessage();

        }
        db.close();
        return resultado;
    }
}
