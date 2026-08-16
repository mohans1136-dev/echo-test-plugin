package com.outsystems.plugin.echo

import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Echo Cordova Plugin written in Kotlin for OutSystems Mobile App Builder (MAB).
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
     * Synchronously echoes the input string message back to JavaScript.
     */
    private fun echo(args: JSONArray, callbackContext: CallbackContext) {
        try {
            val message = if (!args.isNull(0)) args.getString(0) else ""
            if (message.isNotEmpty()) {
                callbackContext.success(message)
            } else {
                callbackContext.error("Expected a non-empty string argument.")
            }
        } catch (e: Exception) {
            callbackContext.error("Kotlin Echo Error: ${e.localizedMessage}")
        }
    }

    /**
     * Asynchronously echoes message back on Kotlin thread pool with simulated delay.
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
                callbackContext.error("Kotlin Async Error: ${e.localizedMessage}")
            }
        }
    }
}
