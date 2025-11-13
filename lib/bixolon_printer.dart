
import 'bixolon_printer_platform_interface.dart';

class BixolonPrinter {
  Future<String?> getPlatformVersion() {
    return BixolonPrinterPlatform.instance.getPlatformVersion();
  }
  Future<String?> intiSDK() {
    return BixolonPrinterPlatform.instance.initPrinter();
  }
  Future<String?> connectSDK({required String macAddress}) {
    return BixolonPrinterPlatform.instance.connectPrinter(macAddress: macAddress);
  }
  Future<String?> printSample() {
    return BixolonPrinterPlatform.instance.printSample();
  }
}
