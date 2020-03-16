package pl.edu.pw.meil.knr.fragments

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.viewModels.ConnectedScreenViewModel
import timber.log.Timber

/* Created by AdiJr in March 2020 for KNR PW */

class ConnectedScreenFragment : Fragment() {

    private var mMovementControlButton: Button? = null
    private var mDisconnectButton: Button? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null

    companion object {
        fun newInstance() = ConnectedScreenFragment()
    }

    private lateinit var viewModel: ConnectedScreenViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.connected_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
            val disableBtIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            activity!!.registerReceiver(mBluetoothStateReceiver, disableBtIntent)
        }
    }

    // Receiver for listening to Bluetooth state changes
    private val mBluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action!!
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        Timber.i("Bluetooth OFF")
                        findNavController().navigate(ConnectedScreenFragmentDirections.actionConnectedScreenFragmentToConnectScreen())
                    }
                    BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_TURNING_ON -> {
                    }
                }
            }
        }
    }

}