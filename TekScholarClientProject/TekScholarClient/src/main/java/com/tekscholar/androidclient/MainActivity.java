package com.tekscholar.androidclient;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;

import java.util.List;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ContinuousDictationFragment.ContinuousDictationFragmentResultsCallback {

    public static BluetoothConnection btConnection;
    private NfcAdapter mNfcAdapter;
    public String mac = "00:00:00:00:00:00";
    public BluetoothDevice device;
    public static boolean btConnected = false;
    private ContinuousDictationFragment mContinuousDictationFragment;
    private String result;


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int COMMAND_SEND = 6;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String COMMAND = "command";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public static String recvCommandString = new String();
    public static List<String> recvCommandArray = new ArrayList<String>();

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    public String pinString = "1234";
    public IntentFilter pairingRequestIntent = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static boolean[] chActive;
    public int numOfCh = 4;

    public BroadcastReceiver mPairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BluetoothDevice.ACTION_PAIRING_REQUEST) {
                Log.d("ADJ", "Yay");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.setPin(convertPinToBytes(pinString));
                device.setPairingConfirmation(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getActionBar().setIcon(R.drawable.ic_launcher_red);

//        File storageDir = new File(
//                Environment.getExternalStoragePublicDirectory(
//                        Environment.DIRECTORY_PICTURES
//                ),
//                "TekShots"
//        );
//        galleryAddPic(storageDir);


        mContinuousDictationFragment = new ContinuousDictationFragment(this);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        //ft.attach(mContinuousDictationFragment).commit();
        //ft.add(R.id.dictationStateButton, mContinuousDictationFragment, "ADJ").commit();
        ft.add(mContinuousDictationFragment, "ADJ").commit();
        mContinuousDictationFragment.startVoiceRecognitionCycle();

        chActive = new boolean[numOfCh];
        for(int i = 0; i < numOfCh; i++){
            chActive[i] = false;
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = getIntent();
        if (!mNfcAdapter.isEnabled())
        {
            Toast.makeText(this, "Please activate NFC and press Back to return to the application!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        }
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] message = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msg = (NdefMessage) message[0];
            mac = extractMessage(msg);




            // Get local Bluetooth adapter
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.d("ADJ", mac);
            device = mBluetoothAdapter.getRemoteDevice(mac);
            if(mChatService != null) {
                Log.d("ADJ", "chatservice null");
                mChatService.connect(device);
            }

            // If the adapter is null, then Bluetooth is not supported
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }




//            getActionBar().setIcon(R.drawable.ic_launcher);
        }
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

//        mac = "64:27:37:C1:29:83";
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        Log.d("ADJ", mac);
//        device = mBluetoothAdapter.getRemoteDevice(mac);
//        if(mChatService != null) {
//            Log.d("ADJ", "chatservice null");
//            mChatService.connect(device);
//        }
    }

    private String extractMessage(NdefMessage msg) {
        byte[] array = null;
        array = msg.getRecords()[0].getPayload();
        String str = new String(array);
        String str2 = str.substring(3);
        Log.d("myapp",str2);
        return str2;
    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ZoomFragment.newInstance(position + 1, mHandler))
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingsFragment.newInstance(position + 1, mHandler))
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            default:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                device = mBluetoothAdapter.getRemoteDevice("00:12:11:12:09:55");
                device.setPin(convertPinToBytes(pinString));
                device.setPairingConfirmation(true);
                mChatService.connect(device);
                return true;
            case R.id.action_test:
                sendBTMessage("Testing");
                //btConnection.sendMessage("horizontal:scale?\n");
