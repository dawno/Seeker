package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobSeekerDTO;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.dto.MatchesDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private RelativeLayout seekDroplet, referDroplet, jobPost;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavView;
    private TextView userNameTv, seekNum, referNum;
    private ImageView profilePic;
    private ImageButton menuBtn, chatBtn;

    private UserSessionManager session;
    private LoginDTO user;
    private Gson gson;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean madeInitNetworkCalls = false;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_home, container, false);

        session = UserSessionManager.getInstance();
        user = session.getUserProfile();
        gson = Utils.getGsonInstance();

        mDrawer = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
        mNavView = (NavigationView) rootView.findViewById(R.id.nav_view);
        setupDrawer();
        String userImageUrl = user.user_def.UD_IMG;
        setupNavigationHeader(user.user_def.UD_NAME, userImageUrl);

        userNameTv = (TextView) rootView.findViewById(R.id.userName);
        profilePic = (CircleImageView) rootView.findViewById(R.id.profilePic);

        menuBtn = (ImageButton) rootView.findViewById(R.id.navMenu);
        chatBtn = (ImageButton) rootView.findViewById(R.id.chatBtn);
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
                startActivity(new Intent(getActivity(), ChatActivity.class));
            }
        });

        seekNum = (TextView) rootView.findViewById(R.id.seekDropletText);
        referNum = (TextView) rootView.findViewById(R.id.referDropletText);
        seekDroplet = (RelativeLayout) rootView.findViewById(R.id.seekDroplet);
        referDroplet = (RelativeLayout) rootView.findViewById(R.id.referDroplet);
        jobPost = (RelativeLayout) rootView.findViewById(R.id.jobPost);

        seekDroplet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ChatActivity.class));
            }
        });
        referDroplet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ReferActivity.class));
            }
        });
        jobPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), JobPostActivity.class));
            }
        });

        Utils.checkUnreadMessages(chatBtn);
        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(chatBtn);

        return rootView;
    }

    private void makeOtherBackgroundInitCalls() {
        if (madeInitNetworkCalls)
            return;
        madeInitNetworkCalls = true;
        refreshCurrentUser();
        Utils.fetchJobCategories(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(getActivity(), mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
        Utils.checkUnreadMessages(chatBtn);
        updateCounts();
        user = session.getUserProfile();
        userNameTv.setText("Hi " + user.user_def.UD_NAME);
        Utils.loadImageByPicasso(getActivity(), profilePic, user.user_def.UD_IMG);
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(getActivity(), mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void updateCounts() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", user.user_def.UD_ID);
        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.FETCH_SEEKERS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test refer count", "response: " + response);

                JobSeekerDTO seekers = null;
                try {
                    seekers = gson.fromJson(response, JobSeekerDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (seekers == null) {
                    Log.d("test", "server error: seekers null");
                    referNum.setText("0");
                } else {
                    int sn = seekers.RESULTS.size();
                    Log.d("test refer count", sn + "");
                    referNum.setText(sn + "");
                }
            }

            @Override
            public void onError(String error) {
                Log.d("test", "Error: " + error);
            }
        });

        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.FETCH_MATCHES_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test seeker count", "response: " + response);

                MatchesDTO matches = null;
                try {
                    matches = gson.fromJson(response, MatchesDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (matches == null) {
                    Log.d("test", "server error: matches null");
                    seekNum.setText("0");
                } else {
                    int rn = matches.SEEK.size();
                    Log.d("test seeker count", rn + "");
                    seekNum.setText(rn + "");
                }

                makeOtherBackgroundInitCalls();
            }

            @Override
            public void onError(String error) {
                Log.d("test", "Error: " + error);

                makeOtherBackgroundInitCalls();
            }
        });
    }

    // Refresh user info from server once at beginning & update name & pic
    private void refreshCurrentUser() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", user.user_def.UD_ID);
        params.put("ACT", "4");
        params.put("DATA", "user_details");
        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.SERVER_REQUESTS_URL, params, 0, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("user refresh", "response: " + response);

                LoginDTO updatedUser = null;
                try {
                    updatedUser = gson.fromJson(response, LoginDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("user refresh", "json err: " + e.getLocalizedMessage());
                }
                if (updatedUser == null) {
                    Log.d("user refresh", "server error");
                } else {
                    // successfully got user, merge with session user
                    user.user_def = updatedUser.user_def;
                    user.data = updatedUser.data;
                    session.setUserProfile(user);
                    userNameTv.setText("Hi " + user.user_def.UD_NAME);
                    Utils.loadImageByPicasso(getActivity(), profilePic, user.user_def.UD_IMG);
                }
            }

            @Override
            public void onError(String error) {
                Log.d("user refresh", "error: " + error);
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle("Navigation!");
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mActivityTitle);
                getActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        //mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavView.setNavigationItemSelectedListener(new Utils.NavigationItemSelectedListenerClass(getActivity(), session, mDrawer));
    }

    public void setupNavigationHeader(String name, String imageUrl) {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.nav_header, null);
        mNavView.addHeaderView(header);

        TextView userName = (TextView) header.findViewById(R.id.navUserName);
        ImageView userPic = (CircleImageView) header.findViewById(R.id.navUserPic);
        userName.setText(name);
        Utils.loadImageByPicasso(getActivity(), userPic, imageUrl);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}