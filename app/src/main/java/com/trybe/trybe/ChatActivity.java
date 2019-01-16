package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.dto.MatchesDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;
import com.trybe.trybe.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavView;
    private ImageButton menuBtn, chatBtn;

    private UserSessionManager session;
    private Gson gson;
    private MaterialDialog progressDialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        session = UserSessionManager.getInstance();
        if (!session.isLoggedIn()) {
            startActivity(new Intent(ChatActivity.this, LoginActivity.class));
            finish();
            return;
        }
        LoginDTO user = session.getUserProfile();
        gson = Utils.getGsonInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        progressDialog = new MaterialDialog.Builder(this)
                .title("Loading your matches")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawer();
        String userImageUrl = user.user_def.UD_IMG;
        setupNavigationHeader(user.user_def.UD_NAME, userImageUrl);

        viewPager = (ViewPager) findViewById(R.id.chatViewpager);
        tabLayout = (TabLayout) findViewById(R.id.chatTabs);

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

        fetchMatches(user.user_def.UD_ID);

        Utils.checkUnreadMessages(chatBtn, R.drawable.chaticon_selected);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    Message msg = (Message) intent.getSerializableExtra("data");
                    Intent resultIntent = new Intent(ChatActivity.this, ChatMessageActivity.class);
                    resultIntent.putExtra(ChatMessageActivity.ARG_USER, msg.UD_ID);
                    resultIntent.putExtra(ChatMessageActivity.ARG_USER_TYPE, ChatMessageActivity.TYPE_REFER);

                    NotificationUtils notificationUtils = new NotificationUtils(context);
                    notificationUtils.showNotificationMessage(intent.getStringExtra("title"), msg.MESSAGE, msg.CREATE_DT, resultIntent);

                    Utils.notifyChat(chatBtn);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(ChatActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
        Utils.checkUnreadMessages(chatBtn, R.drawable.chaticon_selected);
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(ChatActivity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void fetchMatches(String userId) {
        Log.d("test", "fetching matches");
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", userId);
        Utils.sendVolleyJsonRequest(ChatActivity.this, Config.Server.FETCH_MATCHES_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test", "response: " + response);
                progressDialog.dismiss();

                MatchesDTO matches = null;
                try {
                    matches = gson.fromJson(response, MatchesDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (matches == null) {
                    Log.d("test", "server error");
                    Toast.makeText(ChatActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("test", "seek: " + matches.SEEK.size() + ", refer: " + matches.REFER.size());
                    setupViewPager(matches);
                }
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Log.d("test", "Error: " + error);
                Toast.makeText(ChatActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupViewPager(MatchesDTO matches) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        // sort refer matches by position
        Collections.sort(matches.REFER, new Comparator<MatchesDTO.Match>() {
            @Override
            public int compare(MatchesDTO.Match lhs, MatchesDTO.Match rhs) {
                try {
                    return lhs.JR_POSITION.compareTo(rhs.JR_POSITION);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 1;
                }
            }
        });

        adapter.addFragment(ChatFragment1.newInstance(matches.SEEK), "Matches for Referring you");
        adapter.addFragment(ChatFragment2.newInstance(matches.REFER), "Matches to be Referred");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}