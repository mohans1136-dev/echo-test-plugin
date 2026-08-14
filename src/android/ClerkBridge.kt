package com.outsystems.plugin.clerk

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.clerk.api.Clerk
import com.clerk.api.ClerkConfigurationOptions
import com.clerk.api.SharedSessionSyncConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Kotlin Helper Bridge for Clerk Android SDK.
 * Manages Kotlin Coroutines scopes and converts suspending SDK calls into Java callbacks.
 */
object ClerkBridge {

    interface BridgeCallback {
        fun onSuccess(result: JSONObject)
        fun onError(errorMessage: String)
    }

    interface TokenCallback {
        fun onSuccess(jwtToken: String)
        fun onError(errorMessage: String)
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var isInitialized = false

    /**
     * Initializes the Clerk SDK with Shared Session Synchronization enabled.
     */
    @JvmStatic
    fun initialize(context: Context, publishableKey: String, callback: BridgeCallback) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val options = ClerkConfigurationOptions(
                        sharedSessionSync = SharedSessionSyncConfig.enabled
                    )
                    Clerk.initialize(
                        context = context,
                        publishableKey = publishableKey,
                        options = options
                    )
                }
                isInitialized = true
                val response = JSONObject().apply {
                    put("status", "initialized")
                    put("sharedSessionEnabled", true)
                    put("publishableKey", publishableKey)
                }
                callback.onSuccess(response)
            } catch (e: Exception) {
                callback.onError("Clerk Initialization Error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Handles incoming deep link intents (clerk://...) for authentication redirect callbacks.
     */
    @JvmStatic
    fun handleIntent(intent: Intent?) {
        if (intent == null || !isInitialized) return
        try {
            Clerk.onNewIntent(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Launches hosted authentication (OAuth / Web Sign-in) using Clerk SDK.
     */
    @JvmStatic
    fun startHostedAuth(activity: Activity, callback: BridgeCallback) {
        if (!isInitialized) {
            callback.onError("Clerk SDK is not initialized yet. Call initialize() first.")
            return
        }

        scope.launch {
            try {
                Clerk.auth.startHostedAuth(activity)
                val user = Clerk.user
                val session = Clerk.session

                val response = JSONObject().apply {
                    put("status", "success")
                    put("userId", user?.id ?: "")
                    put("sessionId", session?.id ?: "")
                    put("email", user?.primaryEmailAddress?.emailAddress ?: "")
                    put("firstName", user?.firstName ?: "")
                    put("lastName", user?.lastName ?: "")
                }
                callback.onSuccess(response)
            } catch (e: Exception) {
                callback.onError("Authentication Failed: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Sign out the active user session locally and remotely.
     */
    @JvmStatic
    fun signOut(callback: BridgeCallback) {
        if (!isInitialized) {
            callback.onError("Clerk SDK is not initialized.")
            return
        }

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Clerk.signOut()
                }
                val response = JSONObject().apply {
                    put("status", "signed_out")
                    put("message", "User session successfully signed out.")
                }
                callback.onSuccess(response)
            } catch (e: Exception) {
                callback.onError("SignOut Error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Fetches the active Clerk JWT session token.
     */
    @JvmStatic
    fun getSessionToken(callback: TokenCallback) {
        if (!isInitialized) {
            callback.onError("Clerk SDK is not initialized.")
            return
        }

        scope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    Clerk.session?.getToken()
                }
                if (!token.isNullOrEmpty()) {
                    callback.onSuccess(token)
                } else {
                    callback.onError("No active session token found.")
                }
            } catch (e: Exception) {
                callback.onError("Token Retrieval Error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Checks current session state (useful on app launch for shared session auto-login).
     */
    @JvmStatic
    fun getSessionState(callback: BridgeCallback) {
        if (!isInitialized) {
            callback.onError("Clerk SDK is not initialized.")
            return
        }

        scope.launch {
            try {
                val session = Clerk.session
                val user = Clerk.user
                val isAuthenticated = session != null && session.isActive

                val response = JSONObject().apply {
                    put("isAuthenticated", isAuthenticated)
                    put("sessionId", session?.id ?: "")
                    put("userId", user?.id ?: "")
                    put("email", user?.primaryEmailAddress?.emailAddress ?: "")
                    put("sharedSessionSynced", isAuthenticated)
                }
                callback.onSuccess(response)
            } catch (e: Exception) {
                callback.onError("Session Check Error: ${e.localizedMessage}")
            }
        }
    }
}
