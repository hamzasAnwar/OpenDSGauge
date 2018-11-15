package de.dfki.opends.gauge.application;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anastr.speedviewlib.ImageSpeedometer;

import java.util.HashMap;
import java.util.Map;

import de.dfki.opends.gauge.api.TcpClient;
import de.dfki.opends.gauge.util.Tags;
import de.dfki.opends.gauge.util.ViewMappings;

public class MainActivity extends AppCompatActivity {


    private static String TAG = Tags.MAIN_ACTIVITY;
    private TcpClient tcpClient;
    private TextView speedDigital;
    private TextView gearDigital;
    private ImageView handbrake;
    private ImageView leftTurn;
    private ImageView rightTurn;

    private ImageSpeedometer speedometer;
    private ImageSpeedometer accelerometer;
    private PowerManager.WakeLock mWakeLock;
    private Map<String,View> viewMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewMap = new HashMap<>();

        //configure default settings
        setDefaultGearSettings();
        setDefaultAccelerometerSettings();
        setDefaultSpeedometerSettings();
        setDefaultHandbrakeSettings();
        setDefaultTurnSignalSettings();
        setScreenAlwaysOnSetting();
        initialzeTcpClient();
    }

    private void setDefaultTurnSignalSettings() {
        leftTurn = findViewById(R.id.left);
        rightTurn = findViewById(R.id.right);
        viewMap.put(ViewMappings.LEFT_TURN,leftTurn);
        viewMap.put(ViewMappings.RIGHT_TURN,rightTurn);

    }

    private void setDefaultHandbrakeSettings() {
        handbrake = findViewById(R.id.handbrake);
        viewMap.put(ViewMappings.HANDBRAKE,handbrake);
    }


    private void initialzeTcpClient() {

        String ip = getIntent().getStringExtra(Tags.IP);
        int port = Integer.parseInt(getIntent().getStringExtra(Tags.PORT));

        try {
            tcpClient = new TcpClient(ip, port, this.getResources().openRawResource(R.raw.subscribe), viewMap);
            tcpClient.execute();

        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }


    private void setDefaultSpeedometerSettings() {
        speedometer = findViewById(R.id.speedometer);
        speedometer.setSpeedTextSize(0);
        speedometer.setUnitTextSize(0);
        speedometer.setMaxSpeed(220);


        speedDigital = findViewById(R.id.digitalSpeed);
        speedDigital.setTypeface(Typeface.createFromAsset(getAssets(), "digital-counter-7.regular.ttf"));

        viewMap.put(ViewMappings.SPEEDOMETER,speedometer);
        viewMap.put(ViewMappings.SPEEDOMETER_DIGITAL,speedDigital);
    }


    private void setDefaultAccelerometerSettings() {
        accelerometer = findViewById(R.id.accelerometer);
        accelerometer.setSpeedTextSize(0);
        accelerometer.setUnitTextSize(0);
        accelerometer.setMinMaxSpeed(0, 6000);
        viewMap.put(ViewMappings.RPM_METER,accelerometer);
    }


    private void setDefaultGearSettings() {
        gearDigital = findViewById(R.id.digitalGear);
        gearDigital.setTypeface(Typeface.createFromAsset(getAssets(), "digital-counter-7.regular.ttf"));

        viewMap.put(ViewMappings.CURRENT_SHIFT,gearDigital);

    }

    private void setScreenAlwaysOnSetting() {
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();
    }
}
