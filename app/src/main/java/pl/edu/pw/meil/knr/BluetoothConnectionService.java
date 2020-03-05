package pl.edu.pw.meil.knr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import timber.log.Timber;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "Hal-062";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mBluetoothAdapter;
    private AcceptThread mInsecureAcceptThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private ConnectedThread mConnectedThread;

    BluetoothConnectionService(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void startClient(BluetoothDevice device, UUID uuid) {
        Timber.d("startClient: Started.");

        ConnectThread mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
        HalAPP.setConnectionStatus(1);
    }

    private void connected(BluetoothSocket mmSocket) {
        Timber.d("connected: Starting...");
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        Timber.d("write: Write Called...");
        mConnectedThread.write(out);
    }

    private static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            Timber.d("ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

       /* public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }*/

        //Call this from the main activity to send data to the remote device
        private void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Timber.d("write: Writing to outputstream: %s", text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Timber.e("write: Error writing to output stream. %s", e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Timber.d("AcceptThread: Setting up Server using: %s", MY_UUID_INSECURE);
            } catch (IOException e) {
                Timber.e("AcceptThread: IOException: %s", e.getMessage());
            }

            mmServerSocket = tmp;
        }

        public void run() {
            Timber.d("run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Timber.d("run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Timber.d("run: RFCOM server socket accepted connection.");

            } catch (IOException e) {
                Timber.e("AcceptThread: IOException: %s", e.getMessage());
            }

            //talk about this is in the 3rd
            if (socket != null) {
                connected(socket);
            }

            Timber.i("END mAcceptThread ");
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        ConnectThread(BluetoothDevice device, UUID uuid) {
            Timber.d("ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Timber.d("ConnectThread: Trying to create InsecureRfcommSocket using UUID: %s", MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Timber.e("ConnectThread: Could not create InsecureRfcommSocket %s", e.getMessage());
            }

            mmSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

                Timber.d("run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Timber.d("run: Closed Socket.");
                } catch (IOException e1) {
                    Timber.e("mConnectThread: run: Unable to close connection in socket %s", e1.getMessage());
                }
                Timber.d("run: ConnectThread: Could not connect to UUID: %s", MY_UUID_INSECURE);
            }
            connected(mmSocket);
        }
    }
}
