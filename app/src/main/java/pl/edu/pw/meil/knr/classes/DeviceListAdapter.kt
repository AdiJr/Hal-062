package pl.edu.pw.meil.knr.classes

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import pl.edu.pw.meil.knr.R
import java.util.*

class DeviceListAdapter(context: Context, tvResourceId: Int, private val mDevices: ArrayList<BluetoothDevice>) : ArrayAdapter<BluetoothDevice?>(context, tvResourceId, mDevices as List<BluetoothDevice?>) {
    private val mLayoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mViewResourceId: Int = tvResourceId

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val convertView = mLayoutInflater.inflate(mViewResourceId, null)
        val device = mDevices[position]
        val deviceName = convertView.findViewById<TextView>(R.id.tvDeviceName)
        val deviceAddress = convertView.findViewById<TextView>(R.id.tvDeviceAddress)

        if (deviceName != null) {
            deviceName.text = device.name
        }
        if (deviceAddress != null) {
            deviceAddress.text = device.address
        }
        return convertView
    }
}