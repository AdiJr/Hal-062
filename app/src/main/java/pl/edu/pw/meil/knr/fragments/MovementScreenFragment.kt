package pl.edu.pw.meil.knr.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.classes.*
import pl.edu.pw.meil.knr.viewModels.MovementScreenViewModel
import java.util.*
import kotlin.math.cos
import kotlin.math.sin


class MovementScreenFragment : Fragment() {

    private var mFrameHandling: FrameHandling? = null
    private var knrImage: ImageView? = null
    private var mStream: WebView? = null

    companion object {
        fun newInstance() = MovementScreenFragment()
    }

    private lateinit var viewModel: MovementScreenViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.movement_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        val connectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        mFrameHandling = FrameHandling()
        mStream = activity!!.findViewById<View>(R.id.webView) as WebView?
        val t = Timer()
        val joystick: JoystickView = activity!!.findViewById(R.id.joystickView)
        val engineBtnOn: Button = activity!!.findViewById<Button>(R.id.engineOnBtn)
        val engineBtnOff: Button = activity!!.findViewById<Button>(R.id.engineOffBtn)
        knrImage = activity!!.findViewById<View>(R.id.knr_logo) as ImageView?
        val engineStatus: TextView = activity!!.findViewById<TextView>(R.id.engineStatusTxt)
        val streamFab: FloatingActionButton = activity!!.findViewById(R.id.streamFAB)

        viewModel = ViewModelProviders.of(this).get(MovementScreenViewModel::class.java)
        // TODO: Use the ViewModel

        if (!DetectConnection.checkInternetConnection(activity)) {
            Toast.makeText(activity, "You are not connected to LAN", Toast.LENGTH_SHORT).show()
        } else {
            streamFab.visibility = View.VISIBLE
        }

        engineStatus.setText(R.string.engine_status_info)
        engineBtnOn.setOnClickListener {
            if (mFrameHandling != null) {
                val charTab = intArrayOf(1, 1, 1, 40)
                mFrameHandling!!.sendFrameInt(21, 4, charTab)
            }
            engineStatus.setText(R.string.engine_status_on)
        }

        engineBtnOff.setOnClickListener {
            if (mFrameHandling != null) {
                val charTab = intArrayOf(0, 0, 0, 40)
                mFrameHandling!!.sendFrameInt(21, 4, charTab)
            }
            engineStatus.setText(R.string.engine_status_off)
        }

        t.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (HalAPP.getConnectionStatus() == 1) {
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
                    mFrameHandling!!.sendFrameInt(20, 2, outputTab)
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

}