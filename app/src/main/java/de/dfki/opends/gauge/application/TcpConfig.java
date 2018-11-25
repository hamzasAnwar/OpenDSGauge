package de.dfki.opends.gauge.application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import de.dfki.opends.gauge.util.Tags;

public class TcpConfig extends AppCompatActivity {

    private EditText editTextIp;
    private EditText editTextPort;
    private Switch alignRight;

    private static final String TAG = Tags.TCP_CONFIG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_config);
        editTextPort = findViewById(R.id.inputPort);
        editTextIp = findViewById(R.id.inputURL);
        alignRight = findViewById(R.id.alignRight);

    }

    public void connectButton(View view) {

        Intent gauge = new Intent(this, MainActivity.class);
        gauge.putExtra(Tags.IP, editTextIp.getText().toString());
        gauge.putExtra(Tags.PORT, editTextPort.getText().toString());
        Log.d(TAG,alignRight.isChecked()+"");
        gauge.putExtra(Tags.RPM_RIGHT,String.valueOf(alignRight.isChecked()));
        this.startActivity(gauge);
        
    }

    public void connectTab(View view) {

        Intent gauge = new Intent(this, MainActivity.class);
        gauge.putExtra(Tags.IP, "172.16.49.209");
        gauge.putExtra(Tags.PORT, editTextPort.getText().toString());
        this.startActivity(gauge);

    }
}
