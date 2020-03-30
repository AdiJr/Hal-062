package pl.edu.pw.meil.knr.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.classes.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/* Created by AdiJr and Przygo in March 2020 for KNR PW */

class MovementScreenFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movement_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val t = Timer()
        val mFrameHandling = FrameHandling()
        val mStream = activity!!.findViewById<WebView>(R.id.webView)
        val joystick = activity!!.findViewById<JoystickView>(R.id.joystickView)
        val engineBtnOn = activity!!.findViewById<Button>(R.id.engineOnBtn)
        val engineBtnOff = activity!!.findViewById<Button>(R.id.engineOffBtn)
        val engineStatus = activity!!.findViewById<TextView>(R.id.engineStatusTxt)
        val noInternetAnimation = activity!!.findViewById<LottieAnimationView>(R.id.noInternetAnimation)

        if (!DetectConnection.checkInternetConnection(activity!!)) {
            noInternetAnimation.visibility = View.VISIBLE
        } else {
            noInternetAnimation.visibility = View.GONE
            mStream!!.visibility = View.VISIBLE
            mStream.loadUrl("http://192.168.1.138:8081")
        }

        engineStatus.setText(R.string.engine_status_info)
        engineBtnOn.setOnClickListener {
            val charTab = intArrayOf(1, 1, 1, 40)
            mFrameHandling.sendFrameInt(21, 4, charTab)
            engineStatus.setText(R.string.engine_status_on)
        }

        engineBtnOff.setOnClickListener {
            val charTab = intArrayOf(0, 0, 0, 40)
            mFrameHandling.sendFrameInt(21, 4, charTab)
            engineStatus.setText(R.string.engine_status_off)
        }

        t.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (HalAPP.connectionStatus == 1) {
                    var right = -JoystickValue.joystickX - JoystickValue.joystickY
                    var left = -JoystickValue.joystickX + JoystickValue.joystickY
                    if (right > 100) {
                        right = 100
                    } else if (right < -100) {
                        right = -100
                    }
                    if (left > 100) {
                        left = 100
                    } else if (left < -100) {
                        left = -100
                    }
                    val outputTab = intArrayOf(left, right)
                    mFrameHandling.sendFrameInt(20, 2, outputTab)
                }
            }
        },  //Set how long before to start calling the TimerTask (in milliseconds)
                0,  //Set the amount of time between each execution (in milliseconds)
                100)

        joystick.setOnMoveListener { angle, strength ->
            val x = (-(strength.toDouble() * sin(angle.toDouble() * 6.28 / 360.0))).toInt()
            val y = (strength.toDouble() * cos(angle.toDouble() * 6.28 / 360.0)).toInt()
            JoystickValue.setX(x)
            JoystickValue.setY(y)
        }
    }

    var isMovementScreenFragment = false
    override fun onStart() {
        super.onStart()
        isMovementScreenFragment = true
    }

    override fun onStop() {
        super.onStop()
        isMovementScreenFragment = false
    }

}
