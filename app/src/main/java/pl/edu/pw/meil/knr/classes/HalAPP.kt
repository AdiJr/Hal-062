package pl.edu.pw.meil.knr.classes

import android.bluetooth.BluetoothDevice
import android.content.Context
import java.util.*

class HalAPP private constructor() {
    var bluetoothConnection: BluetoothConnectionService? = null
        private set
    private var mBluetoothDevice: BluetoothDevice? = null

    fun startBTConnectionService(context: Context) {
        bluetoothConnection = BluetoothConnectionService(context)
    }

    fun setBluetoothDevice(mBluetoothDevice: BluetoothDevice) {
        this.mBluetoothDevice = mBluetoothDevice
    }

    fun startBTConnection() {
        bluetoothConnection!!.startClient(mBluetoothDevice, MY_UUID_INSECURE)
    }

    companion object {
        private val MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var connectionStatus = 0
        var instance: HalAPP? = null
            get() {
                if (field == null) {
                    field = HalAPP()
                }
                return field
            }
            private set
    }
}