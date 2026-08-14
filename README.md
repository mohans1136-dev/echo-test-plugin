# Echo Test Plugin (Cordova Android Kotlin Plugin for OutSystems)

This plugin is a recreation and enhancement of the tutorial **"Creating a Cordova Android Plugin in Kotlin"** by Erisu (published in *The Web Tub*), tailored for use in **Apache Cordova** projects and **OutSystems Native Mobile Applications**.

---

## 🔍 Background & Architecture

Starting with **Cordova-Android 9.0.0**, Apache Cordova natively supports Kotlin without requiring third-party hook scripts or Gradle workarounds. OutSystems Mobile App Builder (MAB) uses standard Cordova Android under the hood, making Kotlin fully compatible when Gradle source directories and Kotlin stdlib are configured via `build-extras.gradle`.

### Plugin Directory Structure
```text
echo-test-plugin/
├── package.json               # Plugin npm metadata
├── plugin.xml                 # Cordova plugin manifest & Kotlin configuration
├── outsystems-example.json    # Template for OutSystems Extensibility Configurations
├── www/
│   └── echo.js                # JavaScript bridge (Callbacks & Promises)
└── src/
    └── android/
        ├── Echo.kt            # Native Kotlin implementation
        └── build-extras.gradle# Gradle extras for Kotlin stdlib & source directory mapping
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
Asynchronous echo executing on a background Kotlin thread (`cordova.threadPool`). Returns a JSON object with metadata.

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
git commit -m "Update Kotlin plugin with build-extras.gradle for OutSystems"
git push origin main
```

### Step 2: Configure Extensibility Configurations in OutSystems
In Service Studio, update **Extensibility Configurations**:

```json
{
    "plugin": {
        "url": "https://github.com/mohans1136-dev/echo-test-plugin#main",
        "variables": [
            {
                "name": "GRADLEPLUGINKOTLINENABLED",
                "value": "true" 
            },
            {
                "name": "GRADLEPLUGINKOTLINCODESTYLE",
                "value": "official" 
            },
            {
                "name": "GRADLEPLUGINKOTLINVERSION",
                "value": "1.9.0" 
            }
        ]
    }
}
```

### Step 3: OutSystems JavaScript Element (Client Action)

When creating a JavaScript element in an OutSystems Client Action, **you MUST ensure `$resolve()` is called in all execution branches** (including `else` and `.catch()`). If `$resolve()` is skipped, OutSystems will wait indefinitely and freeze the action!

```javascript
if (typeof cordova !== 'undefined' && cordova.plugins && cordova.plugins.echo) {
    cordova.plugins.echo.echoPromise($parameters.Message)
        .then(function(result) {
            $parameters.EchoedMessage = result;
            $resolve();
        })
        .catch(function(err) {
            $parameters.EchoedMessage = "Kotlin Error: " + (err.message || err);
            $resolve(); // Call $resolve() to allow OutSystems flow to proceed
        });
} else {
    $parameters.EchoedMessage = "Plugin not loaded / Running in Web Browser";
    $resolve(); // CRITICAL: Always call $resolve() in else branch!
}
```

---

## 🧪 Local Testing with Cordova CLI

```bash
# 1. Create test project
cordova create test-app com.example.testapp TestApp
cd test-app

# 2. Add Android platform
cordova platform add android@10

# 3. Add local Kotlin plugin
cordova plugin add ../echo-test-plugin

# 4. Build and run
cordova run android
```

---

## 📄 License
Licensed under Apache 2.0.
