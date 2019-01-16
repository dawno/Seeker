package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.trybe.trybe.app.BaseApplication;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChangePassActivity extends AppCompatActivity {

    private static final String TAG = ChangePassActivity.class.getSimpleName();

    @Bind(R.id.oldPass)
    EditText oldPass;
    @Bind(R.id.newPass)
    EditText newPass;
    @Bind(R.id.confirmPass)
    EditText confirmPass;
    @Bind(R.id.done)
    ImageButton submit;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldP = oldPass.getText().toString();
                String newP = newPass.getText().toString();
                String confirmP = confirmPass.getText().toString();

                if (!newP.equals(confirmP)) {
                    Toast.makeText(ChangePassActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("UD_ID", UserSessionManager.getInstance().getUserProfile().user_def.UD_ID);
                    params.put("UD_PASSWORD", oldP);
                    params.put("PASSWORD_NEW", newP);
                    Utils.sendVolleyJsonRequest(ChangePassActivity.this, false, Config.Server.SERVER_PASS_RESET, params, new Utils.VolleyRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "response: " + response);

                            try {
                                JSONObject json = new JSONObject(response);
                                if (json.getBoolean("STATUS")) {
                                    // success
                                    Toast.makeText(BaseApplication.getInstance(), "Password Change Successful", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChangePassActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "json err: " + e.getLocalizedMessage());
                                Toast.makeText(ChangePassActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.d(TAG, "error: " + error);
                            Toast.makeText(ChangePassActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(ChangePassActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(ChangePassActivity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
