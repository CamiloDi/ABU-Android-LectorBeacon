package com.mindsoft.abu.abu_mvp.estimote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


import com.mindsoft.abu.abu_mvp.MainActivity;

import com.mindsoft.abu.abu_mvp.Entidades.BeaconSQLITE;
import com.mindsoft.abu.abu_mvp.SQLite.ConexionSQLiteHelper;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.eliminarTablaSQLITE;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.hayBeaconsSQLite;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.registrarBeacon;

import static com.mindsoft.abu.abu_mvp.API_Rest.ServiceAPI.EnviaAlerta;
import static com.mindsoft.abu.abu_mvp.API_Rest.ServiceAPI.guardarBeacon;
import static com.mindsoft.abu.abu_mvp.API_Rest.ServiceAPI.guardarBeacons;



//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class ProximityContentManager {
    public boolean registroStatus = false;
    static boolean errored = false;


    //region Valores SQLite

    public static String bdBeacon = "bd_beacons_sqlite";
    public static ConexionSQLiteHelper conn;
    public Beacon beacon;
    public BeaconSQLITE[] beacons;
    public int usuario=1;
    private Notification notification;
    private int notificationId = 1;

    //endregion
    //region Timer
    public int milisegundosAlerta=300000;
    public Timer enviarAlerta = new Timer();

    //endregion

    private Context context;
    private ProximityContentAdapter proximityContentAdapter;
    private EstimoteCloudCredentials cloudCredentials;
    private ProximityObserver.Handler proximityObserverHandler;
    private NotificationManager notificationManager;


    public ProximityContentManager(Context context, ProximityContentAdapter proximityContentAdapter, EstimoteCloudCredentials cloudCredentials) {
        this.context = context;
        this.proximityContentAdapter = proximityContentAdapter;
        this.cloudCredentials = cloudCredentials;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void start() {

        ProximityObserver proximityObserver = new ProximityObserverBuilder(context, cloudCredentials)
                .onError(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Log.e("app", "proximity observer error: " + throwable);
                        return null;
                    }
                })
                .withBalancedPowerMode()
                .build();

        ProximityZone zone = new ProximityZoneBuilder()
                .forTag("camilodiazsev-gmail-com-s--f5t")
                .inCustomRange(3.0)
                .onContextChange(new Function1<Set<? extends ProximityZoneContext>, Unit>() {
                    @Override
                    public Unit invoke(Set<? extends ProximityZoneContext> contexts) {

                        List<ProximityContent> nearbyContent = new ArrayList<>(contexts.size());

                        for (ProximityZoneContext proximityContext : contexts) {
                            String title = proximityContext.getAttachments().get("camilodiazsev-gmail-com-s--f5t/title");
                            if (title == null) {
                                title = "unknown";
                            }
                            String subtitle = Utils.getShortIdentifier(proximityContext.getDeviceId());

                            nearbyContent.add(new ProximityContent(title, subtitle));
                        }

                        //proximityContentAdapter.setNearbyContent(nearbyContent);
                        proximityContentAdapter.notifyDataSetChanged();

                        return null;
                    }
                })
                .build();

        proximityObserverHandler = proximityObserver.startObserving(zone);
    }

    private Notification buildNotification(String title, String text,int icon) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(contentChannel);
        }

        return new NotificationCompat.Builder(this.context, "content_channel")
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    public ProximityContentAdapter ObtengoBeacon() {

        ProximityObserver proximityObserver = new ProximityObserverBuilder(context, cloudCredentials)
                .onError(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Log.e("app", "proximity observer error: " + throwable);
                        return null;
                    }
                })
                .withBalancedPowerMode()
                .build();

        ProximityZone zone = new ProximityZoneBuilder()
                .forTag("camilodiazsev-gmail-com-s--f5t")
                .inCustomRange(3.0)
                .onContextChange(new Function1<Set<? extends ProximityZoneContext>, Unit>() {
                    @Override
                    public Unit invoke(Set<? extends ProximityZoneContext> contexts) {

                        List<Beacon> nearbyContent = new ArrayList<>(contexts.size());

                        for (ProximityZoneContext proximityContext : contexts) {
                            String nombre = proximityContext.getAttachments().get("camilodiazsev-gmail-com-s--f5t/title");
                            if (nombre == null) {
                                nombre = "unknown";
                            }


                            Map<String,String> attachments = proximityContext.getAttachments();
                            String id = proximityContext.getDeviceId();
                            Beacon beaconLeido = new Beacon(nombre, id,new Date(),attachments);
                            nearbyContent.add(beaconLeido);
                        }
                        proximityContentAdapter.setNearbyContent(nearbyContent);
                        proximityContentAdapter.notifyDataSetChanged();
                        if(enviarAlerta!=null && proximityContentAdapter.getBeacons().size()>0) {
                            enviarAlerta.cancel();
                            enviarAlerta.purge();
                            enviarAlerta=null;
                            Log.d("ALERTA","Se cancelo el envio de la alerta");
                            enviarAlerta=new Timer();
                        }

                        //SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        //String fecha = sf.format(beacon.getFecha());
                        
                        //notification=buildNotification("Beacon", "Nombre:"+beacon.getTitle()+" - Fecha:"+fecha,android.R.drawable.ic_dialog_info);
                        //notificationManager.notify(notificationId, notification);
                        conn = new ConexionSQLiteHelper(context, bdBeacon, null, 1); ;
                        if(proximityContentAdapter.getBeacons().size()>0){
                            beacon = proximityContentAdapter.getBeacons().get(0);

                            Log.d("ALERTA","Se enviara la alerta en 20 segundos más: "+beacon.getFecha().getSeconds());
                            enviarAlerta.schedule(generarAlerta(),milisegundosAlerta);

                            if(conectado()){
                                beacons=hayBeaconsSQLite(conn);
                                if(beacons!=null){
                                    AsyncCallWsEnvioListaBeacons task = new AsyncCallWsEnvioListaBeacons();
                                    task.execute();
                                }else{
                                    AsyncCallWsRegistro task = new AsyncCallWsRegistro();
                                    task.execute();
                                }


                            }else{

                                registrarBeacon(beacon,conn,usuario);
                            }
                        }

                        return null;
                    }
                })
                .build();

        proximityObserverHandler = proximityObserver.startObserving(zone);
        //LeoBeacon();
        return proximityContentAdapter;
    }

    public void stop() {
        proximityObserverHandler.stop();
    }


    //region Registro 1 Beacon
    private class AsyncCallWsRegistro extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.i("AsyncTask", "onPreExecute");
        }
        @Override
        protected Void doInBackground(Void... params) {

            try {
                registroStatus =guardarBeacon(beacon,usuario);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        //Once WebService returns response
        protected void onPostExecute(Void result) {

            if (!errored) {
                if (registroStatus!=false) {                }
            }
            errored = false;
        }
    }
    //endregion

    //region Registro lista de Beacons
    private class AsyncCallWsEnvioListaBeacons extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                registroStatus = guardarBeacons(beacons);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        //Once WebService returns response
        protected void onPostExecute(Void result) {

            if (!errored) {

                if (registroStatus!=false) {
                    eliminarTablaSQLITE(conn);
                    //Navigate to Home Screen
                    AsyncCallWsRegistro task = new AsyncCallWsRegistro();
                    task.execute();
                }
            }
            errored = false;
        }
    }
    //endregion

    //region Enviar Alerta
    private class AsyncCallWSEnviarAlerta extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.i("AsyncTask", "onPreExecute");
        }
        @Override
        protected Void doInBackground(Void... params) {

            try {
                registroStatus =EnviaAlerta(beacon,usuario);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        //Once WebService returns response
        protected void onPostExecute(Void result) {

            if (!errored) {
                if (registroStatus!=false) {

                    Log.d("ALERTA","Se envio la alerta");
                    notification=buildNotification("Alerta!", "Se ha enviado una alerta por inactividad!",android.R.drawable.ic_dialog_alert);
                    notificationManager.notify(notificationId, notification);
                }else{
                    Log.d("ALERTA","Se cancelo el por error del servicio");
                }
            }else{
                Log.d("ALERTA","ocurrio un error");
            }
            errored = false;
        }
    }
    //endregion

    //region Conectado?
    public boolean conectado(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }
    //endregion

    //region generaAlerta
    public TimerTask generarAlerta(){
       return new TimerTask() {
            @Override
            public void run() {
                Log.d("ALERTA","Se enviara una alerta : "+new Date().getSeconds());
                AsyncCallWSEnviarAlerta task = new AsyncCallWSEnviarAlerta();
                task.execute();
            }
        };
    }
    //endregion
}
