package pl.edu.pw.meil.knr;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Timer;
import java.util.TimerTask;

import pl.edu.pw.meil.knr.classes.DetectConnection;
import pl.edu.pw.meil.knr.classes.FrameHandling;
import pl.edu.pw.meil.knr.classes.HalAPP;
import pl.edu.pw.meil.knr.classes.JoystickValue;
import pl.edu.pw.meil.knr.classes.JoystickView;

public class MovementActivity extends Activity {

    private FrameHandling mFrameHandling;
    private ImageView knrImage;
    private WebView mStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        mFrameHandling = new FrameHandling();
        mStream = findViewById(R.id.webView);
        final Timer t = new Timer();
        JoystickView joystick = findViewById(R.id.joystickView);
        Button engineBtnOn = findViewById(R.id.engineOnBtn);
        Button engineBtnOff = findViewById(R.id.engineOffBtn);
        knrImage = findViewById(R.id.knr_logo);
        final TextView engineStatus = findViewById(R.id.engineStatusTxt);
        FloatingActionButton streamFab = findViewById(R.id.streamFAB);

        if (!DetectConnection.checkInternetConnection(this)) {
            Toast.makeText(this, "You are not connected to LAN", Toast.LENGTH_SHORT).show();
        } else {
            streamFab.setVisibility(View.VISIBLE);
        }

        engineStatus.setText(R.string.engine_status_info);
        engineBtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFrameHandling != null) {
                    int[] charTab = {1, 1, 1, 40};
                    mFrameHandling.sendFrameInt(21, 4, charTab);
                }
                engineStatus.setText(R.string.engine_status_on);
            }
        });

        engineBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFrameHandling != null) {
                    int[] charTab = {0, 0, 0, 40};
                    mFrameHandling.sendFrameInt(21, 4, charTab);
                }
                engineStatus.setText(R.string.engine_status_off);
            }
        });

        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      if (HalAPP.getConnectionStatus() == 1) {
                                          int right = (-JoystickValue.joystickX - JoystickValue.joystickY);
                                          int left = (-JoystickValue.joystickX + JoystickValue.joystickY);
                                          if (right > 100) {
                                              right = 100;
                                          } else if (right < -100) {
                                              right = -100;
                                          }
                                          if (left > 100) {
                                              left = 100;
                                          } else if (left < -100) {
                                              left = -100;
                                          }
                                          int[] outputTab = {left, right};
                                          mFrameHandling.sendFrameInt(20, 2, outputTab);
                                      }
                                  }
                              },//Set how long before to start calling the TimerTask (in milliseconds)
                0,
                //Set the amount of time between each execution (in milliseconds)
                100);

        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                int x = -(int) ((double) strength * Math.sin((double) angle * 6.28 / 360.0));
                int y = (int) ((double) strength * Math.cos((double) angle * 6.28 / 360.0));
                JoystickValue.setX(x);
                JoystickValue.setY(y);
            }
        });
    }

    public void showStream(View view) {
        knrImage.setVisibility(View.GONE);
        mStream.setVisibility(View.VISIBLE);
        mStream.loadUrl("https://www.spidersweb.pl");
    }
}