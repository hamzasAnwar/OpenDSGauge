package de.dfki.opends.gauge.api;


import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anastr.speedviewlib.ImageSpeedometer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.dfki.opends.gauge.application.R;
import de.dfki.opends.gauge.util.Tags;
import de.dfki.opends.gauge.util.ViewMappings;



public class TcpClient extends AsyncTask<Void, String, Void> {

    private static final String TAG = Tags.TCP_CLIENT;
    private String dstAddress;
    private int dstPort;
    private byte[] request = null;
    private Socket socket;
    private TextView speedLimitText;
    private ImageView speedLimitSign;
    private TextView speedDigital;
    private TextView mileageDigital;
    private TextView gearDigital;
    private TextView currentGearMode;
    private ImageView handbrake;
    private ImageView leftTurn;
    private ImageView rightTurn;
    private ImageView headlights;
    private ImageView navigation;
    private ImageView fuelLights;
    private ImageView frostLights;
    private ImageView seatbeltLights;
    private ImageView oilPressureLights;
    private ImageView tyrePressureLights;
    private ImageView cruiseControlLights;
    private ImageView batteryLights;
    private ImageView checkLights;
    private ImageView autopilotSign;
    private ImageView fogBeam;
    private ImageView rearFogBeam;
    private ImageView gearShift;
    private ImageSpeedometer speedometer;
    private ImageSpeedometer accelerometer;
    private ImageSpeedometer fuelGauge;
    private XPath xPath;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder = null;
    private Document document = null;
    private String previousSignal = "OFF";
    private String previousNavigation = "";
    private String previousBeam = "";
    private String previousDrivingMode="";
    private String[] values;
    private Node[] node;
    private Animation turnAnimation;
    private Animation navAnimation;
    private Animation fuelAnimation;
    private Animation seatbeltAnimation;
    private Animation takeControlAnimation;

