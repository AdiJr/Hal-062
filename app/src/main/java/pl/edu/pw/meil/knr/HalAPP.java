package pl.edu.pw.meil.knr;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.UUID;

public class HalAPP extends Application {
    private static HalAPP singleton;
    private static int connectionStatus = 0; // zmienna odpowiadająca za możliwość wysyłania

    public static HalAPP getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public static int getConnectionStatus() {
        return connectionStatus;
    }

    public static void setConnectionStatus(int connection) {
        HalAPP.connectionStatus = connection;
    }
}
