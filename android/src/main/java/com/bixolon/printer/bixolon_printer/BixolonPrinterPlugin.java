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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bixolon.labelprinter.BixolonLabelPrinter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class BixolonPrinterPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    private MethodChannel channel;
    private EventChannel.EventSink eventSink;
    private MethodChannel.Result pendingPrintResult;
    private MethodChannel.Result pendingConnectResult;
    static BixolonLabelPrinter mBixolonLabelPrinter;
    private Activity activity;

    // -------------------------
    // EVENT CHANNEL
    // -------------------------
    private void sendCallback(Object value) {
        if (eventSink != null) {
            eventSink.success(value);
        }
    }

    // -------------------------
    // HANDLER CALLBACK
    // -------------------------
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case BixolonLabelPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {

                        case BixolonLabelPrinter.STATE_CONNECTED:
                            Log.e("Bixolon", "Connected");
                            sendCallback("connected");
                            break;

                        case BixolonLabelPrinter.STATE_CONNECTING:
                            Log.e("Bixolon", "Connecting...");
                            break;

                        case BixolonLabelPrinter.STATE_NONE:
                            Log.e("Bixolon", "Connection Failed");
                            if (eventSink != null) {
                                eventSink.error("connection_failed", "Unable to connect", null);
                            }
                            break;
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_READ:
                    Log.e("Bixolon", "MESSAGE_READ (print success assumed)");
                    sendCallback("print_success");
                    break;

                case BixolonLabelPrinter.MESSAGE_TOAST:
                    Log.e("Bixolon", "Toast message (print error triggered)");
                    if (eventSink != null) {
                        eventSink.error("print_failed", "Print failed", null);
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_DEVICE_NAME:
                    Log.e("Bixolon", "Device: " +
                            msg.getData().getString(BixolonLabelPrinter.DEVICE_NAME));
                    break;
            }
        }
    };

    // -------------------------
    // PLUGIN ATTACH
    // -------------------------
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {

        channel = new MethodChannel(binding.getBinaryMessenger(), "bixolon_printer");
        channel.setMethodCallHandler(this);

        new EventChannel(binding.getBinaryMessenger(), "bixolon_printer_callback")
                .setStreamHandler(new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventChannel.EventSink events) {
                        eventSink = events;
                    }

                    @Override
                    public void onCancel(Object arguments) {
                        eventSink = null;
                    }
                });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    // -------------------------
    // ACTIVITY ATTACH
    // -------------------------
    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override public void onDetachedFromActivityForConfigChanges() { this.activity = null; }
    @Override public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) { this.activity = binding.getActivity(); }
    @Override public void onDetachedFromActivity() { this.activity = null; }

    // -------------------------
    // METHOD CALLS FROM FLUTTER
    // -------------------------
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {

        switch (call.method) {

            case "initPrinter":
                mBixolonLabelPrinter = new BixolonLabelPrinter(activity, mHandler, Looper.getMainLooper());
                result.success("initialized");
                break;

            case "connectSDK":
                String mac = call.argument("mac_address");
                pendingConnectResult = result;
                mBixolonLabelPrinter.connect(mac);
                result.success("connecting");
                break;

            case "printSample":
                pendingPrintResult = result;
                printLabel(
                        call.argument("dstWidth"),
                        call.argument("dstHeight"),
                        call.argument("horizontalStartPosition"),
                        call.argument("verticalStartPosition"),
                        call.argument("width"),
                        call.argument("level"),
                        call.argument("printQty"),
                        call.argument("base64Image")
                );
                result.success("printing");
                break;

            default:
                result.notImplemented();
        }
    }

    // -------------------------
    // PRINT IMAGE
    // -------------------------
    private Bitmap base64ToBitmap(String data) {
        if (data == null || data.isEmpty()) return null;
        byte[] decodedString = Base64.decode(data, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private void printLabel(
            int dstWidth,
            int dstHeight,
            int horizontalStartPosition,
            int verticalStartPosition,
            int width,
            int level,
            int printQty,
            String base64Image
    ) {

        try {
            mBixolonLabelPrinter.beginTransactionPrint();

            Bitmap bmp = Bitmap.createScaledBitmap(
                    base64ToBitmap(base64Image),
                    dstWidth,
                    dstHeight,
                    true
            );

            mBixolonLabelPrinter.drawBitmap(
                    bmp,
                    horizontalStartPosition,
                    verticalStartPosition,
                    width,
                    level,
                    true
            );

            mBixolonLabelPrinter.print(printQty, 1);

            mBixolonLabelPrinter.endTransactionPrint();

            sendCallback("print_done");
            sendCallback("print_count:" + printQty);

        } catch (Exception e) {
            if (eventSink != null) {
                eventSink.error("print_error", e.getMessage(), null);
            }
        }
    }
}
