package pl.edu.pw.meil.knr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement);
        setTitle(R.string.control_title);
    }
}
