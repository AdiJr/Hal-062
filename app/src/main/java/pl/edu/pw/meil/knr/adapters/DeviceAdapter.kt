package pl.edu.pw.meil.knr.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bluetooth_device.view.*
import pl.edu.pw.meil.knr.R

class DeviceAdapter(private val listener: (BluetoothDevice) -> Unit) : ListAdapter<BluetoothDevice, DeviceAdapter.DevicesViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DevicesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.item_bluetooth_device,
                    parent,
                    false
            )
    )

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) = holder.bind(getItem(position), listener)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BluetoothDevice>() {
            override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean =
                    oldItem.name!! == newItem.name

            override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean =
                    oldItem == newItem
        }
    }

    class DevicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(device: BluetoothDevice, listener: (BluetoothDevice) -> Unit) {
            itemView.apply {
                tvDeviceName.text = device.name
                tvDeviceAddress.text = device.address
                setOnClickListener { listener(device) }
            }
        }
    }
}