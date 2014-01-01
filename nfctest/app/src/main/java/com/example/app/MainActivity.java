package com.example.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;




public class MainActivity extends Activity {
    String hi="047243D2853280";
    String bye = "04A836D2853280";
    private NfcAdapter mNfcAdapter;
    public static final String MIME_TEXT_PLAIN = "text/plain";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = getIntent();
        if (!mNfcAdapter.isEnabled())
        {
            Toast.makeText(this, "Please activate NFC and press Back to return to the application!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndefTag = Ndef.get(tag);
            Parcelable[] message = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            //Log.d("myapp",message);
            NdefMessage msg = (NdefMessage) message[0];
            extractMessage(msg);
            //Log.d("myapp",array);
            //String s = new String(tag.getId());
            //Log.e("myapp",ByteArrayToHexString(tag.getId()));
            Toast.makeText(this,ByteArrayToHexString(tag.getId()),Toast.LENGTH_LONG).show();
            //if(ByteArrayToHexString(tag.getId()).equals(hi))
                //setBluetooth(true);
            //else
                //setBluetooth(false);
            /*try {
                write(tag);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }*/
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public String ByteArrayToHexString(byte [] inarray)
    {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    private void extractMessage(NdefMessage msg) {
        byte[] array = null;
        array = msg.getRecords()[0].getPayload();
        String str = new String(array);
        String str2 = str.substring(3);
        Log.d("myapp",str2);
    }

    private NdefRecord createRecord() throws UnsupportedEncodingException {
        String text       = "Hello, World!";
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT,
                new byte[0],
                payload);

        return record;
    }

    private void write(Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord() };
        NdefMessage  message = new NdefMessage(records);

        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);

        // Enable I/O
        ndef.connect();

        // Write the message
        ndef.writeNdefMessage(message);

        // Close the connection
        ndef.close();
    }
}