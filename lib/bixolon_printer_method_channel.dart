import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'bixolon_printer_platform_interface.dart';

/// An implementation of [BixolonPrinterPlatform] that uses method channels.
class MethodChannelBixolonPrinter extends BixolonPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('bixolon_printer');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
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
  Future<String?> printSample()async {
    log('init printSample');
    final response = await methodChannel.invokeMethod<String>('printSample');
    return response;
  }

}
