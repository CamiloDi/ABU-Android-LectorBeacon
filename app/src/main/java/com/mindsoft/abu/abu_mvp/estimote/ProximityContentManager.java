package com.mindsoft.abu.abu_mvp.estimote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import com.mindsoft.abu.abu_mvp.Entidades.BeaconSQLITE;
import com.mindsoft.abu.abu_mvp.MainActivity;
import com.mindsoft.abu.abu_mvp.SQLite.ConexionSQLiteHelper;

import static com.mindsoft.abu.abu_mvp.API_Rest.ServiceAPI.guardarBeacon;
import static com.mindsoft.abu.abu_mvp.API_Rest.ServiceAPI.guardarBeacons;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.eliminarTablaSQLITE;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.hayBeaconsSQLite;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.registrarBeacon;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class ProximityContentManager {
    public boolean registroStatus = false;
    static boolean errored = false;
    private MainActivity ma = new MainActivity();



    //region Valores SQLite

    public static String bdBeacon = "bd_beacons_sqlite";
    public static ConexionSQLiteHelper conn;
    public Beacon beacon;
    public BeaconSQLITE[] beacons;
    public String usuario="UsuarioPrueba";


    //endregion

    private Context context;
    private ProximityContentAdapter proximityContentAdapter;
    private EstimoteCloudCredentials cloudCredentials;
    private ProximityObserver.Handler proximityObserverHandler;

    public ProximityContentManager(Context context, ProximityContentAdapter proximityContentAdapter, EstimoteCloudCredentials cloudCredentials) {
        this.context = context;
        this.proximityContentAdapter = proximityContentAdapter;
        this.cloudCredentials = cloudCredentials;
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

                        beacon = proximityContentAdapter.getBeacons().get(0);
                        conn = new ConexionSQLiteHelper(context, bdBeacon, null, 1); ;
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
    private class AsyncCallWsRegistro extends AsyncTask<String, Void, Void> {
        String x ="";

        @Override
        protected Void doInBackground(String... params) {
            try {
                registroStatus = guardarBeacon(beacon,usuario);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        //Once WebService returns response
        protected void onPostExecute(Void result) {

            //Error status is false
            if (!errored) {
                //Based on Boolean value returned from WebService

                if (registroStatus!=false) {
                    //Navigate to Home Screen


                } else {
                    //Set Error message


                }
                //Error status is true
            } else {

            }
            //Re-initialize Error Status to False
            errored = false;
        }
    }
    //endregion

    //region Registro lista de Beacons
    private class AsyncCallWsEnvioListaBeacons extends AsyncTask<String, Void, Void> {
        String x ="";
        @Override
        protected Void doInBackground(String... params) {
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

            //Error status is false
            if (!errored) {
                //Based on Boolean value returned from WebService

                if (registroStatus!=false) {
                    eliminarTablaSQLITE(conn);
                    //Navigate to Home Screen
                    AsyncCallWsRegistro task = new AsyncCallWsRegistro();
                    task.execute();

                } else {
                    //Set Error message


                }
                //Error status is true
            } else {

            }
            //Re-initialize Error Status to False
            errored = false;
        }
    }
    //endregion

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
}
