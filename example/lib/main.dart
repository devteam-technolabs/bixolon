import 'dart:async';

import 'package:bixolon_printer/bixolon_printer.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_blue_classic/flutter_blue_classic.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(home: MainScreen());
  }
}

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  final _flutterBlueClassicPlugin = FlutterBlueClassic();

  BluetoothAdapterState _adapterState = BluetoothAdapterState.unknown;
  StreamSubscription? _adapterStateSubscription;

  final Set<BluetoothDevice> _scanResults = {};
  StreamSubscription? _scanSubscription;

  bool _isScanning = false;
  int? _connectingToIndex;
  StreamSubscription? _scanningStateSubscription;
  final _bixolonPrinterPlugin = BixolonPrinter();

  @override
  void initState() {
    _bixolonPrinterPlugin.intiSDK();
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    BluetoothAdapterState adapterState = _adapterState;

    try {
      adapterState = await _flutterBlueClassicPlugin.adapterStateNow;
      _adapterStateSubscription = _flutterBlueClassicPlugin.adapterState.listen(
        (current) {
          if (mounted) setState(() => _adapterState = current);
        },
      );
      _scanSubscription = _flutterBlueClassicPlugin.scanResults.listen((
        device,
      ) {
        if (mounted) setState(() => _scanResults.add(device));
      });

      _flutterBlueClassicPlugin.bondedDevices.then((value) {
        value?.forEach((element) {
          if (mounted) setState(() => _scanResults.add(element));
        });
      });

      _scanningStateSubscription = _flutterBlueClassicPlugin.isScanning.listen((
        isScanning,
      ) {
        if (mounted) setState(() => _isScanning = isScanning);
      });
    } catch (e) {
      if (kDebugMode) print(e);
    }

    if (!mounted) return;

    setState(() {
      _adapterState = adapterState;
    });
  }

  @override
  void dispose() {
    _adapterStateSubscription?.cancel();
    _scanSubscription?.cancel();
    _scanningStateSubscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    List<BluetoothDevice> scanResults = _scanResults.toList();

    return Scaffold(
      appBar: AppBar(title: const Text('FlutterBlueClassic example app')),
      body: ListView(
        children: [
          ListTile(
            title: const Text("Bluetooth Adapter state"),
            subtitle: const Text("Tap to enable"),
            trailing: Text(_adapterState.name),
            leading: const Icon(Icons.settings_bluetooth),
            onTap: () => _flutterBlueClassicPlugin.turnOn(),
          ),
          const Divider(),
          if (scanResults.isEmpty)
            const Center(child: Text("No devices found yet"))
          else
            for (var (index, result) in scanResults.indexed)
              ListTile(
                title: Text("${result.name ?? "???"} (${result.address})"),
                subtitle: Text(
                  "Bondstate: ${result.bondState.name}, Device type: ${result.type.name}",
                ),
                trailing: index == _connectingToIndex
                    ? const CircularProgressIndicator()
                    : Text("${result.rssi} dBm"),
                onTap: () async {
                  BluetoothConnection? connection;
                  // setState(() => _connectingToIndex = index);
                  // try {
                  //   connection = await _flutterBlueClassicPlugin.connect(
                  //     result.address,
                  //   );
                  //   if (!this.context.mounted) return;
                  //   if (connection != null && connection.isConnected) {
                  var address = await _bixolonPrinterPlugin.connectSDK(
                    macAddress: result.address,
                  );
                  print(address);
                  await Future.delayed(Duration(seconds: 5));
                  _bixolonPrinterPlugin.printSample();
                  //   }
                  // } catch (e) {
                  //   if (mounted) setState(() => _connectingToIndex = null);
                  //   if (kDebugMode) print(e);
                  //   connection?.dispose();
                  //   ScaffoldMessenger.maybeOf(context)?.showSnackBar(
                  //     const SnackBar(
                  //       content: Text("Error connecting to device"),
                  //     ),
                  //   );
                  // }
                },
              ),
        ],
      ),
      floatingActionButton: Row(
        crossAxisAlignment: CrossAxisAlignment.end,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          FloatingActionButton.extended(
            onPressed: () async {
              await Permission.location.request();
              if (_isScanning) {
                _flutterBlueClassicPlugin.stopScan();
              } else {
                _scanResults.clear();
                _flutterBlueClassicPlugin.startScan();
              }
            },
            label: Text(_isScanning ? "Scanning..." : "Start device scan"),
            icon: Icon(
              _isScanning ? Icons.bluetooth_searching : Icons.bluetooth,
            ),
          ),
        ],
      ),
    );
  }
}

//
// class MyApp extends StatefulWidget {
//   const MyApp({super.key});
//
//   @override
//   State<MyApp> createState() => _MyAppState();
// }
//
// class _MyAppState extends State<MyApp> {
//   String _platformVersion = 'Unknown';
//   final _bixolonPrinterPlugin = BixolonPrinter();
//
//   @override
//   void initState() {
//     super.initState();
//     initPlatformState();
//   }
//
//   // Platform messages are asynchronous, so we initialize in an async method.
//   Future<void> initPlatformState() async {
//     String platformVersion;
//     // Platform messages may fail, so we use a try/catch PlatformException.
//     // We also handle the message potentially returning null.
//     try {
//       platformVersion =
//           await _bixolonPrinterPlugin.getPlatformVersion() ?? 'Unknown platform version';
//     } on PlatformException {
//       platformVersion = 'Failed to get platform version.';
//     }
//
//     // If the widget was removed from the tree while the asynchronous platform
//     // message was in flight, we want to discard the reply rather than calling
//     // setState to update our non-existent appearance.
//     if (!mounted) return;
//
//     setState(() {
//       _platformVersion = platformVersion;
//     });
//   }
//
//   @override
//   Widget build(BuildContext context) {
//     return MaterialApp(
//       home: Scaffold(
//         appBar: AppBar(
//           title: const Text('Plugin example app'),
//         ),
//         body: Column(
//           children: [
//             Center(
//               child: Text('Running on: $_platformVersion\n'),
//             ),
//             MaterialButton(
//               color: Colors.blue,
//               onPressed: ()async {
//                 await Permission.bluetooth.request();
//                 await Permission.bluetoothAdvertise.request();
//                 await Permission.bluetoothConnect.request();
//                 await Permission.bluetoothScan.request();
//                 await Permission.location.request();
//               final connectedDevices = FlutterBluePlus.connectedDevices;
//               log(connectedDevices.length.toString());
//               for (var element in connectedDevices) {
//                 log("Name ${element.advName} Remote Address ${element.remoteId.str}");
//               }
//
//               FlutterBluePlus.startScan(timeout: Duration(seconds: 50));
//
//                 var subscription = FlutterBluePlus.onScanResults.listen((results) {
//                   if (results.isNotEmpty) {
//                     ScanResult r = results.last; // the most recently found device
//                     print('${r.device.remoteId}: "${r.advertisementData.advName}" found!');
//                   }
//                 },
//                   onError: (e) => print(e),
//                 );
//
// // cleanup: cancel subscription when scanning stops
//                 FlutterBluePlus.cancelWhenScanComplete(subscription);
//
//
//             },child: Text("Search Bluetooth devices"),)
//           ],
//         ),
//       ),
//     );
//   }
// }
