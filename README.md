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
│          Java/Kotlin Plugin (ClerkPlugin.kt)            │
│  • Manages Cordova lifecycle & onNewIntent() callbacks │
│  • Exposes pure Java/Kotlin interface to OutSystems     │
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

## 🛠 OutSystems Mobile Lifecycle Best Practices

### ⚠️ CRITICAL: Do NOT Call `Clerk_Initialize` inside Screen `OnInitialize`!

In OutSystems Mobile, the **`OnInitialize`** event runs **before the screen renders**. If an asynchronous JavaScript action (using `$resolve()`) is placed in `OnInitialize`, OutSystems **blocks screen rendering and holds the splash screen** until the promise resolves. Because `Clerk.initialize` performs network/session setup, placing it in `OnInitialize` freezes the splash screen.

#### ✅ Recommended Placement: Screen `OnReady` or Action Trigger
Call `Clerk_Initialize` inside the **Screen `OnReady`** event (which fires *after* the splash screen closes and screen renders) or when the user interacts with the app.

```javascript
// JavaScript Action: Clerk_Initialize (In Screen OnReady)
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
            $parameters.ErrorMessage = err.message || err;
            $resolve();
        });
} else {
    $parameters.IsAuthenticated = false;
    $parameters.ErrorMessage = "Running in browser";
    $resolve();
}
```

---

## 🔐 How Shared Session Sync Works Between `ClerkApp1` & `ClerkApp2`

1. User logs into **`ClerkApp1`** (`org.luvelo.dev.ClerkApp1`) via `cordova.plugins.clerk.signIn()`.
2. When the user opens **`ClerkApp2`** (`org.luvelo.dev.ClerkApp2`), `clerk.initialize()` runs on `OnReady`.
3. The Clerk Android SDK detects the active session stored under certificate `65:2...7:47`.
4. `getSessionState()` returns `isAuthenticated: true` in **`ClerkApp2`** without requiring the user to sign in again!

---

## 📄 License
Licensed under Apache 2.0.
