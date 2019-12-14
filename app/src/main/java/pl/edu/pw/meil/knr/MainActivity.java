package pl.edu.pw.meil.knr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private TextView connectionState;
    private TextView clickToConnect;
    private ImageButton connectBtn;
    private BluetoothConnectionService bluetoothConnectionService;
    private BluetoothAdapter bluetoothAdapter;
    private Button movementBtn;
    private Button disconnectBtn;
    private ImageView roverImg;
    private HalAPP halAPP = HalAPP.getInstance();
    private BluetoothDevice mBTDevice;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter listAdapter;
    private ListView devicesList;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");

                        movementBtn.setVisibility(View.INVISIBLE);
                        connectBtn.setVisibility(View.VISIBLE);
                        roverImg.setVisibility(View.INVISIBLE);
                        connectBtn.setBackgroundResource(R.drawable.button_normal);
                        connectBtn.setImageResource(R.drawable.bluetooth);
                        connectionState.setTextColor(getResources().getColor(R.color.colorAccent));
                        clickToConnect.setVisibility(View.INVISIBLE);
                        disconnectBtn.setVisibility(View.INVISIBLE);
                        connectionState.setText(getString(R.string.rover_disconnected));
                        clickToConnect.setVisibility(View.VISIBLE);
                        clickToConnect.setText(getString(R.string.connect));
                        devicesList.setVisibility(View.INVISIBLE);

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");

                        Snackbar.make(connectBtn, "Bluetooth turned on", BaseTransientBottomBar.LENGTH_LONG)
                                .show();

                        startDiscovering();

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                listAdapter = new DeviceListAdapter(context, R.layout.devices_list, mBTDevices);
                devicesList.setAdapter(listAdapter);
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                    Log.d(TAG, "mBTDevice = " + mDevice.toString());

                    new AlertDialog.Builder(MainActivity.this).setMessage("Pairing successful! Start connection?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    halAPP.setBluetoothDevice(mBTDevice);
                                    if (halAPP.getBluetoothDevice() != null) {
                                        halAPP.startBTConnectionService(MainActivity.this);
                                        halAPP.startBTConnection();

                                        connectionState.setText(getString(R.string.rover_connected));
                                        connectionState.setTextColor(getResources().getColor(R.color.green));
                                        connectionState.setTextSize(22);
                                        clickToConnect.setText(getString(R.string.disconnect));
                                        movementBtn.setVisibility(View.VISIBLE);
                                        connectBtn.setVisibility(View.INVISIBLE);
                                        roverImg.setVisibility(View.VISIBLE);
                                        disconnectBtn.setVisibility(View.VISIBLE);
                                        clickToConnect.setVisibility(View.INVISIBLE);

                                    } else Log.e(TAG, "Error BT Device is null");
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        bluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connectBtn);
        connectionState = findViewById(R.id.connectionStateTextView);
        clickToConnect = findViewById(R.id.clickToConnectTxt);
        movementBtn = findViewById(R.id.moveBtn);
        roverImg = findViewById(R.id.roverImg);
        disconnectBtn = findViewById(R.id.disconnectBtn);
        devicesList = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        devicesList.setOnItemClickListener(MainActivity.this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        movementBtn.setVisibility(View.INVISIBLE);
        roverImg.setVisibility(View.INVISIBLE);
        disconnectBtn.setVisibility(View.INVISIBLE);

        connectionState.setText(getString(R.string.rover_disconnected));
        connectionState.setTextColor(getResources().getColor(R.color.colorAccent));

        if (bluetoothAdapter.isEnabled()) {
            connectionState.setText(getString(R.string.rover_connected));
            connectionState.setTextColor(getResources().getColor(R.color.green));
            connectionState.setTextSize(22);
            clickToConnect.setText(getString(R.string.disconnect));
            movementBtn.setVisibility(View.VISIBLE);
            connectBtn.setVisibility(View.INVISIBLE);
            roverImg.setVisibility(View.VISIBLE);
            disconnectBtn.setVisibility(View.VISIBLE);
            clickToConnect.setVisibility(View.INVISIBLE);
        }

    }

    public void startConnection(View view) {

        if (bluetoothAdapter == null) {
            Toast toast = Toast.makeText(this, getString(R.string.no_bluetooth), Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBluetooth);

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBroadcastReceiver1, BTIntent);
            }
        }
    }

    public void startDiscovering() {

        Log.d(TAG, "Looking for paired devices...");

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                Log.d(TAG, pairedDevices.size() + " paired devices: " + deviceName);
                if (deviceName.equals("ADRIAN")) {
                    bluetoothAdapter.cancelDiscovery();
                }
            }
        }

        Log.d(TAG, "Looking for unpaired devices...");


        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery...");

            checkBTPermissions();
            bluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!bluetoothAdapter.isDiscovering()) {
            checkBTPermissions();
            bluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        } else {
            Toast toast = Toast.makeText(this, "Turn the Bluetooth on to begin searching for devices", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void checkBTPermissions() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }

    public void disconnect(View view) {
        bluetoothAdapter.disable();

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        bluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);

        //create the bond.
        Log.d(TAG, "Trying to pair with " + deviceName);
        mBTDevices.get(i).createBond();
        mBTDevice = mBTDevices.get(i);
    }

    public void roverMovement(View view) {
        Intent intent = new Intent(this, MovementActivity.class);
        startActivity(intent);
    }
}