//                btConnection.sendMessage("123456789\n");
//                btConnection.sendMessage("123456789\n");
//                btConnection.sendMessage("123456789\n");
//                btConnection.sendMessage("123456789\n");
                return true;
            case R.id.action_connect:
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                device = mBluetoothAdapter.getRemoteDevice("64:27:37:C1:29:83");
                device.setPairingConfirmation(true);
                mChatService.connect(device);
                return true;
            case R.id.action_example:
                Log.d("ADJ", recvCommandArray.toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    /**
     * A zoom fragment containing a simple view.
     */
    public static class ZoomFragment extends Fragment{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private ZoomSurface mZoomView;
        private SurfaceHolder mZoomViewHolder;
        private Handler mHandler;

        MultiImageSwitch multiSwitch1;
        MultiImageSwitch multiSwitch2;
        MultiImageSwitch multiSwitch3;
        MultiImageSwitch multiSwitch4;

        Button startStopImage;
        Button captureImage;
        Button refreshImage;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ZoomFragment newInstance(int sectionNumber, Handler handler) {
            ZoomFragment fragment = new ZoomFragment(handler);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ZoomFragment() {

        }

        public ZoomFragment(Handler handler) {
            mHandler = handler;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.zoom_fragment, container, false);
            mZoomView = (ZoomSurface) rootView.findViewById(R.id.zoom_surfaceView);
            mZoomViewHolder = mZoomView.getHolder();
            mZoomView.setHandler(mHandler);

            multiSwitch1 = (MultiImageSwitch) rootView.findViewById(R.id.multiSwitch1);
            multiSwitch2 = (MultiImageSwitch) rootView.findViewById(R.id.multiSwitch2);
            multiSwitch3 = (MultiImageSwitch) rootView.findViewById(R.id.multiSwitch3);
            multiSwitch4 = (MultiImageSwitch) rootView.findViewById(R.id.multiSwitch4);
            try {
                multiSwitch1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        multiSwitch1.onClick(view);
                        if (multiSwitch1.getState() != 1) {

//                            btConnection.sendMessage("SELECT:CH1 1\n");
                            chActive[0] = true;
                        } else {
//                            btConnection.sendMessage("SELECT:CH1 0\n");
                            chActive[0] = false;

                        }
//                        multiSwitch2.setState(0);
//                        multiSwitch3.setState(0);
//                        multiSwitch4.setState(0);
                    }
                });
                multiSwitch2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        multiSwitch2.onClick(view);
                        if (multiSwitch2.getState() != 1) {

//                            btConnection.sendMessage("SELECT:CH2 1\n");
                            chActive[1] = true;
                        } else {
//                            btConnection.sendMessage("SELECT:CH2 0\n");
                            chActive[1] = false;
                        }
//                        multiSwitch1.setState(0);
//                        multiSwitch3.setState(0);
//                        multiSwitch4.setState(0);
                    }
                });
                multiSwitch3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        multiSwitch3.onClick(view);
                        if (multiSwitch3.getState() != 1) {

//                            btConnection.sendMessage("SELECT:CH3 1\n");
                            chActive[2] = true;
                        } else {
//                            btConnection.sendMessage("SELECT:CH3 0\n");
                            chActive[2] = false;
                        }
//                        multiSwitch1.setState(0);
//                        multiSwitch2.setState(0);
//                        multiSwitch4.setState(0);
                    }
                });
                multiSwitch4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        multiSwitch4.onClick(view);
                        if (multiSwitch4.getState() != 1) {

//                            btConnection.sendMessage("SELECT:CH4 1\n");
                            chActive[3] = true;
                        } else {
//                            btConnection.sendMessage("SELECT:CH4 0\n");
                            chActive[3] = false;
                        }
//                        multiSwitch1.setState(0);
//                        multiSwitch2.setState(0);
//                        multiSwitch3.setState(0);
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
            }
//            zoomView.setOnTouchListener(new View.OnTouchListener() {
//                public boolean onTouch(View v, MotionEvent event) {
//                    Log.d(TAG, "It touched me!");
//                    return true;
//                }
//            });
//            textView.setText("ZoomFragment" + Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
//            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
//                case 1:
//                    Log.d(TAG, "Section 1");
//                case 2:
//                    Log.d(TAG, "Section 2");
//                case 3:
//                    Log.d(TAG, "Section 3");
//
//            }

            //Waveform control buttons
            captureImage = (Button) rootView.findViewById(R.id.captureimage_button);
            refreshImage = (Button) rootView.findViewById(R.id.refresh_button);
            startStopImage = (Button) rootView.findViewById(R.id.startstop_button);

            captureImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    mZoomView.generatePoints();
//
//                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                    String imageFileName = timeStamp + "_" + "tekShot";
//
//                    File storageDir = new File(
//                            Environment.getExternalStoragePublicDirectory(
//                                    Environment.DIRECTORY_PICTURES
//                            ),
//                            "TekShots"
//                    );
//
//                    if (storageDir != null) {
//                        if (! storageDir.mkdirs()) {
//                            if (! storageDir.exists()){
//                                Log.d("CameraSample", "failed to create directory");
//                                return;
//                            }
//                        }
//                    }
//
//                    try {
//                        File f = File.createTempFile(imageFileName, ".jpeg", storageDir);
//
//
//                        Bitmap bitmap = Bitmap.createBitmap(mZoomView.getWidth(), mZoomView.getHeight(), Bitmap.Config.ARGB_8888);
//
//                        mZoomView.draw(new Canvas(bitmap));
//                        try {
//                            OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                        } catch (IOException e) {
//                            Log.w("ADJ", e);
//                        }
//                        //MainActivity.galleryAddPic(f);
//                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                        //File f = new File(mCurrentPhotoPath);
//                        Uri contentUri = Uri.fromFile(f);
//                        mediaScanIntent.setData(contentUri);
//                        getActivity().sendBroadcast(mediaScanIntent);
//                    } catch (Exception e){
//                        e.printStackTrace();
//                    }
//
                }
            });

            refreshImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    mZoomView.generatePoints();
