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
import com.trybe.trybe.dto.ChatMessageDTO;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.dto.MatchesDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;
import com.trybe.trybe.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatMessageActivity extends AppCompatActivity {

    public static final String TAG = ChatMessageActivity.class.getSimpleName();
    public static final String ARG_USER = "chatUser";
    public static final String ARG_USER_MATCH = "chatUserMatch";
    public static final String ARG_USER_TYPE = "chatUserType";
    public static final String TYPE_SEEK = "seek";
    public static final String TYPE_REFER = "refer";

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavView;
    private ImageView msgUserPic;
    private TextView msgUserName;
    private ImageButton menuBtn, chatBtn;

    private String userType;
    private String chatUserId;
    private MatchesDTO.Match matchObj;
    private UserSessionManager session;
    private Gson gson;
    private LoginDTO currentUser, otherUser;
    private List<Message> messageHistory;
    private MaterialDialog progressDialog;
    private AtomicInteger responseFetchUser, responseFetchChat;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        session = UserSessionManager.getInstance();
        if (!session.isLoggedIn()) {
            startActivity(new Intent(ChatMessageActivity.this, LoginActivity.class));
            finish();
            return;
        }
        currentUser = session.getUserProfile();
        gson = Utils.getGsonInstance();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        progressDialog = new MaterialDialog.Builder(this)
                .title("Fetching chat history")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatUserId = extras.getString(ARG_USER);
            if (chatUserId != null)
                chatUserId = chatUserId.replaceAll("\"", "");
            userType = extras.getString(ARG_USER_TYPE);
            String matchJson = extras.getString(ARG_USER_MATCH);
            matchObj = gson.fromJson(matchJson, MatchesDTO.Match.class);

        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawer();
        setupNavigationHeader(currentUser.user_def.UD_NAME, currentUser.user_def.UD_IMG);

        msgUserPic = (CircleImageView) findViewById(R.id.chatMsgPic);
        msgUserName = (TextView) findViewById(R.id.chatMsgName);

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

        responseFetchUser = new AtomicInteger(0);
        responseFetchChat = new AtomicInteger(0);
        fetchChatHistory(chatUserId);
        fetchUserDetails(chatUserId);

        Utils.markMessageAsRead(chatUserId, chatBtn);
        Utils.checkUnreadMessages(chatBtn, R.drawable.chaticon_selected);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received

                    if (viewPager.getCurrentItem() == 0) {
                        // handled by ChatMessageFragment.java
                        return;
                    } else {
                        Message msg = (Message) intent.getSerializableExtra("data");
                        Intent resultIntent = new Intent(getApplicationContext(), ChatMessageActivity.class);
                        resultIntent.putExtra(ChatMessageActivity.ARG_USER, msg.UD_ID);
                        resultIntent.putExtra(ChatMessageActivity.ARG_USER_TYPE, ChatMessageActivity.TYPE_REFER);

                        NotificationUtils notificationUtils = new NotificationUtils(context);
                        notificationUtils.showNotificationMessage(intent.getStringExtra("title"), msg.MESSAGE, msg.CREATE_DT, resultIntent);
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(ChatMessageActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
        Utils.markMessageAsRead(chatUserId, chatBtn);
        Utils.checkUnreadMessages(chatBtn, R.drawable.chaticon_selected);
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(ChatMessageActivity.this, mRegistrationBroadcastReceiver);
        Utils.markMessageAsRead(chatUserId, chatBtn);
        super.onPause();
    }


    private void fetchChatHistory(String chatUserId) {
        Log.d("test", "fetching chat history");
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID_S", currentUser.user_def.UD_ID);
        params.put("UD_ID_R", chatUserId);
        Utils.sendVolleyJsonRequest(ChatMessageActivity.this, Config.Server.CHAT_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                responseFetchChat.set(1);
                Log.d("test", "response: " + response);

                ChatMessageDTO chats = null;
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getString("ERROR") != null) {
                        Log.d(TAG, "error: " + json.getString("ERROR"));
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "NO ERROR Param in json " + e.getLocalizedMessage());
                }
                try {
                    chats = gson.fromJson(response, ChatMessageDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d(TAG, "json error: " + e.getLocalizedMessage());
                }

                if (chats == null) {
                    Log.d("test", "server error");
                    handleError();
                    Toast.makeText(ChatMessageActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    messageHistory = chats.DATA;
                    if (responseFetchUser.get() == 1)
                        handleSuccess();
                }
            }

            @Override
            public void onError(String error) {
                responseFetchChat.set(2);
                Log.d("test", "error: " + error);
                handleError();
                Toast.makeText(ChatMessageActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetails(String userId) {
        Log.d("test", "fetching user details");
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", userId);
        params.put("ACT", "4");
        params.put("DATA", "user_details");
        Utils.sendVolleyJsonRequest(ChatMessageActivity.this, Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                responseFetchUser.set(1);
                Log.d("test", "response: " + response);

                otherUser = null;
                try {
                    otherUser = gson.fromJson(response, LoginDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (otherUser == null) {
                    Log.d("test", "server error");
                    handleError();
                    Toast.makeText(ChatMessageActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    msgUserName.setText(otherUser.user_def.UD_NAME);
                    String userPic = otherUser.user_def.UD_IMG;
                    Utils.loadImageByPicasso(getApplicationContext(), msgUserPic, userPic);
                    if (responseFetchChat.get() == 1)
                        handleSuccess();
                }
            }

            @Override
            public void onError(String error) {
                responseFetchUser.set(2);
                Log.d("test", "error: " + error);
                handleError();
                Toast.makeText(ChatMessageActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccess() {
        progressDialog.dismiss();
        setupViewPager();
    }

    private void handleError() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        // TODO show fragment with error message and refresh
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ChatMessageFragment.newInstance(currentUser, otherUser, messageHistory), "Chat");
        adapter.addFragment(ChatMessageAboutFragment.newInstance(otherUser, matchObj, userType), "About");
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