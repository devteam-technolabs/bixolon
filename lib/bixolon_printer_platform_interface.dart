import 'package:bixolon_printer/model/print_config.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'bixolon_printer_method_channel.dart';

abstract class BixolonPrinterPlatform extends PlatformInterface {
  /// Constructs a BixolonPrinterPlatform.
  BixolonPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static BixolonPrinterPlatform _instance = MethodChannelBixolonPrinter();

  /// The default instance of [BixolonPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelBixolonPrinter].
  static BixolonPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BixolonPrinterPlatform] when
  /// they register themselves.
  static set instance(BixolonPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> disconnectPrinter() {
    throw UnimplementedError('disconnectPrinter() has not been implemented.');
  }

  Future<String?> initPrinter() {
    throw UnimplementedError('initPrinter() has not been implemented.');
  }

  Future<String?> connectPrinter({required String macAddress}) {
    throw UnimplementedError('connectPrinter() has not been implemented.');
  }

  Future<bool?> isConnected() {
    throw UnimplementedError('isConnected() has not been implemented.');
  }

  Future<String?> printSample({required PrintConfig printConfig}) {
    throw UnimplementedError('connectPrinter() has not been implemented.');
  }

  Stream<dynamic> get printerEvents {
    throw UnimplementedError("printerEvents has not been implemented.");
  }
}
