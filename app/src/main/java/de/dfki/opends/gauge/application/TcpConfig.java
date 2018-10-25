package de.dfki.opends.gauge.application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class TcpConfig extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_config);
    }


    public void connectButton(View view) {

        Intent gauge = new Intent(this, MainActivity.class);
        gauge.putExtra("tcpListener", 5);
        this.startActivity(gauge);

    }
}
