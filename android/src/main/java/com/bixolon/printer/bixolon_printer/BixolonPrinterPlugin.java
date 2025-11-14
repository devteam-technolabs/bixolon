package com.bixolon.printer.bixolon_printer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bixolon.labelprinter.BixolonLabelPrinter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;


public class BixolonPrinterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {


    private static final String[] FUNCTIONS = {"drawText", "drawVectorFontText", "draw1dBarcode", "drawMaxicode", "drawPdf417", "drawQrCode", "drawDataMatrix", "drawBlock", "drawCircle", "setCharacterSet", "setPrintingType", "setMargin", "setLength", "setWidth", "setBufferMode", "clearBuffer", "setSpeed", "setDensity", "setOrientation", "setOffset", "setCutterPosition", "drawBitmap", "initializePrinter", "printInformation", "setAutoCutter", "getStatus", "getPrinterInformation", "executeDirectIo", "printSample", "drawPDF", "setupRFID", "calibrateRFID", "setRFIDPosition", "writeRFID", "emptyLabelPrint", "disableInactivityTime", "setLeftMarginPosition", "transferFile", "printerFirmwareDownload", "wlanFirmwareDownload", "WLAN Setting"};
    static BixolonLabelPrinter mBixolonLabelPrinter;
    static int count = 1;
    private static Set<BluetoothDevice> discoverDevices = new HashSet<>();

    static {
        try {
            System.loadLibrary("bxl_common");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        this.activity = null;
    }


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonLabelPrinter.MESSAGE_TRANSFER_FILE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.RC_SUCCESS:
                            if (progressDialog != null) progressDialog.dismiss();
                            progressDialog = null;
                            Log.e("BixolonHandler", "file transfer success");
                            break;
                        case BixolonLabelPrinter.RC_PROGRESS:
                            if (progressDialog != null && !progressDialog.isShowing()) progressDialog.show();
                            TextView message = progressDialog.findViewById(android.R.id.message);
                            message.setText("WLAN Firmware Send: " + msg.obj.toString());
                            Log.e("BixolonHandler", "file sending: " + msg.obj);
                            break;
                        case BixolonLabelPrinter.RC_FAIL:
                            int errorCode = msg.arg2;
                            if (progressDialog != null) progressDialog.dismiss();
                            progressDialog = null;
                            Log.e("BixolonHandler", "file transfer fail: " + errorCode);
                            break;
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_FIRMWARE_UPDATE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.FIRMWARE_COMPLETE:
                            Log.e("BixolonHandler", "firmware download complete");
                            if (progressDialog != null) progressDialog.dismiss();
                            progressDialog = null;
                            break;
                        case BixolonLabelPrinter.FIRMWARE_PROGRESS:
                            if (progressDialog != null && !progressDialog.isShowing()) progressDialog.show();
                            TextView message = progressDialog.findViewById(android.R.id.message);
                            message.setText(msg.obj.toString());
                            Log.e("BixolonHandler", "firmware progress: " + msg.obj);
                            break;
                        case BixolonLabelPrinter.FIRMWARE_DELAY_UPDATE:
                            if (msg.obj != null) Log.e("BixolonHandler", "delay: " + msg.obj);
                            break;
                        case BixolonLabelPrinter.FIRMWARE_EXCEPTION:
                            if (msg.obj != null) Log.e("BixolonHandler", "exception :" + msg.obj);
                            break;
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.STATE_CONNECTED:
                            Log.e("BixolonHandler", "STATE_CONNECTED");
                            break;

                        case BixolonLabelPrinter.STATE_CONNECTING:
                            Log.e("BixolonHandler", "STATE_CONNECTING");
                            break;

                        case BixolonLabelPrinter.STATE_NONE:
                            Log.e("BixolonHandler", "STATE_NONE: " + msg.toString());
                            break;
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_READ:
                    Log.e("BixolonHandler", "MESSAGE_READ");
                    break;

                case BixolonLabelPrinter.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(BixolonLabelPrinter.DEVICE_NAME);
                    Log.e("BixolonHandler", "Connected device name: " + mConnectedDeviceName);
                    break;

                case BixolonLabelPrinter.MESSAGE_TOAST:
                    Log.e("BixolonHandler", "Toast message: " +
                            msg.getData().getString(BixolonLabelPrinter.TOAST));
                    break;

                case BixolonLabelPrinter.MESSAGE_LOG:
                    Log.e("BixolonHandler", "Log message: " +
                            msg.getData().getString(BixolonLabelPrinter.LOG));
                    break;

                case BixolonLabelPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    if (msg.obj == null) {
                        Log.e("BixolonHandler", "No paired device");
                    } else {
                        Log.e("BixolonHandler", "Bluetooth devices: " + msg.obj.toString());
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_USB_DEVICE_SET:
                    if (msg.obj == null) {
                        Log.e("BixolonHandler", "No connected USB device");
                    } else {
                        Log.e("BixolonHandler", "USB devices: " + msg.obj.toString());
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_NETWORK_DEVICE_SET:
                    if (msg.obj == null) {
                        Log.e("BixolonHandler", "No connectable network device");
                    } else {
                        Log.e("BixolonHandler", "Network devices: " + msg.obj.toString());
                    }
                    break;
            }
        }
    };

    private final int REQUEST_PERMISSION = 0;
    private final int REQUEST_FIRMWARE_SELECT = 10;
    private final int REQUEST_TRANSFER_FILE = 11;
    private final int REQUEST_WLAN_FIRMWARE = 12;
    private final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public Handler m_hHandler = null;
    public BluetoothAdapter m_BluetoothAdapter = null;
    public BluetoothLeScanner mLEScanner = null;
    public ScanSettings settings = null;
    public List<ScanFilter> filters;
    public ArrayAdapter<String> adapter = null;
    public ArrayList<BluetoothDevice> m_LeDevices;
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    private ListView mListView;
    private boolean mIsConnected;
    private boolean checkedManufacture = false;
    private ScanCallback mScanCallback;
    private PendingIntent mPermissionIntent;
    private UsbManager usbManager;
    private UsbDevice device;

    private Activity activity;
    private boolean tryedAutoConnect = true;
    private AlertDialog progressDialog;
    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "bixolon_printer");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("initPrinter")) {
            mBixolonLabelPrinter = new BixolonLabelPrinter(activity, mHandler, Looper.getMainLooper());
            result.success("init sdk");
        } else if (call.method.equals("connectSDK")) {
            String macAddress = call.argument("mac_address");
            mBixolonLabelPrinter.connect(macAddress);
            result.success(macAddress);
        }else if (call.method.equals("printSample")) {
            printLabelSample();
            result.success("print Sample");
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private int convert203To300DPI(int value) {
        if (mBixolonLabelPrinter.getPrinterDpi() == 2) return value;
        else return (int) (((float) value / 203.0f) * 300);
    }

    private Bitmap base64ToBitmap(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        byte[] decodedString = Base64.decode(data, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private void printLabelSample() {
        Log.e("printLabelSample", "printLabelSample: init" );
        mBixolonLabelPrinter.beginTransactionPrint();
        mBixolonLabelPrinter.drawBitmap(
                Bitmap.createScaledBitmap(
                        base64ToBitmap("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"),
                        convert203To300DPI(110),
                        convert203To300DPI(140),
                        true
                ),
                convert203To300DPI(60),
                convert203To300DPI(60),
                convert203To300DPI(110),
                convert203To300DPI(140),
                true
        );
        mBixolonLabelPrinter.print(1, 1);
        mBixolonLabelPrinter.endTransactionPrint();
    }
}

//---------------------------------------

