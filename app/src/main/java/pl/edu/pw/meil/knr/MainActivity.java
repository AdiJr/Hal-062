package pl.edu.pw.meil.knr;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageButton connectBtn;
    private TextView connectionState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn =  findViewById(R.id.connectBtn);
        connectionState = findViewById(R.id.connectionStateTextView);

        connectionState.setText("The Hal Rover is currently DISCONNECTED");
        connectionState.setTextColor(getResources().getColor(R.color.colorPrimary));

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionState.setText("The Hal Rover is currently CONNECTED");
                connectionState.setTextColor(Color.parseColor("#00ff0"));
            }
        });
    }
}
