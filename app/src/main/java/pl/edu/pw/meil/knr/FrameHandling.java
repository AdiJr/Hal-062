package pl.edu.pw.meil.knr;

import androidx.appcompat.app.AppCompatActivity;

public class FrameHandling extends AppCompatActivity {
    private BluetoothConnectionService bluetoothConnectionService = new BluetoothConnectionService(getApplicationContext());


    public void sendFrameInt(int tag, int length, int[] output) {    //metoda do wysyłania ramek intów
        byte[] outputTab = new byte[19];
        String y = "#";
        byte[] z1 = y.getBytes();
        outputTab[0] = z1[0];
        //bluetoothConnectionService.write(z1);
        String hex = Integer.toHexString(tag);
        y = "" + hex.charAt(0);
        z1 = y.getBytes();
        outputTab[1] = z1[0];

        //bluetoothConnectionService.write(z1);
        y = "" + hex.charAt(1);
        z1 = y.getBytes();
        outputTab[2] = z1[0];
        //bluetoothConnectionService.write(z1);


        for (int i = 0; i < length; i++) {
            //if(output[i]<0)
            //{
            int unsignedByte = (byte) output[i] & (0xff);
            //byte outputbyte=(byte)(255-unsignedByte);
            hex = String.format("%02x", unsignedByte).toUpperCase();
                    /*
                }else {
                    hex = String.format("%02x", (byte) output[i]);
                }*/
            y = "" + hex.charAt(0);
            z1 = y.getBytes();
            outputTab[i * 2 + 3] = z1[0];
            //bluetoothConnectionService.write(z1);
            y = "" + hex.charAt(1);
            z1 = y.getBytes();
            outputTab[i * 2 + 4] = z1[0];
            //bluetoothConnectionService.write(z1);
        }
        for (int i = 0; i < 8 - length; i++) {
            y = "x";
            z1 = y.getBytes();
            outputTab[i * 2 + 3 + 2 * length] = z1[0];
            //bluetoothConnectionService.write(z1);

            y = "x";
            z1 = y.getBytes();
            outputTab[i * 2 + 4 + 2 * length] = z1[0];
            //bluetoothConnectionService.write(z1);

        }
        bluetoothConnectionService.write(outputTab);

    }
}