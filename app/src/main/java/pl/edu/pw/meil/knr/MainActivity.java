package pl.edu.pw.meil.knr;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private TextView mConnectionState;
    private TextView mConnectTextView;
    private ImageButton mConnectBtn;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBTDevice;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter listAdapter;
    public HalAPP mHallAPP;
    private ListView mDevicesList;
    private ProgressDialog mLoader;
    private LinkedHashSet<BluetoothDevice> linkedHashSet = new LinkedHashSet<>();
    private TextView mNewDevices;

    private final BroadcastReceiver mBondDevicesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Timber.d("BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                    mHallAPP.setBluetoothDevice(mBTDevice);
                    mHallAPP.startBTConnectionService(MainActivity.this);
                    mHallAPP.startBTConnection();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Timber.d("BroadcastReceiver: BOND_BONDING.");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Timber.d("BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    // Broadcast Receiver for listing devices that are not yet paired
    private BroadcastReceiver mDevicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            mLoader.cancel();
            listAdapter = new DeviceListAdapter(context, R.layout.list_item, mBTDevices);
            mDevicesList.setAdapter(listAdapter);

            Timber.d("onReceive: ACTION FOUND");
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                mConnectBtn.setVisibility(View.GONE);
                mConnectionState.setVisibility(View.GONE);
                mNewDevices.setVisibility(View.VISIBLE);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                linkedHashSet.addAll(mBTDevices);
                mBTDevices.clear();
                mBTDevices.addAll(linkedHashSet);
                listAdapter.notifyDataSetChanged();
                Timber.d("onReceive: " + device.getName() + ": " + device.getAddress());
            }
        }
    };
    // Receiver for listening to Bluetooth state changes
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Timber.i("Bluetooth OFF");
                        setContentView(R.layout.activity_main);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Timber.i("Bluetooth ON");
                        startDiscovering();
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStateReceiver);
        unregisterReceiver(mDevicesFoundReceiver);
        unregisterReceiver(mBondDevicesReceiver);
        mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnectBtn = findViewById(R.id.bluetooth_imageButton);
        mConnectionState = findViewById(R.id.connectionStateTextView);
        Button mMovementActivityButton = findViewById(R.id.moveBtn);
        ImageView mRoverImg = findViewById(R.id.connected_image);
        Button mDisconnectButton = findViewById(R.id.disconnectBtn);
        mDevicesList = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        mNewDevices = findViewById(R.id.newDevicesTxt);
        mNewDevices.setVisibility(View.GONE);
        mHallAPP = HalAPP.getInstance();
        mDevicesList.setOnItemClickListener(MainActivity.this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void startConnection(View view) {

        if (mBluetoothAdapter == null) {
            Toast toast = Toast.makeText(this, getString(R.string.no_bluetooth), Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBluetooth);

                IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBluetoothStateReceiver, btIntent);
            } else {
                startDiscovering();
            }
        }
    }

    public void startDiscovering() {
        mLoader = ProgressDialog.show(this, getString(R.string.loader_search), getString(R.string.loader_wait), true);

        Timber.d("Looking for unpaired devices...");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Timber.d("btnDiscover: Canceling discovery...");

            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent);
        } else {
            Toast toast = Toast.makeText(this, "Turn the Bluetooth on to begin searching for devices", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void checkBTPermissions() {
        int permissionCheck = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        }
        if (permissionCheck != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
    }

    public void disconnect(View view) {
        mBluetoothAdapter.disable();

        IntentFilter disableBtIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, disableBtIntent);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();

        String deviceName = mBTDevices.get(i).getName();
        Timber.d("onItemClick: deviceName = %s", deviceName);
        Timber.d("Trying to pair with %s", deviceName);
        mBTDevices.get(i).createBond();
        mBTDevice = mBTDevices.get(i);

        Timber.d("mBTDevice: %s", mBTDevice);

        if (mBTDevice != null) {
            mHallAPP.setBluetoothDevice(mBTDevice);
            mHallAPP.startBTConnectionService(MainActivity.this);
            mHallAPP.startBTConnection();

            setContentView(R.layout.devices_connected);
        } else Timber.e("Error BT Device is null");
    }

    public void roverMovement(View view) {
        Intent intent = new Intent(MainActivity.this, MovementActivity.class);
        this.startActivity(intent);
    }
}