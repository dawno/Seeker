package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.trybe.trybe.adapter.JobReferPagerAdapter;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobSeekerDTO;
import com.trybe.trybe.dto.UserDef;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReferFragment extends Fragment {

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private JobReferPagerAdapter jobReferAdapter;
    private NavigationView mNavView;
    private ViewPager viewPager;
    private TextView tvCurrentJob, referJobNum;
    private LinearLayout emptyFrame;
    private RelativeLayout referContainer, referCircleLayout;
    private ImageButton lBtn, rBtn, prevBtn, nextBtn, menuBtn, chatBtn;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private UserSessionManager session;
    private Gson gson;
    private List<JobSeekerDTO.JobSeeker> mSeekerList;


    public ReferFragment() {
    }

    public static ReferFragment newInstance(List<JobSeekerDTO.JobSeeker> seekerList, UserDef user) {
        ReferFragment fragment = new ReferFragment();

        Gson gson = Utils.getGsonInstance();
        String seekers = gson.toJson(seekerList);
        String user_def = gson.toJson(user);

        Bundle args = new Bundle();
        args.putString("seekers", seekers);
        args.putString("user", user_def);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_refer, container, false);
        gson = Utils.getGsonInstance();
        session = UserSessionManager.getInstance();
        Bundle args = getArguments();
        String seekersJson = args.getString("seekers");
        String user_def = args.getString("user");
        UserDef user = gson.fromJson(user_def, UserDef.class);
        Type listType = new TypeToken<ArrayList<JobSeekerDTO.JobSeeker>>() {
        }.getType();
        mSeekerList = gson.fromJson(seekersJson, listType);

        mDrawer = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
        mNavView = (NavigationView) rootView.findViewById(R.id.nav_view);
        setupDrawer();
        setupNavigationHeader(user.UD_NAME, user.UD_IMG);

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

        tvCurrentJob = (TextView) rootView.findViewById(R.id.tvCurrentJob);
        referCircleLayout = (RelativeLayout) rootView.findViewById(R.id.referCirleLayout);
        referJobNum = (TextView) rootView.findViewById(R.id.referJobNum);
        referJobNum.setText(mSeekerList.size() + "");
        referCircleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ReferActivity) getActivity()).showOverlay();
            }
        });

        emptyFrame = (LinearLayout) rootView.findViewById(R.id.emptyFrame);
        referContainer = (RelativeLayout) rootView.findViewById(R.id.referContainer);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        emptyFrame.setVisibility(View.GONE);
        referContainer.setVisibility(View.VISIBLE);

        lBtn = (ImageButton) rootView.findViewById(R.id.swipeLeft);
        rBtn = (ImageButton) rootView.findViewById(R.id.swipeRight);
        final Toast mSwipeToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        lBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO save swipe choice on local storage /send to server
                int position = viewPager.getCurrentItem();
                JobSeekerDTO.JobSeeker swipeUser = jobReferAdapter.getAdapterArrayList().get(position);
                Log.d("test", createPostJson(swipeUser.SW_ID, false));
                Utils.sendVolleyJsonRequest(getActivity(), Config.Server.SERVER_REQUESTS_URL, createPostParams(swipeUser.SW_ID, false), null);

                mSeekerList.remove(swipeUser);
                referJobNum.setText(mSeekerList.size() + "");
                ReferJobOverlay frag = (ReferJobOverlay) getActivity().getSupportFragmentManager().findFragmentByTag(ReferJobOverlay.class.getName());
                frag.refreshJobList(mSeekerList);

                jobReferAdapter.getAdapterArrayList().remove(position);
                jobReferAdapter.notifyDataSetChanged();
                viewPager.setAdapter(jobReferAdapter);
                if (position >= jobReferAdapter.getCount())
                    position = jobReferAdapter.getCount() - 1;
                viewPager.setCurrentItem(position);
                // TODO change to SnackBar with Undo & dismiss option
                mSwipeToast.setText("Rejected Candidate");
                mSwipeToast.show();
                handleSingleOrEmptyAdapter();
            }
        });
        rBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO save swipe choice on local storage /send to server
                int position = viewPager.getCurrentItem();
                JobSeekerDTO.JobSeeker swipeUser = jobReferAdapter.getAdapterArrayList().get(position);
                Log.d("test", createPostJson(swipeUser.SW_ID, true));
                Utils.sendVolleyJsonRequest(getActivity(), Config.Server.SERVER_REQUESTS_URL, createPostParams(swipeUser.SW_ID, true), null);

                mSeekerList.remove(swipeUser);
                referJobNum.setText(mSeekerList.size() + "");
                ReferJobOverlay frag = (ReferJobOverlay) getActivity().getSupportFragmentManager().findFragmentByTag(ReferJobOverlay.class.getName());
                frag.refreshJobList(mSeekerList);

                jobReferAdapter.getAdapterArrayList().remove(position);
                jobReferAdapter.notifyDataSetChanged();
                viewPager.setAdapter(jobReferAdapter);
                if (position >= jobReferAdapter.getCount())
                    position = jobReferAdapter.getCount() - 1;
                viewPager.setCurrentItem(position);
                // TODO change to SnackBar with Undo & dismiss option
                mSwipeToast.setText("Accepted Candidate");
                mSwipeToast.show();
                handleSingleOrEmptyAdapter();
            }
        });
        prevBtn = (ImageButton) rootView.findViewById(R.id.prev);
        nextBtn = (ImageButton) rootView.findViewById(R.id.next);
        prevBtn.setVisibility(View.INVISIBLE);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        setupJobSeekersPager(mSeekerList);

        Utils.checkUnreadMessages(chatBtn);
        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(chatBtn);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(getActivity(), mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
        Utils.checkUnreadMessages(chatBtn);
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(getActivity(), mRegistrationBroadcastReceiver);
        super.onPause();
    }

    public void refreshSeekers(String jobName) {
        List<JobSeekerDTO.JobSeeker> newList = new ArrayList<JobSeekerDTO.JobSeeker>();
        for (JobSeekerDTO.JobSeeker seeker : mSeekerList) {
            String s = seeker.JR_POSITION + ", " + seeker.JR_COMPANY;
            if (s.equals(jobName))
                newList.add(seeker);
        }
        tvCurrentJob.setText(jobName);
        refreshSeekers(newList);
    }

    public void refreshSeekers(List<JobSeekerDTO.JobSeeker> jobSeekerList) {
        emptyFrame.setVisibility(View.GONE);
        referContainer.setVisibility(View.VISIBLE);
        setupJobSeekersPager(jobSeekerList);
    }

    private String createPostJson(int swId, boolean accept) {
        JsonArray swipes = new JsonArray();
        JsonObject swipe = new JsonObject();
        swipe.addProperty("SW_ID", Integer.toString(swId));
        swipe.addProperty("SW_REFERER_ACCEPT", (accept ? "1" : "0"));
        swipes.add(swipe);
        return gson.toJson(swipes);
    }

    private Map<String, String> createPostParams(int swId, boolean accept) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ACT", "2");
        params.put("UD_ID", session.getUserProfile().user_def.UD_ID);
        params.put("swipes", createPostJson(swId, accept));
        return params;
    }

    private void setupJobSeekersPager(final List<JobSeekerDTO.JobSeeker> jobSeekerList) {
        jobReferAdapter = new JobReferPagerAdapter(getActivity(), jobSeekerList);
        viewPager.setAdapter(jobReferAdapter);
        Log.d("test", jobReferAdapter.getCount() + "");
        if (jobSeekerList.size() <= 1)
            handleSingleOrEmptyAdapter();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getActivity().invalidateOptionsMenu();
                // Log.d("test", "adapter size: " + jobReferAdapter.getCount());
                if (jobReferAdapter.getCount() == 0) {
                    Log.d("test", "empty adapter");
                } else if (jobReferAdapter.getCount() == 1) {
                    prevBtn.setVisibility(View.INVISIBLE);
                    nextBtn.setVisibility(View.INVISIBLE);
                } else if (viewPager.getCurrentItem() == 0) {
                    prevBtn.setVisibility(View.INVISIBLE);
                    nextBtn.setVisibility(View.VISIBLE);
                } else if (viewPager.getCurrentItem() == jobReferAdapter.getCount() - 1) {
                    nextBtn.setVisibility(View.INVISIBLE);
                    prevBtn.setVisibility(View.VISIBLE);
                } else {
                    prevBtn.setVisibility(View.VISIBLE);
                    nextBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void showNoDataScreen() {
        emptyFrame.setVisibility(View.VISIBLE);
        referContainer.setVisibility(View.GONE);
    }

    private void handleSingleOrEmptyAdapter() {
        if (jobReferAdapter.getCount() == 0) {
            Log.d("test", "empty adapter");
            showNoDataScreen();
        } else if (jobReferAdapter.getCount() == 1) {
            prevBtn.setVisibility(View.INVISIBLE);
            nextBtn.setVisibility(View.INVISIBLE);
        }
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

    //@Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}