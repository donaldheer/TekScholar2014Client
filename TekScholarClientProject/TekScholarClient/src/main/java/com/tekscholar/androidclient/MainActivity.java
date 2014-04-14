package com.tekscholar.androidclient;

import android.app.Activity;
;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static BluetoothConnection btConnection;
    public int REQUEST_ENABLE_BT = 1;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //Determine if the bluetooth adapter is enabled, and if not, then enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        btConnection = new BluetoothConnection(this);
//        btConnection = new BluetoothConnection(this, "00:12:11:12:09:55");
        if(!btConnection.isConnected()) {
            Log.d("ADJ", "Bluetooth says its connected");
            btConnection.connect();
        }

        registerReceiver(btConnection.mPairingReceiver, btConnection.pairingRequestIntent);
        registerReceiver(btConnection.mBondReceiver, btConnection.connectedIntent);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ZoomFragment.newInstance(position + 1))
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingsFragment.newInstance(position + 1))
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

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ZoomFragment newInstance(int sectionNumber) {
            ZoomFragment fragment = new ZoomFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ZoomFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.zoom_fragment, container, false);
            mZoomView = (ZoomSurface) rootView.findViewById(R.id.zoom_surfaceView);

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
        public int channelSelected;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SettingsFragment newInstance(int sectionNumber) {
            SettingsFragment fragment = new SettingsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SettingsFragment() {
        }

        public void channelSelectHandler(){
            Log.d("ADJ", "Button Pressed!");

        }




        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.settings_fragment, container, false);


            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

            channelButton1 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch1);
            channelButton2 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch2);
            channelButton3 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch3);
            channelButton4 = (ChannelSelect) rootView.findViewById(R.id.settingsMultiSwitch4);

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
                }
            });

            //Set buttons from bluetooth here

            Button testButton = (Button) rootView.findViewById(R.id.testbutton);
            testButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btConnection.sendMessage("SELECT:CH1?");
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
                        btConnection.sendMessage(message + "1");
                    } else {
                        //Waveform off
                        btConnection.sendMessage(message + "0");
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

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop(){
        super.onStop();
        unregisterReceiver(btConnection.mPairingReceiver);
        unregisterReceiver(btConnection.mBondReceiver);
    }

}
