package com.trybe.trybe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobSeekerDTO;
import com.trybe.trybe.dto.JobsPostedDTO;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ReferActivity extends AppCompatActivity {

    private UserSessionManager session;
    private Gson gson;
    private LoginDTO user;
    private MaterialDialog progressDialog;
    private List<JobSeekerDTO.JobSeeker> mSeekerList;
    private List<JobsPostedDTO.JobPost> mJobsList;
    private AtomicInteger responseSeekers, responseJobs;

    private FrameLayout overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer);

        session = UserSessionManager.getInstance();
        if (!session.isLoggedIn()) {
            startActivity(new Intent(ReferActivity.this, LoginActivity.class));
            finish();
            return;
        }
        gson = Utils.getGsonInstance();
        user = session.getUserProfile();

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

        overlay = (FrameLayout) findViewById(R.id.refer_overlay);

        responseSeekers = new AtomicInteger(0);
        responseJobs = new AtomicInteger(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSeekers(user.user_def.UD_ID);
        fetchJobsPosted(user.user_def.UD_ID);
    }

    private void fetchSeekers(String userId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", userId);
        Utils.sendVolleyJsonRequest(ReferActivity.this, Config.Server.FETCH_SEEKERS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                responseSeekers.set(1);
                Log.d("test", "response: " + response);

                JobSeekerDTO seekers = null;
                try {
                    seekers = gson.fromJson(response, JobSeekerDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (seekers == null) {
                    Log.d("test", "server error");
                    handleError();
                    Toast.makeText(ReferActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("test", "seekers: " + seekers.RESULTS.size());
                    mSeekerList = seekers.RESULTS;
                    if (responseJobs.get() == 1) {
                        handleSuccess();
                    }
                }
            }

            @Override
            public void onError(String error) {
                responseSeekers.set(2);
                Log.d("test", error);

                handleError();
                Toast.makeText(ReferActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchJobsPosted(String userId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ACT", "4");
        params.put("data", "job_refer");
        params.put("UD_ID", userId);
        Utils.sendVolleyJsonRequest(ReferActivity.this, Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                responseJobs.set(1);
                Log.d("test", response);

                JobsPostedDTO jobs = null;
                try {
                    jobs = gson.fromJson(response, JobsPostedDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (jobs == null) {
                    Log.d("test", "server error");
                    handleError();
                    Toast.makeText(ReferActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("test", "jobs: " + jobs.RESULTS.size());
                    mJobsList = jobs.RESULTS;
                    if (responseSeekers.get() == 1) {
                        handleSuccess();
                    }
                }
            }

            @Override
            public void onError(String error) {
                responseJobs.set(2);
                Log.d("test", error);
                handleError();
                Toast.makeText(ReferActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void handleSuccess() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        ReferFragment frag = ReferFragment.newInstance(mSeekerList, user.user_def);
        getSupportFragmentManager().beginTransaction().add(R.id.refer_frag_container, frag, ReferFragment.class.getName()).commit();
        ReferJobOverlay frag1 = ReferJobOverlay.newInstance(mJobsList, mSeekerList, user.user_def);
        getSupportFragmentManager().beginTransaction().add(R.id.refer_overlay, frag1, ReferJobOverlay.class.getName()).commit();
    }

    private void handleError() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        // TODO show fragment with error message and refresh
    }

    public void showOverlay() {
        overlay.setVisibility(View.VISIBLE);
    }

    public void removeOverlay() {
        overlay.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (overlay.getVisibility() == View.VISIBLE)
            super.onBackPressed();
        else
            showOverlay();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
