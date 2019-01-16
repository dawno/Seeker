package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobCatDTO;
import com.trybe.trybe.dto.JobLocDTO;
import com.trybe.trybe.dto.JobsPostedDTO;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class JobPostActivity extends AppCompatActivity {

    @Bind(R.id.etCompany)
    EditText etCompany;
    @Bind(R.id.etPosition)
    EditText etPosition;
    @Bind(R.id.tvLocation)
    TextView location;
    @Bind(R.id.tvJobCategory)
    TextView jobCategory;
    @Bind(R.id.salary_spinner1)
    NumberPicker salary_picker_min;
    @Bind(R.id.salary_spinner2)
    NumberPicker salary_picker_max;
    @Bind(R.id.etRemarks)
    EditText etRemarks;
    @Bind(R.id.done)
    ImageButton done;
    @Bind(R.id.navMenu)
    ImageButton menuBtn;
    @Bind(R.id.chatBtn)
    ImageButton chatBtn;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @Bind(R.id.nav_view)
    NavigationView mNavView;

    private String selectedJobCatId, selectedJobLocation;
    private ActionBarDrawerToggle mDrawerToggle;
    private UserSessionManager session;
    private LoginDTO user;
    private MaterialDialog progressDialog;
    private Gson gson;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_post);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        gson = Utils.getGsonInstance();
        session = UserSessionManager.getInstance();
        user = session.getUserProfile();

        progressDialog = new MaterialDialog.Builder(this)
                .title("Posting job")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .build();

        setupDrawer();
        setupNavigationHeader(user.user_def.UD_NAME, user.user_def.UD_IMG);

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
                startActivity(new Intent(JobPostActivity.this, ChatActivity.class));
            }
        });

        selectedJobLocation = JobPref2Activity.NO_PREF_STRING;
        location.setText(JobPref2Activity.NO_PREF_STRING);
        List<JobLocDTO.JobLoc> jobLocs = session.getJobLocations().RESULTS;
        final ArrayList<String> jobLocArray = new ArrayList<>();
        jobLocArray.add(JobPref2Activity.NO_PREF_STRING);
        if (jobLocs != null) {
            for (int j = 0; j < jobLocs.size(); j++) {
                jobLocArray.add(jobLocs.get(j).NAME);
            }
        }
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(JobPostActivity.this)
                        .title("Select Job Category")
                        .items(jobLocArray)
                        .itemsCallbackSingleChoice(jobLocArray.indexOf(selectedJobLocation), new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                selectedJobLocation = text.toString();
                                location.setText(text);
                                return true;
                            }
                        })
                        .positiveText("SELECT")
                        .show();
            }
        });

        jobCategory.setText("Select Job Category");
        final List<JobCatDTO.JobCat> jobCats = session.getJobCategories().RESULTS;
        final ArrayList<String> jobCatArray = new ArrayList<>();
        if (jobCats != null) {
            for (int i = 0; i < jobCats.size(); i++) {
                JobCatDTO.JobCat item = jobCats.get(i);
                jobCatArray.add(item.JC_CAT + ", " + item.JC_SUB_CAT);
            }
        }
        jobCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(JobPostActivity.this)
                        .title("Select Job Category")
                        .items(jobCatArray)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which >= 0) {
                                    selectedJobCatId = jobCats.get(which).JC_ID + "";
                                    jobCategory.setText(text);
                                }
                                return true;
                            }
                        })
                        .positiveText("SELECT")
                        .show();
            }
        });

        salary_picker_min.setMinValue(0);
        salary_picker_min.setMaxValue(99);
        salary_picker_min.setValue(10);
        salary_picker_min.setWrapSelectorWheel(false);

        salary_picker_max.setMinValue(1);
        salary_picker_max.setMaxValue(100);
        salary_picker_max.setValue(18);
        salary_picker_max.setWrapSelectorWheel(false);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (salary_picker_min.getValue() >= salary_picker_max.getValue()) {
                    Toast.makeText(JobPostActivity.this, "Max salary cannot be less than min salary", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedJobCatId == null || selectedJobCatId.trim().equals("")) {
                    Toast.makeText(JobPostActivity.this, "Job Category cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                String newComp = etCompany.getText().toString().trim();
                String newPos = etPosition.getText().toString().trim();
                if (newComp.equals("") || newPos.equals("")) {
                    Toast.makeText(JobPostActivity.this, "Job Company or Position cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();
                JobsPostedDTO.JobPost newJob = new JobsPostedDTO.JobPost();
                newJob.JR_COMPANY = newComp;
                newJob.JR_POSITION = newPos;
                newJob.JR_LOCATION = selectedJobLocation;
                newJob.JR_JC_ID = selectedJobCatId;
                newJob.JR_SALARY_MIN = salary_picker_min.getValue();
                newJob.JR_SALARY_MAX = salary_picker_max.getValue();
                newJob.JR_REMARK = etRemarks.getText().toString().trim();

                Map<String, String> params = new HashMap<String, String>();
                params.put("ACT", "1");
                params.put("UD_ID", user.user_def.UD_ID);
                params.put("JOB_REFER", gson.toJson(newJob));
                Utils.sendVolleyJsonRequest(JobPostActivity.this, Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("JR_POST", response);
                        progressDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("JR_POST", error);
                        progressDialog.dismiss();
                        Toast.makeText(JobPostActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Utils.checkUnreadMessages(chatBtn);
        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(chatBtn);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(JobPostActivity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
        Utils.checkUnreadMessages(chatBtn);
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(JobPostActivity.this, mRegistrationBroadcastReceiver);
        super.onPause();
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}