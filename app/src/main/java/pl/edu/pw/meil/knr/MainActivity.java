package pl.edu.pw.meil.knr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private TextView connectionState;
    private TextView clickToConnect;
    private ImageButton connectBtn;
    private BluetoothConnectionService bluetoothConnectionService;
    private BluetoothAdapter bluetoothAdapter;
    private Button movementBtn;
    private Button armBtn;
    private Button disconnectBtn;
    private ImageView roverImg;
    private HalAPP halAPP = HalAPP.getInstance();
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
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
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
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
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
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
        unregisterReceiver(mBroadcastReceiver2);
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
        armBtn = findViewById(R.id.armBtn);
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
        armBtn.setVisibility(View.INVISIBLE);
        roverImg.setVisibility(View.INVISIBLE);
        disconnectBtn.setVisibility(View.INVISIBLE);

        connectionState.setText(getString(R.string.rover_disconnected));
        connectionState.setTextColor(getResources().getColor(R.color.colorAccent));

        if (bluetoothAdapter.isEnabled()) {
            connectionState.setText(getString(R.string.rover_connected));
            connectionState.setTextColor(getResources().getColor(R.color.green));
            connectionState.setTextSize(22);
            clickToConnect.setText(getString(R.string.disconnect));
            armBtn.setVisibility(View.VISIBLE);
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
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "btnDiscover: Canceling discovery.");

                //check BT permissions in manifest
                checkBTPermissions();

                bluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
            if (!bluetoothAdapter.isDiscovering()) {

                //check BT permissions in manifest
                checkBTPermissions();

                bluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
        }


/*        connectionState.setText(getString(R.string.rover_connected));
        connectionState.setTextColor(getResources().getColor(R.color.green));
        connectionState.setTextSize(22);
        clickToConnect.setText(getString(R.string.disconnect));
        armBtn.setVisibility(View.VISIBLE);
        movementBtn.setVisibility(View.VISIBLE);
        connectBtn.setVisibility(View.INVISIBLE);
        roverImg.setVisibility(View.VISIBLE);
        disconnectBtn.setVisibility(View.VISIBLE);
        clickToConnect.setVisibility(View.INVISIBLE);*/
    }

    private void checkBTPermissions() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }

    public void connect(View v) {

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


    public void disconnect(View view) {
        bluetoothAdapter.disable();

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);

        if (!bluetoothAdapter.isEnabled()) {
            armBtn.setVisibility(View.INVISIBLE);
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
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        bluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

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
