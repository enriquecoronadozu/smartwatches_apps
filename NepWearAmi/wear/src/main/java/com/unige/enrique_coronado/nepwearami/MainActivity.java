/**
 * Android wear app to obtain and send sensor information
 *
 * @author Luis Enrique Coronado Zuñiga
 * @date   June, 2016
 */

package com.unige.enrique_coronado.nepwearami;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Main function
 */
public class MainActivity extends Activity implements SensorEventListener {

    //Interface variables
    private TextView mTextView;
    private TextView mButton;
    private CheckBox checkBox1;

    //Variables to measure time
    long init_t = 0;
    long fin_t = 0;
    double elapsedSeconds = 0;
    long tDelta = 0;
    double prom = 0;
    double sum = 0;
    double std = 0;

    GoogleApiClient client;

    boolean writeEnabled=false;
    long timestamp = 0;
    int inizioreg=1;
    String nodeId;

    //Sensor variables
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyro;
    private Sensor senSteps;


    float[] acc ={0,0,0};
    float[] gyro ={0,0,0};

    ArrayList<String> toSendMov1 = new ArrayList<String>();
    ArrayList<String> toSendMov2 = new ArrayList<String>();

    int packetSize = 10;

    String dataAcc = "";
    String dataGyro = "";
    String strSendAcc = "";
    String strSendGyro = "";

    List<Node> nodes;

    //Interface variables
    private CheckBox checkBoxAcc;
    private CheckBox checkBoxGyro;
    private CheckBox checkBoxSteps;
    private RadioButton rgame;
    private  RadioButton rui;
    private  RadioButton rnormal;

    Boolean a_checked = true;
    Boolean g_checked = false;
    Boolean s_checked = false;

