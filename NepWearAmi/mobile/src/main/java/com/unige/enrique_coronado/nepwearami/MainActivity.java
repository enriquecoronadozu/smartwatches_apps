/**
 * Android app for transmission of data from a smartwatch to a computer
 *
 * @author Luis Enrique Coronado Zuñiga
 * @date   June, 2016
 */

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
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;


/**
 * Main function
 */
public class MainActivity extends Activity implements SensorEventListener {

    //Client variable
    Socket client = null;
    PrintStream oos = null;

    //Sensor variables
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    //Interface variables
    private CheckBox checkBoxData;
    private CheckBox checkBoxS;
    private CheckBox checkBoxPhone;

    private RadioButton accr;
    private RadioButton gyror;
    private RadioButton stepsr;

    //Variables to measure time
    long init_t = 0;
    long fin_t = 0;
    double elapsedSeconds = 0;
    long tDelta = 0;
    double prom = 0;
    double sum = 0;
    double std = 0;


    float[] acc_value = {0,0,0}; //!< Value where the accelerometer data are stored to show in the screen
    float[] gyro_value = {0,0,0}; //!< Value where the gyroscope data are stored to show in the screen
    float[] step_value = {0}; //!< Value where the steps count  data are stored to show in the screen

    String ipaddress ="192.168.2.1"; //!< IP address variable
    int port = 8080; //!< Port number ex: 8080

    int cont;


    ArrayList<String> toRec = new ArrayList<String>();
    String lastMessageMov = "";
    long time_mov = 0;
    String type_mov = "";
    int i = 0;
    int l = 0;

    Boolean sendData = false;
    Boolean recording = false;
    Boolean saveData = false;
    EditText editText;
    StringTokenizer info;
    StringTokenizer value;
    String message;

    FileOutputStream f;
    PrintStream p;

