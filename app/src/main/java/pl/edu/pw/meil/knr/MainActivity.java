package pl.edu.pw.meil.knr;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView connectionState;
    private TextView clickToConnect;
    private ImageButton connectBtn;
    private boolean isClicked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connectBtn);
        connectionState = findViewById(R.id.connectionStateTextView);
        clickToConnect = findViewById(R.id.clickToConnectTxt);

        connectionState.setText(getString(R.string.rover_disconnected));
        connectionState.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    public void connect(View v) {
        if (isClicked) {
            connectBtn.setBackgroundResource(R.drawable.button_pressed);
            connectionState.setText(getString(R.string.rover_connected));
            connectionState.setTextColor(getResources().getColor(R.color.green));
            clickToConnect.setText(getString(R.string.disconnect));
            isClicked = false;
        } else {
            connectBtn.setBackgroundResource(R.drawable.button_normal);
            connectionState.setText(getString(R.string.rover_disconnected));
            connectionState.setTextColor(getResources().getColor(R.color.colorAccent));
            clickToConnect.setText(R.string.btnDcs);
            isClicked = true;
        }
    }
}
