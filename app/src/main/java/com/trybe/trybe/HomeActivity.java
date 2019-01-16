package com.trybe.trybe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.gcm.GcmIntentService;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String NOTIFICATION_FLAG = "app_notification_home_flag";
    public static final String APP_NOTIFICATION = "app_notification_home_key";
    public static final String APP_NOTIFICATION_TITLE = "app_notification_home_key_title";
    private UserSessionManager session;
    private LoginDTO user;
    private Integer LOG_ID;
    private boolean retryLocation = true;
    private boolean sentAppOpenCallback = false;
    private boolean isForceDisabled = false;

    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_LOCATION_PERM = 3;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FrameLayout usageScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        session = UserSessionManager.getInstance();
        if (!session.isLoggedIn()) {
            Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
            loginIntent.putExtras(getIntent());
            startActivity(loginIntent);
            finish();
            return;
        }
        user = session.getUserProfile();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        String notifFlag = null, notifData = null, notifTitle = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            notifFlag = extras.getString(NOTIFICATION_FLAG);
            notifTitle = extras.getString(APP_NOTIFICATION_TITLE);
            notifData = extras.getString(APP_NOTIFICATION);
        }
        if (notifFlag == null) {

        } else if (notifFlag.equals(Config.PUSH_TYPE_NOTIF)) {
            new MaterialDialog.Builder(HomeActivity.this)
                    .title(notifTitle)
                    .content(Html.fromHtml(notifData))
                    .positiveText("OK")
                    .show();
        } else if (notifFlag.equals(Config.PUSH_TYPE_UPDATE)) {
            new MaterialDialog.Builder(HomeActivity.this)
                    .title(notifTitle)
                    .content(notifData)
                    .positiveText("Update")
                    .negativeText("Later")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            final String appPackageName = getPackageName();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        usageScreen = (FrameLayout) findViewById(R.id.home_usage_screen);
        usageScreen.setVisibility(View.GONE);

        HomeFragment frag = new HomeFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.home_main_container, frag, HomeFragment.class.getName()).commit();
        HomeUsageOverlay frag1 = new HomeUsageOverlay();
        getSupportFragmentManager().beginTransaction().add(R.id.home_usage_screen, frag1, HomeUsageOverlay.class.getName()).commit();

        // showUsageScreen();
        if (session.showHomeUsageScreen()) {
            session.showHomeUsageScreen(false);
            if (session.isLinkedInLogin())
                downloadLinkedInPic();
            showUsageScreen();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Start IntentService to register this application with GCM.
        if (Utils.checkPlayServices(HomeActivity.this)) {
            Intent intent = new Intent(this, GcmIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isForceDisabled)
            forceDisableApp();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        Branch branch = Branch.getInstance();
        branch.setIdentity(user.user_def.UD_ID);
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    Log.i("BranchConfigTest", "deep link data: " + referringParams.toString());
                }
            }
        }, this.getIntent().getData(), this);
        super.onStart();
    }


    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void downloadLinkedInPic() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(user.user_def.UD_IMG, new FileAsyncHttpResponseHandler(this) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                Log.d("test", "image downloaded successfully");
                if (file != null) {
                    File newImageFile = new File(Utils.getImagesFolder(), "IMG_LI.jpg");
                    Utils.copyFile(file, newImageFile);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.d("test", "error downloading file: " + throwable.getLocalizedMessage());
            }
        });
    }

    public void showUsageScreen() {
        usageScreen.setVisibility(View.VISIBLE);
    }

    public void removeUsageScreen() {
        usageScreen.setVisibility(View.GONE);
    }

    public void openSeek(View v) {
        startActivity(new Intent(HomeActivity.this, SeekActivity.class));
    }

    public void openRefer(View v) {
        startActivity(new Intent(HomeActivity.this, ReferActivity.class));
    }

    @Override
    public void onBackPressed() {
        if (usageScreen.getVisibility() == View.VISIBLE)
            removeUsageScreen();
        else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        saveAppCloseEvent();
        super.onDestroy();
    }

    private void getLocation() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(
                        PERMISSIONS_LOCATION,
                        REQUEST_LOCATION_PERM
                );
            } else {
                fetchLocation();
            }
        } else {
            fetchLocation();
        }
    }

    private void fetchLocation() {
        if (sentAppOpenCallback)
            return;

        try {
            Location lastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();
                Log.d("GoogleLocation", "Latitude: " + latitude + ", Longitude: " + longitude);
                saveAppOpenEvent(latitude + "," + longitude);
            } else {
                Log.d("GoogleLocation", "location null");
                if (retryLocation) {
                    Log.d("GoogleLocation", "retrying");
                    retryLocation = false;
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fetchLocation();
                        }
                    }, 1000);
                } else {
                    // tried 2 times, both null
                    saveAppOpenEvent(null);
                }
            }
        } catch (SecurityException e) {
            Log.d("GoogleLocation", "Security exception: permission not provided");
            e.printStackTrace();
            saveAppOpenEvent(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERM: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("test", "yay! perm granted");
                    fetchLocation();
                } else {
                    Log.d("test", "perm denied");
                    // TODO
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("GoogleLocation", "connected");
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("GoogleLocation", "Connection Suspended");
        saveAppOpenEvent(null);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("GoogleLocation", "Connection failed");
        saveAppOpenEvent(null);
    }

    private void forceDisableApp() {
        new MaterialDialog.Builder(HomeActivity.this)
                .title("Update Required")
                .cancelable(false)
                .content("You need to update the app as the latest features are not supported on this version")
                .positiveText("Update")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .show();
    }

    private void saveAppOpenEvent(String location) {
        if (location == null || location.trim().equals(""))
            location = "NotProvided";
        if (sentAppOpenCallback)
            return;

        Log.d("APP OPEN", "sending app open call with location: " + location);
        sentAppOpenCallback = true;
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", user.user_def.UD_ID);
        params.put("LG_LOCATION", location);
        params.put("LG_DEVICE_ID", Utils.getDeviceId(getApplicationContext()));
        params.put("APP_VERSION", Utils.getVersionName());
        Utils.sendVolleyJsonRequest(this, false, Config.Server.SERVER_APP_OPEN_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("OPEN APP", response);
                try {
                    JSONObject json = new JSONObject(response);
                    LOG_ID = (Integer) json.get("LOG_ID");
                    if (json.has("UPDATE")) {
                        String updateParam = json.getString("UPDATE");
                        if (updateParam.equals("force")) {
                            isForceDisabled = true;
                            forceDisableApp();
                        }
                    }
                } catch (Exception e) {
                    Log.d("APP OPEN", "invalid json from server");
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                Log.d("OPEN APP", "Error: " + error);
            }
        });
    }

    private void saveAppCloseEvent() {
        if (LOG_ID == null)
            return;

        Map<String, String> params = new HashMap<String, String>();
        params.put("LOG_ID", LOG_ID.toString());
        Utils.sendVolleyJsonRequest(this, false, Config.Server.SERVER_APP_CLOSE_URL, params, null);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
