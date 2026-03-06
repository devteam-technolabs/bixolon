import 'package:bixolon_printer/model/print_config.dart';

import 'bixolon_printer_platform_interface.dart';

/// A wrapper class that exposes simple methods
/// to interact with the Bixolon printer plugin.
class BixolonPrinter {
  /// Returns the platform version (Android/iOS) from the native side.
  Future<String?> getPlatformVersion() {
    return BixolonPrinterPlatform.instance.getPlatformVersion();
  }

  /// Initializes the Bixolon printer SDK.
  /// Should be called before attempting to connect or print.
  Future<String?> intiSDK() {
    return BixolonPrinterPlatform.instance.initPrinter();
  }

  /// Connects to a Bixolon printer using its MAC address.
  ///
  /// [macAddress] must be the Bluetooth MAC address of the printer.
  Future<String?> connectSDK({required String macAddress}) {
    return BixolonPrinterPlatform.instance.connectPrinter(macAddress: macAddress);
  }

  /// Sends a sample print job to the connected Bixolon printer.
  Future<String?> printSample({required PrintConfig printConfig}) {
    return BixolonPrinterPlatform.instance.printSample(printConfig: printConfig);
  }

  /// Sends a sample print job to the connected Bixolon printer.
  Future<bool?> isConnected() {
    return BixolonPrinterPlatform.instance.isConnected();
  }

  /// Listen to ALL native events (connected, print_success, errors)
  Stream<dynamic> listenToEvents() {
    return BixolonPrinterPlatform.instance.printerEvents;
  }

  /// Disconnect printer
  Future<String?> disconnectSDK() {
    return BixolonPrinterPlatform.instance.disconnectPrinter();
  }
}
