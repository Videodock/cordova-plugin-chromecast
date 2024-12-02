# cordova-plugin-chromecast

> [!NOTE]
> This repository is a fork of https://github.com/jellyfin-archive/cordova-plugin-chromecast and maintained each quarter
> by https://github.com/Videodock

## Installation

To install this fork, you can use the full GitHub URL with commit hash. Always try using a commit hash to prevent 
installing malicious code. This package is not published on NPM.

```
cordova plugin add https://github.com/Videodock/cordova-plugin-chromecast.git#5aca0b94b11c3c169d46c9ba1c4adfb6eaa8de46
```

### Android

Add the following metadata to your AndroidManifest.xml inside the `<application>` tag.

Replace `_APP_ID_` with your receiver application id.

```xml
<manifest>
  <application>
    <activity>
      ...
    </activity>
    <meta-data
            android:name="com.google.android.gms.cast.framework.RECEIVER_APPLICATION_ID"
            android:value="_APP_ID_" />
  </application>
</manifest>
```


### iOS

If you have trouble installing the plugin or running the project for iOS, from `/platforms/ios/` try running:

```bash
sudo gem install cocoapods
pod repo update
pod install
```
#### Privacy Manifest

Apple mandates that app developers specify approved reasons for API usage to enhance user privacy. By May 1st, 2024, it's required to include these reasons when submitting apps to the App Store Connect.

When using this specific plugin in your app, you must create a `PrivacyInfo.xcprivacy` file in your XCode project, specifying the usage reasons.

**This plugin maps `kGCKMetadataKeyCreationDate` from the [Chromecast API](https://developers.google.com/cast/docs/reference/ios/interface_g_c_k_media_metadata) to `creationDate` from Apple. The recommended reason is [C617.1](https://developer.apple.com/documentation/bundleresources/privacy_manifest_files/describing_use_of_required_reason_api#4278393).**

#### Example PrivacyInfo.xcprivacy

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
  <dict>
    <key>NSPrivacyAccessedAPITypes</key>
    <array>
      <!-- Add this dict entry to the array if the PrivacyInfo file already exists -->
      <dict>
        <key>NSPrivacyAccessedAPIType</key>
        <string>NSPrivacyAccessedAPICategoryFileTimestamp</string>
        <key>NSPrivacyAccessedAPITypeReasons</key>
        <array>
          <string>C617.1</string>
        </array>
      </dict>
    </array>
  </dict>
</plist>
```

#### Additional iOS Installation Instructions
To **distribute** an iOS app with this plugin you must add usage descriptions to your project's `config.xml`.  
The "*Description" key strings will be used when asking the user for permission to use the microphone/bluetooth/local network.  
```xml
<platform name="ios">
  <!-- ios 6-13 (deprecated) -->
  <config-file parent="NSBluetoothPeripheralUsageDescription" target="*-Info.plist" comment="cordova-plugin-chromecast">
      <string>Bluetooth is required to scan for nearby Chromecast devices with guest mode enabled.</string>
  </config-file>
  <!-- ios 13+ -->
  <config-file parent="NSBluetoothAlwaysUsageDescription" target="*-Info.plist" comment="cordova-plugin-chromecast">
      <string>Bluetooth is required to scan for nearby Chromecast devices with guest mode enabled.</string>
  </config-file>
  <config-file parent="NSMicrophoneUsageDescription" target="*-Info.plist" comment="cordova-plugin-chromecast">
      <string>The microphone is required to pair with nearby Chromecast devices with guest mode enabled.</string>
  </config-file>
  <!-- ios 14+ -->
  <config-file parent="NSLocalNetworkUsageDescription" target="*-Info.plist" comment="cordova-plugin-chromecast">
      <string>The local network permission is required to discover Cast-enabled devices on your WiFi network.</string>
  </config-file>
  <config-file parent="NSBonjourServices" target="*-Info.plist" comment="cordova-plugin-chromecast">
    <array>
      <string>_googlecast._tcp</string>
      <!-- The default receiver ID -->
      <string>_CC1AD845._googlecast._tcp</string>
      <!-- IF YOU USE A CUSTOM RECEIVER, replace the line above, and put your ID instead of "[YourCustomRecieverID]" -->
      <!-- <string>_[YourCustomRecieverID]._googlecast._tcp</string> -->
    </array>
  </config-file>
</platform>
```

1. In AppDelegate.m (or AppDelegate.swift) add

```
#import <GoogleCast/GoogleCast.h>
```

```swift
import GoogleCast
```

and insert the following in the `application:didFinishLaunchingWithOptions` method, ideally at the beginning:

```java
NSString *receiverAppID = kGCKDefaultMediaReceiverApplicationID; // or @"ABCD1234"
GCKDiscoveryCriteria *criteria = [[GCKDiscoveryCriteria alloc] initWithApplicationID:receiverAppID];
GCKCastOptions* options = [[GCKCastOptions alloc] initWithDiscoveryCriteria:criteria];
[GCKCastContext setSharedInstanceWithOptions:options];
```

```swift
let receiverAppID = kGCKDefaultMediaReceiverApplicationID // or "ABCD1234"
let criteria = GCKDiscoveryCriteria(applicationID: receiverAppID)
let options = GCKCastOptions(discoveryCriteria: criteria)
GCKCastContext.setSharedInstanceWith(options)
```

If using a custom receiver, replace kGCKDefaultMediaReceiverApplicationID with your receiver app id.

You can also automatically set the receiver ID by parsing the Bonjour Services string. This is useful when you have 
multiple Info.plist files for different environments. 

```swift
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        initializeCastSender()
        return true
    }
    
    func initializeCastSender() {
        if let value = Bundle.init(for: AppDelegate.self).infoDictionary?["NSBonjourServices"] as? [String] {
            
            if let rule = value.first(where: { $0.hasSuffix("._googlecast._tcp") }) {
                let startIndex = rule.index(rule.startIndex, offsetBy: 1)
                let endIndex = rule.index(rule.startIndex, offsetBy: 9)
                let receiverAppID = String(rule[startIndex..<endIndex]);
                
                let criteria = GCKDiscoveryCriteria(applicationID: receiverAppID)
                let options = GCKCastOptions(discoveryCriteria: criteria)
                
                GCKCastContext.setSharedInstanceWith(options)
                
                print("Initialized Cast SDK with appId: \(receiverAppID)")
            } else {
                print("Couldn't initialize Cast SDK, NSBonjourServices is defined, but couldn't find _<appId>._googlecast._tcp in the Array");
            }
        } else {
            print("Couldn't initialize Cast SDK, NSBonjourServices is missing from the Info.plist");
        }
    }
