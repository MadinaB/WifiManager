package com.madinabektayeva.wifimanager;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView data;
    private String info;


    private boolean running = false;
    private float startStepCount;
    private float prevStepCount;
    private float lastStepCount;

    private int x;
    private int y;
    private int stepSize;
    private int stepDistance;
    private String move;

    private int degree;
    private float currentDegree;

    private static SensorManager sensorServiceCompass;
    private static SensorManager sensorServiceSteps;
    private Sensor compasSensor;
    private Sensor stepCountSensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = (TextView) findViewById(R.id.data);
        data.setMovementMethod(new ScrollingMovementMethod());
        info ="";
        Button b1 = (Button)findViewById(R.id.button);
        b1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiMan.startScan();
                int linkspeed = wifiMan.getConnectionInfo().getLinkSpeed();
                int newRssi = wifiMan.getConnectionInfo().getRssi();
                int level = wifiMan.calculateSignalLevel(newRssi, 10);
                int percentage = (int) ((level/10.0)*100);
                String macAdd = wifiMan.getConnectionInfo().getBSSID();
                info+=("\n"+"x: "+ x + " / y: "+ y+" / steps: "+ (lastStepCount -startStepCount) + " / move: "+ move+"\n"+
                        " / linkspeed: " + linkspeed + " / newRssi: " + newRssi + "dbm" +"\n"+ " / BSSID: "+wifiMan.getConnectionInfo().getBSSID()+ " / macAdd : " + macAdd + "\n"+" / level : "+level +  " / precentage : "+percentage +"\n" );
                data.setText(info);
            }
        });

        sensorServiceCompass = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorServiceSteps = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        compasSensor = sensorServiceCompass.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        stepCountSensor = sensorServiceSteps.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        startStepCount = -1;
        prevStepCount = 0;
        lastStepCount = 0;
        stepDistance = 1; //TODO() stepDistance
        currentDegree = 0f;
        degree = 0;
        move = "up";
        stepSize = 1;
        x = 0;
        y = 0;

    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        if (compasSensor != null) {
            sensorServiceCompass.registerListener(this, compasSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(MainActivity.this, "Not supported", Toast.LENGTH_SHORT).show();
        }

        if (stepCountSensor != null) {
            sensorServiceSteps.registerListener((SensorEventListener) this, stepCountSensor, SensorManager.SENSOR_DELAY_UI);

        } else {
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_SHORT).show();
        }

    }

    protected void onPause() {
        super.onPause();
        sensorServiceCompass.unregisterListener(this);
        sensorServiceSteps.unregisterListener((SensorListener) this);
        running = false;
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            degree = Math.round(sensorEvent.values[0]);
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            lastStepCount = sensorEvent.values[0];
            if (startStepCount == -1) {
                startStepCount = sensorEvent.values[0];
                prevStepCount = startStepCount;
            }

        }

        if (lastStepCount > prevStepCount + stepDistance) {
            prevStepCount = lastStepCount;
            updateNextMove();
        }
    }


    public void updateNextMove() {

        if (0 < degree && degree <= 90) {
            move = "left";
        } else if (90 < degree && degree <= 180) {
            move = "up";
        } else if (180 < degree && degree <= 270) {
            move = "right";
        } else {
            move = "down";
        }

        Log.d("Log", "Move: " + move);
        Log.d("Log", "Degree: " + degree);

        if (move.equals("up")) {
            goUp();
        } else if (move.equals("down")) {
            goDown();
        } else if (move.equals("left")) {
            goLeft();
        } else if (move.equals("right")) {
            goRight();
        }

    }

    public void goRight() {
        x += stepSize;
    }

    public void goLeft() {
        x -= stepSize;
    }

    public void goUp() {
        y += stepSize;
    }

    public void goDown() {
        y -= stepSize;
    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}


/*

private BroadcastReceiver rssiReceiver = new BroadcastReceiver() {
  @Override
  public void onReceive(Context context, Intent intent) {
      WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
      wifiMan.startScan();
      int linkspeed = wifiMan.getConnectionInfo().getLinkSpeed();
      int newRssi = wifiMan.getConnectionInfo().getRssi();
      int level = wifiMan.calculateSignalLevel(newRssi, 10);
      int percentage = (int) ((level/10.0)*100);
      String macAdd = wifiMan.getConnectionInfo().getBSSID();
      //debugtext.setText("링크 스피드 : " + linkspeed + " / 신호 감도 : " + percentage + " / 맥어드레스 : " + macAdd );
  }
};

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter rssiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(rssiReceiver, rssiFilter);
        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiMan.startScan();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(rssiReceiver);
    }
*/

