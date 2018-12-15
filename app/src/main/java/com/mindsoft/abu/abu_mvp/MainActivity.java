package com.mindsoft.abu.abu_mvp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.mindsoft.abu.abu_mvp.Base.Constantes;
import com.mindsoft.abu.abu_mvp.Entidades.BeaconSQLITE;
import com.mindsoft.abu.abu_mvp.SQLite.ConexionSQLiteHelper;
import com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite;
import com.mindsoft.abu.abu_mvp.estimote.Beacon;
import com.mindsoft.abu.abu_mvp.estimote.ProximityContentAdapter;
import com.mindsoft.abu.abu_mvp.estimote.ProximityContentManager;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static com.mindsoft.abu.abu_mvp.API_Rest.ServiceAPI.guardarBeacon;
import static com.mindsoft.abu.abu_mvp.API_Rest.ServiceAPI.guardarBeacons;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.eliminarTablaSQLITE;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.hayBeaconsSQLite;
import static com.mindsoft.abu.abu_mvp.SQLite.ConsultasSQLite.registrarBeacon;

public class MainActivity extends AppCompatActivity {

    private Button leerBeacon,hayInternet;
    public TextView txtNombre, txtId,txtFecha;
    public boolean registroStatus = false;
    static boolean errored = false;
    public int usuario=1;

    private Notification notification;
    private int notificationId = 1;



    public Notification testNotification;
    //region Valores Estimote

    public static final String appId="camilodiazsev-gmail-com-s--f5t",appToken="0c33811cb299dd6eb88bb9a5c3e81d56";
    public EstimoteCloudCredentials cloudCredentials = new EstimoteCloudCredentials(appId, appToken);
    private ProximityContentManager proximityContentManager;
    private ProximityContentAdapter proximityContentAdapter;

    //endregion

    //region Valores SQLite
    public static ConexionSQLiteHelper conn ;
    public static String bdBeacon = "bd_beacons_sqlite";
    public Beacon beacon;
    public BeaconSQLITE[] beacons;

    //endregion




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNombre = (TextView) findViewById(R.id.txtNombre);
        txtId = (TextView) findViewById(R.id.txtID);
        txtFecha = (TextView) findViewById(R.id.txtFecha);

        leerBeacon = (Button) findViewById(R.id.btnLeerBeacon);
        hayInternet = (Button) findViewById(R.id.btnInternet);
        conn = new ConexionSQLiteHelper(this, bdBeacon, null, 1);
        proximityContentAdapter = new ProximityContentAdapter(this);

        leerBeacon.setOnClickListener(new View.OnClickListener() {public void onClick(View v) {
            beacons=hayBeaconsSQLite(conn);
            if(beacons!=null) {
                Toast.makeText(getBaseContext(), "Hay " + beacons.length + " beacons guardados!", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "No hay beacons guardados!", Toast.LENGTH_SHORT).show();
            }
            }
        });

        hayInternet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                proximityContentManager.enviarAlerta.cancel();
                proximityContentManager.enviarAlerta.purge();
            }
        });
        //region leer beacon

        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(this,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                //startProximityContentManager();
                                obtengoBeacon();

                                return null;
                            }
                        },
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                Log.e("app", "requirements missing: " + requirements);
                               obtengoBeacon();
                                return null;
                            }
                        },
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "requirements error: " + throwable);
                                return null;
                            }
                        });

        //endregion


    }


    private void obtengoBeacon() {
        proximityContentManager = new ProximityContentManager(this,proximityContentAdapter,cloudCredentials);
        proximityContentManager.ObtengoBeacon();
        //leerBeacon();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (proximityContentManager != null)
            proximityContentManager.stop();
    }



    public void leerBeacon(){
        try {

            if(proximityContentAdapter.getNearbyContent().size()==0){
                txtNombre.setText("No he encontrado un Beacon!");
                txtId.setText("");
                txtFecha.setText("");
            }else{
                beacon = proximityContentAdapter.getBeacons().get(0);
                txtNombre.setText(beacon.getTitle());
                txtId.setText(beacon.getID());
                txtFecha.setText(beacon.getFecha().toString());
                if(conectado()){
                    Toast.makeText(getBaseContext(), "Conectado!,Enviando datos...", Toast.LENGTH_SHORT).show();
                    AsyncCallWsRegistro task = new AsyncCallWsRegistro();
                    task.execute();

                }else{
                    if(registrarBeacon(beacon,conn,usuario)){
                        Toast.makeText(getBaseContext(), "se ha guardado beacon, se enviara al encontrar internet", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getBaseContext(), "error al guardar beacon", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (Exception e){

            //Toast.makeText(getBaseContext(), "No he encontrado un Beacon! :(", Toast.LENGTH_SHORT).show();
            txtNombre.setText(e.getMessage());
            txtId.setText("");
            txtFecha.setText("");
        }

    }





    //region Registro 1 Beacon
    private class AsyncCallWsRegistro extends AsyncTask<String, Void, Void> {
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
                    Toast.makeText(getBaseContext(), "Datos Enviados!", Toast.LENGTH_SHORT).show();

                } else {
                    //Set Error message
                    Toast.makeText(getBaseContext(), "Problemas al enviar datos!", Toast.LENGTH_SHORT).show();

                }
                //Error status is true
            } else {
                Toast.makeText(getBaseContext(), "Fallo el Registro, Intente nuevamente", Toast.LENGTH_SHORT).show();
            }
            //Re-initialize Error Status to False
            errored = false;
        }

    }
    //endregion

    //region Registro lista de Beacons
    private class AsyncCallWsEnvioListaBeacons extends AsyncTask<String, Void, Void> {
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

                    //Navigate to Home Screen
                    Toast.makeText(getBaseContext(), "Datos Enviados!", Toast.LENGTH_SHORT).show();
                    if(eliminarTablaSQLITE(conn)){
                        Toast.makeText(getBaseContext(), "Tabla Eliminada", Toast.LENGTH_SHORT).show();
                    }{
                        Toast.makeText(getBaseContext(), "Tabla no Eliminada", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    //Set Error message
                    Toast.makeText(getBaseContext(), "Problemas al enviar datos!", Toast.LENGTH_SHORT).show();

                }
                //Error status is true
            } else {
                Toast.makeText(getBaseContext(), "Fallo el Envio, Intente nuevamente", Toast.LENGTH_SHORT).show();
            }
            //Re-initialize Error Status to False
            errored = false;
        }

    }
    //endregion

    public boolean conectado(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }


}

