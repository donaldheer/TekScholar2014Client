package com.tekscholar.androidclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by aidandj on 4/9/14.
 */
public class BluetoothConnection {

    Context context;
    public int REQUEST_ENABLE_BT = 1;
    public String SERVER_MAC = "00:12:11:12:09:55";
    public BufferedReader tmpIn = null;
    public OutputStream tmpOut = null;
    public String TAG = "ADJ";
    public byte[] pin = {1,2,3,4};
    public String pinString = "1234";
    public IntentFilter pairingRequestIntent = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
    public IntentFilter connectedIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    public BluetoothDevice remoteDevice;
    public boolean connected = false;
    public List<String> commandsArray = new ArrayList<String>();
    BluetoothSocket btSocket;

    public BroadcastReceiver mPairingReceiver = new BroadcastReceiver() {
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

    public BroadcastReceiver mBondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                Log.d(TAG, "Yay bond change" + Integer.toString(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 10)));
                if(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 10) == BluetoothDevice.BOND_BONDED){
                    connect();
                }
            }
        }
    };


//    public BluetoothConnection(Context context, AttributeSet attrs){
//
//    }

    public BluetoothConnection(Context context) {
        this.context = context;
        //Get the default bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //Determine if the bluetooth adapter is enabled, and if not, then enable it.
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }

        remoteDevice = mBluetoothAdapter.getRemoteDevice(SERVER_MAC);

        Log.d(TAG, pin.toString());

//        registerReceiver(mPairingReceiver, pairingRequestIntent);
//        registerReceiver(mBondReceiver, connectedIntent);
        remoteDevice.createBond();
    }

    public BluetoothConnection(Context context, String serverMac) {
        this.context = context;
        //Get the default bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //Determine if the bluetooth adapter is enabled, and if not, then enable it.
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }

        remoteDevice = mBluetoothAdapter.getRemoteDevice(serverMac);

        Log.d(TAG, pin.toString());

//        registerReceiver(mPairingReceiver, pairingRequestIntent);
//        registerReceiver(mBondReceiver, connectedIntent);
        remoteDevice.createBond();
    }

    public void connect() {
        Log.d(TAG, Boolean.toString(connected));
        Log.d(TAG, Integer.toString(remoteDevice.getBondState()));
        if(remoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            try {

                btSocket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                //btSocket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                btSocket.connect();
                tmpIn = new BufferedReader(new InputStreamReader(btSocket.getInputStream()));
                tmpOut = btSocket.getOutputStream();
                connected = true;
                sendTestMessage();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("MYERROR", e.getMessage());
            }
        } else {
            remoteDevice.createBond();
        }
    }

    public void writeBytes(byte[] bytes) {
        try {
            tmpOut.write(bytes);
        } catch (IOException e) {
            Log.d("CATCH", "Attempted to write data");
        }
    }

    public void sendMessage(String message){
        if(connected){
            writeBytes(message.getBytes());
        } else {
//            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
//            Toast.make
        }
    }

    public List<String> readMessage(){
        int c;
        StringBuilder message= new StringBuilder();
        commandsArray.add("fail");
        if(connected) {
            commandsArray.clear();
            try {
                if(tmpIn.ready()) {
                    while ((c = tmpIn.read()) != -1) {
                        //Since c is an integer, cast it to a char. If it isn't -1, it will be in the correct range of char.

                        if((c == '\n') && (message.length() != 0)){
                            commandsArray.add(message.toString());
                            message.delete(0, message.length());
                        } else {
                            message.append((char) c);
                        }
                        if(!tmpIn.ready()){
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            connect();
            commandsArray.add("not connected");
        }
        commandsArray.add("End off commands");
        commandsArray.add("End now");
        return commandsArray;
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

    public boolean isConnected(){
        try {
            connected = btSocket.isConnected();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return connected;
//        return connected;
    }

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
