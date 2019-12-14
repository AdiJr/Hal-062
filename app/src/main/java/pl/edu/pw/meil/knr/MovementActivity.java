package pl.edu.pw.meil.knr;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

public class MovementActivity extends Activity {

    private Button engineBtnOn;
    private Button engineBtnOff;
    private FrameHandling mFrameHandling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement);

        mFrameHandling = new FrameHandling();
        final Timer t = new Timer();

        engineBtnOn = findViewById(R.id.engineBtnOn);
        engineBtnOff = findViewById(R.id.engineBtnOFF);

        engineBtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFrameHandling != null) {
                    int[] charTab = {1, 1, 1, 40};
                    mFrameHandling.sendFrameInt(21, 4, charTab);
                }
            }
        });
        engineBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFrameHandling != null) {
                    int[] charTab = {0, 0, 0, 40};
                    mFrameHandling.sendFrameInt(21, 4, charTab);
                }
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
    }
}