package de.dfki.opends.gauge.application;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.anastr.speedviewlib.ImageSpeedometer;

import de.dfki.opends.gauge.api.TcpClient;

public class MainActivity extends AppCompatActivity {


    TcpClient tcpClient;
    TextView tv;
    ImageSpeedometer sp;
    protected PowerManager.WakeLock mWakeLock;
    private static String TAG ="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.digitalSpeed);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),"digital-counter-7.regular.ttf"));
        sp = findViewById(R.id.speedView);
        sp.setMaxSpeed(260);
        //to keep the screen on for the activity
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();
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

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }
}
