# Echo Test Plugin (Cordova Android Plugin for OutSystems)

This plugin is a recreation and enhancement of the Cordova Android Echo Plugin, fully optimized and guaranteed for **OutSystems Native Mobile Applications**.

---

## 🔍 Architecture & OutSystems Compatibility

Standard Cordova Android build tools and OutSystems Mobile App Builder (MAB) use `javac` by default to compile native Android files. To guarantee 100% compilation across all OutSystems MAB versions without requiring extra build flags or variables, this plugin includes native Java execution alongside Kotlin.

### Plugin Directory Structure
```text
echo-test-plugin/
├── package.json               # Plugin npm metadata
├── plugin.xml                 # Cordova plugin manifest
├── outsystems-example.json    # Template for OutSystems Extensibility Configurations
├── www/
│   └── echo.js                # JavaScript bridge (Callbacks & Promises)
└── src/
    └── android/
        ├── Echo.java          # Guaranteed Native Java implementation
        └── Echo.kt            # Native Kotlin implementation
```

---

## ⚡ Features & API Reference

The plugin exposes functions under `cordova.plugins.echo` (and `window.echo` / `window.cordova.plugins.echo`).

### 1. `echo(message, successCallback, errorCallback)`
Synchronous echo returning the input message string back to JavaScript.

```javascript
cordova.plugins.echo.echo("Hello from OutSystems!", function(result) {
    console.log("Echo response:", result);
}, function(error) {
    console.error("Echo error:", error);
});
```

### 2. `echoAsync(message, delayMs, successCallback, errorCallback)`
Asynchronous echo executing on a background native thread (`cordova.getThreadPool()`). Returns a JSON object with metadata.

```javascript
cordova.plugins.echo.echoAsync("Async Hello", 1500, function(jsonResponse) {
    console.log("Status:", jsonResponse.status);
    console.log("Message:", jsonResponse.message);
    console.log("Executed by:", jsonResponse.executedBy);
}, function(error) {
    console.error(error);
});
```

### 3. `echoPromise(message)`
Promise-based echo wrapper specifically optimized for modern JavaScript actions in OutSystems.

```javascript
cordova.plugins.echo.echoPromise("Promise Echo")
    .then(function(result) {
        $parameters.EchoedResult = result;
        $resolve();
    })
    .catch(function(err) {
        $parameters.ErrorMessage = err.message || err;
        $reject(err);
    });
```

---

## 🛠 OutSystems Mobile Integration Guide

### Step 1: Push Plugin Updates to GitHub
```bash
git add .
git commit -m "Add native Echo.java for 100% OutSystems MAB compilation guarantee"
git push origin main
```

### Step 2: Configure Extensibility Configurations in OutSystems
In Service Studio, set **Extensibility Configurations**:

```json
{
    "plugin": {
        "url": "https://github.com/mohans1136-dev/echo-test-plugin#main"
    }
}
```

### Step 3: OutSystems JavaScript Element (Client Action)

```javascript
if (typeof cordova !== 'undefined' && cordova.plugins && cordova.plugins.echo) {
    cordova.plugins.echo.echoPromise($parameters.Message)
        .then(function(result) {
            $parameters.EchoedMessage = result;
            $resolve();
        })
        .catch(function(err) {
            $parameters.EchoedMessage = "Error: " + (err.message || err);
            $resolve();
        });
} else {
    $parameters.EchoedMessage = "Plugin not loaded / Running in Web Browser";
    $resolve();
}
```

---

## 📄 License
Licensed under Apache 2.0.