    @Override
    /**
     * Function that are executed in the beginning of the program
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cont = 0;
        init_t = System.currentTimeMillis();

        //Here the tools of the interface are defined
        checkBoxData = (CheckBox) findViewById(R.id.checkBox);
        checkBoxS = (CheckBox) findViewById(R.id.checkBoxSend);
        checkBoxPhone = (CheckBox) findViewById(R.id.checkBoxPhone);

        accr = (RadioButton) findViewById(R.id.radioButtonAcc);
        gyror = (RadioButton) findViewById(R.id.radioButtonGyro);
        stepsr = (RadioButton) findViewById(R.id.radioButtonSteps);

        //Here the instances to obtain data from sensors are defined
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //An accelerometer sensor is defined
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //New Listener of the accelerometer sensor.
        //SensorManager define the frequency, Options: SENSOR_DELAY_NORMAL, SENSOR_DELAY_NORMAL, SENSOR_DELAY_GAME, SENSOR_DELAY_UI
        senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //For connection with the computer (client side)
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


    /**
     * Use to transfer the data from smartwatch to the pc
     */
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        /**
         *This function is executed when data from the smartwatch are received
         */
        public void onReceive(Context context, Intent intent) {

            message = intent.getStringExtra("message");

            if (saveData && recording) {
                //p.print(message);
                p.print(elapsedSeconds);
                p.print("\n");
            }
            //A new thread is executed (this for no block the main loop)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Socket client connection
                        if(client==null || client.isClosed()) {
                            editText = (EditText) findViewById(R.id.ipText);
                            ipaddress = editText.getText().toString();
                            client = new Socket(ipaddress,port);
                            oos = new PrintStream(client.getOutputStream());
                        }

                        //Send the data of the smartwatch
                        if (recording) {
                            oos.println(message);
                        }
                        //The robot is not allow to move
                        else
                        {
                            //oos.println("a;000;0.0;0.0;0.0");

                        }
                        oos.flush();


                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

            //If data is acceleration, type == 1
            if(intent.getStringExtra("type")=="1") {

                fin_t = System.currentTimeMillis();
                tDelta = fin_t - init_t;
                elapsedSeconds = tDelta;

                sum = sum + elapsedSeconds;
                cont++;
                if(cont==50)
                {
                    prom = sum/50;
                    sum = 0;
                    cont = 0;
                }

                info = new StringTokenizer(message, "\n");
                while (info.hasMoreTokens()) {
                    toRec.add(info.nextToken());
                }

                lastMessageMov = toRec.get(0);
                //Split message
                value = new StringTokenizer(lastMessageMov, ";");
                type_mov = value.nextToken();
                time_mov = Long.valueOf(value.nextToken());

                //Convert values to float
                for (i = 0; i < 3; i++) {
                    acc_value[i] = Float.valueOf(value.nextToken());
                }


                //Show in the screen the data of acceleration
                if (accr.isChecked()&& !checkBoxPhone.isChecked()) {
                    ((TextView) findViewById(R.id.dato)).setText("\n IP :" + ipaddress + ", " + "port :" + port + "\n Acceleration data: \n"  + "x: " + acc_value[0] + "\n" + "y: " + acc_value[1] + "\n" + "z: " + acc_value[2] + " \nFreq:= " + 1000/prom );
                }

                //Reset values (maybe this is not needed)
                for (l = 0; l < 3; l++) {
                    acc_value[l] = 0;
                }
                toRec.clear();
                init_t = fin_t;
            }

            //If data is gyroscope, type == 2
            if(intent.getStringExtra("type")=="2") {

                fin_t = System.currentTimeMillis();
                tDelta = fin_t - init_t;
                elapsedSeconds = tDelta;

                sum = sum + elapsedSeconds;
                cont++;
                if(cont==50)
                {
                    prom = sum/50;
                    sum = 0;
                    cont = 0;
                }

                info = new StringTokenizer(message, "\n");
                while (info.hasMoreTokens()) {
                    toRec.add(info.nextToken());
                }

                lastMessageMov = toRec.get(0);
                //Split message
                value = new StringTokenizer(lastMessageMov, ";");
                type_mov = value.nextToken();
                time_mov = Long.valueOf(value.nextToken());

                //Convert values to float
                for (i = 0; i < 3; i++) {
                    gyro_value[i] = Float.valueOf(value.nextToken());
                }

                //Show in the screen the data of gyroscope data
                if (gyror.isChecked() && !checkBoxPhone.isChecked()) {
                    ((TextView) findViewById(R.id.dato)).setText("\n IP :" + ipaddress + ", " + "port :" + port + "\n  Gyroscope data: \n"  + "x: " + gyro_value[0] + "\n" + "y: " + gyro_value[1] + "\n" + "z: " + gyro_value[2] + " \nFreq:= " + 1/prom );
                }

                //Reset values (maybe this is not needed)
                for (l = 0; l < 3; l++) {
                    gyro_value[l] = 0;
                }
                toRec.clear();
                init_t = fin_t;

            }
        }
    }

    /**
     * Close and reset the connection
     */
    public void buttonClickReset(View view) throws IOException {
        if(client!=null) {
            client.close();
        }
    }

    /**
     * Send save signal
     */
    public void buttonClickStart(View view) throws IOException {
        recording = true;
        checkBoxS.setChecked(true);
        //A new thread is executed (this for no block the main loop)
        if(saveData)
        {
            //Saved in mnt/emulated/0/NepWearami
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentDateandTime = sdf.format(new Date());

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath()+ "/NepWearAmi");
            dir.mkdirs();

            File file = new File(dir, "/file" + currentDateandTime+ ".txt");

            try {
                f = new FileOutputStream(file,false); //True = Append to file, false = Overwrite
                p = new PrintStream(f);
                String pt = file.getPath();
                Log.i("path", pt);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i("mes", "******* File not found. Did you" +
                        " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
            } catch (IOException e) {
                e.printStackTrace();
            }


        }



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Socket client connection
                    if(client==null || client.isClosed()) {
                        editText = (EditText) findViewById(R.id.ipText);
                        ipaddress = editText.getText().toString();
                        client = new Socket(ipaddress,port);
                        oos = new PrintStream(client.getOutputStream());
                    }

                    oos.println("start;000;0.0;0.0;0.0");
                    oos.flush();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }


    //event gyro 
    public void onCheckboxClickedSend(View view){
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            sendData = true;
        } else {
            sendData = false;
        }
    }

    public void onCheckboxClickedSave(View view){
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            saveData = true;
        } else {
            saveData = false;
        }
    }


    /**
     * Stop button
     */
    public void buttonClickStop(View view) throws IOException {

        if(saveData && recording)
        {
            try {
                p.close();
                f.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        recording = false;
        checkBoxS.setChecked(false);
//A new thread is executed (this for no block the main loop)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Socket client connection
                    if(client==null || client.isClosed()) {
                        editText = (EditText) findViewById(R.id.ipText);
                        ipaddress = editText.getText().toString();
                        client = new Socket(ipaddress,port);
                        oos = new PrintStream(client.getOutputStream());
                    }

                    oos.println("stop;000;0.0;0.0;0.0");
                    oos.flush();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    //No used
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    //No used
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //No used
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    //No used
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
