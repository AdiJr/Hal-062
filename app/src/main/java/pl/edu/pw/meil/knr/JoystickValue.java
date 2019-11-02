package pl.edu.pw.meil.knr;

/**
 * Created by T460s on 09.03.2018.
 */

public class JoystickValue {

    static public int joystickX;
    static public int joystickY;
    static public FrameHandling mFrameHandling;

    static public void setX(int x) {
        joystickX = x;
    }

    static public void setY(int y) {
        joystickY = y;
    }
}
