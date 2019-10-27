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
    private ImageButton connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connectBtn);
        connectionState = findViewById(R.id.connectionStateTextView);

        connectionState.setText(getString(R.string.rover_disconnected));
        connectionState.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    public void connect(View v) {
        connectBtn.setBackgroundResource(R.drawable.button_pressed);
        Toast toast = Toast.makeText(this, "kkkk", Toast.LENGTH_SHORT);
        toast.show();

    }
}
