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
    return BixolonPrinterPlatform.instance.connectPrinter(
      macAddress: macAddress,
    );
  }

  /// Sends a sample print job to the connected Bixolon printer.
  Future<String?> printSample() {
    return BixolonPrinterPlatform.instance.printSample();
  }
}
