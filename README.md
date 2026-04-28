# 📦 Bixolon Printer Plugin for Flutter

A powerful, lightweight, and developer-friendly Flutter plugin for seamless communication with **Bixolon Bluetooth printers**.  
Easily initialize the SDK, connect to printers, and print Base64 images with complete customization options.

## ✨ Features

- 🔌 Bluetooth-based printer connection
- 🧭 Simple, modern API using Method Channels
- 🖼️ Print images directly from Base64
- ⚙️ Fully customizable print configurations
- ⚡ Lightweight, fast, and production-ready
- 📂 Easy integration with Flutter apps

## 📦 Installation

Add the dependency in your **pubspec.yaml**:

```yaml
dependencies:
   bixolon_printer: ^latest_version
   ```
## Get packages:
```yaml
  flutter pub get
   ```

## Android Configuration
In android/app/build.gradle.kts
```yaml
    buildTypes {
        release {
            minifyEnabled true ///  👈 Need to add this 
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro' ///  👈 Need to add this 
signingConfig = signingConfigs.debug
        }
    }
   ```

## Need to create 'proguard-rules.pro' in android folder
```yaml
/// copy paste below content in file 
  
-ignorewarnings
-keep class * {
public private *;
}

   ```

# 🚀 Usage Guide
## 1. Import the plugin
```yaml
   import 'package:bixolon_printer/bixolon_printer.dart';
   ```

## 2. Create an instance
```yaml
   final _bixolonPrinterPlugin = BixolonPrinter();
   ```
## 3. Initialize the SDK
```yaml
   _bixolonPrinterPlugin.intiSDK();
   ```
## 4. Connect to the Bluetooth printer
```yaml
   var address = await _bixolonPrinterPlugin.connectSDK(
     macAddress: result.address,
   );
   ```
## 5. Print an image
```yaml
   _bixolonPrinterPlugin.printSample(
          printConfig: PrintConfig(
                          printQty: 1,
                          base64Image: "BASE64_OF_IMAGE",
                          dstHeight: 200,
                          dstWidth: 400,
                          verticalStartPosition: 5,
                          horizontalStartPosition: 15,
                          width: 400,
                          level: 50,
                              ),
                      );
   ```

## 6. Disconnect from the printer
```yaml
   _bixolonPrinterPlugin.disconnectSDK();
   ```
## 7. isConnected
```yaml
   var isConnected = await _bixolonPrinterPlugin.isConnected();
   print("Is printer connected? $isConnected");
   ```

## MIT License

```yaml
Copyright (c) 2025 ITECHNOLABS

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
   ```