//                    Canvas temp = mZoomViewHolder.lockCanvas();
//                    mZoomView.draw(temp);
//                    mZoomViewHolder.unlockCanvasAndPost(temp);
                    //mZoomView.paint();
                }
            });

            startStopImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    public static class SettingsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        ChannelSelect channelButton1;
        ChannelSelect channelButton2;
        ChannelSelect channelButton3;
        ChannelSelect channelButton4;
        public int channelSelected = 1;
        TextView textView;
        public Handler mHandler;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SettingsFragment newInstance(int sectionNumber, Handler handler) {
            SettingsFragment fragment = new SettingsFragment(handler);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SettingsFragment(Handler handler) {
            mHandler = handler;
        }

        public void channelSelectHandler(){
            Log.d("ADJ", "Button Pressed!");

        }




        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.settings_fragment, container, false);


            textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Channel 1");

            channelButton1 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch1);
            channelButton2 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch2);
            channelButton3 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch3);
            channelButton4 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch4);

            channelButton1.setState(1);

            channelButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(channelButton1.getState() != 1) {
                        channelButton1.onClick(view);
                    }
                    channelButton2.setState(0);
                    channelButton3.setState(0);
                    channelButton4.setState(0);
                    channelSelected = 1;
                    textView.setText("Channel 1");
                }
            });
            channelButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(channelButton2.getState() != 1) {
                        channelButton2.onClick(view);
                    }
                    channelButton1.setState(0);
                    channelButton3.setState(0);
                    channelButton4.setState(0);
                    channelSelected = 2;
                    textView.setText("Channel 2");
                }
            });
            channelButton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(channelButton3.getState() != 1) {
                        channelButton3.onClick(view);
                    }
                    channelButton1.setState(0);
                    channelButton2.setState(0);
                    channelButton4.setState(0);
                    channelSelected = 3;
                    textView.setText("Channel 3");
                }
            });
            channelButton4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(channelButton4.getState() != 1) {
                        channelButton4.onClick(view);
                    }
                    channelButton1.setState(0);
                    channelButton2.setState(0);
                    channelButton3.setState(0);
                    channelSelected = 4;
                    textView.setText("Channel 4");
                }
            });

            //Set buttons from bluetooth here

            Button testButton = (Button) rootView.findViewById(R.id.testbutton);
            testButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString(MainActivity.COMMAND, "SELECT:CH1?");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    //btConnection.sendMessage("SELECT:CH1?");
                    List<String> message = btConnection.readMessage();
                    Log.d("ADJ", message.get(0));
                }
            });


            Switch waveFormDisplaySwitch = (Switch) rootView.findViewById(R.id.switch2);
            waveFormDisplaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String message;
                    switch(channelSelected){
                        case 1:
                            message = "SELECT:CH1 ";
                            break;
                        case 2:
                            message = "SELECT:CH2 ";
                            break;
                        case 3:
                            message = "SELECT:CH3 ";
                            break;
                        case 4:
                            message = "SELECT:CH4 ";
                            break;
                        default:
                            message = "";
                            break;
                    }
                    if(b){
                        //Waveform on
                        Message msg = mHandler.obtainMessage(MainActivity.COMMAND_SEND);
                        Bundle bundle = new Bundle();
                        bundle.putString(MainActivity.COMMAND, message + "1" + '\n');
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        //btConnection.sendMessage(message + "1" +'\n');
                    } else {
                        //Waveform off
                        Message msg = mHandler.obtainMessage(MainActivity.COMMAND_SEND);
                        Bundle bundle = new Bundle();
                        bundle.putString(MainActivity.COMMAND, message + "0" +'\n');
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        //btConnection.sendMessage(message + "0" +'\n');
                    }
                }
            });

            return rootView;
        }




        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendBTMessage(String message) {
        Log.d("ADJ", "In sendBTMessage command: " + message);
        // Check that we're actually connected before trying anything
        try {
            if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch(Exception e) {
            Toast.makeText(this, "Error sending", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

        }
    }


    private void setupChat() {
        Log.d("ADJ", "In setupChat");
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    // The Handler that gets information back from the BluetoothChatService
    public final Handler mHandler = new Handler() {
        //Commands


        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
//                            mTitle.setText(R.string.title_connected_to);
//                            mTitle.append(mConnectedDeviceName);
//                            mConversationArrayAdapter.clear();
                            getActionBar().setIcon(R.drawable.ic_launcher);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
//                            mTitle.setText(R.string.title_connecting);
                            getActionBar().setIcon(R.drawable.tek_button_1_color);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            getActionBar().setIcon(R.drawable.ic_launcher_red);
//                            mTitle.setText(R.string.title_not_connected);

                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    for( int i = 0; i < readBuf.length; i++){
                        if(readBuf[i] == '\r'){
                            String fail = readBuf.toString();
                        }
                    }
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    if(readMessage.contains(Character.toString('\r'))) {
                        recvCommandString = recvCommandString.concat(readMessage.substring(0, readMessage.indexOf('\r')));
                        recvCommandArray.add(recvCommandString);
                        recvCommandString = readMessage.substring(readMessage.indexOf('\r') + 1);
                    } else {
                        recvCommandString = recvCommandString.concat(readMessage);
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case COMMAND_SEND:
                    sendBTMessage(msg.getData().getString(COMMAND));
                    recvCommandArray.clear();
            }
        }
    };


//    public static void galleryAddPic(File f) {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        //File f = new File(mCurrentPhotoPath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        MainActivity.sendBroadcast(mediaScanIntent);
//    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mPairingReceiver);
        try {
            mContinuousDictationFragment.stopVoiceRecognition();
        } catch(Exception e){
//            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onResume(){
        super.onResume();
        registerReceiver(mPairingReceiver, pairingRequestIntent);
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services

                mChatService.connect(device);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (btConnection.isConnected()) {

                unregisterReceiver(btConnection.mPairingReceiver);
                unregisterReceiver(btConnection.mBondReceiver);

                btConnection.btSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mContinuousDictationFragment.stopVoiceRecognition();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
//                    String address = data.getExtras()
//                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
//                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
//                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, "Bluetooth Not Enabled", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if(!(mBluetoothAdapter == null)) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else {
                if (mChatService == null) setupChat();
            }
        }
    }

    @Override
    public void onDictationStart() {
        //Toast.makeText(this, "Start", Toast.LENGTH_LONG).show();
//        Log.d("ADJ", "Voice Rec Started");
    }

    @Override
    public void onResults(ContinuousDictationFragment delegate, ArrayList<String> dictationResults) {
        result = dictationResults.get(0);
        for(int i = 0; i < dictationResults.size(); i++) {
            result = dictationResults.get(i).toLowerCase();
            //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            if (result.contains("start")) {
                sendBTMessage("FPAnel:PRESS RUNSTOP\n\r");
                break;
            } else if (result.contains("stop") | result.contains("run")) {
                sendBTMessage("FPAnel:PRESS runstop\n\r");
                break;
            }
            if (result.contains("start")) {
                sendBTMessage("FPAnel:PRESS RUNSTOP\n\r");
                break;
            } else if (result.contains("stop")) {
                sendBTMessage("FPAnel:PRESS runstop\n\r");
                break;
            } else if (result.contains("channel 1 off") | result.contains("channel one off") | result.contains("channel one of") | result.contains("channel 1 of")) {
                sendBTMessage("SELECT:CH1 0\n\r");
                break;
            } else if (result.contains("channel 1 on") | result.contains("channel one on")) {
                sendBTMessage("SELECT:CH1 1\n\r");
                break;
            } else if (result.contains("channel 2 off") | result.contains("channel two off") | result.contains("channel two of") | result.contains("channel 2 of")) {
                sendBTMessage("SELECT:CH2 0\n\r");
                break;
            } else if (result.contains("channel 2 on") | result.contains("channel two on")) {
                sendBTMessage("SELECT:CH2 1\n\r");
                break;
            } else if (result.contains("channel 3 off") | result.contains("channel three off") | result.contains("channel three of") | result.contains("channel 3 of")) {
                sendBTMessage("SELECT:CH3 0\n\r");
                break;
            } else if (result.contains("channel 3 on") | result.contains("channel three on")) {
                sendBTMessage("SELECT:CH3 1\n\r");
                break;
            } else if (result.contains("channel 4 off") | result.contains("channel four off") | result.contains("channel four of") | result.contains("channel 4 of")) {
                sendBTMessage("SELECT:CH4 0\n\r");
                break;
            } else if (result.contains("channel 4 on") | result.contains("channel four on")) {
                sendBTMessage("SELECT:CH4 1\n\r");
                break;
            }
        }

    }

    @Override
    public void onDictationFinish() {
        //Toast.makeText(this, "Stop", Toast.LENGTH_LONG).show();
//        Log.d("ADJ", "Voice Rec Stopped");
    }

    public byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        byte[] pinBytes;
        try {
            pinBytes = pin.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Log.e("ADJ", "UTF-8 not supported?!?");  // this should not happen
            return null;
        }
        if (pinBytes.length <= 0 || pinBytes.length > 16) {
            return null;
        }
        return pinBytes;
    }

}
