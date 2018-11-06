package de.dfki.opends.gauge.api;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.github.anastr.speedviewlib.ImageSpeedometer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import de.dfki.opends.gauge.util.Tags;


public class TcpClient extends AsyncTask<Void, String, Void> {

    private static final String TAG = Tags.TCP_CLIENT;
    private String dstAddress;
    private int dstPort;
    private byte[] request = null;
    private Socket socket;
    private TextView speedDigital;
    private TextView gearDigital;

    private ImageSpeedometer speedometer;
    private ImageSpeedometer accelerometer;

    private String[] values = new String[3];
    private Node[] node = new Node[3];

    public TcpClient(String address, int port, InputStream is, TextView speedDigital, TextView gearDigital, ImageSpeedometer speedometer, ImageSpeedometer accelerometer) {
        this.dstAddress = address;
        this.dstPort = port;
        this.request = subscribeFromFile(is);
        this.speedDigital = speedDigital;
        this.speedometer = speedometer;
        this.accelerometer = accelerometer;
        this.gearDigital = gearDigital;
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
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

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
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String speed = String.valueOf(Double.valueOf(values[0]).intValue());

        float rpm = Float.valueOf(values[1]);

        String gear = String.valueOf(values[2]);

        //Normalize RPM
        rpm = (rpm-750)/1000;

        speedometer.setSpeedAt(Float.valueOf(speed));
        speedDigital.setText(speed);
        accelerometer.setSpeedAt(rpm);
        gearDigital.setText(gear);
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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            document = builder.parse(is);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    public String[] getContent(Document document) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            node[0] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/physicalAttributes/Properties/speed/text()", document, XPathConstants.NODE);
            node[1] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/engineCompartment/engine/Properties/actualRpm/text()", document, XPathConstants.NODE);
            node[2] = (Node) xPath.evaluate("/Message/Event/root/thisVehicle/exterior/gearUnit/Properties/currentGear/text()", document, XPathConstants.NODE);

            if (node[0] != null) {
                values[0] = node[0].getNodeValue();
            }
            if (node[1] != null) {
                values[1] = node[1].getNodeValue();
            }
            if (node[2] != null) {
                values[2] = node[2].getNodeValue();

            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return values;
    }

}