    Boolean game_checked = true;
    Boolean ui_checked = false;
    Boolean normal_checked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        checkBoxAcc = (CheckBox) findViewById(R.id.checkBoxAcc);
        checkBoxGyro = (CheckBox) findViewById(R.id.checkBox1);
        checkBoxSteps = (CheckBox) findViewById(R.id.checkBox);
        rgame = (RadioButton) findViewById(R.id.radioButton);
        rui = (RadioButton) findViewById(R.id.radioButton2);
        rnormal = (RadioButton) findViewById(R.id.radioButton3);


        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.textT);
                mButton = (Button) stub.findViewById(R.id.btnWearStart);

            }
        });

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyro = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSteps = senSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        retrieveDeviceNode();

    }

    //event acc
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        if(checked)
        {
            a_checked = true;
        }
        else
        {
            a_checked = false;
        }
    }

    //event gyro
    public void onCheckboxClicked2(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        if(checked)
        {
            g_checked = true;
        }
        else
        {
            g_checked = false;
        }
    }

    //event step count
    public void onCheckboxClicked3(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        if(checked)
        {
            s_checked = true;
        }
        else
        {
            s_checked = false;
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        if(checked)
        {
            packetSize = 2;
           // game_checked = true;
           // ui_checked = false;
           // normal_checked = false;
        }
    }


    public void onRadioButtonClicked2(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        if(checked)
        {
            packetSize = 5;
            //game_checked = false;
            //ui_checked = true;
            //normal_checked = false;
        }
    }

    public void onRadioButtonClicked3(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        if(checked)
        {
            packetSize = 10;
           // game_checked = false;
           // ui_checked = false;
           // normal_checked = true;
        }
    }

    //Start button
    public void buttonClickStart(View view) throws IOException {
        //Accelerometer sensor and frequency
        senSensorManager.unregisterListener(this);

        if(a_checked) {

            senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
            Log.d("mess", "Acc + Game");

            /*if(game_checked)
            {
                senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
                Log.d("mess", "Acc + Game");
            }
            else
            {
                if (ui_checked)
                {
                    senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, SensorManager.SENSOR_DELAY_UI);
                    Log.d("mess", "Acc + UI");
                }
                else
                {
                    if(normal_checked)
                    {
                        senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        Log.d("mess", "Acc + Normal");
                    }

                }
            }*/
        }

        //Gyroscope sensor and frequency
        if(g_checked) {

            senSensorManager.registerListener((SensorEventListener) this, senGyro, SensorManager.SENSOR_DELAY_GAME);
            Log.d("mess", "Gyro + Game");

           /*if(game_checked)
            {
                senSensorManager.registerListener((SensorEventListener) this, senGyro, SensorManager.SENSOR_DELAY_GAME);
                Log.d("mess", "Gyro + Game");
            }
            else
            {
                if (ui_checked)
                {
                    senSensorManager.registerListener((SensorEventListener) this, senGyro, SensorManager.SENSOR_DELAY_UI);
                    Log.d("mess", "Gyro + UI");
                }
                else
                {
                    if(normal_checked)
                    {
                        senSensorManager.registerListener((SensorEventListener) this, senGyro, SensorManager.SENSOR_DELAY_NORMAL);
                        Log.d("mess", "Gyro + Normal");
                    }

                }
            }*/

        }

        //Step count sensor and frequency
        if(s_checked) {
            senSensorManager.registerListener((SensorEventListener) this, senSteps, SensorManager.SENSOR_DELAY_GAME);
            Log.d("mess", "Steps + Game");

            /*if(game_checked)
            {
                senSensorManager.registerListener((SensorEventListener) this, senSteps, SensorManager.SENSOR_DELAY_GAME);
                Log.d("mess", "Steps + Game");
            }
            else
            {
                if (ui_checked)
                {
                    senSensorManager.registerListener((SensorEventListener) this, senSteps, SensorManager.SENSOR_DELAY_UI);
                    Log.d("mess", "Steps + UI");
                }
                else
                {
                    if(normal_checked)
                    {
                        senSensorManager.registerListener((SensorEventListener) this, senSteps, SensorManager.SENSOR_DELAY_NORMAL);
                        Log.d("mess", "Steps + Normal");
                    }

                }
            }*/
        }
        writeEnabled=true;
        inizioreg=0;
        timestamp = System.currentTimeMillis();
        ((TextView) findViewById(R.id.textT)).setText("Sending");
        Log.d("mess", "Start Write");
    }

    //Stop button
    public void buttonClickStop(View view) throws IOException {
        senSensorManager.unregisterListener(this);
        writeEnabled = false;
        timestamp = 0;
        toSendMov1.clear();
        toSendMov2.clear();
        ((TextView)findViewById(R.id.textT)).setText("Waiting");
        Log.d("mess", "Stop Write");
    }

    //Functions for the communication
    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void retrieveDeviceNode() {
        final GoogleApiClient client = getGoogleApiClient(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.blockingConnect(150, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                nodes = result.getNodes();
                if (nodes.size() > 0) {
                    for(Node node : nodes) {
                        if (node.isNearby())
                            nodeId = node.getId();
                    }
                }
                client.disconnect();
            }
        }).start();
    }


    /**
     * Function that send the message
     */
    private void sendMessage(final String mess, final int type_mex, final int type_sensor) {

        fin_t = System.currentTimeMillis();
        tDelta = fin_t - init_t;
        elapsedSeconds = tDelta;
        ((TextView)findViewById(R.id.textT)).setText(String.valueOf(elapsedSeconds));
        init_t = fin_t;

        client = getGoogleApiClient(this);
        if(writeEnabled==true) {
            if (nodeId != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.blockingConnect(150, TimeUnit.MILLISECONDS);
                        if (type_mex == 1) {
                            Wearable.MessageApi.sendMessage(client, nodeId, "/motion1", mess.getBytes());
                        }
                        if (type_mex == 2) {
                            Wearable.MessageApi.sendMessage(client, nodeId, "/motion2", mess.getBytes());
                        }
                        client.disconnect();
                    }
                }).start();
            }
        }

    }

    /**
     * This event runs when a sensor produce data
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            acc[0] = sensorEvent.values[0];
            acc[1] = sensorEvent.values[1];
            acc[2] = sensorEvent.values[2];


            dataAcc = "a" + ";" + sensorEvent.timestamp + ";" + acc[0] + ";" + acc[1] + ";" + acc[2];
            toSendMov1.add(dataAcc);

        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro[0] = sensorEvent.values[0];
            gyro[1] = sensorEvent.values[1];
            gyro[2] = sensorEvent.values[2];

            dataGyro = "y" + ";" + sensorEvent.timestamp +";"+gyro[0]+";"+gyro[1]+";"+gyro[2];
            toSendMov2.add(dataGyro);
        }

        //Send acceleration data
        if (toSendMov1.size() == packetSize) {
            for (int i = 0; i < packetSize; i++)
                strSendAcc += toSendMov1.get(i) + "\n";
            //The extra string is 1
            sendMessage(strSendAcc,1, 1);
            toSendMov1.clear();
            strSendAcc = "";
        }

        if(toSendMov2.size() == packetSize)
        {
            for(int i=0; i<packetSize; i++)
                strSendGyro+=toSendMov2.get(i)+"\n";
            //The extra string is 2
            sendMessage(strSendGyro,2, 2);
            toSendMov2.clear();
            strSendGyro = "";
        }
    }

    //Not used
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
