package pl.edu.pw.meil.knr.fragments

import android.app.AlertDialog
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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.classes.Notification
import pl.edu.pw.meil.knr.viewModels.ConnectedScreenViewModel

/* Created by AdiJr in March 2020 for KNR PW */

class ConnectedScreenFragment : Fragment() {

    private var mMovementControlButton: Button? = null
    private var mDisconnectButton: Button? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var viewModel: ConnectedScreenViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.connected_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Notification.createNotification(context!!, "Rover Connected", "You can begin steering the Rover", BitmapFactory.decodeResource(context!!.resources, R.drawable.bluetooth_on))
        mMovementControlButton = view!!.findViewById(R.id.moveBtn)
        mDisconnectButton = view!!.findViewById(R.id.disconnectBtn)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        viewModel = ViewModelProviders.of(this).get(ConnectedScreenViewModel::class.java)
        // TODO: Use the ViewModel

        mMovementControlButton!!.setOnClickListener {
            findNavController().navigate(ConnectedScreenFragmentDirections.actionConnectedScreenFragmentToMovementScreenFragment())
        }

        mDisconnectButton!!.setOnClickListener {
            mBluetoothAdapter!!.disable()
            showNoBTAlertDialog()
        }
    }

    private fun showNoBTAlertDialog() {
        activity!!.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Bluetooth is OFF, please turn it on and connect again")
                setPositiveButton("HomePage") { _, _ ->
                    findNavController().navigate(ConnectedScreenFragmentDirections.actionConnectedScreenFragmentToConnectScreen())
                }
            }
            builder.create().show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = activity!!.getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.cancelAll()
        }
    }
}