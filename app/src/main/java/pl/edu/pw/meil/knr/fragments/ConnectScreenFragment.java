package pl.edu.pw.meil.knr.fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import pl.edu.pw.meil.knr.MovementActivity;
import pl.edu.pw.meil.knr.R;
import pl.edu.pw.meil.knr.classes.DeviceListAdapter;
import pl.edu.pw.meil.knr.classes.HalAPP;
import pl.edu.pw.meil.knr.viewModels.ConnectScreenViewModel;
import timber.log.Timber;

public class ConnectScreenFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    private HalAPP mHallAPP;
    private TextView mConnectionState;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView mDevicesList;
    private LinkedHashSet<BluetoothDevice> linkedHashSet = new LinkedHashSet<>();
    private TextView mNewDevices;
    private TextView mNewDevices2;
    private LottieAnimationView mBluetoothAnimation;
    private LottieAnimationView mRobotAnimation;
    private ConnectScreenViewModel mViewModel;

    //Broadcast Receiver for listing devices that are not yet paired
    private BroadcastReceiver mDevicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            mBluetoothAnimation.setVisibility(View.GONE);
            mRobotAnimation.setVisibility(View.VISIBLE);
            mConnectionState.setVisibility(View.GONE);
            mNewDevices2.setVisibility(View.VISIBLE);
            mNewDevices.setText(R.string.connectInfoTV);
            mNewDevices2.setText(R.string.connectInfoTV2);
            DeviceListAdapter listAdapter = new DeviceListAdapter(context, R.layout.list_item, mBTDevices);
            mDevicesList.setAdapter(listAdapter);

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Timber.d("onReceive: " + device.getName() + ": " + device.getAddress());

                mBTDevices.add(device);
                linkedHashSet.addAll(mBTDevices);
                mBTDevices.clear();
                mBTDevices.addAll(linkedHashSet);
                listAdapter.notifyDataSetChanged();
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
                        getActivity().setContentView(R.layout.connect_screen_fragment);
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

    public static ConnectScreenFragment newInstance() {
        return new ConnectScreenFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBluetoothStateReceiver);
        getActivity().unregisterReceiver(mDevicesFoundReceiver);
        mBluetoothAdapter.cancelDiscovery();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.connect_screen_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBluetoothAnimation = getView().findViewById(R.id.bluetoothAnimation);
        mRobotAnimation = getView().findViewById(R.id.robotAnimation);
        mBluetoothAnimation.pauseAnimation();
        mNewDevices = getView().findViewById(R.id.connectInfoTv);
        mNewDevices2 = getView().findViewById(R.id.connectInfoTv2);
        mConnectionState = getView().findViewById(R.id.connectionStateTextView);
        Button mMovementActivityButton = getView().findViewById(R.id.moveBtn);
        ImageView mRoverImg = getView().findViewById(R.id.connected_image);
        Button mDisconnectButton = getView().findViewById(R.id.disconnectBtn);
        mDevicesList = getView().findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();
        mHallAPP = HalAPP.getInstance();
        mDevicesList.setOnItemClickListener(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mViewModel = ViewModelProviders.of(getActivity()).get(ConnectScreenViewModel.class);
        // TODO: Use the ViewModel

        mBluetoothAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection(v);
            }
        });
    }

    private void startConnection(View view) {

        if (mBluetoothAdapter == null) {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.no_bluetooth), Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBluetooth);

                IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                getActivity().registerReceiver(mBluetoothStateReceiver, btIntent);
            } else {
                startDiscovering();
            }
        }
    }

    private void startDiscovering() {
        Timber.d("Looking for unpaired devices...");
        mBluetoothAnimation.resumeAnimation();

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Timber.d("btnDiscover: Canceling discovery...");

            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mDevicesFoundReceiver, discoverDevicesIntent);
        } else {
            Toast toast = Toast.makeText(getActivity(), "Turn the Bluetooth on to begin searching for devices", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void checkBTPermissions() {
        int permissionCheck = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck = getActivity().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += getActivity().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        }
        if (permissionCheck != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
    }

    public void disconnect(View view) {
        mBluetoothAdapter.disable();

        IntentFilter disableBtIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(mBluetoothStateReceiver, disableBtIntent);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();

        String deviceName = mBTDevices.get(i).getName();
        BluetoothDevice mBTDevice = mBTDevices.get(i);
        Timber.d("mBTDevice: %s", mBTDevice);

        if (mBTDevice != null) {
            if (mBTDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                Timber.i("Device not bonded");
                mBTDevices.get(i).createBond();
                if (mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Timber.i("Device bonded");
                    mHallAPP.setBluetoothDevice(mBTDevice);
                    mHallAPP.startBTConnectionService(getActivity());
                    mHallAPP.startBTConnection();
                    getActivity().setContentView(R.layout.devices_connected);
                }
            }
            if (mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                Timber.i("Device bonded");
                mHallAPP.setBluetoothDevice(mBTDevice);
                mHallAPP.startBTConnectionService(getActivity());
                mHallAPP.startBTConnection();
                getActivity().setContentView(R.layout.devices_connected);
            }
        } else Timber.e("Error BT Device is null");
    }

    public void roverMovement(View view) {
        Intent intent = new Intent(getActivity(), MovementActivity.class);
        getActivity().startActivity(intent);
    }

}