    public TcpClient(String address, int port, InputStream is, Map<ViewMappings,View> viewMap) {
        this.dstAddress = address;
        this.dstPort = port;
        this.request = subscribeFromFile(is);
        this.speedDigital = (TextView) viewMap.get(ViewMappings.SPEEDOMETER_DIGITAL);
        this.speedometer = (ImageSpeedometer) viewMap.get(ViewMappings.SPEEDOMETER);
        this.accelerometer = (ImageSpeedometer) viewMap.get(ViewMappings.RPM_METER);
        this.fuelGauge = (ImageSpeedometer) viewMap.get(ViewMappings.FUEL_METER);
        this.gearDigital = (TextView) viewMap.get(ViewMappings.CURRENT_SHIFT);
        this.currentGearMode = (TextView) viewMap.get(ViewMappings.CURRENT_GEAR);
        this.handbrake = (ImageView) viewMap.get(ViewMappings.HANDBRAKE);
        this.leftTurn = (ImageView) viewMap.get(ViewMappings.LEFT_TURN);
        this.rightTurn = (ImageView) viewMap.get(ViewMappings.RIGHT_TURN);
        this.headlights = (ImageView) viewMap.get(ViewMappings.HEAD_LIGHTS);
        this.frostLights = (ImageView) viewMap.get(ViewMappings.FROST_LIGHTS);
        this.fuelLights = (ImageView) viewMap.get(ViewMappings.FUEL_LIGHTS);
        this.seatbeltLights = (ImageView) viewMap.get(ViewMappings.SEAT_BELT_LIGHTS);
        this.batteryLights = (ImageView) viewMap.get(ViewMappings.BATTERY_LIGHTS);
        this.tyrePressureLights = (ImageView) viewMap.get(ViewMappings.TYRE_PRESSURE_LIGHTS);
        this.oilPressureLights = (ImageView) viewMap.get(ViewMappings.OIL_LIGHTS);
        this.checkLights = (ImageView) viewMap.get(ViewMappings.CHECK_ENGINE_LIGHTS);
        this.cruiseControlLights = (ImageView) viewMap.get(ViewMappings.CRUISE_CONTROL_LIGHTS);
        this.navigation = (ImageView) viewMap.get(ViewMappings.NAVIGATION);
        this.mileageDigital = (TextView) viewMap.get(ViewMappings.MILEAGE);
        this.gearShift = (ImageView) viewMap.get(ViewMappings.GEAR_SHIFT);
        this.speedLimitSign = (ImageView) viewMap.get(ViewMappings.SPEED_LIMIT_SIGN);
        this.speedLimitText = (TextView) viewMap.get(ViewMappings.SPEED_LIMIT_TEXT);
        this.autopilotSign = (ImageView)viewMap.get(ViewMappings.AUTOPILOT);
        this.fogBeam = (ImageView)viewMap.get(ViewMappings.FOG_LIGHTS);
        this.rearFogBeam = (ImageView)viewMap.get(ViewMappings.REAR_FOG_LIGHTS);
        this.node = new Node[viewMap.size()];
        this.values = new String[viewMap.size()];
        this.xPath = XPathFactory.newInstance().newXPath();
        this.factory = DocumentBuilderFactory.newInstance();
        this.setupTurnSignalAnimation();
        this.setupNavigationAnimation();
        this.setupFuelAnimation();
        this.setupSeatbeltAnimation();
        this.setupTakeControlAnimation();

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    private Animation setupTakeControlAnimation() {
        if(takeControlAnimation==null){
            takeControlAnimation = createNewDefaultAnimation();
        }
        return takeControlAnimation;
    }

    private Animation createNewDefaultAnimation(){
        Animation animation = new AlphaAnimation(1,(float) 0.15);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }
    private Animation setupFuelAnimation() {
        if(fuelAnimation==null){
            fuelAnimation = createNewDefaultAnimation();
        }
        return fuelAnimation;
    }
    private Animation setupSeatbeltAnimation() {
        if(seatbeltAnimation==null){
            seatbeltAnimation = createNewDefaultAnimation();
        }
        return seatbeltAnimation;
    }
    private Animation setupTurnSignalAnimation() {
      if(turnAnimation==null){
            turnAnimation = createNewDefaultAnimation();
            turnAnimation.setDuration(400);
      }
      return turnAnimation;
    }

    private void setupNavigationAnimation() {
        navAnimation = new AlphaAnimation(1,(float) 0.15);
        navAnimation.setDuration(1500);
        navAnimation.setInterpolator(new LinearInterpolator());
        navAnimation.setRepeatCount(5);
        navAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                navigation.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        navAnimation.setRepeatMode(Animation.REVERSE);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {

            if (socket == null) {
                socket = new Socket(dstAddress, dstPort);
            }

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(request);

            while (true) {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                    String messageValue = "";

                    try {
                        while (true) {
                            String line = r.readLine();

                            if (line == null) {
                                System.err.println("Connection closed by server.");
                                break;
                            } else {
                                messageValue += line;

                                if (line.contains("</Message>"))
                                    break;
                            }
                        }
                    } catch (SocketException e) {
                        System.err.println("Connection closed by server.");
                        break;
                    }

                    if (!messageValue.equals("")) {
                        publishProgress(getContent(loadXMLFromString(messageValue)));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "UnknownHostException: " + e.toString());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    Log.d(TAG, "Socket Closed");
                } catch (IOException e) {
                    Log.d(TAG, "Close Socket Error: " + e.toString());
                }
            }
        }

        return null;
    }