```

## Supports

**Android** 4.4+ (may support lower, untested)  
**iOS** 10.0+ (The [Google Cast iOS Sender SDK 4.5.0](https://developers.google.com/cast/docs/release-notes#september-14,-2020) says iOS 10+ but all tests on the plugin work fine for iOS 9.3.5, so it appears to work on iOs 9 anyways. :/)

### Quirks
* Android 4.4 (maybe 5.x and 6.x) are not able automatically rejoin/resume a chromecast session after an app restart.  

## Usage

This project attempts to implement the [official Google Cast API for Chrome](https://developers.google.com/cast/docs/reference/chrome#chrome.cast) within the Cordova webview.  
This means that you should be able to write almost identical code in cordova as you would if you were developing for desktop Chrome.  

We have not implemented every function in the [API](https://developers.google.com/cast/docs/reference/chrome#chrome.cast) but most of the core functions are there.  If you find a function is missing we welcome [pull requests](#contributing)!  Alternatively, you can file an [issue](https://github.com/jellyfin/cordova-plugin-chromecast/issues), please include a code sample of the expected functionality if possible!

The most significant usage difference between the [cast API](https://developers.google.com/cast/docs/reference/chrome#chrome.cast) and this plugin is the initialization.

In **Chrome desktop** you would do:
```js
window['__onGCastApiAvailable'] = function(isAvailable, err) {
    if (isAvailable) {
        // start using the api!
    }
};
```

But in **cordova-plugin-chromecast** you do:
```js
document.addEventListener("deviceready", function () {
    // start using the api!
});
```

The `SessionRequest#appId` property is not used to define the receiver app and can be set to an empty string to disable the warning.
Reason for this is that the Cast SDK can be initialized on app startup. Also, for iOS, the Info.plist must be configured with your receiver ID. This makes it easy to mis align the application ID in the initialization and native config.  
```js
var sessionRequest = new chrome.cast.SessionRequest('A11B22C'); // doesn't use A11B22C (triggers console warning)
var sessionRequest = new chrome.cast.SessionRequest(''); // disables console warning

var apiConfig = new chrome.cast.ApiConfig(sessionRequest);
```


