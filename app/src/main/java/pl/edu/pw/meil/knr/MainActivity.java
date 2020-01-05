package pl.edu.pw.meil.knr;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private TextView mConnectionState;
    private TextView mConnectTextView;
    private ImageButton mConnectBtn;
    private BluetoothAdapter mBluetoothAdapter;
    private Button mMovementActivityButton;
    private Button mDisconnectButton;
    private ImageView mRoverImg;
    private BluetoothDevice mBTDevice;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter listAdapter;
    public HalAPP mHallAPP;
    private ListView mDevicesList;
    private ProgressDialog mLoader;
    private AlertDialog.Builder newDevicesDialog;
    private LinkedHashSet<BluetoothDevice> linkedHashSet = new LinkedHashSet<>();

    // Receiver for listening to Bluetooth state changes
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:

                        mMovementActivityButton.setVisibility(View.INVISIBLE);
                        mConnectBtn.setVisibility(View.VISIBLE);
                        mRoverImg.setVisibility(View.INVISIBLE);
                        mConnectBtn.setImageResource(R.drawable.bluetooth);
                        mConnectionState.setTextColor(getResources().getColor(R.color.colorAccent));
                        mConnectTextView.setVisibility(View.INVISIBLE);
                        mDisconnectButton.setVisibility(View.INVISIBLE);
                        mConnectionState.setText(getString(R.string.rover_disconnected));
                        mConnectTextView.setVisibility(View.VISIBLE);
                        mConnectTextView.setText(getString(R.string.connect));
                        mDevicesList.setVisibility(View.INVISIBLE);
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:

                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;

                    case BluetoothAdapter.STATE_ON:
                        startDiscovering();
                        break;
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

            Log.d(TAG, "onReceive: ACTION FOUND");
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);

                linkedHashSet.addAll(mBTDevices);
                mBTDevices.clear();
                mBTDevices.addAll(linkedHashSet);

                if (device.getName().equals("Elon_Musk")) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
            }
            listAdapter = new DeviceListAdapter(context, R.layout.devices_list, mBTDevices);
            mDevicesList.setAdapter(listAdapter);

            showDialog();
        }
    };

    private final BroadcastReceiver mBondDevicesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            assert action != null;

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;

                    new AlertDialog.Builder(MainActivity.this).setMessage("Pairing successful! Start connection?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mBTDevice != null) {

                                        mHallAPP.setBluetoothDevice(mBTDevice);
                                        mHallAPP.startBTConnectionService(MainActivity.this);
                                        mHallAPP.startBTConnection();

                                        mConnectionState.setText(getString(R.string.rover_connected));
                                        mConnectionState.setTextColor(getResources().getColor(R.color.green));
                                        mConnectionState.setTextSize(22);
                                        mConnectTextView.setText(getString(R.string.disconnectBrn));
                                        mMovementActivityButton.setVisibility(View.VISIBLE);
                                        mConnectBtn.setVisibility(View.INVISIBLE);
                                        mRoverImg.setVisibility(View.VISIBLE);
                                        mDisconnectButton.setVisibility(View.VISIBLE);
                                        mConnectTextView.setVisibility(View.INVISIBLE);

                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                }
                //case2: creating a bond
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
        mConnectTextView = findViewById(R.id.clickToConnectTxt);
        mMovementActivityButton = findViewById(R.id.moveBtn);
        mRoverImg = findViewById(R.id.connected_image);
        mDisconnectButton = findViewById(R.id.disconnectBtn);
        mDevicesList = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        newDevicesDialog = new AlertDialog.Builder(this);

        mHallAPP = HalAPP.getInstance();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondDevicesReceiver, filter);

        mDevicesList.setOnItemClickListener(MainActivity.this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mMovementActivityButton.setVisibility(View.INVISIBLE);
        mRoverImg.setVisibility(View.INVISIBLE);
        mDisconnectButton.setVisibility(View.INVISIBLE);

        mConnectionState.setText(getString(R.string.rover_disconnected));
        mConnectionState.setTextColor(getResources().getColor(R.color.colorAccent));
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
        mLoader = ProgressDialog.show(this, "Searching...", "Please wait while searching for " +
                "Bluetooth devices", true);

        Log.d(TAG, "Looking for unpaired devices...");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery...");

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

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, BTIntent);
    }

    public void showDialog() {
        newDevicesDialog.setTitle("Available devices");
        newDevicesDialog.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBluetoothAdapter.cancelDiscovery();

                String deviceName = mBTDevices.get(which).getName();
                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "Trying to pair with " + deviceName);
                mBTDevices.get(which).createBond();
                mBTDevice = mBTDevices.get(which);

                new AlertDialog.Builder(MainActivity.this).setMessage("Start connection?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mBTDevice != null) {

                                    mHallAPP.setBluetoothDevice(mBTDevice);
                                    mHallAPP.startBTConnectionService(MainActivity.this);
                                    mHallAPP.startBTConnection();

                                    mDevicesList.setVisibility(View.GONE);
                                    mConnectionState.setText(getString(R.string.rover_connected));
                                    mConnectionState.setTextColor(getResources().getColor(R.color.green));
                                    mConnectionState.setTextSize(22);
                                    mConnectTextView.setText(getString(R.string.btnDcs));
                                    mMovementActivityButton.setVisibility(View.VISIBLE);
                                    mConnectBtn.setVisibility(View.INVISIBLE);
                                    mRoverImg.setVisibility(View.VISIBLE);
                                    mDisconnectButton.setVisibility(View.VISIBLE);
                                    mConnectTextView.setVisibility(View.INVISIBLE);

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
        });
        newDevicesDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();

        String deviceName = mBTDevices.get(i).getName();
        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "Trying to pair with " + deviceName);
        mBTDevices.get(i).createBond();
        mBTDevice = mBTDevices.get(i);

        Log.d(TAG, "mBTDevice: " + mBTDevice);

        new AlertDialog.Builder(MainActivity.this).setMessage("Start connection?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mBTDevice != null) {

                            mHallAPP.setBluetoothDevice(mBTDevice);
                            mHallAPP.startBTConnection();
                            mHallAPP.startBTConnectionService(MainActivity.this);

                            mDevicesList.setVisibility(View.GONE);
                            mConnectionState.setText(getString(R.string.rover_connected));
                            mConnectionState.setTextColor(getResources().getColor(R.color.green));
                            mConnectionState.setTextSize(22);
                            mConnectTextView.setText(getString(R.string.disconnectBrn));
                            mMovementActivityButton.setVisibility(View.VISIBLE);
                            mConnectBtn.setVisibility(View.INVISIBLE);
                            mRoverImg.setVisibility(View.VISIBLE);
                            mDisconnectButton.setVisibility(View.VISIBLE);
                            mConnectTextView.setVisibility(View.INVISIBLE);

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

    public void roverMovement(View view) {
        Intent intent = new Intent(MainActivity.this, MovementActivity.class);
        this.startActivity(intent);
    }
}