package com.trybe.trybe;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.Utils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileSetupActivity extends AppCompatActivity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frag_container, new ProfileSetupFragment1(), ProfileSetupFragment1.class.getName());
        ft.commit();

        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(ProfileSetupActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(ProfileSetupActivity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}