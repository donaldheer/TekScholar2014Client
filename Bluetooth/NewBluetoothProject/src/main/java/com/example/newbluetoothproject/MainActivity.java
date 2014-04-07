package com.example.newbluetoothproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MainActivity extends Activity {

    public int REQUEST_ENABLE_BT = 1;
    public String SERVER_MAC = "00:14:02:25:02:61";
    public InputStream tmpIn = null;
    public OutputStream tmpOut = null;
    public String TAG = "ADJ";
    public byte[] pin = {1,2,3,4};
    public String pinString = "1234";
    public IntentFilter pairingRequestIntent = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
    public IntentFilter connectedIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    public BluetoothDevice remoteDevice;
    public boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.send);

        //Get the default bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //Determine if the bluetooth adapter is enabled, and if not, then enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        remoteDevice = mBluetoothAdapter.getRemoteDevice(SERVER_MAC);

        Log.d(TAG, pin.toString());

        registerReceiver(mPairingReceiver, pairingRequestIntent);
        registerReceiver(mBondReceiver, connectedIntent);
        remoteDevice.createBond();

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendTestMessage();
            }
        });


    }

    public void setServerMac(String Mac){
        SERVER_MAC = Mac;
    }

    public void sendTestMessage() {
        String test = "Test";
        Log.d(TAG, test + " " + Boolean.toString(connected));
        if(connected) {
            writeBytes(test.getBytes());
        } else {
            connect();
        }

    }

    public void writeBytes(byte[] bytes) {
        try {
            tmpOut.write(bytes);
        } catch (IOException e) {
            Log.d("CATCH", "Attempted to write data");
        }
    }

    public void connect() {
        Log.d(TAG, Boolean.toString(connected));
        Log.d(TAG, Integer.toString(remoteDevice.getBondState()));
        BluetoothSocket btSocket;
        try {

            btSocket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            //btSocket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            btSocket.connect();
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
            connected = true;
            sendTestMessage();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MYERROR", e.getMessage());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(mPairingReceiver, pairingRequestIntent);
        registerReceiver(mBondReceiver, connectedIntent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mPairingReceiver);
        unregisterReceiver(mBondReceiver);
    }

    private BroadcastReceiver mPairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BluetoothDevice.ACTION_PAIRING_REQUEST) {
                Log.d(TAG, "Yay");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.setPin(convertPinToBytes(pinString));
                device.setPairingConfirmation(true);
            }
        }
    };

    private BroadcastReceiver mBondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                Log.d(TAG, "Yay bond change");
            }
        }
    };

    public byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        byte[] pinBytes;
        try {
            pinBytes = pin.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "UTF-8 not supported?!?");  // this should not happen
            return null;
        }
        if (pinBytes.length <= 0 || pinBytes.length > 16) {
            return null;
        }
        return pinBytes;
    }
}
