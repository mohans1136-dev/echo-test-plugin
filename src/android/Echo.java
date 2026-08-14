package com.outsystems.plugin.echo;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Native Android Echo Plugin for OutSystems Mobile App Builder (MAB).
 * Written in Java for 100% guaranteed compilation across all OutSystems MAB versions.
 */
public class Echo extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("echo".equals(action)) {
            this.echo(args, callbackContext);
            return true;
        } else if ("echoAsync".equals(action)) {
            this.echoAsync(args, callbackContext);
            return true;
        }
        return false;
    }

    private void echo(JSONArray args, CallbackContext callbackContext) {
        try {
            String message = args.optString(0, "");
            if (message != null && message.length() > 0) {
                callbackContext.success(message);
            } else {
                callbackContext.error("Expected a non-empty string argument.");
            }
        } catch (Exception e) {
            callbackContext.error("Native Echo Error: " + e.getMessage());
        }
    }

    private void echoAsync(JSONArray args, CallbackContext callbackContext) {
        final String message = args.optString(0, "");
        final long delayMs = args.optLong(1, 1000);

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (delayMs > 0) {
                        Thread.sleep(delayMs);
                    }
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", message);
                    jsonResponse.put("timestamp", System.currentTimeMillis());
                    jsonResponse.put("executedBy", "Native ThreadPool");
                    callbackContext.success(jsonResponse);
                } catch (Exception e) {
                    callbackContext.error("Native Async Error: " + e.getMessage());
                }
            }
        });
    }
}
