package de.dfki.opends.gauge.application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.anastr.speedviewlib.ImageSpeedometer;

import de.dfki.opends.gauge.api.TcpClient;

public class MainActivity extends AppCompatActivity {


    TcpClient tcpClient;
    TextView tv;
    ImageSpeedometer sp;
    private static String TAG ="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.digitalSpeed);
        sp = findViewById(R.id.speedView);
        sp.setMaxSpeed(260);
        initialzeTcpClient();
    }



    private boolean initialzeTcpClient() {

        String ip = getIntent().getStringExtra("ip");
        int port = Integer.parseInt(getIntent().getStringExtra("port"));

        try {
            tcpClient = new TcpClient(ip,port,this.getResources().openRawResource(R.raw.subscribe), tv,sp);
            tcpClient.execute();

        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
