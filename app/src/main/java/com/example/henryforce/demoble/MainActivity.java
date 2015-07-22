package com.example.henryforce.demoble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{

    private static final long SCANNING_TIMEOUT = 5 * 1000; /* 5 seconds */
    private static final int ENABLE_BT_REQUEST_ID = 1;

    private boolean mScanning = false;
    private Handler mHandler = new Handler();

    private ListView listView;
    //private VMenuListAdapter listAdapter;
    private DeviceListAdapter mDevicesListAdapter = null;
    private BleWrapper mBleWrapper = null;

    private SharedPreferences devicePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)this.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        // create BleWrapper with empty callback object except uiDeficeFound function (we need only that here)
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {
            @Override
            public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
                handleFoundDevice(device, rssi, record);
            }
        });

        // check if we have BT and BLE on board
        if(mBleWrapper.checkBleHardwareAvailable() == false) {
            bleMissing();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // on every Resume check if BT is enabled (user could turn it off while app was in background etc.)
        if(mBleWrapper.isBtEnabled() == false) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            // see onActivityResult to check what is the status of our request
        }

        // initialize BleWrapper object
        mBleWrapper.initialize();

        mDevicesListAdapter = new DeviceListAdapter(this);
        mDevicesListAdapter.devicePreferences = devicePref;
        listView.setAdapter(mDevicesListAdapter);

        // Automatically start scanning for devices
        mScanning = true;
        // remember to add timeout for scanning to not run it forever and drain the battery
        addScanningTimeout();
        mBleWrapper.startScanning();

        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanning = false;
        mBleWrapper.stopScanning();
        invalidateOptionsMenu();

        mDevicesListAdapter.clearList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user didn't want to turn on BT
        if (requestCode == ENABLE_BT_REQUEST_ID) {
            if(resultCode == Activity.RESULT_CANCELED) {
                btDisabled();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* make sure that potential scanning will take no longer
	 * than <SCANNING_TIMEOUT> seconds from now on */
    private void addScanningTimeout() {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if(mBleWrapper == null) return;
                mScanning = false;
                mBleWrapper.stopScanning();
                invalidateOptionsMenu();
            }
        };
        mHandler.postDelayed(timeout, SCANNING_TIMEOUT);
    }

    /* add device to the current list of devices */
    private void handleFoundDevice(final BluetoothDevice device,
                                   final int rssi,
                                   final byte[] scanRecord)
    {
        // adding to the UI have to happen in UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDevicesListAdapter.addDevice(device, rssi, scanRecord);
                mDevicesListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void btDisabled() {
        Toast.makeText(this, "Sorry, BT has to be turned ON for us to work!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void bleMissing() {
        Toast.makeText(this, "BLE Hardware is required but not available!", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = mDevicesListAdapter.getDevice(position);
        if (device == null) return;

        /*final Intent intent = new Intent(this, TabActivity.class);
        intent.putExtra(TabActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(TabActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        intent.putExtra(TabActivity.EXTRAS_DEVICE_RSSI, mDevicesListAdapter.getRssi(position));*/

        final Intent intent = new Intent(this, DebugBLE.class);
        intent.putExtra(DebugBLE.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DebugBLE.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        intent.putExtra(DebugBLE.EXTRAS_DEVICE_RSSI, mDevicesListAdapter.getRssi(position));

        if (mScanning) {
            mScanning = false;
            invalidateOptionsMenu();
            mBleWrapper.stopScanning();
        }

        startActivity(intent);
    }
}
