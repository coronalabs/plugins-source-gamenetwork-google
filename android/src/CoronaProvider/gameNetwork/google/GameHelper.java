/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CoronaProvider.gameNetwork.google;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;

public class GameHelper implements GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener {
    
    public interface GameHelperListener {
        /** 
         * Called when sign-in fails. As a result, a "Sign-In" button can be shown to the user;
         * when that button is clicked, call @link{#beginUserInitiatedSignIn}. Note that not
         * all calls to this method signify errors; it may be a result of the fact that automatic
         * sign-in could not proceed because user interaction was required (consent dialogs).
         * So implementations of this method should NOT display an error message.
         */
        void onSignInFailed();

        /** Called when sign-in succeeds. */
        void onSignInSucceeded();
    }
    
    Activity mActivity = null;
    Context mContext = null;
    String mScopes[];

    // Request code we use when invoking other Activities to complete the sign-in flow.
    static int RC_RESOLVE = 9001;

    // Request code when invoking Activities whose result we don't care about.
    final static int RC_UNUSED = 9002;

    // Client objects we manage. If a given client is not enabled, it is null.
    GamesClient mGamesClient = null;
    
    // What clients we wrap (OR-able values, so we can use as flags too)
    public final static int CLIENT_NONE = 0x00;
    public final static int CLIENT_GAMES = 0x01;
    public final static int CLIENT_ALL = CLIENT_GAMES;
    
    // What clients were requested? (bit flags)
    int mRequestedClients = CLIENT_NONE;
    
    // What clients are currently connected? (bit flags)
    int mConnectedClients = CLIENT_NONE;
    
    // What client are we currently connecting?
    int mClientCurrentlyConnecting = CLIENT_NONE;
    
    // A progress dialog we show when we are trying to sign-in the user
    ProgressDialog mProgressDialog = null;

    // Whether to automatically try to sign in on onStart(). This will be true
    // unless the user specifically signed out.
    boolean mAutoSignIn = true;

    // Whether user has specifically requested that the sign-in process begin
    // (if false, we're in the automatic sign-in attempt that try once the Activity
    // is started -- if true, then the user has already clicked a "Sign-In" button or
    // something similar)
    boolean mUserInitiatedSignIn = false;

    // The connection result we got from our last attempt to sign-in
    ConnectionResult mConnectionResult = null;

    // Whether we launched the sign-in dialog flow and therefore are expecting an
    // onActivityResult with the result of that.
    boolean mExpectingActivityResult = false;

    // Are we signed in?
    boolean mSignedIn = false;
    
    // Print debug logs?
    boolean mDebugLog = true;
    String mDebugTag = "BaseGameActivity";

    // Sign-in message
    final static String SIGN_IN_MESSAGE = "Signing in with Google...";
    final static String SIGN_OUT_MESSAGE = "Signing out...";
    final static String SIGN_IN_ERROR_MESSAGE = "Could not sign in. Please try again.";
    
    // Listener
    GameHelperListener mListener = null;
    
    public GameHelper(Activity activity) {
        mActivity = activity;
        mContext = activity;
    }

    public void setup(GameHelperListener listener) {
        setup(listener, CLIENT_GAMES);
    }
    
    private void addToScope(StringBuilder scopeStringBuilder, String scope) {
        if ( scopeStringBuilder.length() == 0 ) {
            scopeStringBuilder.append("oauth2:");
        } else {
            scopeStringBuilder.append(" ");
        }
        scopeStringBuilder.append(scope);
    }
    
