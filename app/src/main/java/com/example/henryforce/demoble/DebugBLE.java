package com.example.henryforce.demoble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Henryforce on 7/21/15.
 */
public class DebugBLE extends ActionBarActivity implements BLEDataCallback,
        BleWrapperUiCallbacks{

    public static final String EXTRAS_DEVICE_NAME    = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI    = "BLE_DEVICE_RSSI";

    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;

    private BleWrapper mBleWrapper;
    private ArrayList<BluetoothGattService> listServices;
    private ArrayList<BluetoothGattCharacteristic> listCharacteristics;
    private BluetoothGattCharacteristic mainCharacteristic;

    private byte warmBuffer[];
    Boolean didWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exampleble);


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceRSSI = intent.getIntExtra(EXTRAS_DEVICE_RSSI, 0) + " db";

        didWrite = true;

        /*SharedPreferences pref = this.getSharedPreferences("deviceLog", Context.MODE_PRIVATE);
        String realname = pref.getString(mDeviceName, "!");
        if(realname == "!"){
            showEditDialog();
        }*/

        Button but = (Button)findViewById(R.id.button);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mBleWrapper.requestCharacteristicValue(mainCharacteristic);
                //mBleWrapper.getCharacteristicValue(mainCharacteristic);
                //mBleWrapper.setNotificationForCharacteristic(mainCharacteristic, true);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mBleWrapper == null) mBleWrapper = new BleWrapper(this, this);

        if(mBleWrapper.initialize() == false) {
            finish();
        }

        mBleWrapper.connect(mDeviceAddress);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBleWrapper.stopMonitoringRssiValue();
        mBleWrapper.diconnect();
        mBleWrapper.close();
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.tab_name) {
            //showEditDialog();
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void writeData(byte[] data){
        Log.i("BYTE DATA", String.valueOf(data.length));
    }

    @Override
    public void writeRGBColor(int red, int green, int blue){
        //Log.i("Data SENT", "AH?");
        if(mainCharacteristic != null) {
            //Log.i("Data SENT", "YES");
            byte auxByte[] = new byte[7];
            auxByte[0] = 86;
            auxByte[1] = (byte)(red & 0xFF);
            auxByte[2] = (byte)(green & 0xFF);
            auxByte[3] = (byte)(blue & 0xFF);
            auxByte[4] = 0;
            auxByte[5] = -16;
            auxByte[6] = -86;
            mBleWrapper.writeDataToCharacteristic(mainCharacteristic, auxByte);
        }
    }

    @Override
    public void writeWarmColor(int warm){
        if((mainCharacteristic != null)) {
            didWrite = false;
            byte auxByte[] = new byte[7];
            auxByte[0] = 86;
            auxByte[1] = 0;
            auxByte[2] = 0;
            auxByte[3] = 0;
            auxByte[4] = (byte)(warm & 0xFF);
            auxByte[5] = 15;
            auxByte[6] = -86;
            //warmBuffer[4] = (byte)(warm & 0xFF);

            mBleWrapper.writeDataToCharacteristic(mainCharacteristic, auxByte);
        }
    }

    @Override
    public void writeAnimation(int animationIndex, int velocity){
        if((mainCharacteristic != null) && (mBleWrapper != null)) {
            byte auxByte[] = new byte[4];
            auxByte[0] = -69;
            auxByte[1] = (byte)(animationIndex & 0xFF);
            auxByte[2] = (byte)(velocity & 0xFF);
            auxByte[3] = 68;
            mBleWrapper.writeDataToCharacteristic(mainCharacteristic, auxByte);
        }
    }

    @Override
    public void writeState(boolean state){
        if((mainCharacteristic != null) && (mBleWrapper != null)) {
            state = !state;
            byte auxByte[] = new byte[3];
            auxByte[0] = -52;
            auxByte[1] = state ? (byte)35 : (byte)36;
            auxByte[2] = 51;
            mBleWrapper.writeDataToCharacteristic(mainCharacteristic, auxByte);
        }
    }

    @Override
    public void uiDeviceFound(final BluetoothDevice device, int rssi, byte[] record){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Device Found", device.toString());
            }
        });
    }

    @Override
    public void uiDeviceConnected(final BluetoothGatt gatt, final BluetoothDevice device){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Device Connected", device.toString());
            }
        });
    }

    @Override
    public void uiDeviceDisconnected(final BluetoothGatt gatt, final BluetoothDevice device){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Device Disconnected", device.toString());
            }
        });
    }

    @Override
    public void uiAvailableServices(final BluetoothGatt gatt, final BluetoothDevice device,
                                    final List<BluetoothGattService> services){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(BluetoothGattService service : services){
                    Log.i("Service Found", service.getUuid().toString());

                    //0000ffe5-0000-1000-8000-00805f9b34fb
                    /*if(service.getUuid().toString().equals("0000ffe5-0000-1000-8000-00805f9b34fb")){
                        mBleWrapper.getCharacteristicsForService(service);
                    }*/
                    //
                    //if(service.getUuid().toString().equals("0bd51666-e7cb-469b-8e4d-2742f1ba77cc")){
                        mBleWrapper.getCharacteristicsForService(service);
                    //}
                }
            }
        });
    }

    @Override
    public void uiCharacteristicForService(final BluetoothGatt gatt, final BluetoothDevice device,
                                           final BluetoothGattService service,
                                           final List<BluetoothGattCharacteristic> chars){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(BluetoothGattCharacteristic characteristic : chars){
                    Log.i("Characteristic Found", characteristic.getUuid().toString());
                    //0000ffe9-0000-1000-8000-00805f9b34fb
                    //e7add780-b042-4876-aae1-112855353cc1
                    //0000dfb1-0000-1000-8000-00805f9b34fb
                    if(characteristic.getUuid().toString().equals("e7add780-b042-4876-aae1-112855353cc1")){
                        Log.i("Characteristic SET", "YES");
                        mainCharacteristic = characteristic;
                        boolean success = gatt.setCharacteristicNotification(characteristic, true);
                        if(!success) {
                            Log.i("Notification failed!", "Failed");
                        }
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleDefinedUUIDs.Descriptor.CHAR_CLIENT_CONFIG);
                        if(descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                            Log.i("Notification enabled", "Enabled");
                        }
                        else {
                            Log.i("Negative", "Negative");
                        }

                        //mBleWrapper.requestCharacteristicValue(characteristic);
                        //mBleWrapper.getCharacteristicValue(characteristic);
                        //gatt.readCharacteristic(characteristic);



                        /*byte auxByte[] = new byte[3];
                        auxByte[0] = -52;
                        auxByte[1] = 36;
                        auxByte[2] = 51;
                        mBleWrapper.writeDataToCharacteristic(mainCharacteristic, auxByte);*/

                        //mBleWrapper.setNotificationForCharacteristic(mainCharacteristic, true);
                    }
                }
            }
        });
    }

    @Override
    public void uiCharacteristicsDetails(final BluetoothGatt gatt, final BluetoothDevice device,
                                         final BluetoothGattService service,
                                         final BluetoothGattCharacteristic characteristic){
        Log.i("Charact", "Updated");
    }

    @Override
    public void uiNewValueForCharacteristic(final BluetoothGatt gatt, final BluetoothDevice device,
                                            final BluetoothGattService service, final BluetoothGattCharacteristic ch,
                                            final String strValue, final int intValue, final byte[] rawValue,
                                            final String timestamp){
        Log.i("Data Received", strValue);
    }

    @Override
    public void uiGotNotification(final BluetoothGatt gatt, final BluetoothDevice device,
                                  final BluetoothGattService service, final BluetoothGattCharacteristic characteristic){
        Log.i("Got Notification", "Not");
    }

    @Override
    public void uiSuccessfulWrite(final BluetoothGatt gatt, final BluetoothDevice device,
                                  final BluetoothGattService service, final BluetoothGattCharacteristic ch,
                                  final String description){
        didWrite = true;
    }

    @Override
    public void uiFailedWrite(final BluetoothGatt gatt, final BluetoothDevice device,
                              final BluetoothGattService service, final BluetoothGattCharacteristic ch,
                              final String description){

    }

    @Override
    public void uiNewRssiAvailable(final BluetoothGatt gatt, final BluetoothDevice device, final int rssi){

    }
}
