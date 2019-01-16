package com.trybe.trybe.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.trybe.trybe.R;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gopi on 13-Apr-16.
 */
public class GcmIntentService extends IntentService {

    private static final String TAG = GcmIntentService.class.getSimpleName();
    private UserSessionManager session;

    public GcmIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        session = UserSessionManager.getInstance();
        registerGCM();
    }

    /**
     * Registering with GCM and obtaining the gcm registration id
     */
    private void registerGCM() {
        String token = null;

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.d(TAG, "GCM Registration Token: " + token);

            if (session.getGcmToken() == null)
                sendRegistrationToServer(token);
            else {
                if (!session.getGcmToken().equals(token))
                    sendRegistrationToServer(token);
                else {
                    // gcm id unchanged
                    Log.d(TAG, "GCM Registration Token unchanged");
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }

    // sending the registration id to our server
    private void sendRegistrationToServer(final String token) {
        // TODO Send the registration token to our server
        Log.d(TAG, "Sending GCM Registration Token to server");

        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", session.getUserProfile().user_def.UD_ID);
        params.put("GCM_ID", token);
        Utils.sendVolleyJsonRequest(this, Config.Server.PROFILE_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "response: " + response);
                session.setGcmToken(token);
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "error: " + error);
            }
        });
    }
}
