package com.example.leechungwan.testversion;

import java.util.ArrayList;
import java.util.List;

import com.chipsen.bleservice.SampleGattAttributes;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BLEActivity extends Activity implements Runnable {

    private BluetoothGattCharacteristic UART_Read;
    private BluetoothGattCharacteristic UART_Write;
    private BluetoothGattCharacteristic PWM_Read_Write;
    private BluetoothGattCharacteristic PIO_Read_Write;
    private BluetoothGattCharacteristic PIO_State;
    private BluetoothGattCharacteristic PIO_Direction;
    private BluetoothGattCharacteristic AIO_Read;

    private static String TAG = "SCAN";
    private static int PERIOD_READ = 100;

    public static boolean isConnected = false;

    // BLE
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private BluetoothAdapter mBluetoothAdapter;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;

    // Components
    private Button scan_button;
    private Button disconnection_button;
    private EditText mac_edittext;
    private Button setting_button;
    private EditText value_edittext;
    private Button send_button;
    private TextView read_textview;

    // Etc
    private int REQUEST_ENABLE_BT = 1;
    private boolean button = false;
    private boolean isREAD = false;
    private String macAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 필요 - 쓰레드 부분
        setup();

        // 필요
        setupBLE();
    }

    // 필요
    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            filters = new ArrayList<ScanFilter>();
        }
    }

    // 필요
    @Override
    public void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    // 필요
    @Override
    public void onDestroy() {
        button = false;

        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    // 필요
    private void setup() {
        scan_button = (Button) findViewById(R.id.scan_button);
        disconnection_button = (Button) findViewById(R.id.disconnection_button);
        mac_edittext = (EditText) findViewById(R.id.mac_edittext);
        setting_button = (Button) findViewById(R.id.setting_button);
        value_edittext = (EditText) findViewById(R.id.value_edittext);
        send_button = (Button) findViewById(R.id.send_button);
        read_textview = (TextView) findViewById(R.id.read_textview);

        scan_button.setOnClickListener(clickListener);
        disconnection_button.setOnClickListener(clickListener);
        setting_button.setOnClickListener(clickListener);
        send_button.setOnClickListener(clickListener);

        mac_edittext.setText("74:F0:7D:C9:CF:7E");

        button = true;
        isREAD = false;

        // 블루투스 수신 관련
        Thread thread = new Thread(this);
        thread.start();
    }

    // 필요
    private void setupBLE() {
        // Use this check to determine whether BLE is supported on the device.
        // Then you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter. For API level 18 and above,
        // get a reference to BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "이 디바이스는 BLE를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    // 필요
    protected void onRead() {
        isREAD = true;
    }

    // 필요
    protected void offRead() {
        isREAD = false;
    }

    // 필요
    protected void disconnection() {
        if (mGatt != null) {
            mGatt.disconnect();
        }
    }

    // 필요
    protected void write(byte[] bytes) {
        UART_Write.setValue(bytes);
        UART_Write.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mGatt.writeCharacteristic(UART_Write);
    }

    // 필요
    protected boolean scanLeDevice(final boolean enable) {
        if (enable) {
            mLEScanner.startScan(filters, settings, mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
        return enable;
    }

    private boolean isConnection = false;

    // 필요
    protected boolean connectToDevice(BluetoothDevice device) {

        if (isConnection)
            return false;

        isConnection = true;

        mGatt = device.connectGatt(this, false, gattCallback);
        scanLeDevice(false); // will stop after first device detection

        Log.i(TAG, "connect mGatt: Success");

        if (mGatt != null)
            return true;
        return false;
    }

    // 필요
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            final BluetoothDevice device = result.getDevice();
            if (device.getAddress().equals(macAddress)) {
                //connectToDevice(device);


                // print BLE bytes
                String re = "[";
                for ( Byte obj : result.getScanRecord().getBytes())
                {
                    re += String.format("%02X,",obj);
                }
                re += "]";
                Log.i("SCAN1", "result:" + re);

                // Connect to Server using Thread
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("SCAN2", "ScanResult - Results:" + sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan Failed:Error Code: " + errorCode);
        }
    };

    // 필요
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange:" + "Status: " + status);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:

                    gatt.discoverServices();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Connected: " + gatt.getDevice().getAddress(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    onRead();

                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    offRead();

                    Log.e(TAG, "gattCallback:" + "STATE_DISCONNECTED");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                        }
                    });

                    switch (status) {
                        case 133:
                        case BluetoothGatt.GATT_FAILURE:
                            Log.e(TAG, "gattCallback:" + "GATT_FAILURE");
                            break;
                        default:
                    }
                    mGatt.close();
                    isConnection = false;
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Log.e(TAG, "gattCallback:" + "STATE_OFF");
                    break;
                default:
                    Log.e(TAG, "gattCallback:" + "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // Log.i(TAG, "onServicesDiscovered:" + services.toString());
            List<BluetoothGattService> services = gatt.getServices();
            findCharacteristic(gatt, services);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // Log.i(TAG, "onCharacteristicWrite:" + characteristic.toString());

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // Log.i(TAG, "onCharacteristicRead:" + characteristic.getValue());
        }
    };

    // 필요
    private void findCharacteristic(BluetoothGatt gatt, List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;

        String TAG = "SERVICE";

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                if (SampleGattAttributes.UART_READ_UUID.equals(uuid)) {
                    UART_Read = gattCharacteristic;
                    Log.e(TAG, uuid + "//" + UART_Read);

                    mGatt.setCharacteristicNotification(UART_Read, true);
                }
                if (SampleGattAttributes.UART_WRITE_UUID.equals(uuid)) {
                    UART_Write = gattCharacteristic;
                    Log.e(TAG, uuid + "//" + UART_Write);
                }
                if (SampleGattAttributes.PWM_READ_WRITE_UUID.equals(uuid)) {
                    PWM_Read_Write = gattCharacteristic;
                    Log.e(TAG, uuid + "//" + PWM_Read_Write);
                }
                if (SampleGattAttributes.PIO_READ_WRITE_UUID.equals(uuid)) {
                    PIO_Read_Write = gattCharacteristic;
                    Log.e(TAG, uuid + "//" + PIO_Read_Write);
                }
                if (SampleGattAttributes.PIO_DIRECTION_UUID.equals(uuid)) {
                    PIO_Direction = gattCharacteristic;
                    Log.e(TAG, uuid + "//" + PIO_Direction);
                }
                if (SampleGattAttributes.PIO_STATE_UUID.equals(uuid)) {
                    PIO_State = gattCharacteristic;
                    Log.e(TAG, uuid + "//" + PIO_State);
                }
                if (SampleGattAttributes.AIO_READ_UUID.equals(uuid)) {
                    AIO_Read = gattCharacteristic;
                    Log.e(TAG, uuid + "//" + AIO_Read);
                }
            }
        }
    }

    private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == scan_button) {
                scanLeDevice(true);
            } else if (v == disconnection_button) {
                disconnection();
            } else if (v == setting_button) {
                macAddress = mac_edittext.getText().toString();
                Log.d("TAG", macAddress);
            } else if (v == send_button) {
                Log.d("TAG", "HI: " + value_edittext.getText().toString());
                write(value_edittext.getText().toString().getBytes());
            }
        }
    };

    @Override
    public void run() {
        while (button) {
            try {


                Thread.sleep(PERIOD_READ);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}