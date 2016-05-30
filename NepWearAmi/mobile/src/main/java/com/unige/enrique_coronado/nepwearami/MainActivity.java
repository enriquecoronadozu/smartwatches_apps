package com.unige.enrique_coronado.nepwearami;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CheckBox;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.net.UnknownHostException;

import java.io.*;

public class MainActivity extends Activity implements SensorEventListener {

    Socket client = null;
    PrintStream oos = null;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private CheckBox checkBoxData;
    private CheckBox checkBoxS;

    ArrayList<String> toRec = new ArrayList<String>();

    String lastMessageMov = "";


    int count  =0;
    long time_mov = 0;
    float[] valori = {0,0,0};
    float bpm = 0;

    String type_mov = "";

    int i = 0;
    int l = 0;

    String ipaddress ="130.251.13.125";
    int port = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBoxData = (CheckBox) findViewById(R.id.checkBoxData);
        checkBoxS = (CheckBox) findViewById(R.id.checkBoxS);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();

        Wearable.MessageApi.addListener(googleApiClient, new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {

            }
        });

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

    }

    public void buttonClick(View view) throws IOException {
        if(client!=null) {
            client.close();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }



    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String message = intent.getStringExtra("message");


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if(client==null || client.isClosed()) {
                            EditText editText = (EditText) findViewById(R.id.ipText);
                            ipaddress = editText.getText().toString();

                            client = new Socket(ipaddress,port);
                            oos = new PrintStream(client.getOutputStream());
                        }

                        if (checkBoxS.isChecked()) {
                            oos.println(message);//message);
                        }
                        else
                        {
                            oos.println("a;000;0.0;0.0;0.0");
                        }
                        oos.flush();


                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

            //((TextView) findViewById(R.id.dato)).setText(message);


            if(intent.getStringExtra("type")=="1") {

                StringTokenizer info = new StringTokenizer(message, "\n");

                while (info.hasMoreTokens()) {
                    toRec.add(info.nextToken());
                }

                count++;

                lastMessageMov = toRec.get(0);

                StringTokenizer value = new StringTokenizer(lastMessageMov, ";");

                type_mov = value.nextToken();

                time_mov = Long.valueOf(value.nextToken());


                for (i = 0; i < 3; i++) {
                    valori[i] = Float.valueOf(value.nextToken());
                }

                if (checkBoxData.isChecked()) {
                    //Mostro solo i valori dell'accelerometro, ma posso mostrare quello che voglio (in valori[] ho tutti i dati)
                    ((TextView) findViewById(R.id.dato)).setText("IP :" + ipaddress + "\n" + "port :" + port + "\n" + "x: " + valori[0] + "\n" + "y: " + valori[1] + "\n" + "z: " + valori[2]);
                }

                for (l = 0; l < 3; l++) {
                    valori[l] = 0;
                }
                toRec.clear();
            }
        }
    }
}