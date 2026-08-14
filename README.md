# Clerk Cordova Plugin for OutSystems (Shared Session Sync)

This plugin wraps the official **Clerk Android SDK** (`com.clerk:clerk-android-api:1.0.35`) for **OutSystems Native Mobile Applications**, featuring **Java-friendly architecture** and **Shared Session Synchronization** between sibling applications (`Clerk_App_1` & `Clerk_App_2`).

---

## 🔍 Architecture & Design

```text
┌─────────────────────────────────────────────────────────┐
│           OutSystems Native Mobile Application           │
│           (Client Action in Service Studio)             │
└────────────────────────────┬────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│             JavaScript Bridge (www/clerk.js)            │
│  • clerk.initialize() • clerk.signIn() • clerk.signOut()│
└────────────────────────────┬────────────────────────────┘
                             │ (cordova.exec)
                             ▼
┌─────────────────────────────────────────────────────────┐
│          Java Cordova Plugin (ClerkPlugin.java)         │
│  • Manages Cordova lifecycle & onNewIntent() callbacks │
│  • Exposes pure Java interface to OutSystems            │
└────────────────────────────┬────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│            Kotlin Bridge (src/android/ClerkBridge.kt)   │
│  • Handles Coroutines scope & Kotlin suspend calls     │
│  • Calls Clerk.initialize(..., SharedSessionSync.enabled│
└────────────────────────────┬────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────┐
│        Clerk Android SDK (clerk-android-api)            │
│  • Native Auth, Hosted Auth, Shared Session Sync        │
└────────────────────────────┴────────────────────────────┘
```

---

## ⚡ JavaScript API Reference (`cordova.plugins.clerk`)

### 1. `initialize(publishableKey)`
Initializes Clerk SDK with `SharedSessionSyncConfig.enabled`.
```javascript
cordova.plugins.clerk.initialize("pk_test_...")
    .then(function(res) {
        console.log("Clerk Initialized with Shared Session:", res);
    })
    .catch(function(err) {
        console.error("Init Error:", err);
    });
```

### 2. `signIn()`
Triggers Hosted Auth / Web Login flow.
```javascript
cordova.plugins.clerk.signIn()
    .then(function(userSession) {
        console.log("User Logged In:", userSession.userId);
        console.log("Primary Email:", userSession.email);
    })
    .catch(function(err) {
        console.error("Sign-In Error:", err);
    });
```

### 3. `signOut()`
Signs out the user session locally and remotely across sibling apps.
```javascript
cordova.plugins.clerk.signOut()
    .then(function(res) {
        console.log("Signed Out:", res.message);
    })
    .catch(function(err) {
        console.error("Sign-Out Error:", err);
    });
```

### 4. `getToken()`
Retrieves the active Clerk JWT session token (for OutSystems REST API `Authorization: Bearer <token>` calls).
```javascript
cordova.plugins.clerk.getToken()
    .then(function(jwtToken) {
        console.log("JWT Token:", jwtToken);
    })
    .catch(function(err) {
        console.error("Token Error:", err);
    });
```

### 5. `getSessionState()`
Checks if an active session exists (used on app launch in `ClerkApp2` for automatic SSO sync from `ClerkApp1`).
```javascript
cordova.plugins.clerk.getSessionState()
    .then(function(state) {
        if (state.isAuthenticated) {
            console.log("User automatically authenticated via Shared Session!");
            console.log("User ID:", state.userId);
        } else {
            console.log("User is not signed in.");
        }
    })
    .catch(function(err) {
        console.error("Session Check Error:", err);
    });
```

---

## 🛠 OutSystems Mobile Integration Guide

### Step 1: Configure Service Studio Extensibility Configurations

In your OutSystems Mobile App module property **Extensibility Configurations**:

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
                "name": "GRADLEPLUGINKOTLINVERSION",
                "value": "1.9.20" 
            }
        ]
    }
}
```

### Step 2: Ensure OutSystems App Identifier Matches Clerk Dashboard

1. **`Clerk_App_1`**: In Service Studio, set App Identifier to `org.luvelo.dev.ClerkApp1`.
2. **`Clerk_App_2`**: In Service Studio, set App Identifier to `org.luvelo.dev.ClerkApp2`.

### Step 3: OutSystems Client Actions (JavaScript Elements)

#### Action `Clerk_Initialize` (Call on Application Ready / Screen OnInitialize)
```javascript
if (typeof cordova !== 'undefined' && cordova.plugins && cordova.plugins.clerk) {
    cordova.plugins.clerk.initialize($parameters.PublishableKey)
        .then(function() {
            return cordova.plugins.clerk.getSessionState();
        })
        .then(function(state) {
            $parameters.IsAuthenticated = state.isAuthenticated;
            $parameters.UserId = state.userId || "";
            $parameters.Email = state.email || "";
            $resolve();
        })
        .catch(function(err) {
            $parameters.IsAuthenticated = false;
            $parameters.ErrorMessage = "Clerk Init Error: " + (err.message || err);
            $resolve();
        });
} else {
    $parameters.IsAuthenticated = false;
    $parameters.ErrorMessage = "Clerk Plugin not loaded (Running in browser)";
    $resolve();
}
```

#### Action `Clerk_SignIn`
```javascript
if (typeof cordova !== 'undefined' && cordova.plugins && cordova.plugins.clerk) {
    cordova.plugins.clerk.signIn()
        .then(function(userSession) {
            $parameters.UserId = userSession.userId;
            $parameters.Email = userSession.email;
            $resolve();
        })
        .catch(function(err) {
            $parameters.ErrorMessage = err.message || err;
            $resolve();
        });
} else {
    $parameters.ErrorMessage = "Plugin not loaded";
    $resolve();
}
```

#### Action `Clerk_SignOut`
```javascript
if (typeof cordova !== 'undefined' && cordova.plugins && cordova.plugins.clerk) {
    cordova.plugins.clerk.signOut()
        .then(function() {
            $parameters.Success = true;
            $resolve();
        })
        .catch(function(err) {
            $parameters.Success = false;
            $resolve();
        });
} else {
    $parameters.Success = false;
    $resolve();
}
```

---

## 🔐 How Shared Session Sync Works Between `ClerkApp1` & `ClerkApp2`

1. User logs into **`ClerkApp1`** (`org.luvelo.dev.ClerkApp1`) via `cordova.plugins.clerk.signIn()`.
2. When the user opens **`ClerkApp2`** (`org.luvelo.dev.ClerkApp2`), `clerk.initialize()` runs on app startup.
3. The Clerk Android SDK detects the active session stored under certificate `65:2...7:47`.
4. `getSessionState()` returns `isAuthenticated: true` in **`ClerkApp2`** without requiring the user to sign in again!

---

## 📄 License
Licensed under Apache 2.0.
