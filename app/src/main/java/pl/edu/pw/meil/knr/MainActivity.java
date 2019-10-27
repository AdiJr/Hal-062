package pl.edu.pw.meil.knr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView connectionState;
    private TextView clickToConnect;
    private ImageButton connectBtn;
    private boolean isClicked = true;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connectBtn);
        connectionState = findViewById(R.id.connectionStateTextView);
        clickToConnect = findViewById(R.id.clickToConnectTxt);

        connectionState.setText(getString(R.string.rover_disconnected));
        connectionState.setTextColor(getResources().getColor(R.color.colorAccent));
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
            }
        }
    };

    public void connect(View v) {

        if (isClicked) {

            if (bluetoothAdapter == null) {
                Toast toast = Toast.makeText(this, getString(R.string.no_bluetooth), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBluetooth);

                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(receiver, BTIntent);
                }
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                }
            }
            bluetoothAdapter.startDiscovery();

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);


            connectBtn.setBackgroundResource(R.drawable.button_pressed);
            connectBtn.setImageResource(R.drawable.rover);
            connectionState.setText(getString(R.string.rover_connected));
            connectionState.setTextColor(getResources().getColor(R.color.green));
            clickToConnect.setText(getString(R.string.disconnect));

            isClicked = false;
        } else {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(receiver, BTIntent);
            }

            connectBtn.setBackgroundResource(R.drawable.button_normal);
            connectBtn.setImageResource(R.drawable.bluetooth);
            connectionState.setText(getString(R.string.rover_disconnected));
            connectionState.setTextColor(getResources().getColor(R.color.colorAccent));
            clickToConnect.setText(R.string.btnDcs);

            isClicked = true;
        }
    }
}
