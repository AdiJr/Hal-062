package pl.edu.pw.meil.knr.classes

/**
 * Created by T460s on 09.03.2018.
 */
object JoystickValue {
    var joystickX = 0
    var joystickY = 0

    fun setX(x: Int) {
        joystickX = x
    }

    fun setY(y: Int) {
        joystickY = y
    }
}