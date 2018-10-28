package de.dfki.opends.gauge.application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class TcpConfig extends AppCompatActivity {

    private EditText editTextIp;
    private EditText editTextPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_config);
        editTextPort = findViewById(R.id.inputPort);
        editTextIp = findViewById(R.id.inputURL);

    }

    public void connectButton(View view) {

        Intent gauge = new Intent(this, MainActivity.class);
        gauge.putExtra("ip", editTextIp.getText().toString());
        gauge.putExtra("port", editTextPort.getText().toString());
        this.startActivity(gauge);


    }


}
