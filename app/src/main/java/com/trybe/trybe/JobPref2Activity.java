package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobLocDTO;
import com.trybe.trybe.dto.JobPref;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class JobPref2Activity extends AppCompatActivity {

    private NumberPicker salary_picker_min, salary_picker_max;
    private TextView noPrefTv;
    private LinearLayout jlCol1, jlCol2;

    private ArrayList<String> locations;
    private UserSessionManager session;
    private LoginDTO user;
    private Gson gson;
    private MaterialDialog progressDialog;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public static final String NO_PREF_STRING = "No Preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_pref2);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        gson = Utils.getGsonInstance();
        session = UserSessionManager.getInstance();
        user = session.getUserProfile();
        progressDialog = new MaterialDialog.Builder(this)
                .title("Saving Data")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .build();

        noPrefTv = (TextView) findViewById(R.id.tvNoPrefLoc);

        salary_picker_min = (NumberPicker) findViewById(R.id.salary_spinner1);
        salary_picker_max = (NumberPicker) findViewById(R.id.salary_spinner2);
        salary_picker_min.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        salary_picker_max.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        salary_picker_min.setMinValue(0);
        salary_picker_min.setMaxValue(99);
        salary_picker_min.setValue(10);
        salary_picker_min.setWrapSelectorWheel(false);

        salary_picker_max.setMinValue(1);
        salary_picker_max.setMaxValue(100);
        salary_picker_max.setValue(18);
        salary_picker_max.setWrapSelectorWheel(false);

        jlCol1 = (LinearLayout) findViewById(R.id.jlCol1);
        jlCol2 = (LinearLayout) findViewById(R.id.jlCol2);
        locations = new ArrayList<>();
        JobLocDTO jobLocs = session.getJobLocations();
        if (jobLocs != null)
            initializeJobLocs(jobLocs);

        if (user.job_seek != null)
            initUserJobPrefs(user.job_seek);

        ImageButton next = (ImageButton) findViewById(R.id.nextBtn);
        ImageButton back = (ImageButton)findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), JobPref1Activity.class));
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (salary_picker_min.getValue() >= salary_picker_max.getValue()) {
                    Toast.makeText(JobPref2Activity.this, "Max salary cannot be less than min salary", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();

                String locChoice = "";
                if (locations.size() == 0)
                    locChoice = NO_PREF_STRING;
                else {
                    for (String l : locations)
                        locChoice = locChoice + l + ",";
                    locChoice = locChoice.substring(0, locChoice.length() - 1);
                }
                Log.d("pref data", salary_picker_min.getValue() + " - " + salary_picker_max.getValue());
                Log.d("pref data", locChoice);

                final JobPref jpNew = new JobPref();
                jpNew.JS_JC_IDS = user.job_seek.JS_JC_IDS;
                jpNew.JS_LOCATION = locChoice;
                jpNew.JS_SALARY_MIN = salary_picker_min.getValue();
                jpNew.JS_SALARY_MAX = salary_picker_max.getValue();

                Map<String, String> params = new HashMap<String, String>();
                params.put("ACT", "1_2");
                params.put("TABLE", "job_seek");
                params.put("UD_ID", user.user_def.UD_ID);
                params.put("D", gson.toJson(jpNew));
                Utils.sendVolleyJsonRequest(getApplicationContext(), Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("test", "response: " + response);
                        progressDialog.dismiss();
                        user.PREFERENCE_STATUS = true;
                        jpNew.JS_ID = user.job_seek.JS_ID;
                        user.job_seek = jpNew;
                        session.setUserProfile(user);
                        startActivity(new Intent(getApplicationContext(), ProfileSetupCompletedActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("test", "error: " + error);
                        progressDialog.dismiss();
                        Toast.makeText(JobPref2Activity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(JobPref2Activity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(JobPref2Activity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void initializeJobLocs(JobLocDTO jobLocs) {
        if (jobLocs == null || jobLocs.RESULTS.size() == 0)
            return;

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < jobLocs.RESULTS.size(); i++) {
            JobLocDTO.JobLoc jl = jobLocs.RESULTS.get(i);

            View layoutJl = inflater.inflate(R.layout.layout_job_loc, null, false);
            TextView tvJl = (TextView) ((ViewGroup) layoutJl).getChildAt(0);

            tvJl.setText(jl.NAME);
            tvJl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    locationClick(v);
                }
            });

            if (i % 2 == 0) {
                jlCol2.addView(layoutJl, -1, params);
            } else {
                jlCol1.addView(layoutJl, -1, params);
            }
        }
    }

    private void initUserJobPrefs(JobPref job_seek) {
        if (job_seek.JS_SALARY_MIN >= 0)
            salary_picker_min.setValue(job_seek.JS_SALARY_MIN);
        if (job_seek.JS_SALARY_MAX > 0)
            salary_picker_max.setValue(job_seek.JS_SALARY_MAX);

        ViewGroup v = (ViewGroup) findViewById(R.id.locationContainer);
        if (job_seek.JS_LOCATION != null) {
            String[] initLocs = job_seek.JS_LOCATION.split(",");
            initUserLocations(v, Arrays.asList(initLocs));
        }
    }

    private void initUserLocations(ViewGroup v, List<String> initLocs) {
        for (int i = 0; i < v.getChildCount(); i++) {
            Object child = v.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (initLocs.contains(tv.getText().toString())) {
                    if (tv.getText().toString().equals(NO_PREF_STRING)) {
                        noPrefLocationClick(tv);
                    } else {
                        locationClick(tv);
                    }
                }
            } else if (child instanceof ViewGroup) {
                initUserLocations((ViewGroup) child, initLocs);
            }
        }
    }

    public void noPrefLocationClick(View v) {
        TextView tv = (TextView) v;

        resetAll((ViewGroup) findViewById(R.id.locationContainer));
        tv.setTextColor(getResources().getColor(R.color.textDarkGreen));
        tv.setBackgroundResource(R.drawable.round_corner_jobview_yellow);

        locations = new ArrayList<>();
    }

    private void resetAll(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            Object child = v.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                tv.setTextColor(getResources().getColor(R.color.textGray));
                tv.setBackgroundResource(R.drawable.round_corner_jobview);
            } else if (child instanceof ViewGroup) {
                resetAll((ViewGroup) child);
            }
        }
    }

    public void locationClick(View v) {
        noPrefTv.setTextColor(getResources().getColor(R.color.textGray));
        noPrefTv.setBackgroundResource(R.drawable.round_corner_jobview);

        TextView tv = (TextView) v;
        String loc = tv.getText().toString();

        if (locations.contains(loc)) {
            // undo click
            tv.setTextColor(getResources().getColor(R.color.textGray));
            tv.setBackgroundResource(R.drawable.round_corner_jobview);
            locations.remove(loc);
            return;
        } else {
            // click on id
            tv.setTextColor(getResources().getColor(R.color.textDarkGreen));
            tv.setBackgroundResource(R.drawable.round_corner_jobview_yellow);
            locations.add(loc);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}