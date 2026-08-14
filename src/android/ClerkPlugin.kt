package com.outsystems.plugin.clerk

import android.content.Intent
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Clerk Cordova Plugin written in Kotlin for OutSystems Mobile App Builder (MAB).
 * Fully compatible with Clerk Android SDK and Shared Session Synchronization.
 */
class ClerkPlugin : CordovaPlugin() {

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Pass incoming deep link intent (clerk://...) to Clerk SDK
        ClerkBridge.handleIntent(intent)
    }

    @Throws(JSONException::class)
    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        return when (action) {
            "initialize" -> {
                initializeClerk(args, callbackContext)
                true
            }
            "signIn" -> {
                signIn(callbackContext)
                true
            }
            "signOut" -> {
                signOut(callbackContext)
                true
            }
            "getToken" -> {
                getToken(callbackContext)
                true
            }
            "getSessionState" -> {
                getSessionState(callbackContext)
                true
            }
            else -> false
        }
    }

    private fun initializeClerk(args: JSONArray, callbackContext: CallbackContext) {
        try {
            val publishableKey = if (!args.isNull(0)) args.getString(0) else ""
            if (publishableKey.isEmpty()) {
                callbackContext.error("Missing Clerk Publishable Key.")
                return
            }

            cordova.activity.runOnUiThread {
                ClerkBridge.initialize(
                    cordova.activity.applicationContext,
                    publishableKey,
                    object : ClerkBridge.BridgeCallback {
                        override fun onSuccess(result: JSONObject) {
                            callbackContext.success(result)
                        }

                        override fun onError(errorMessage: String) {
                            callbackContext.error(errorMessage)
                        }
                    }
                )
            }
        } catch (e: Exception) {
            callbackContext.error("Initialize Plugin Error: ${e.localizedMessage}")
        }
    }

    private fun signIn(callbackContext: CallbackContext) {
        cordova.activity.runOnUiThread {
            ClerkBridge.startHostedAuth(
                cordova.activity,
                object : ClerkBridge.BridgeCallback {
                    override fun onSuccess(result: JSONObject) {
                        callbackContext.success(result)
                    }

                    override fun onError(errorMessage: String) {
                        callbackContext.error(errorMessage)
                    }
                }
            )
        }
    }

    private fun signOut(callbackContext: CallbackContext) {
        ClerkBridge.signOut(object : ClerkBridge.BridgeCallback {
            override fun onSuccess(result: JSONObject) {
                callbackContext.success(result)
            }

            override fun onError(errorMessage: String) {
                callbackContext.error(errorMessage)
            }
        })
    }

    private fun getToken(callbackContext: CallbackContext) {
        ClerkBridge.getSessionToken(object : ClerkBridge.TokenCallback {
            override fun onSuccess(jwtToken: String) {
                try {
                    val res = JSONObject().apply {
                        put("token", jwtToken)
                    }
                    callbackContext.success(res)
                } catch (e: Exception) {
                    callbackContext.success(jwtToken)
                }
            }

            override fun onError(errorMessage: String) {
                callbackContext.error(errorMessage)
            }
        })
    }

    private fun getSessionState(callbackContext: CallbackContext) {
        ClerkBridge.getSessionState(object : ClerkBridge.BridgeCallback {
            override fun onSuccess(result: JSONObject) {
                callbackContext.success(result)
            }

            override fun onError(errorMessage: String) {
                callbackContext.error(errorMessage)
            }
        })
    }
}
