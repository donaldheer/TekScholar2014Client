package com.example.newbluetoothproject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {

    public int REQUEST_ENABLE_BT = 1;
    public String SERVER_MAC = "00:1B:DC:06:AD:C3";
    public InputStream tmpIn = null;
    public OutputStream tmpOut = null;

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

        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(SERVER_MAC);

        BluetoothSocket btSocket;
        try {
            btSocket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(remoteDevice.getUuids().toString()));
            //btSocket = remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            btSocket.connect();
            tmpIn = btSocket.getInputStream();
            tmpOut = btSocket.getOutputStream();
            sendTestMessage();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("MYERROR", e.getMessage());
        }

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
        writeBytes(test.getBytes());
    }

    public void writeBytes(byte[] bytes) {
        try {
            tmpOut.write(bytes);
        } catch (IOException e) {
            Log.d("CATCH", "Attempted to write data");
        }
    }
}
