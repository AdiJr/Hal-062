package pl.edu.pw.meil.knr.classes

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

class BluetoothConnectionService(context: Context?) {
    private val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mmDevice: BluetoothDevice? = null
    private var deviceUUID: UUID? = null
    private var mConnectedThread: ConnectedThread? = null

    fun startClient(device: BluetoothDevice?, uuid: UUID?) {
        Timber.d("startClient: Started.")
        val mConnectThread = ConnectThread(device, uuid)
        mConnectThread.start()
        HalAPP.connectionStatus = 1
    }

    private fun connected(mmSocket: BluetoothSocket?) {
        Timber.d("connected: Starting...")
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(mmSocket)
        mConnectedThread!!.start()
    }

    fun write(out: ByteArray) {
        Timber.d("write: Write Called...")
        mConnectedThread!!.write(out)
    }

    private class ConnectedThread internal constructor(socket: BluetoothSocket?) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(1024) // buffer store for the stream
            var bytes: Int // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) { // Read from the InputStream
                try {
                    bytes = mmInStream!!.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)
                    Timber.d("InputStream: %s", incomingMessage)
                } catch (e: IOException) {
                    Timber.e("write: Error reading Input Stream. %s", e.message)
                    break
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        fun write(bytes: ByteArray) {
            val text = String(bytes, Charset.defaultCharset())
            Timber.d("write: Writing to outputstream: %s", text)
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Timber.e("write: Error writing to output stream. %s", e.message)
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (ignored: IOException) {
            }
        }

        init {
            Timber.d("ConnectedThread: Starting.")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = mmSocket!!.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    private inner class AcceptThread : Thread() {
        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?

        override fun run() {
            Timber.d("run: AcceptThread Running.")
            var socket: BluetoothSocket? = null
            try { // This is a blocking call and will only return on a
                // successful connection or an exception
                Timber.d("run: RFCOM server socket start.....")
                socket = mmServerSocket!!.accept()
                Timber.d("run: RFCOM server socket accepted connection.")
            } catch (e: IOException) {
                Timber.e("AcceptThread: IOException: %s", e.message)
            }
            //talk about this is in the 3rd
            socket?.let { connected(it) }
            Timber.i("END mAcceptThread ")
        }

        init {
            var tmp: BluetoothServerSocket? = null
            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE)
                Timber.d("AcceptThread: Setting up Server using: %s", MY_UUID_INSECURE)
            } catch (e: IOException) {
                Timber.e("AcceptThread: IOException: %s", e.message)
            }
            mmServerSocket = tmp
        }
    }

    private inner class ConnectThread internal constructor(device: BluetoothDevice?, uuid: UUID?) : Thread() {
        private var mmSocket: BluetoothSocket? = null
        override fun run() {
            var tmp: BluetoothSocket? = null
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Timber.d("ConnectThread: Trying to create InsecureRfcommSocket using UUID: %s", MY_UUID_INSECURE)
                tmp = mmDevice!!.createRfcommSocketToServiceRecord(deviceUUID)
            } catch (e: IOException) {
                Timber.e("ConnectThread: Could not create InsecureRfcommSocket %s", e.message)
            }
            mmSocket = tmp
            mBluetoothAdapter.cancelDiscovery()
            // Make a connection to the BluetoothSocket
            try { // This is a blocking call and will only return on a
// successful connection or an exception
                mmSocket!!.connect()
                Timber.d("run: ConnectThread connected.")
            } catch (e: IOException) { // Close the socket
                try {
                    mmSocket!!.close()
                    Timber.d("run: Closed Socket.")
                } catch (e1: IOException) {
                    Timber.e("mConnectThread: run: Unable to close connection in socket %s", e1.message)
                }
                Timber.d("run: ConnectThread: Could not connect to UUID: %s", MY_UUID_INSECURE)
            }
            connected(mmSocket)
        }

        init {
            Timber.d("ConnectThread: started.")
            mmDevice = device
            deviceUUID = uuid
        }
    }

    companion object {
        private const val appName = "Hal-062"
        private val MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

}