### Example Usage
Here is a simple [example](doc/example.js) that loads a video, pauses it, and ends the session.

## API
Here are the supported [Chromecast API]((https://developers.google.com/cast/docs/reference/chrome#chrome.cast)) methods.  Any object types required by any of these methods are also supported. (eg. [chrome.cast.ApiConfig](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.ApiConfig)).  You can search [chrome.cast.js](www/chrome.cast.js) to check if an API is supported.

* [chrome.cast.initialize](https://developers.google.com/cast/docs/reference/chrome/chrome.cast#.initialize)  
* [chrome.cast.requestSession](https://developers.google.com/cast/docs/reference/chrome/chrome.cast#.requestSession)  

[chrome.cast.Session](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session)  
Most *Properties* Supported.  
Supported *Methods*:  
* [setReceiverVolumeLevel](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#setReceiverVolumeLevel)  
* [setReceiverMuted](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#setReceiverMuted)  
* [stop](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#stop)  
* [leave](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#leave)  
* [sendMessage](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#sendMessage)  
* [loadMedia](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#loadMedia)  
* [queueLoad](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#queueLoad)  
* [addUpdateListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#addUpdateListener)  
* [removeUpdateListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#removeUpdateListener)  
* [addMessageListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#addMessageListener)  
* [removeMessageListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#removeMessageListener)  
* [addMediaListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#addMediaListener)  
* [removeMediaListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.Session#removeMediaListener)  

[chrome.cast.media.Media](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media)  
Most *Properties* Supported.  
Supported *Methods*:  
* [play](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#play)  
* [pause](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#pause)  
* [seek](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#seek)  
* [stop](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#stop)  
* [setVolume](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#setVolume)  
* [supportsCommand](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#supportsCommand)  
* [getEstimatedTime](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#getEstimatedTime)  
* [editTracksInfo](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#editTracksInfo)  
* [queueJumpToItem](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#queueJumpToItem)  
* [addUpdateListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#addUpdateListener)  
* [removeUpdateListener](https://developers.google.com/cast/docs/reference/chrome/chrome.cast.media.Media.html#removeUpdateListener)  


### Specific to this plugin
We have added some additional methods that are unique to this plugin (that *do not* exist in the chrome cast API).
They can all be found in the `chrome.cast.cordova` object. 

To make your own **custom route selector** use this:
```js
// This will begin an active scan for routes
chrome.cast.cordova.scanForRoutes(function (routes) {
    // Here is where you should update your route selector view with the current routes
    // This will called each time the routes change
    // routes is an array of "Route" objects (see below)
}, function (err) {
    // Will return with err.code === chrome.cast.ErrorCode.CANCEL when the scan has been ended
});

// When the user selects a route
// stop the scan to save battery power
chrome.cast.cordova.stopScan();

// and use the selected route.id to join the route
chrome.cast.cordova.selectRoute(route.id, function (session) {
    // Save the session for your use
}, function (err) {
    // Failed to connect to the route
});

```

**Route** object
```text
id             {string}  - Route id
name           {string}  - User friendly route name
isCastGroup    {boolean} - Is the route a cast group?
isNearbyDevice {boolean} - Is it a device only accessible via guest mode?
                           (aka. probably not on the same network, but is nearby and allows guests)
```


## Plugin Development

### Setup

Follow these direction to set up for plugin development:

* You will need an existing cordova project or [create a new cordova project](https://cordova.apache.org/#getstarted).
* Add the chromecast and chromecast tests plugins:
  * `cordova plugin add --link <path to plugin>`
  * `cordova plugin add --link <path to plugin>/tests`
  * This --link** option may require **admin permission**

### **About the `--link` flag
The `--link` flag allows you to modify the native code (java/swift/obj-c) directly in the relative platform folder if desired.
  * This means you can work directly from Android Studio/Xcode!
  * Note: Be careful about adding and deleting files.  These changes will be exclusive to the platform folder and will not be transferred back to your plugin folder.
  * Note: The link only works for native files.  Other files such as js/css/html/etc must **not** be modified in the platform folder, these changes will be lost.
    * To update the js/css/html/etc files you must run:
        * `cordova plugin remove <plugin-name>`
        * With **admin permission**: `cordova plugin add --link <relative path to the plugin's root dir>`

## Testing

### Code Format

Run `npm test` to ensure your code fits the styling.  It will also find some errors.

  * If errors are found, you can try running `npm run style`, this will attempt to automatically fix the errors.
