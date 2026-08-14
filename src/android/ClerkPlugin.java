package com.outsystems.plugin.clerk;

import android.content.Intent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Pure Java Cordova Plugin for Clerk Authentication & Shared Session Synchronization.
 * Fully compatible with OutSystems Mobile App Builder (MAB).
 */
public class ClerkPlugin extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Pass incoming deep link intent (clerk://...) to Clerk SDK
        ClerkBridge.handleIntent(intent);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("initialize".equals(action)) {
            this.initializeClerk(args, callbackContext);
            return true;
        } else if ("signIn".equals(action)) {
            this.signIn(callbackContext);
            return true;
        } else if ("signOut".equals(action)) {
            this.signOut(callbackContext);
            return true;
        } else if ("getToken".equals(action)) {
            this.getToken(callbackContext);
            return true;
        } else if ("getSessionState".equals(action)) {
            this.getSessionState(callbackContext);
            return true;
        }
        return false;
    }

    private void initializeClerk(JSONArray args, final CallbackContext callbackContext) {
        try {
            String publishableKey = args.optString(0, "");
            if (publishableKey == null || publishableKey.trim().length() == 0) {
                callbackContext.error("Missing Clerk Publishable Key.");
                return;
            }

            ClerkBridge.initialize(
                cordova.getActivity().getApplicationContext(),
                publishableKey,
                new ClerkBridge.BridgeCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        callbackContext.success(result);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callbackContext.error(errorMessage);
                    }
                }
            );
        } catch (Exception e) {
            callbackContext.error("Initialize Plugin Error: " + e.getMessage());
        }
    }

    private void signIn(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ClerkBridge.startHostedAuth(
                    cordova.getActivity(),
                    new ClerkBridge.BridgeCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callbackContext.error(errorMessage);
                        }
                    }
                );
            }
        });
    }

    private void signOut(final CallbackContext callbackContext) {
        ClerkBridge.signOut(new ClerkBridge.BridgeCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String errorMessage) {
                callbackContext.error(errorMessage);
            }
        });
    }

    private void getToken(final CallbackContext callbackContext) {
        ClerkBridge.getSessionToken(new ClerkBridge.TokenCallback() {
            @Override
            public void onSuccess(String jwtToken) {
                try {
                    JSONObject res = new JSONObject();
                    res.put("token", jwtToken);
                    callbackContext.success(res);
                } catch (Exception e) {
                    callbackContext.success(jwtToken);
                }
            }

            @Override
            public void onError(String errorMessage) {
                callbackContext.error(errorMessage);
            }
        });
    }

    private void getSessionState(final CallbackContext callbackContext) {
        ClerkBridge.getSessionState(new ClerkBridge.BridgeCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String errorMessage) {
                callbackContext.error(errorMessage);
            }
        });
    }
}