    public void setup(GameHelperListener listener, int clientsToUse) {
        mListener = listener;
        mRequestedClients = clientsToUse;
        
        Vector<String> scopesVector = new Vector<String>();
        if (0 != (clientsToUse & CLIENT_GAMES)) {
            scopesVector.add(Scopes.GAMES);
        }

        mScopes = new String[scopesVector.size()];
        scopesVector.copyInto(mScopes);
        
        final android.view.View aView = new android.view.View(mContext);
        if (0 != (clientsToUse & CLIENT_GAMES)) {
            //We need to add this view otherwise the activity might not find a view to show the achievements popup which then causes
            //game services to crash
            com.ansca.corona.CoronaEnvironment.getCoronaActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    android.widget.AbsoluteLayout fAbsoluteLayout = new android.widget.AbsoluteLayout(com.ansca.corona.CoronaEnvironment.getCoronaActivity());
                    com.ansca.corona.CoronaEnvironment.getCoronaActivity().getOverlayView().addView(fAbsoluteLayout);
                    android.widget.RelativeLayout fAdViewGroup = new android.widget.RelativeLayout(com.ansca.corona.CoronaEnvironment.getCoronaActivity());
                    fAdViewGroup.addView(aView);
                    fAbsoluteLayout.addView(fAdViewGroup);        
                }
            });

            debugLog("onCreate: creating GamesClient");
            mGamesClient = new GamesClient.Builder(mContext, this, this)
                .setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                .setScopes(mScopes)
                .setViewForPopups(aView)
                .create();
        }
    }
    
    public GamesClient getGamesClient() {
        return mGamesClient;
    }

    void startConnections() {
        mConnectedClients = CLIENT_NONE;
        connectNextClient();
    }
    
    void connectNextClient() {
        // do we already have all the clients we need?
        int pendingClients = mRequestedClients & ~mConnectedClients;
        if (pendingClients == 0) {
            succeedSignIn();
            return;
        }
        
        try {
            // which client should be the next one to connect?
            if (mGamesClient != null && (0 != (pendingClients & CLIENT_GAMES))) {
                debugLog("Connecting GamesClient.");
                mClientCurrentlyConnecting = CLIENT_GAMES;
            }
            else {
                throw new AssertionError("Not all clients connected, yet no one is next. R="  
                        + mRequestedClients + ", C="  + mConnectedClients);
            }
            
            connectCurrentClient();
        }
        catch (Exception ex) {
            Log.e(mDebugTag, "*** EXCEPTION while attempting to connect. Details follow.");
            ex.printStackTrace();
            giveUp();
        }
    }
    
    
    void connectCurrentClient() {
        switch (mClientCurrentlyConnecting) {
            case CLIENT_GAMES:
                mGamesClient.connect();
                break;
        }
    }
    
    public void reconnectClients(int whatClients) {
        
        if ((whatClients & CLIENT_GAMES) != 0 && mGamesClient != null 
                && mGamesClient.isConnected()) {
            mConnectedClients &= ~CLIENT_GAMES;
            mGamesClient.reconnect();
        }
    }

    /** Called when we successfully obtain a connection to a client. */
    @Override
    public void onConnected(Bundle connectionHint) {
        debugLog("onConnected: connected! client=" + mClientCurrentlyConnecting);
        
        // Mark the current client as connected
        mConnectedClients |= mClientCurrentlyConnecting;
        
        // connect the next client in line, if any.
        connectNextClient();
    }
    
    void succeedSignIn() {
        mSignedIn = true;
        mAutoSignIn = true;
        if (mListener != null) mListener.onSignInSucceeded();                
    }
    
    /** Handles a connection failure reported by a client */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // save connection result for later reference
        mConnectionResult = result;
        debugLog("onConnectionFailed: result " + result.getErrorCode());
        
        if (!mUserInitiatedSignIn) {
            // if the user didn't initiate the sign-in, we don't try to resolve
            // the connection problem automatically -- instead, we fail and wait for
            // the user to want to sign in.
            debugLog("onConnectionFailed: since user didn't initiate sign-in, failing now.");
            mConnectionResult = result;
            if (mListener != null) mListener.onSignInFailed();
            return;
        }

        debugLog("onConnectionFailed: since user initiated sign-in, trying to resolve problem.");

        // Resolve the connection result. This usually means showing a dialog or
        // starting an Activity that will allow the user to give the appropriate consents
        // so that sign-in can be successful.
        resolveConnectionResult();
    }

    /**
     * Attempts to resolve a connection failure. This will usually involve starting a UI
     * flow that lets the user give the appropriate consents necessary for sign-in to work.
     */
    void resolveConnectionResult() {
        // Try to resolve the problem
        debugLog("resolveConnectionResult: trying to resolve result: " + mConnectionResult);
        if (mConnectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            debugLog("result has resolution. Starting it.");
            try {
                // launch appropriate UI flow (which might, for example, be the sign-in flow)
                mExpectingActivityResult = true;
                mConnectionResult.startResolutionForResult(mActivity, RC_RESOLVE);
            } catch (SendIntentException e) {
                // Try connecting again
                debugLog("SendIntentException.");
                connectCurrentClient();
            }
        }
        else {
            // It's not a problem what we can solve, so give up and show an error.
            debugLog("resolveConnectionResult: result has no resolution. Giving up.");
            giveUp();
        }
    }

    /**
     * Give up on signing in due to an error. Shows the appropriate error message to the user,
     * using a standard error dialog as appropriate to the cause of the error. That dialog
     * will indicate to the user how the problem can be solved (for example, re-enable
     * Google Play Services, upgrade to a new version, etc).
     */
    void giveUp() {
        debugLog("giveUp: giving up on connection. " +
                ((mConnectionResult == null) ? "(no connection result)" :
                    ("Status code: "  + mConnectionResult.getErrorCode())));

        Dialog errorDialog = null;
        if (mConnectionResult != null) {
            // get error dialog for that specific problem
            errorDialog = getErrorDialog(mConnectionResult.getErrorCode());
        }
        else {
            // make a default error dialog
            errorDialog = makeSignInErrorDialog(SIGN_IN_ERROR_MESSAGE);
        }

        mAutoSignIn = false;
        // errorDialog.show();
        if (mListener != null) mListener.onSignInFailed();
    }

    /**
     * Handle activity result. Call this method from your Activity's onActivityResult
     * callback. If the activity result pertains to the sign-in process,
     * processes it appropriately.
     */
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_RESOLVE) {
            // We're coming back from an activity that was launched to resolve a connection
            // problem. For example, the sign-in UI.
            mExpectingActivityResult = false;
            debugLog("onActivityResult, req " + requestCode + " response " + responseCode);
            if (responseCode == Activity.RESULT_OK) {
                // Ready to try to connect again.
                debugLog("responseCode == RESULT_OK. So connecting.");
                connectCurrentClient();
            }
            else {
                // Whatever the problem we were trying to solve, it was not solved.
                // So give up and show an error message.
                debugLog("responseCode != RESULT_OK, so not reconnecting.");
                giveUp();
            }
        }
    }

    /** 
     * Starts a user-initiated sign-in flow. This should be called when the user 
     * clicks on a "Sign In" button.
     */
    public void beginUserInitiatedSignIn() {
        if (mSignedIn) return; // nothing to do

        // reset the flag to sign in automatically on onStart() -- now a
        // wanted behavior
        mAutoSignIn = true;

        // Is Google Play services available?
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        debugLog("isGooglePlayServicesAvailable returned " + result);
        if (result != ConnectionResult.SUCCESS) {
            // Nope.
            debugLog("Google Play services not available. Show error dialog.");
            Dialog errorDialog = getErrorDialog(result);
            if (mListener != null) mListener.onSignInFailed();
            errorDialog.show();
            return;
        }

        mUserInitiatedSignIn = true;
        if (mConnectionResult != null) {
            // We have a pending connection result from a previous failure, so start with that.
            debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.");
            resolveConnectionResult();
        }
        else {
            // We don't have a pending connection result, so start anew.
            debugLog("beginUserInitiatedSignIn: starting new sign-in flow.");
            startConnections();
        }
    }
    
    @Override
    public void onDisconnected() {
        debugLog("onDisconnected.");
        mConnectionResult = null;
        mAutoSignIn = false;
        mSignedIn = false;
        mConnectedClients = CLIENT_NONE;
        if (mListener != null) mListener.onSignInFailed();
    }
    
    /** Call this method from your Activity's onStart(). */
    public void onStart(Activity act) {
        mActivity = act;
        mContext = act;
    }

    /** Returns an error dialog that's appropriate for the given error code. */
    Dialog getErrorDialog(int errorCode) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // try to get a standard Google Play Services error dialog
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                mConnectionResult.getErrorCode(), mActivity, RC_UNUSED, null);
            if (errorDialog != null) return errorDialog;
        }

        // No standard dialog is available, so construct our own dialog.
        String userMessage, logMessage;

        switch (errorCode) {
            case ConnectionResult.DEVELOPER_ERROR:
                userMessage = "Application configuration problem.";
                logMessage = "DEVELOPER_ERROR: Check package name, signing certificate, app ID.";
                break;
            case ConnectionResult.INTERNAL_ERROR:
                userMessage = "Internal error. Please try again later.";
                logMessage = "INTERNAL_ERROR";
                break;
            case ConnectionResult.INVALID_ACCOUNT:
                userMessage = "Invalid account. Try using a different account.";
                logMessage = "INVALID_ACCOUNT";
                break;
            case ConnectionResult.LICENSE_CHECK_FAILED:
                userMessage = "Cannot verify application license.";
                logMessage = "LICENSE_CHECK_FAILED: app license could not be verified.";
                break;
            case ConnectionResult.NETWORK_ERROR:
                userMessage = "There was a network problem while connecting. Please check that you are online and try again later.";
                logMessage = "NETWORK_ERROR: check connection, try again.";
                break;
            case ConnectionResult.RESOLUTION_REQUIRED:
                // this should not normally happen, since we would have resolved it.
                userMessage = "There was a sign-in issue that could not be resolved.";
                logMessage = "RESOLUTION_REQUIRED: Result resolution is required, but was not performed.";
                break;
            case ConnectionResult.SERVICE_DISABLED:
                userMessage = "Cannot sign-in. Verify that Google Play services are enabled and try again.";
                logMessage = "SERVICE_DISABLED: Google Play services may have been manually disabled.";
                break;
            case ConnectionResult.SERVICE_INVALID:
                userMessage = "Cannot sign-in. Verify that Google Play services are correctly set up and try again.";
                logMessage = "SERVICE_INVALID. Google Play services may need to be reinstalled on device.";
                break;
            case ConnectionResult.SERVICE_MISSING:
                userMessage = "Cannot sign-in. Verify that Google Play services are correctly installed and try again.";
                logMessage = "SERVICE_MISSING. Google Play services may not be installed on the device.";
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                userMessage = "A newer version of Google Play services is required. Please update and try again.";
                logMessage = "SERVICE_VERSION_UPDATE_REQUIRED. Must install newer version of Google Play services.";
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                // should not happen -- normally resolvable
                userMessage = "There was an issue with sign-in.";
                logMessage = "SIGN_IN_REQUIRED";
                break;
            case ConnectionResult.SUCCESS:
                // this should DEFINITELY not happen
                userMessage = "Sign-in successful.";
                logMessage = "SUCCESS";
                break;
            default:
                userMessage = "An unexpected error occurred during sign-in. Try again later.";
                logMessage = "Unexpected error: " + mConnectionResult.getErrorCode();
        }

        debugLog("ERROR CODE " + errorCode + ": message=" + userMessage + "; details=" + logMessage);
        return makeSignInErrorDialog(userMessage);
    }

    
    Dialog makeSignInErrorDialog(String message) {
        return (new AlertDialog.Builder(mContext)).setTitle("Sign-in error")
                .setMessage(message)
                .setNeutralButton("OK", null)
                .create();
    }

    public void enableDebugLog(boolean enabled, String tag) {
        mDebugLog = enabled;
        mDebugTag = tag;
    }

    void debugLog(String message) {
        if (mDebugLog) Log.d(mDebugTag, message);
    }

    /** Sign out and disconnect from the APIs. */
    public void signOut() {
        mConnectionResult = null;
        mAutoSignIn = false;
        mSignedIn = false;
        
        if (mGamesClient != null) {
            try {
                mGamesClient.signOut();
            } catch (java.lang.SecurityException ex) {

            }
            mGamesClient.disconnect();
            mGamesClient = null;
        }
    }

    /**
     * Returns the current requested scopes.  This is not valid until setup() has been called.
     * @return the requested scopes, including the oauth2: prefix
     */
    public String getScopes() {
        StringBuilder scopeStringBuilder = new StringBuilder();
        int clientsToUse = mRequestedClients;
        if (0 != (clientsToUse & CLIENT_GAMES)) {
            addToScope(scopeStringBuilder, Scopes.GAMES);
        }
        return scopeStringBuilder.toString();
    }

    public void setRequestCode(int requestCode) {
        RC_RESOLVE = requestCode;
    }

    public int getRequestCode() {
        return RC_RESOLVE;
    }
}
