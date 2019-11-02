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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView connectionState;
    private TextView clickToConnect;
    private ImageButton connectBtn;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Button movementBtn;
    private Button armBtn;
    private Button disconnectBtn;
    private ImageView roverImg;

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

    public void disconnect(View view) {
        bluetoothAdapter.disable();

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, BTIntent);

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
}
