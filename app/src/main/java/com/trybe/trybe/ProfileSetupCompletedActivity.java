package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.trybe.trybe.dto.JobPref;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.Utils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileSetupCompletedActivity extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_setup_completed);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ImageButton next = (ImageButton) findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });

        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(ProfileSetupCompletedActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(ProfileSetupCompletedActivity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}