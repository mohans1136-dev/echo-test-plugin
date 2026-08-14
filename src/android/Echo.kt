package com.outsystems.plugin.echo

import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Echo Cordova Plugin written in Kotlin.
 * Fully compatible with Cordova Android 9+ and OutSystems Mobile App Builder (MAB).
 */
class Echo : CordovaPlugin() {

    @Throws(JSONException::class)
    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        return when (action) {
            "echo" -> {
                echo(args, callbackContext)
                true
            }
            "echoAsync" -> {
                echoAsync(args, callbackContext)
                true
            }
            else -> false
        }
    }

    /**
     * Echoes the string passed in the first parameter.
     */
    private fun echo(args: JSONArray, callbackContext: CallbackContext) {
        try {
            val message = if (!args.isNull(0)) args.getString(0) else null

            if (!message.isNullOrEmpty()) {
                val result = PluginResult(PluginResult.Status.OK, message)
                callbackContext.sendPluginResult(result)
            } else {
                val result = PluginResult(
                    PluginResult.Status.ERROR,
                    "Expected a non-empty string argument."
                )
                callbackContext.sendPluginResult(result)
            }
        } catch (e: Exception) {
            callbackContext.error("Failed to execute echo: ${e.localizedMessage}")
        }
    }

    /**
     * Demonstrates asynchronous task execution on Kotlin thread pool.
     */
    private fun echoAsync(args: JSONArray, callbackContext: CallbackContext) {
        val message = if (!args.isNull(0)) args.getString(0) else ""
        val delayMs = if (args.length() > 1) args.getLong(1) else 1000L

        cordova.threadPool.execute {
            try {
                if (delayMs > 0) {
                    Thread.sleep(delayMs)
                }

                val jsonResponse = JSONObject().apply {
                    put("status", "success")
                    put("message", message)
                    put("timestamp", System.currentTimeMillis())
                    put("executedBy", "Kotlin ThreadPool")
                }

                callbackContext.success(jsonResponse)
            } catch (e: Exception) {
                callbackContext.error("Error executing echoAsync: ${e.localizedMessage}")
            }
        }
    }
}