    /**
     * [0] = Speed
     * [1] = RPM
     * [2] = Gear
     * [3] = Handbrake
     * [4] = Turn signals
     * [5] = TransmissionMode
     * [6] = Navigation Image
     * [7] = Fuel Gauge
     * [8] = Headlights
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        if (values[0] != null) {
            applySpeedSettings(values[0]);
        }

        if (values[1] != null) {
            applyRpmSettings(values[1]);
        }

        if (values[2] != null) {
            applyGearSettings(values[2]);
        }
        if (values[3] != null) {
            applyHandbrakeSettings(values[3]);
        }
        if (values[4] != null) {
            applyTurnSignalSettings(values[4]);
        }
        if(values[5]!=null){
            applyCurrentGearMode(values[5]);
        }
        if(values[6]!=null){
            applyNavigation(values[6]);
        }
        if(values[7]!=null){
            applyFuel(values[7]);
        }
        if(values[8]!=null){
            applyHeadlights(values[8]);
        }

        if(values[9]!=null){
            applyFrostLights(values[9]);
        }
        if(values[10]!=null){
            applyCruiseControlLights(values[10]);
        }
        if(values[11]!=null){
            applyCheckLights(values[11]);
        }
        if(values[12]!=null){
            applyOilLights(values[12]);
        }
        if(values[13]!=null){
            applyBatteryLights(values[13]);
        }
        if(values[14]!=null){
            applyTyrePressureLights(values[14]);
        }
        if(values[15]!=null){
            applySeatBeltLights(values[15]);
        }
        if(values[16]!=null){
            applyMileageSettings(values[16]);
        }
        if(values[17]!=null){
            applyGearShiftSettings(values[17]);
        }
        if(values[18]!=null){
            applySpeedLimitSettings(values[18]);
        }
        if(values[19]!=null){
            applyAutopilotSettings(values[19]);
        }
        if(values[20]!=null){
            applyFogBeamSettings(values[20]);
        }
        if(values[21]!=null){
            applyRearFogBeamSettings(values[21]);
        }

    }

    private void applyRearFogBeamSettings(String value) {
        if(value.equals("true")){
            fogBeam.setAlpha((float)1);
        }else{
            fogBeam.setAlpha((float)0.1);
        }
    }

    private void applyFogBeamSettings(String value) {
        if(value.equals("true")){
            rearFogBeam.setAlpha((float)1);
        }else{
            rearFogBeam.setAlpha((float)0.1);
        }
    }

    private void applyAutopilotSettings(String value) {

        if(!previousDrivingMode.equals(value)){

            if(value.equals("ON")){
                autopilotSign.clearAnimation();
                autopilotSign.setVisibility(View.VISIBLE);
                autopilotSign.setImageResource(R.drawable.autopilot);
            }else if(value.equals("CONTROL")){
                autopilotSign.setVisibility(View.VISIBLE);
                autopilotSign.setImageResource(R.drawable.transfercontrol);
                autopilotSign.setAnimation(takeControlAnimation);
            }else if(value.equals("OFF")){
                autopilotSign.clearAnimation();
                autopilotSign.setVisibility(View.VISIBLE);
                autopilotSign.setImageResource(R.drawable.autopilotoff);
            }else{
                autopilotSign.setVisibility(View.INVISIBLE);
                autopilotSign.clearAnimation();
            }

            previousDrivingMode = value;
        }


    }

    private void applySpeedLimitSettings(String value) {
        if(!speedLimitText.equals("")){
            speedLimitText.setText(value);
            speedLimitSign.setVisibility(View.VISIBLE);
            speedLimitText.setVisibility(View.VISIBLE);
        }else{
            speedLimitText.setVisibility(View.INVISIBLE);
            speedLimitSign.setVisibility(View.INVISIBLE);
        }
    }

    private void applyGearShiftSettings(String value) {
        if(value.equals("UP")){
            gearShift.setVisibility(View.VISIBLE);
            gearShift.setImageResource(R.drawable.shiftup);
        }else if(value.equals("DOWN")){
            gearShift.setVisibility(View.VISIBLE);
            gearShift.setImageResource(R.drawable.shiftdown);
        }else{
            gearShift.setVisibility(View.INVISIBLE);
            gearShift.setAlpha((float)0);
        }
    }

    private void applyMileageSettings(String value) {
        mileageDigital.setText(value);
    }

    private void applyFrostLights(String value) {
        if(value.equals("true")){
            frostLights.setAlpha((float)1);
        }else{
            frostLights.setAlpha((float)0.1);
        }

    }

    private void applyCruiseControlLights(String value) {

        if(value.equals("true")){
            cruiseControlLights.setAlpha((float)1);
        }else{
            cruiseControlLights.setAlpha((float)0.1);
        }
    }

    private void applyOilLights(String value) {
        if(value.equals("true")){
            oilPressureLights.setAlpha((float)1);
        }else{
            oilPressureLights.setAlpha((float)0.1);
        }
    }

    private void applyCheckLights(String value) {
        if(value.equals("true")){
            checkLights.setAlpha((float)1);
        }else{
            checkLights.setAlpha((float)0.1);
        }
    }

    private void applyBatteryLights(String value) {
        if(value.equals("true")){
            batteryLights.setAlpha((float)1);
        }else{
            batteryLights.setAlpha((float)0.1);
        }
    }

    private void applyTyrePressureLights(String value) {
        if(value.equals("true")){
            tyrePressureLights.setAlpha((float)1);
        }else{
            tyrePressureLights.setAlpha((float)0.1);
        }
    }

    private void applySeatBeltLights(String value) {
        if(value.equals("true")){
            seatbeltLights.setAlpha((float)1);
            seatbeltLights.setAnimation(seatbeltAnimation);
        }else{
            seatbeltLights.setAlpha((float)0.1);
            seatbeltLights.clearAnimation();
        }
    }

    private void applyHeadlights(String value) {
        if(!value.equals(previousBeam)){
            if(value.equals("HighBeam")){
                headlights.setImageResource(R.drawable.highbeam);
                headlights.setAlpha((float)1);
            }else if(value.equals("LowBeam")){
                headlights.setImageResource(R.drawable.lowbeam);
                headlights.setAlpha((float) 1);
            }else{
                headlights.setImageResource(R.drawable.lowbeam);
                headlights.setAlpha((float)0.1);
            }
            previousBeam = value;
        }

    }

    private void applyFuel(String value) {
        float fuel = Float.valueOf(value).intValue();
        fuelGauge.setSpeedAt(fuel);

        if(fuel<10){
            fuelLights.setImageResource(R.drawable.lowfuelred);
            fuelLights.setAnimation(fuelAnimation);
        }else if(fuel<20){
            fuelLights.setImageResource(R.drawable.lowfuel);
            fuelLights.clearAnimation();
        }else{
            fuelLights.setImageResource(R.drawable.fuel);
        }

    }

    private void applyNavigation(String value) {
        if(!value.equals(previousNavigation)){

            if(value.equals("zero")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.zero);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("one")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.one);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("two")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.two);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("three")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.three);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("four")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.four);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("five")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.five);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("six")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.six);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("seven")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.seven);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("eight")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.eight);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("nine")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.nine);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("ten")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.ten);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("eleven")){
                navigation.setVisibility(View.VISIBLE);
                navigation.setImageResource(R.drawable.eleven);
                navigation.startAnimation(navAnimation);
            }else{
                navigation.clearAnimation();
                navigation.setVisibility(View.INVISIBLE);
            }

            previousNavigation = value;
        }



    }

    private void applyTurnSignalSettings(String value) {

        if(!value.equals(previousSignal)){

            if(value.equals("BOTH")){
                rightTurn.setAlpha((float) 1.0);
                leftTurn.setAlpha((float) 1.0);
                leftTurn.startAnimation(turnAnimation);
                rightTurn.startAnimation(turnAnimation);
            }else if(value.equals("LEFT")){
                leftTurn.setAlpha((float) 1.0);
                leftTurn.startAnimation(turnAnimation);
                rightTurn.setAlpha((float) 0.15);
                rightTurn.clearAnimation();
            }else if(value.equals("RIGHT") ){
                rightTurn.startAnimation(turnAnimation);
                rightTurn.setAlpha((float) 1.0);
                leftTurn.setAlpha((float) 0.15);
                leftTurn.clearAnimation();
            }else{
                rightTurn.setAlpha((float) 0.15);
                leftTurn.setAlpha((float) 0.15);
                rightTurn.clearAnimation();
                leftTurn.clearAnimation();
            }
            previousSignal=value;
        }


    }

    private void applyRpmSettings(String value) {
        int rpm = Float.valueOf(value).intValue();
        //Normalize RPM
        rpm-=560;
        if(rpm==140){
            rpm=5;
        }
        accelerometer.setSpeedAt((float)rpm);
    }

    private void applyHandbrakeSettings(String value) {
       if(value.equals("true")){
           handbrake.setAlpha((float) 1.0);
       }else{
           handbrake.setAlpha((float) 0.1);
       }

    }

    private void applySpeedSettings(String value) {
        Float speed = Float.valueOf(value);
        speedometer.setSpeedAt(speed);
        speedDigital.setText(String.valueOf(speed.intValue()));
    }

    private void applyGearSettings(String gear) {
        gearDigital.setText(gear);
    }

    private void applyCurrentGearMode(String mode) {
        currentGearMode.setText(mode);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    public byte[] subscribeFromFile(InputStream is) {
        byte[] msg = null;
        try {
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(is));

            String line = "";
            String xml = "";
            while ((line = bufRead.readLine()) != null) {
                xml += line + "\n";
            }

            bufRead.close();

            msg = (xml).getBytes("UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return msg;
    }


    public Document loadXMLFromString(String xml) {

        try {
            InputSource is = new InputSource(new StringReader(xml));
            document = builder.parse(is);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    public String[] getContent(Document document) {

        try {
            node[0] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/physicalAttributes/Properties/speed/text()", document, XPathConstants.NODE);
            node[1] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/engineCompartment/engine/Properties/actualRpm/text()", document, XPathConstants.NODE);
            node[2] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/gearUnit/Properties/currentGear/text()", document, XPathConstants.NODE);
            node[3] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/physicalAttributes/Properties/handbrake/text()", document, XPathConstants.NODE);
            node[4] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/physicalAttributes/Properties/turnSignal/text()", document, XPathConstants.NODE);
            node[5] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/gearUnit/Properties/currentTransmission/text()", document, XPathConstants.NODE);
            node[6] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/navigationImage/text()", document, XPathConstants.NODE);
            node[7] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/fueling/fuelType/tank/Properties/actualAmount/text()", document, XPathConstants.NODE);
            node[8] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/lights/Properties/headlights/text()", document, XPathConstants.NODE);
            node[9] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/frostLights/text()", document, XPathConstants.NODE);
            node[10] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/cruiseControlLights/text()", document, XPathConstants.NODE);
            node[11] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/checkLights/text()", document, XPathConstants.NODE);
            node[12] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/oilPressureLights/text()", document, XPathConstants.NODE);
            node[13] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/batteryLights/text()", document, XPathConstants.NODE);
            node[14] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/tyrePressureLights/text()", document, XPathConstants.NODE);
            node[15] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/seatbeltLights/text()", document, XPathConstants.NODE);
            node[16] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/physicalAttributes/Properties/mileage/text()", document, XPathConstants.NODE);
            node[17] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/gearUnit/Properties/shift/text()", document, XPathConstants.NODE);
            node[18] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/speedLimit/text()", document, XPathConstants.NODE);
            node[19] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/autoPilot/text()", document, XPathConstants.NODE);
            node[20] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/fogBeam/text()", document, XPathConstants.NODE);
            node[21] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/interior/cockpit/dashboard/rearfogBeam/text()", document, XPathConstants.NODE);

            for(int i=0;i<node.length;i++){
                if(node[i]!=null){
                    values[i]=node[i].getNodeValue();
                }
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return values;
    }

    public void closeConnection(){
        try {
            if(socket!=null){

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


