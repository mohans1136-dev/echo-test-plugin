# Echo Test Plugin (Cordova Android Kotlin Plugin for OutSystems)

This plugin is a recreation and enhancement of the tutorial **"Creating a Cordova Android Plugin in Kotlin"** by Erisu (published in *The Web Tub*), tailored for use in **Apache Cordova** projects and **OutSystems Native Mobile Applications**.

---

## 🔍 Background & Architecture

Starting with **Cordova-Android 9.0.0**, Apache Cordova natively supports Kotlin without requiring third-party hook scripts or Gradle workarounds. OutSystems Mobile App Builder (MAB) uses standard Cordova Android under the hood, making Kotlin fully compatible.

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

Follow these steps to integrate this Kotlin plugin into OutSystems Service Studio:

### Step 1: Host the Plugin Repository
Push this directory to a public or private Git repository (e.g., GitHub, GitLab, Bitbucket):
```bash
git init
git add .
git commit -m "Initial commit of Kotlin Cordova Echo Plugin"
git remote add origin https://github.com/YOUR_USERNAME/echo-test-plugin.git
git push -u origin main
```

*(Alternatively, you can compress this folder into a `.zip` file hosted on an accessible URL or Amazon S3).*

### Step 2: Configure Extensibility Configurations in OutSystems
1. Open your OutSystems Mobile Application module in **Service Studio**.
2. Select the main module in the logic tree and open the **Extensibility Configurations** property.
3. Add the following JSON snippet (replace with your repository URL):

```json
{
    "plugin": {
        "url": "https://github.com/YOUR_USERNAME/echo-test-plugin.git#v1.0.0"
    }
}
```

*(If using a ZIP file directly)*:
```json
{
    "plugin": {
        "url": "https://your-domain.com/plugins/echo-test-plugin.zip"
    }
}
```

### Step 3: Create OutSystems Client Action Wrappers

In Service Studio, create a Client Action named `Echo_CheckPlugin`:

```javascript
// JavaScript element inside Client Action
$parameters.IsLoaded = (typeof window.cordova !== 'undefined' && 
                        typeof window.cordova.plugins !== 'undefined' && 
                        typeof window.cordova.plugins.echo !== 'undefined');
```

Create a Client Action named `Echo_SendMessage`:
* Input Parameter: `Message` (Text)
* Output Parameter: `EchoedMessage` (Text)
* JavaScript element code:

```javascript
if (typeof cordova !== 'undefined' && cordova.plugins && cordova.plugins.echo) {
    cordova.plugins.echo.echoPromise($parameters.Message)
        .then(function(result) {
            $parameters.EchoedMessage = result;
            $resolve();
        })
        .catch(function(err) {
            $parameters.EchoedMessage = "";
            $reject(err);
        });
} else {
    $parameters.EchoedMessage = "Plugin not loaded (running in web browser)";
    $resolve();
}
```

---

## 🧪 Local Testing with Cordova CLI

You can test this plugin in a standard Cordova app:

```bash
# 1. Create test project
cordova create test-app com.example.testapp TestApp
cd test-app

# 2. Add Android platform
cordova platform add android@10

# 3. Add your local Kotlin plugin
cordova plugin add ../echo-test-plugin

# 4. Build and run
cordova run android
```

---

## 📄 License
Licensed under Apache 2.0.
