package pl.edu.pw.meil.knr.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.classes.DeviceListAdapter
import pl.edu.pw.meil.knr.classes.HalAPP
import pl.edu.pw.meil.knr.viewModels.ConnectScreenViewModel
import timber.log.Timber
import java.util.*

/* Created by AdiJr in March 2020 for KNR PW */

class ConnectScreenFragment : Fragment(), OnItemClickListener {
    private var mBTDevices = ArrayList<BluetoothDevice>()
    private var mHallAPP: HalAPP? = null
    private var mConnectionState: TextView? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mDevicesList: ListView? = null
    private val linkedHashSet = LinkedHashSet<BluetoothDevice>()
    private var mNewDevices: TextView? = null
    private var mNewDevices2: TextView? = null
    private var mNewDevices3: TextView? = null
    private var mBluetoothAnimation: LottieAnimationView? = null
    private var mViewModel: ConnectScreenViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        activity?.finish()
                    }
                })
        return inflater.inflate(R.layout.connect_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mBluetoothAnimation = view!!.findViewById(R.id.bluetoothAnimation)
        mBluetoothAnimation!!.pauseAnimation()
        mNewDevices = view!!.findViewById(R.id.connectInfoTv)
        mNewDevices2 = view!!.findViewById(R.id.connectInfoTv2)
        mNewDevices3 = view!!.findViewById(R.id.connectInfoTv3)
        mConnectionState = view!!.findViewById(R.id.connectionStateTextView)
        mDevicesList = view!!.findViewById(R.id.lvNewDevices)
        mBTDevices = ArrayList()
        mHallAPP = HalAPP.instance
        mDevicesList!!.onItemClickListener = this
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        mViewModel = ViewModelProviders.of(activity!!).get(ConnectScreenViewModel::class.java)
        // TODO: Use the ViewModel
        mBluetoothAnimation!!.setOnClickListener {
            startConnection()
        }
    }

    //Broadcast Receiver for listing devices that are not yet paired
    private val mDevicesFoundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            mBluetoothAnimation!!.visibility = View.GONE
            mConnectionState!!.visibility = View.GONE
            mNewDevices2!!.visibility = View.VISIBLE
            mNewDevices3!!.visibility = View.VISIBLE
            mNewDevices!!.setText(R.string.connectInfoTV)
            mNewDevices2!!.setText(R.string.connectInfoTV2)
            mNewDevices3!!.setText(R.string.connectInfoTV3)

            val listAdapter = DeviceListAdapter(context, R.layout.list_item, mBTDevices)
            mDevicesList!!.adapter = listAdapter
            assert(action != null)
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Timber.d("%s%s", "onReceive: " + device.name + ": ", device.address)
                mBTDevices.add(device)
                linkedHashSet.addAll(mBTDevices)
                mBTDevices.clear()
                mBTDevices.addAll(linkedHashSet)
                listAdapter.notifyDataSetChanged()
            }
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
                        mBluetoothAnimation!!.pauseAnimation()
                    }
                    BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_TURNING_ON -> {
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Timber.i("Bluetooth ON")
                        startDiscovering()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity!!.unregisterReceiver(mBluetoothStateReceiver)
        activity!!.unregisterReceiver(mDevicesFoundReceiver)
        mBluetoothAdapter!!.cancelDiscovery()
    }


    private fun startConnection() {
        if (mBluetoothAdapter == null) {
            val toast = Toast.makeText(activity, getString(R.string.no_bluetooth), Toast.LENGTH_SHORT)
            toast.show()
        } else {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enableBluetooth)
                val btIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                activity!!.registerReceiver(mBluetoothStateReceiver, btIntent)
            } else {
                startDiscovering()
            }
        }
    }

    private fun startDiscovering() {
        Timber.d("Looking for unpaired devices...")
        mBluetoothAnimation!!.resumeAnimation()

        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
            Timber.d("btnDiscover: Canceling discovery...")
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            activity!!.registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent)
        }
        if (!mBluetoothAdapter!!.isDiscovering) {
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            activity!!.registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent)
        } else {
            val toast = Toast.makeText(activity, "Turn the Bluetooth on to begin searching for devices", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    private fun checkBTPermissions() {
        var permissionCheck = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck = activity!!.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            permissionCheck += activity!!.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
        }
        if (permissionCheck != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity!!.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1001) //Any number
            }
        }
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
        mBluetoothAdapter!!.cancelDiscovery()
        mBTDevices[i].name
        val mBTDevice = mBTDevices[i]
        Timber.d("mBTDevice: %s", mBTDevice)

        if (mBTDevice.bondState == BluetoothDevice.BOND_NONE) {
            Timber.i("Devices not bonded")
            mBTDevices[i].createBond()
            Toast.makeText(activity, "Click again to start connection", Toast.LENGTH_LONG).show()
        }

        if (mBTDevice.bondState == BluetoothDevice.BOND_BONDED) {
            Timber.i("Device bonded")
            mHallAPP!!.setBluetoothDevice(mBTDevice)
            mHallAPP!!.startBTConnectionService(activity)
            mHallAPP!!.startBTConnection()
            findNavController().navigate(ConnectScreenFragmentDirections.actionConnectScreenToConnectedScreenFragment())
        }
    }

    companion object {
        fun newInstance(): ConnectScreenFragment {
            return ConnectScreenFragment()
        }
    }
}