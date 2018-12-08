package com.mindsoft.abu.abu_mvp.API_Rest;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mindsoft.abu.abu_mvp.Entidades.BeaconSQLITE;
import com.mindsoft.abu.abu_mvp.estimote.Beacon;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceAPI {

    private static String URL = "https://api-rest-beacons.herokuapp.com/";


    //region Guardar Beacon
    public static boolean guardarBeacon(Beacon beacon,String usuario) throws UnsupportedEncodingException {
        boolean Status = false;
        //service api url
        String url =URL+"beacon";
        //String url ="http://192.168.1.95:3000/beacon";

        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        HttpResponse response;

        try {

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
            nameValuePairs.add(new BasicNameValuePair("id",beacon.getID()));
            nameValuePairs.add(new BasicNameValuePair("nombre", beacon.getTitle()));
            nameValuePairs.add(new BasicNameValuePair("fecha", beacon.getFecha().toString()));
            nameValuePairs.add(new BasicNameValuePair("usuario", usuario));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs,"UTF-8");

            httpPost.setEntity(entity);

            BasicHttpParams params = new BasicHttpParams();
            httpPost.setParams(params);

            response = client.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                InputStream in = response.getEntity().getContent();
                BufferedReader buffered = new BufferedReader(new InputStreamReader(in));
                StringBuilder fullLines = new StringBuilder();

                String line;

                while ((line = buffered.readLine()) != null) {
                    fullLines.append(line);
                }
                in.close();

                String result = fullLines.toString();
                JSONObject respJSON = new JSONObject(result);
                Status = respJSON.getBoolean("ok");
            }
            else{
                return Status;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Status;

    }
//endregion

    //region Guardar Beacons SQLITE
    public static boolean guardarBeacons(BeaconSQLITE[] beacons) throws UnsupportedEncodingException {
        boolean Status = false;
        //service api url
        String url =URL+"beacons";
        //String url ="http://192.168.1.95:3000/beacons";
        int largo= beacons.length;
        JSONArray jsonArray = new JSONArray();

        for(int i=0;i<largo;i++){
            JSONObject json = new JSONObject();
            beacons[i].toString();
            try {
                json.put("id", beacons[i].getID());
                json.put("nombre" , beacons[i].getTitle());
                json.put("fecha" , beacons[i].getFecha());
                json.put("usuario" , beacons[i].getUsuario());
                jsonArray.put(json);
            }catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

        }



        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        HttpResponse response;

        try {


            JSONObject beaconsJSON =  new JSONObject();
            beaconsJSON.put("beacons",jsonArray);
            beaconsJSON.put("cantidad",largo);
            String beaconsJSONString = beaconsJSON.toString();

            StringEntity stringEntity = new StringEntity(beaconsJSONString);
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);

            BasicHttpParams params = new BasicHttpParams();

            httpPost.setParams(params);
            httpPost.setHeader("Content-Type","application/json");
            httpPost.setHeader("Accept", "application/json");


            response = client.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

                InputStream in = response.getEntity().getContent();
                BufferedReader buffered = new BufferedReader(new InputStreamReader(in));
                StringBuilder fullLines = new StringBuilder();

                String line;

                while ((line = buffered.readLine()) != null) {
                    fullLines.append(line);
                }
                in.close();

                String result = fullLines.toString();
                JSONObject respJSON = new JSONObject(result);
                if(respJSON.getBoolean("ok") && largo==respJSON.getInt("beaconsGuardados")){
                    Status = true;
                }
                else{
                    Status = false;
                }

            }
            else{
                return Status;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Status;

    }
    //endregion


}
