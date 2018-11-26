package de.dfki.opends.gauge.application;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView mileageDigital;
    private TextView gearDigital;
    private TextView currentGearMode;
    private ImageView handbrake;
    private ImageView leftTurn;
    private ImageView rightTurn;
    private ImageView headlights;
    private ImageView fuelLight;
    private ImageView cruiseLight;
    private ImageView checkLight;
    private ImageView seatbeltLight;
    private ImageView frostLight;
    private ImageView tyrepressureLight;
    private ImageView batteryLight;
    private ImageView oilLight;
    private ImageView navigation;
    private ImageView gearShift;



    private ImageSpeedometer fuelmeter;
    private ImageSpeedometer speedometer;
    private ImageSpeedometer accelerometer;
    private Map<ViewMappings,View> viewMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewMap = new HashMap<>();

        //configure default settings
        setDefaultGearSettings();
        setDefaultAccelerometerSettings();
        setDefaultSpeedometerSettings();
        setDefaultMileageSettings();
        setDefaultHandbrakeSettings();
        setDefaultTurnSignalSettings();
        setScreenAlwaysOnSetting();
        setDefaultNavigationSettings();
        setDefaultFuelSettings();
        setDefaultHeadlights();
        setDefaultBatteryLights();
        setDefaultSeatbeltLights();
        setDefaultCheckEngineLights();
        setDefaultOilLights();
        setDefaultCruiseLights();
        setDefaultFrostLights();
        setDefaultTyrePressureLights();
        initialzeTcpClient();

    }

    private void setRPMRight(){
        RelativeLayout.LayoutParams accParam = (RelativeLayout.LayoutParams)accelerometer.getLayoutParams();

        accParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        accParam.addRule(RelativeLayout.ALIGN_PARENT_END);
        accelerometer.setLayoutParams(accParam); //causes layout update

        RelativeLayout.LayoutParams speedoParam = (RelativeLayout.LayoutParams)speedometer.getLayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            speedoParam.removeRule(RelativeLayout.ALIGN_PARENT_END);
            speedoParam.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        speedometer.setLayoutParams(speedoParam);

    }

    private void setDefaultBatteryLights() {
        batteryLight = findViewById(R.id.battery);
        viewMap.put(ViewMappings.BATTERY_LIGHTS,batteryLight);
    }

    private void setDefaultOilLights() {
        oilLight = findViewById(R.id.oilpressure);
        viewMap.put(ViewMappings.OIL_LIGHTS,oilLight);
    }

    private void setDefaultSeatbeltLights() {
        seatbeltLight = findViewById(R.id.seatbelt);
        viewMap.put(ViewMappings.SEAT_BELT_LIGHTS,seatbeltLight);
    }

    private void setDefaultCheckEngineLights() {
        checkLight = findViewById(R.id.checkengine);
        viewMap.put(ViewMappings.CHECK_ENGINE_LIGHTS,checkLight);
    }

    private void setDefaultFrostLights() {
        frostLight = findViewById(R.id.frostwarning);
        viewMap.put(ViewMappings.FROST_LIGHTS,frostLight);
    }

    private void setDefaultCruiseLights() {
        cruiseLight = findViewById(R.id.cruisecontrol);
        viewMap.put(ViewMappings.CRUISE_CONTROL_LIGHTS,cruiseLight);
    }

    private void setDefaultTyrePressureLights() {
        tyrepressureLight = findViewById(R.id.tyrepressure);
        viewMap.put(ViewMappings.TYRE_PRESSURE_LIGHTS,tyrepressureLight);
    }


    private void setDefaultHeadlights() {
        headlights = findViewById(R.id.headlights);
        viewMap.put(ViewMappings.HEAD_LIGHTS,headlights);
    }

    private void setDefaultFuelSettings() {
        fuelmeter = findViewById(R.id.fuelgauge);
        fuelmeter.setMinMaxSpeed(0,60);
        fuelmeter.setStartDegree(180+15);
        fuelmeter.setEndDegree(360-15);
        fuelmeter.setSpeedTextSize(0);
        fuelmeter.setUnitTextSize(0);
        fuelmeter.setWithTremble(false);
        viewMap.put(ViewMappings.FUEL_METER,fuelmeter);


        fuelLight = findViewById(R.id.fuel);
        viewMap.put(ViewMappings.FUEL_LIGHTS,fuelLight);

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

    private void setDefaultNavigationSettings() {
        navigation = findViewById(R.id.navigation);
        navigation.setVisibility(View.INVISIBLE);
        viewMap.put(ViewMappings.NAVIGATION,navigation);
    }


    private void initialzeTcpClient() {

        String ip = getIntent().getStringExtra(Tags.IP);
        int port = Integer.parseInt(getIntent().getStringExtra(Tags.PORT));

        boolean rpmRight = Boolean.parseBoolean(getIntent().getStringExtra(Tags.RPM_RIGHT));
        if(rpmRight){
            setRPMRight();
        }

        try {
            tcpClient = new TcpClient(ip, port, this.getResources().openRawResource(R.raw.subscribe), viewMap);
            tcpClient.execute();



        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            Toast.makeText(this ,"Error IP/Port Invalid!",Toast.LENGTH_LONG);
        }
    }


    private void setDefaultSpeedometerSettings() {
        speedometer = findViewById(R.id.speedometer);
        speedometer.setSpeedTextSize(0);
        speedometer.setUnitTextSize(0);
        speedometer.setMinMaxSpeed(0,220);

        speedometer.setWithTremble(false);

        speedDigital = findViewById(R.id.digitalSpeed);
        speedDigital.setTypeface(Typeface.createFromAsset(getAssets(), "digital-counter-7.regular.ttf"));

        viewMap.put(ViewMappings.SPEEDOMETER,speedometer);
        viewMap.put(ViewMappings.SPEEDOMETER_DIGITAL,speedDigital);
    }

    private void setDefaultMileageSettings(){
        mileageDigital = findViewById(R.id.mileageDigital);
        mileageDigital.setTypeface(Typeface.createFromAsset(getAssets(), "digital-counter-7.regular.ttf"));

        viewMap.put(ViewMappings.MILEAGE,mileageDigital);
    }

    private void setDefaultAccelerometerSettings() {
        accelerometer = findViewById(R.id.accelerometer);
        accelerometer.setSpeedTextSize(0);
        accelerometer.setUnitTextSize(0);
        accelerometer.setMinMaxSpeed(0, 7000);
        accelerometer.setWithTremble(false);
        viewMap.put(ViewMappings.RPM_METER,accelerometer);
    }


    private void setDefaultGearSettings() {
        gearDigital = findViewById(R.id.digitalGear);
        gearDigital.setTypeface(Typeface.createFromAsset(getAssets(), "digital-counter-7.regular.ttf"));

        currentGearMode = findViewById(R.id.gearMode);

        gearShift = findViewById(R.id.shift);

        viewMap.put(ViewMappings.CURRENT_GEAR,currentGearMode);
        viewMap.put(ViewMappings.CURRENT_SHIFT,gearDigital);
        viewMap.put(ViewMappings.GEAR_SHIFT,gearShift);

    }

    private void setScreenAlwaysOnSetting() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        tcpClient.closeConnection();
        finish();
        super.onPause();
    }
}
