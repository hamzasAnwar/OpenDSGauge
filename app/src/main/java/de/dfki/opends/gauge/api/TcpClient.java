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
    private TextView speedDigital;
    private TextView gearDigital;
    private TextView currentGearMode;
    private ImageView handbrake;
    private ImageView leftTurn;
    private ImageView rightTurn;
    private ImageView headlights;
    private ImageView navigation;
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
    private String[] values;
    private Node[] node;
    private Animation turnAnimation;
    private Animation navAnimation;

    public TcpClient(String address, int port, InputStream is, Map<String,View> viewMap) {
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
        this.navigation = (ImageView) viewMap.get(ViewMappings.NAVIGATION);


        this.node = new Node[viewMap.size()];
        this.values = new String[viewMap.size()];
        this.xPath = XPathFactory.newInstance().newXPath();
        this.factory = DocumentBuilderFactory.newInstance();
        this.setupTurnSignalAnimation();
        this.setupNavigationAnimation();

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    private void setupTurnSignalAnimation() {
        turnAnimation = new AlphaAnimation(1,(float) 0.15);
        turnAnimation.setDuration(400);
        turnAnimation.setInterpolator(new LinearInterpolator());
        turnAnimation.setRepeatCount(Animation.INFINITE);
        turnAnimation.setRepeatMode(Animation.REVERSE);
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
                navigation.setImageResource(R.drawable.nav);
                navigation.setAlpha((float)0.1);
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

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, "UnknownHostException: " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IOException: " + e.toString());

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

    }

    private void applyHeadlights(String value) {
        if(!value.equals(previousBeam)){
            if(value.equals("HighBeam")){
                headlights.setImageResource(R.drawable.highbeam);
                headlights.setAlpha((float)1);
            }else if(value.equals("LowBeam")){
                headlights.setImageResource(R.drawable.lowbeam);
                headlights.setAlpha((float) 1);
            }else if(value.equals("RearFogBeam")){
                headlights.setImageResource(R.drawable.rearfogbeam);
                headlights.setAlpha((float) 1);
            }else if(value.equals("FogBeam")){
                headlights.setImageResource(R.drawable.fogbeam);
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
    }

    private void applyNavigation(String value) {
        if(!value.equals(previousNavigation)){

            if(value.equals("cr")){
                navigation.setImageResource(R.drawable.crossing_right);
                navigation.setAlpha((float)1.0);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("cl")){
                navigation.setImageResource(R.drawable.crossing_left);
                navigation.setAlpha((float)1.0);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("cs")){
                navigation.setImageResource(R.drawable.crossing_straight);
                navigation.setAlpha((float)1.0);
                navigation.startAnimation(navAnimation);
            }else if(value.equals("")){
                navigation.setImageResource(R.drawable.nav);
                navigation.setAlpha((float)0.1);
                navigation.clearAnimation();
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
        Float rpm = Float.valueOf(value);
        //Normalize RPM
        rpm = (rpm - 750);
        accelerometer.setSpeedAt(rpm);
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

        } catch (Exception e) {
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

}


