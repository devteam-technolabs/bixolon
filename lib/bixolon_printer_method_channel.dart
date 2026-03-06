import 'package:bixolon_printer/model/print_config.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'bixolon_printer_platform_interface.dart';

/// An implementation of [BixolonPrinterPlatform] that uses method channels.
class MethodChannelBixolonPrinter extends BixolonPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('bixolon_printer');

  /// Event channel for receiving callbacks from native
  @visibleForTesting
  static const EventChannel eventChannel = EventChannel('bixolon_printer/events');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> initPrinter() async {
    final version = await methodChannel.invokeMethod<String>('initPrinter');
    return version;
  }

  @override
  Future<String?> connectPrinter({required String macAddress}) async {
    final version = await methodChannel.invokeMethod<String>('connectSDK', {
      'mac_address': macAddress,
    });
    return version;
  }

  @override
  Future<String?> printSample({required PrintConfig printConfig}) async {
    final response = await methodChannel.invokeMethod<String>('printSample', printConfig.toJson());
    return response;
  }

  @override
  Future<String?> disconnectPrinter() async {
    final response = await methodChannel.invokeMethod<String>('disconnect');
    return response;
  }
  @override
  Future<bool> isConnected() async {
    final result = await methodChannel.invokeMethod<bool>('isConnected');
    return result ?? false;
  }

  /// Event stream from native Android/iOS
  Stream<dynamic> get printerEvents => eventChannel.receiveBroadcastStream();
}
