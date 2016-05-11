package com.andrea.example.prova;
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

        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.wearable.Node;
        import com.google.android.gms.wearable.NodeApi;
        import com.google.android.gms.wearable.Wearable;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStreamWriter;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;
        import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    private TextView mButton;
    private CheckBox checkBox1;

    boolean writeEnabled=false;
    long timestamp = 0;
    float passitmp = 0;

    int inizioreg=1;

    String nodeId;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyro;


    float[] acc ={0,0,0};
    float[] gyro ={0,0,0};


    ArrayList<String> toSendMov1 = new ArrayList<String>();
    ArrayList<String> toSendMov4 = new ArrayList<String>();

    int packetSize = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.textT);
                mButton = (Button) stub.findViewById(R.id.btnWearStart);
                checkBox1 = (CheckBox) findViewById(R.id.checkBox1);

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        writeEnabled=true;
                        inizioreg=0;
                        timestamp = System.currentTimeMillis();
                        ((TextView) findViewById(R.id.textT)).setText("Sending");
                        Log.d("mess", "Start Write");
                    }
                });

                ((Button) stub.findViewById(R.id.btnWearStop)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        writeEnabled = false;
                        timestamp = 0;
                        toSendMov1.clear();
                        toSendMov4.clear();
                        ((TextView)findViewById(R.id.textT)).setText("Waiting");
                        Log.d("mess", "Stop Write");
                    }
                });


            }
        });

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener((SensorEventListener) this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        senGyro = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSensorManager.registerListener((SensorEventListener) this, senGyro, SensorManager.SENSOR_DELAY_GAME);

        retrieveDeviceNode();

    }

    //Funzione che istanzia un oggetto GoogleApiClient con le API Wearable
    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    //Funzione che rintraccia tutti i nodi raggiungibili e scelgo il migliore a cui mandare poi il pacchetto
    private void retrieveDeviceNode() {
        final GoogleApiClient client = getGoogleApiClient(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.blockingConnect(5000, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = result.getNodes();
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


    //Funzione che manda il messaggio contenente il pacchetto di dati dei sensori
    private void sendMex(final String mess, final int type_mex, final int type_sensor) {
        final GoogleApiClient client = getGoogleApiClient(this);
        if(writeEnabled==true) {
            if (nodeId != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        client.blockingConnect(15000, TimeUnit.MILLISECONDS);
                        if (type_mex == 1) {
                            Wearable.MessageApi.sendMessage(client, nodeId, "/motion1", mess.getBytes());
                        }
                        if (type_mex == 4 && checkBox1.isChecked()) {
                            Wearable.MessageApi.sendMessage(client, nodeId, "/motion4", mess.getBytes());
                        }
                    }
                }).start();
            }
        }

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        long nowD0 = 0;
        long nowD1 = 0;
        long nowD2 = 0;
        long nowD3 = 0;

        long nowD4 = 0;
        long nowD5 = 0;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            acc[0] = sensorEvent.values[0];
            acc[1] = sensorEvent.values[1];
            acc[2] = sensorEvent.values[2];
            nowD0 = sensorEvent.timestamp;

            String pacchetto1 = "";
            pacchetto1 = "a" + ";" + nowD0 + ";" + acc[0] + ";" + acc[1] + ";" + acc[2];
            toSendMov1.add(pacchetto1);

        }
        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro[0] = sensorEvent.values[0];
            gyro[1] = sensorEvent.values[1];
            gyro[2] = sensorEvent.values[2];
            nowD3 = sensorEvent.timestamp;

            String pacchetto4 = "";
            pacchetto4 = "y" + ";" + nowD3+";"+gyro[0]+";"+gyro[1]+";"+gyro[2];
            toSendMov4.add(pacchetto4);
        }

        if (toSendMov1.size() == packetSize) {
            String strSend = "";
            for (int i = 0; i < packetSize; i++)
                strSend += toSendMov1.get(i) + "\n";

            sendMex(strSend,1, 1);
            toSendMov1.clear();
        }

        if(toSendMov4.size() == packetSize)
        {
            String strSend = "";
            for(int i=0; i<packetSize; i++)
                strSend+=toSendMov4.get(i)+"\n";

            sendMex(strSend,4, 4);
            toSendMov4.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
