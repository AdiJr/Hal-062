package pl.edu.pw.meil.knr.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.connect_screen_fragment.view.*
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.adapters.DeviceAdapter
import pl.edu.pw.meil.knr.classes.HalAPP
import pl.edu.pw.meil.knr.classes.Notification.createNotification
import pl.edu.pw.meil.knr.classes.Notification.createNotificationChannel
import timber.log.Timber
import java.util.*

/* Created by AdiJr in March 2020 for KNR PW */

const val CHANNEL_ID = "Hal-062_CHANNEL"

class ConnectScreenFragment : Fragment() {
    private var mBTDevices = ArrayList<BluetoothDevice>()
    private var mHallAPP: HalAPP? = null
    private var mConnectionState: TextView? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val linkedHashSet = LinkedHashSet<BluetoothDevice>()
    private var mNewDevices: TextView? = null
    private var mNewDevices2: TextView? = null
    private var mNewDevices3: TextView? = null
    private var mBluetoothAnimation: LottieAnimationView? = null
    private var mDevicesList: RecyclerView? = null
    private var isActive = false
    private val devicesAdapter by lazy {
        DeviceAdapter {
            mBluetoothAdapter!!.cancelDiscovery()

            if (it.bondState == BluetoothDevice.BOND_NONE) {
                Timber.i("Devices not bonded")
                it.createBond()
                Toast.makeText(activity, getString(R.string.toast_clickAgain), Toast.LENGTH_LONG).show()
            }

            if (it.bondState == BluetoothDevice.BOND_BONDED) {
                Timber.i("Device bonded")
                mHallAPP!!.setBluetoothDevice(it)
                mHallAPP!!.startBTConnectionService(context!!)
                mHallAPP!!.startBTConnection()
                findNavController().navigate(ConnectScreenFragmentDirections.actionConnectScreenToConnectedScreenFragment())
            }
        }
    }

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
        val rootView = inflater.inflate(R.layout.connect_screen_fragment, container, false)
        rootView.rvNewDevices.adapter = devicesAdapter.apply { submitList(mBTDevices) }
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mBluetoothAnimation = view!!.findViewById(R.id.bluetoothAnimation)
        mBluetoothAnimation!!.cancelAnimation()
        mNewDevices = view!!.findViewById(R.id.connectInfoTv)
        mNewDevices2 = view!!.findViewById(R.id.connectInfoTv2)
        mNewDevices3 = view!!.findViewById(R.id.connectInfoTv3)
        mConnectionState = view!!.findViewById(R.id.connectionStateTextView)
        mDevicesList = view!!.findViewById(R.id.rvNewDevices)
        mHallAPP = HalAPP.instance
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        createNotificationChannel(context!!)
        createNotification(context!!, getString(R.string.rover_disconnected), getString(R.string.notification_bt_off), null)

        mBluetoothAnimation!!.setOnClickListener {
            startConnection()
        }
    }

    private val mDevicesFoundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            mBluetoothAnimation!!.visibility = View.GONE
            mConnectionState!!.visibility = View.GONE
            mNewDevices2!!.visibility = View.VISIBLE
            mNewDevices3!!.visibility = View.VISIBLE
            mDevicesList!!.visibility = View.VISIBLE
            mNewDevices!!.setText(R.string.connectInfoTV)
            mNewDevices2!!.setText(R.string.connectInfoTV2)
            mNewDevices3!!.setText(R.string.connectInfoTV3)

            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Timber.d("onReceive: %s, %s ", device!!.name, device.address)
                mBTDevices.add(device)
                linkedHashSet.addAll(mBTDevices)
                mBTDevices.clear()
                mBTDevices.addAll(linkedHashSet)
                createNotification(context, getString(R.string.notifiication_dv_found), getString(R.string.notification_dv_number) + "${mBTDevices.size}", null)
            }
        }
    }

    private val mBluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action!!
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        Timber.i("Bluetooth OFF")
                        createNotification(context, getString(R.string.rover_disconnected), getString(R.string.notification_bt_off), null)
                        if (isActive) {
                            showAlertDialog()
                        } else {
                            activity!!.let {
                                val builder = AlertDialog.Builder(it)
                                builder.apply {
                                    setMessage(getString(R.string.alert_bt_off))
                                    setPositiveButton(getString(R.string.alert_homepage)) { _, _ ->

                                        if (ConnectedScreenFragment().isConnectedScreenFragment) {
                                            findNavController().navigate(ConnectedScreenFragmentDirections.actionConnectedScreenFragmentToConnectScreen())
                                        } else {
                                            if (MovementScreenFragment().isMovementScreenFragment)
                                                findNavController().navigate(MovementScreenFragmentDirections.actionMovementScreenFragmentToConnectScreen())
                                        }
                                    }
                                }
                                builder.create().show()
                            }
                        }
                    }
                    BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_TURNING_ON -> {
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Timber.i("Bluetooth ON")
                        startConnection()
                    }
                }
            }
        }
    }

    private fun showAlertDialog() {
        activity!!.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage(getString(R.string.alert_bt_off))
                setPositiveButton("OK") { dialog, _ ->
                    dialog.cancel()
                }
            }
            builder.create().show()
        }
        mDevicesList!!.visibility = View.GONE
        mBluetoothAnimation!!.cancelAnimation()
        mBluetoothAnimation!!.visibility = View.VISIBLE
        mConnectionState!!.visibility = View.VISIBLE
        mNewDevices2!!.visibility = View.GONE
        mNewDevices3!!.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        isActive = true
    }

    override fun onStop() {
        super.onStop()
        isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = activity!!.getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.cancelAll()
        }
        activity!!.unregisterReceiver(mBluetoothStateReceiver)
        activity!!.unregisterReceiver(mDevicesFoundReceiver)
        mBluetoothAdapter!!.cancelDiscovery()
    }


    @RequiresApi(Build.VERSION_CODES.M)
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startDiscovering() {
        Timber.d("Looking for unpaired devices...")

        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
            Timber.d("btnDiscover: Canceling discovery...")
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            createNotification(activity!!, getString(R.string.rover_disconnected), getString(R.string.notification_bt_on), BitmapFactory.decodeResource(activity!!.resources, R.drawable.bluetooth_search))
            mBluetoothAnimation!!.resumeAnimation()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            activity!!.registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent)
        }
        if (!mBluetoothAdapter!!.isDiscovering) {
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            createNotification(activity!!, getString(R.string.rover_disconnected), getString(R.string.notification_bt_on), BitmapFactory.decodeResource(activity!!.resources, R.drawable.bluetooth_search))
            mBluetoothAnimation!!.resumeAnimation()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            activity!!.registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkBTPermissions() {
        if (activity!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && activity!!.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity!!.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }
}