package pl.edu.pw.meil.knr.fragments

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.classes.Notification

/* Created by AdiJr in March 2020 for KNR PW */

class ConnectedScreenFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.connected_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Notification.createNotification(context!!, getString(R.string.notification_connected), getString(R.string.notification_connected_message), BitmapFactory.decodeResource(context!!.resources, R.drawable.bluetooth_on))
        val mMovementControlButton = view!!.findViewById<Button>(R.id.moveBtn)
        val mDisconnectButton = view!!.findViewById<Button>(R.id.disconnectBtn)
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        mMovementControlButton.setOnClickListener {
            findNavController().navigate(ConnectedScreenFragmentDirections.actionConnectedScreenFragmentToMovementScreenFragment())
        }

        mDisconnectButton.setOnClickListener {
            mBluetoothAdapter.disable()
        }
    }

    var isConnectedScreenFragment = false
    override fun onStart() {
        super.onStart()
        isConnectedScreenFragment = true
    }

    override fun onStop() {
        super.onStop()
        isConnectedScreenFragment = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = activity!!.getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.cancelAll()
        }
    }
}