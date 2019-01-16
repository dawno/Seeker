package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.trybe.trybe.adapter.JobSeekViewAdapter;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobReferralDTO;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SeekActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout emptyFrame;
    private TextView emptyFrameTv;
    private SwipeFlingAdapterView flingContainer;
    private JobSeekViewAdapter jobSeekViewAdapter;
    private NavigationView mNavView;
    private ImageButton lBtn, rBtn, menuBtn, chatBtn;

    private UserSessionManager session;
    private LoginDTO user;
    private Gson gson;
    private MaterialDialog progressDialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String swipesEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seek);

        session = UserSessionManager.getInstance();
        if (!session.isLoggedIn()) {
            startActivity(new Intent(SeekActivity.this, LoginActivity.class));
            finish();
            return;
        }
        user = session.getUserProfile();
        gson = Utils.getGsonInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        progressDialog = new MaterialDialog.Builder(this)
                .title("Fetching jobs")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawer();
        setupNavigationHeader(user.user_def.UD_NAME, user.user_def.UD_IMG);

        menuBtn = (ImageButton) findViewById(R.id.navMenu);
        chatBtn = (ImageButton) findViewById(R.id.chatBtn);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                    mDrawer.closeDrawer(GravityCompat.START);
                } else {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            }
        });
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SeekActivity.this, ChatActivity.class));
            }
        });

        lBtn = (ImageButton) findViewById(R.id.swipeLeft);
        rBtn = (ImageButton) findViewById(R.id.swipeRight);
        lBtn.setVisibility(View.VISIBLE);
        rBtn.setVisibility(View.VISIBLE);

        emptyFrame = (LinearLayout) findViewById(R.id.emptyFrame);
        emptyFrameTv = (TextView) findViewById(R.id.emptyFrameTv);
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.swipeFrame);
        emptyFrame.setVisibility(View.GONE);
        flingContainer.setVisibility(View.VISIBLE);

        fetchJobReferrals(user.user_def.UD_ID);

        lBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectLeft();
            }
        });
        rBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectRight();
            }
        });

        Utils.checkUnreadMessages(chatBtn);
        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(chatBtn);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(SeekActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
        Utils.checkUnreadMessages(chatBtn);
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(SeekActivity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void fetchJobReferrals(String userId) {
        Log.d("test", "fetching job data");
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", userId);
        Utils.sendVolleyJsonRequest(SeekActivity.this, Config.Server.FETCH_JOBS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test", "response: " + response);
                progressDialog.dismiss();

                JobReferralDTO referrals = null;
                try {
                    referrals = gson.fromJson(response, JobReferralDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (referrals == null) {
                    Log.d("test", "server error");
                    Toast.makeText(SeekActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    if (referrals.Results.size() == 0) {
                        Log.d("test", "no referrals");
                        // TODO show no data screen
                        showNoDataScreen(referrals.BG_MSG);
                    } else {
                        Log.d("test", "referrals: " + referrals.Results.size());
                        swipesEmpty = referrals.BG_MSG;
                        setupJobSwipes(referrals.Results);
                    }
                }
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                // TODO handle error
            }
        });
    }

    private void showNoDataScreen(String msg) {
        emptyFrame.setVisibility(View.VISIBLE);
        if (msg != null && !msg.trim().equals(""))
            emptyFrameTv.setText(msg);
        flingContainer.setVisibility(View.GONE);
        lBtn.setVisibility(View.GONE);
        rBtn.setVisibility(View.GONE);
    }

    private void setupJobSwipes(final List<JobReferralDTO.JobReferral> jobArrayList) {
        if (jobArrayList == null || jobArrayList.size() == 0) {
            showNoDataScreen(swipesEmpty);
            return;
        }

        final Toast mSwipeToast = Toast.makeText(SeekActivity.this, "", Toast.LENGTH_SHORT);
        jobSeekViewAdapter = new JobSeekViewAdapter(this, R.layout.item_job_seek, jobArrayList);
        flingContainer.setAdapter(jobSeekViewAdapter);
        jobSeekViewAdapter.notifyDataSetChanged();
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                Log.d("LIST", "Size: " + jobArrayList.size() + "");
                if (jobArrayList.size() > 1) {
                    jobArrayList.remove(0);
                    jobSeekViewAdapter.notifyDataSetChanged();
                } else if (jobArrayList.size() == 1) {
                    jobArrayList.remove(0);
                    jobSeekViewAdapter.notifyDataSetChanged();
                    showNoDataScreen(swipesEmpty);
                } else {
                    showNoDataScreen(swipesEmpty);
                }
            }

            @Override
            public void onLeftCardExit(Object o) {
                JobReferralDTO.JobReferral swipeUser = (JobReferralDTO.JobReferral) o;
                Log.d("swipe left", createPostJson(swipeUser.JR_ID, swipeUser.UD_ID, false));
                Utils.sendVolleyJsonRequest(SeekActivity.this, Config.Server.SERVER_REQUESTS_URL, createPostParams(swipeUser.JR_ID, swipeUser.UD_ID, false), null);
                if (jobArrayList.size() >= 0) {
                    mSwipeToast.setText("Not interested");
                    mSwipeToast.show();
                }
            }

            @Override
            public void onRightCardExit(Object o) {
                JobReferralDTO.JobReferral swipeUser = (JobReferralDTO.JobReferral) o;
                Log.d("swipe right", createPostJson(swipeUser.JR_ID, swipeUser.UD_ID, true));
                Utils.sendVolleyJsonRequest(SeekActivity.this, Config.Server.SERVER_REQUESTS_URL, createPostParams(swipeUser.JR_ID, swipeUser.UD_ID, true), null);
                if (jobArrayList.size() >= 0) {
                    mSwipeToast.setText("Interested!");
                    mSwipeToast.show();
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int i) {
                // Ask for more data here
                //    JobReferralDTO.JobReferral t = new JobReferralDTO.JobReferral();
                //    t.UD_IMG = "http://www.inshad.com/forum/upload/userimages/37342/Tux.png";
                //    t.DW_POSITION = "Software Engineer";
                //    t.JR_COMPANY = "Samsung R&D";
                //    jobArrayList.add(t);
                //    jobSeekViewAdapter.notifyDataSetChanged();
                Log.d("LIST", "about to empty " + i);
                Log.d("LIST", "Size: " + jobArrayList.size() + "");
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });

        // Optionally add an OnItemClickListener
        //        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
        //            @Override
        //            public void onItemClicked(int itemPosition, Object dataObject) {
        //                Toast.makeText(SeekActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
        //            }
        //        });
    }

    private String createPostJson(int jrId, String referUserId, boolean accept) {
        JsonArray swipes = new JsonArray();
        JsonObject swipe = new JsonObject();
        swipe.addProperty("SW_JR_ID", Integer.toString(jrId));
        swipe.addProperty("SW_REFER_ID", referUserId);
        swipe.addProperty("SW_SEEKER_ID", user.user_def.UD_ID);
        swipe.addProperty("SW_SEEKER_ACCEPT", (accept ? "1" : "0"));
        swipes.add(swipe);
        return gson.toJson(swipes);
    }

    private Map<String, String> createPostParams(int jrId, String referUserId, boolean accept) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ACT", "1");
        params.put("UD_ID", user.user_def.UD_ID);
        params.put("swipes", createPostJson(jrId, referUserId, accept));
        return params;
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        //mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavView.setNavigationItemSelectedListener(new Utils.NavigationItemSelectedListenerClass(this, session, mDrawer));
    }

    public void setupNavigationHeader(String name, String imageUrl) {
        View header = LayoutInflater.from(this).inflate(R.layout.nav_header, null);
        mNavView.addHeaderView(header);

        TextView userName = (TextView) header.findViewById(R.id.navUserName);
        ImageView userPic = (CircleImageView) header.findViewById(R.id.navUserPic);
        userName.setText(name);
        Utils.loadImageByPicasso(getApplicationContext(), userPic, imageUrl);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}